package io.demos.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

public class KafkaConfig {

    public final Properties props = new Properties();
    private static final String BOOTSTRAP_SERVERS = System.getenv("bootstrap_servers");
    private static final String USERNAME = System.getenv("username");
    private static final String PWD = System.getenv("pwd");

    // consumer
    private static final String GROUP_ID = "my-java-application";
    public static final String TOPIC = "demo_java";

    public KafkaConfig() {
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.put("sasl.mechanism", "SCRAM-SHA-256");
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.jaas.config", String.format("org.apache.kafka.common.security.scram.ScramLoginModule " +
                "required username=\"%s\" password=\"%s\";", USERNAME, PWD));
    }

    public Properties settingProducerProp() {
        // producer properties
        props.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return props;
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
}
