package io.demos.kafka;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

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

    public Properties settingProducerProp(){
        // producer properties
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        return props;
    }

    public Properties settingConsumerProp(){
        // consumer properties
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());

        props.setProperty("group.id", GROUP_ID);
        // earliest, latest, none
        props.setProperty("auto.offset.reset", "earliest");
        return props;
    }
}
