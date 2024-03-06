package io.demos.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemo {

    private static final Logger logger = LoggerFactory.getLogger(ProducerDemo.class.getSimpleName());

    public static void main(String[] args) {
        // create Producer properties
        String bootstrapServers = System.getenv("bootstrap_servers");
        String username = System.getenv("username");
        String pwd = System.getenv("pwd");
        Properties props = new Properties();

        // connection properties
        props.put("bootstrap.servers", bootstrapServers);
        props.put("sasl.mechanism", "SCRAM-SHA-256");
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.jaas.config", String.format("org.apache.kafka.common.security.scram.ScramLoginModule " +
                "required username=\"%s\" password=\"%s\";", username, pwd));

        // set producer properties
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        // create a producer record
        ProducerRecord<String, String> record =
                new ProducerRecord<>("demo_java", "hello world");

        // send data
        producer.send(record);

        // tell the producer to send all data and block until done (synchronous)
        producer.flush();

        // flush and close the producer
        producer.close();
    }
}
