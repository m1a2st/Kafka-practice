package io.demos.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

import static io.demos.kafka.KafkaConfig.TOPIC;

public class ConsumerDemo {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerDemo.class.getSimpleName());

    public static void main(String[] args) {
        // create consumer properties
        // connection properties
        Properties props = new KafkaConfig().settingConsumerProp();

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        // subscribe consumer to our topic(s)
        consumer.subscribe(List.of(TOPIC));

        // poll for new data
        while (true) {

            logger.info("Polling");

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

            records.forEach(record -> {
                logger.info("Key: {}, Value: {}", record.key(), record.value());
                logger.info("Partition: {}, Offset: {}", record.partition(), record.offset());
            });
        }
    }
}
