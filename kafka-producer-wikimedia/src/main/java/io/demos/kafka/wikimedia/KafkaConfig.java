package io.demos.kafka.wikimedia;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

import static java.lang.Integer.MAX_VALUE;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.clients.producer.ProducerConfig.*;
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;

public class KafkaConfig {

    public final Properties props = new Properties();
    private static final String BOOTSTRAP_SERVERS = "127.0.0.1:9092";

    // consumer
    private static final String GROUP_ID = "my-java-application";
    public static final String TOPIC = "wikimedia.recentchange";

    public KafkaConfig() {
        props.put(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    }

    public KafkaConfig settingProducerProp() {
        // producer properties
        props.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return this;
    }

    public KafkaConfig addSafetyProducerProp() {
        props.put(ACKS_CONFIG, "all"); // same as setting -1
        props.put(RETRIES_CONFIG, MAX_VALUE);
        props.put(MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        props.put(ENABLE_IDEMPOTENCE_CONFIG, true);
        return this;
    }

    public Properties settingConsumerProp() {
        // consumer properties
        props.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        props.setProperty(GROUP_ID_CONFIG, GROUP_ID);
        // earliest, latest, none
        props.setProperty(AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    public Properties getProps() {
        return props;
    }
}
