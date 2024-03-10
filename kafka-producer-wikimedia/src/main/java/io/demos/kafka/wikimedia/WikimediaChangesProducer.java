package io.demos.kafka.wikimedia;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.net.URI;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static io.demos.kafka.wikimedia.KafkaConfig.TOPIC;

public class WikimediaChangesProducer {

    private static final String WIKIMEDIA_URL = "https://stream.wikimedia.org/v2/stream/recentchange";

    public static void main(String[] args) throws InterruptedException {

        // create producer properties
        Properties props = new KafkaConfig().settingProducerProp()
                .addSafetyProducerProp()
                .getProps();

        // create the Producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        EventHandler eventHandler = new WikimediaChangeHandler(producer, TOPIC);
        EventSource eventSource = new EventSource.Builder(eventHandler, URI.create(WIKIMEDIA_URL)).build();

        // start the producer in another thread
        eventSource.start();

        // we produce for 10 minutes and block the program until then
        TimeUnit.MINUTES.sleep(10);
    }
}
