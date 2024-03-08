package io.demos.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import static io.demos.kafka.KafkaConfig.TOPIC;

public class ConsumerDemoWithShutdown {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerDemoWithShutdown.class.getSimpleName());

    public static void main(String[] args) {
        // create consumer properties
        // connection properties
        Properties props = new KafkaConfig().settingConsumerProp();

        //create a consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        addShutdownHook(consumer);

        try {
            // subscribe consumer to our topic(s)
            consumer.subscribe(Arrays.asList(TOPIC));
            // poll for new data
            polling(consumer);
        } catch (WakeupException e) {
            logger.info("Consumer is starting to shutdown...");
        } catch (Exception e) {
            logger.error("Error while consuming", e);
        } finally {
            consumer.close();
            logger.info("Consumer is gracefully closed");
        }
    }

    private static void addShutdownHook(KafkaConsumer<String, String> consumer) {
        // get the reference to the main thread
        Thread mainThread = Thread.currentThread();

        // add a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Detected a shutdown let's exit by calling consumer.wakeup()...");
            consumer.wakeup();

            // join the main thread to allow the execution of the code in main thread
            try {
                mainThread.join();
            } catch (InterruptedException e) {
                logger.error("Error while joining the main thread, {}", e.getMessage());
            }
        }));
    }

    private static void polling(KafkaConsumer<String, String> consumer) {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

            records.forEach(record -> {
                logger.info("Key: " + record.key() + ", Value: " + record.value());
                logger.info("Partition: " + record.partition() + ", Offset: " + record.offset());
            });
        }
    }
}
