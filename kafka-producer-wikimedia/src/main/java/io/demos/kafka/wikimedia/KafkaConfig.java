package io.demos.kafka.wikimedia;

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

    public KafkaConfig addHighThroughputProp() {
        props.put(LINGER_MS_CONFIG, 20);
        props.put(BATCH_SIZE_CONFIG, 32 * 1024);
        props.put(COMPRESSION_TYPE_CONFIG, "snappy");
        return this;
    }

    public Properties getProps() {
        return props;
    }
}
