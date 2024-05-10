package io.demos.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static io.demos.kafka.KafkaConfig.TOPIC;

public class ProducerDemo {

    private static final Logger logger = LoggerFactory.getLogger(ProducerDemo.class.getSimpleName());

    public static void main(String[] args) {
        // create Producer properties
        // connection properties
        Properties props = new KafkaConfig().settingProducerProp();

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        // create a producer record
        ProducerRecord<String, String> record =
                new ProducerRecord<>(TOPIC, "hello world");

        // send data
        producer.send(record);

        // tell the producer to send all data and block until done (synchronous)
        producer.flush();

        // flush and close the producer
        producer.close();
    }
}
