package io.demos.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadProducer {

    public static void main(String[] args) throws InterruptedException {

        Thread.sleep(10000);
        ExecutorService service = Executors.newFixedThreadPool(10);
        Properties config = new KafkaConfig().settingProducerProp();
        KafkaProducer<String, String> producer = new KafkaProducer<>(config);

        service.execute(() -> {
            for (int i = 0; i < 100000000; i++) {
                producer.send(new ProducerRecord<>(
                        KafkaConfig.TOPIC,
                        Thread.currentThread().getName() + " - " + i
                ));
            }
        });
    }
}
