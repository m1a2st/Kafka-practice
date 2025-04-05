package io.demos.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.demos.kafka.KafkaConfig.TOPIC;

public class MultiThreadConsumer {

    public static void main(String[] args) throws InterruptedException {

        Thread.sleep(10000);
        ExecutorService service = Executors.newFixedThreadPool(10);
        Properties props = new KafkaConfig().settingConsumerProp();

        service.execute(() -> {
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
            consumer.subscribe(List.of(TOPIC));
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            }
        });

    }
}
