package io.demos.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class KafkaConfig {

    public final Properties props = new Properties();
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String USERNAME = System.getenv("username");
    private static final String PWD = System.getenv("pwd");

    // consumer
    private static final String GROUP_ID = "my-java-application";
    public static final String TOPIC = "first_topic";

    public KafkaConfig() {
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
//        props.put("sasl.mechanism", "SCRAM-SHA-256");
//        props.put("security.protocol", "SASL_SSL");
//        props.put("sasl.jaas.config", String.format("org.apache.kafka.common.security.scram.ScramLoginModule " +
//                "required username=\"%s\" password=\"%s\";", USERNAME, PWD));
    }

    public Properties settingProducerProp() {
        // producer properties
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return props;
    }

    public Properties settingConsumerProp() {
        // consumer properties
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        // earliest, latest, none
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }
}
