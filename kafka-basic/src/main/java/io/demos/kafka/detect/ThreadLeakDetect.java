package io.demos.kafka.detect;

import io.demos.kafka.KafkaConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

import static io.demos.kafka.KafkaConfig.TOPIC;

public class ThreadLeakDetect {

    private final static Logger LOGGER = LoggerFactory.getLogger(ThreadLeakDetect.class);


    public static void main(String[] args) {
        // create consumer properties
        // connection properties
        Properties props = new KafkaConfig().settingConsumerProp();

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        // subscribe consumer to our topic(s)
        consumer.subscribe(List.of(TOPIC));

        // poll for new data
        while (true) {

            LOGGER.info("Polling");

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

            records.forEach(record -> {
                LOGGER.info("Key: {}, Value: {}", record.key(), record.value());
                LOGGER.info("Partition: {}, Offset: {}", record.partition(), record.offset());
            });
        }
    }
}
