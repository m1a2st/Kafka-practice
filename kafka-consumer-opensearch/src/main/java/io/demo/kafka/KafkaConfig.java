package io.demo.kafka;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

import static java.lang.Integer.MAX_VALUE;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.*;

public class KafkaConfig {

    private final Properties props = new Properties();
    private static final String BOOTSTRAP_SERVERS = "127.0.0.1:9092";

    private static final String GROUP_ID = "consumer-opensearch-demo";
    public static final String TOPIC = "wikimedia.recentchange";

    public KafkaConfig() {
        props.put(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    }

    public Properties settingConsumerProp() {
        // consumer properties
        props.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        props.setProperty(GROUP_ID_CONFIG, GROUP_ID);
        // earliest, latest, none
        props.setProperty(AUTO_OFFSET_RESET_CONFIG, "latest");
        props.setProperty(ENABLE_AUTO_COMMIT_CONFIG, "false");
        return props;
    }

    public Properties getProps() {
        return props;
    }
}
