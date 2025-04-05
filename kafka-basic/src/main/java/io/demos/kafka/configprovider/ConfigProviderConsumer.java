package io.demos.kafka.configprovider;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

import static io.demos.kafka.KafkaConfig.TOPIC;

public class ConfigProviderConsumer {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConfigProviderConsumer.class);

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("config.providers", "map");
        props.put("config.providers.map.class", MapConfigProvider.class.getName());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "123");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // sensitive data, like password, should be stored in a config provider
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "${map:server}");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
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
