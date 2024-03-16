package io.demo.kafka;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.GetIndexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Properties;

import static io.demo.kafka.KafkaConfig.TOPIC;
import static java.util.Collections.singleton;
import static org.opensearch.client.RequestOptions.DEFAULT;
import static org.opensearch.common.xcontent.XContentType.JSON;

public class OpenSearchDemo {

    private static final Logger logger = LoggerFactory.getLogger(OpenSearchDemo.class.getSimpleName());
    private static final String INDEX_NAME = "wikimedia";

    public static void main(String[] args) throws IOException {
        // first create on OpenSearch Client
        RestHighLevelClient openSearchClient = createOpenSearchClient();
        // create our Kafka Client
        Properties props = new KafkaConfig().settingConsumerProp();
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        // we need to create the index if it does not exist already
        try (openSearchClient; consumer) {
            if (!openSearchClient.indices().exists(new GetIndexRequest(INDEX_NAME), DEFAULT)) {
                CreateIndexRequest request = new CreateIndexRequest(INDEX_NAME);
                openSearchClient.indices().create(request, DEFAULT);
            } else {
                logger.info("Index already exists");
            }
            consumer.subscribe(singleton(TOPIC));

            while (true) {
                // poll for new messages
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                int recordCount = records.count();
                logger.info("Received " + recordCount + " records");
                // send the records to OpenSearch
                for (ConsumerRecord<String, String> record : records) {

                    // send the record into Opensearch

                    // strategy 1
                    // define on ID using Kafka Record coordinates
                    // id = record.topic() + "_" + record.partition() + "_" + record.offset();
                    try {
                        // strategy 2
                        // define on ID using the record value
                        String id = extractedId(record.value());

                        IndexRequest indexRequest = new IndexRequest(INDEX_NAME)
                                .source(record.value(), JSON)
                                .id(id);
                        IndexResponse response = openSearchClient.index(indexRequest, DEFAULT);
                        logger.info(response.getId());
                    } catch (Exception e) {

                    }
                }
                // commit offsets after batch is consumed
                consumer.commitSync();
                logger.info("Offsets have been committed");
            }
        }
    }

    public static RestHighLevelClient createOpenSearchClient() {
        String connString = "http://localhost:9200";

        // we build a URI from the connection string
        RestHighLevelClient restHighLevelClient;
        URI connUri = URI.create(connString);
        // extract login information if it exists
        String userInfo = connUri.getUserInfo();

        if (userInfo == null) {
            // REST client without security
            restHighLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost(connUri.getHost(), connUri.getPort(), "http")));

        } else {
            // REST client with security
            String[] auth = userInfo.split(":");

            CredentialsProvider cp = new BasicCredentialsProvider();
            cp.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(auth[0], auth[1]));

            restHighLevelClient = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(connUri.getHost(), connUri.getPort(), connUri.getScheme()))
                            .setHttpClientConfigCallback(
                                    httpAsyncClientBuilder -> httpAsyncClientBuilder.setDefaultCredentialsProvider(cp)
                                            .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())));
        }
        return restHighLevelClient;
    }

    private static String extractedId(String json) {
        try {
            return JsonParser.parseString(json)
                    .getAsJsonObject()
                    .get("id")
                    .getAsJsonObject()
                    .get("id")
                    .getAsString();
        } catch (Exception e) {
            logger.warn("Skipping bad data: " + json);
            throw new RuntimeException("Skipping bad data: " + json);
        }
    }
}
