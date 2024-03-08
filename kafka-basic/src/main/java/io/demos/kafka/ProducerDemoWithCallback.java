package io.demos.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Properties;

import static io.demos.kafka.KafkaConfig.TOPIC;

public class ProducerDemoWithCallback {

    private static final Logger logger = LoggerFactory.getLogger(ProducerDemoWithCallback.class.getSimpleName());

    public static void main(String[] args) {
        // create Producer properties
        // connection properties
        Properties props = new KafkaConfig().props;

        props.setProperty("partitioner.class", RoundRobinPartitioner.class.getName());
        props.setProperty("batch.size", "400");

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        for (int i = 0; i < 10; i++) {
            // create a producer record
            ProducerRecord<String, String> record =
                    new ProducerRecord<>(TOPIC, "hello world" + i);

            // send data
            producer.send(record, (recordMetadata, e) -> {
                // executes every time a record is successfully sent or an exception is thrown
                if (Objects.isNull(e)) {
                    // the record was successfully sent
                    logger.info("Received new metadata. \n" +
                            "Topic: " + recordMetadata.topic() + "\n" +
                            "Partition: " + recordMetadata.partition() + "\n" +
                            "Offset: " + recordMetadata.offset() + "\n" +
                            "Timestamp: " + recordMetadata.timestamp());
                } else {
                    logger.error("Error while producing", e);
                }
            });
        }

        // tell the producer to send all data and block until done (synchronous)
        producer.flush();

        // flush and close the producer
        producer.close();
    }
}
