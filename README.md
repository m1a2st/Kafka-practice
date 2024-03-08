# Kafka practice

## Kafka Producer: Java API - Basic

- View basic configuration parameters
- Confirm we receive the data in a Kafka Console Consumer

### Environment variables:

| Variable | Value |
| --- | --- |
| bootstrap_servers | localhost:9092 |
| username | user |
| pwd | password |

### property

- bootstrap.servers: <bootstrap_servers>
- sasl.mechanism: SCRAM-SHA-256
- security.protocol: SASL_SSL
- sasl.jaas.config: org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\"
- key.serializer: org.apache.kafka.common.serialization.StringSerializer
- value.serializer: org.apache.kafka.common.serialization.StringSerializer

### Create a topic:

```bash
kafka-topics --command-config playground.config --bootstrap-server <bootstrap_servers> --topic demo_java --create --partitions 3
```

### Consume the topic:

```bash
kafka-console-consumer --command-config playground.config --bootstrap-server <bootstrap_servers> --topic demo_java --from-beginning
```

## Kafka Producer: Java API - Callbacks

- Confirm the partition and offset the message was sent to using Callbacks
- We'll look at the interesting behavior of `StickyPartitioner`

![RoundRobin_StickyPartitioner.jpg](img%2FRoundRobin_StickyPartitioner.jpg)

### Environment variables:

properties :

- batch.size 會影響到 partition 的選擇
- partitioner.class: RoundRobinPartitioner StickyPartitioner 

## Kafka Producer: Java API with Keys

- Send non-null keys to the Kafka topic
- Same key = same partition

![Producer_with_key.png](img%2FProducer_with_key.png)

## Kafka Consumer: Java API - Basic

- Learn how to write a basic consumer to receive data from Kafka
- View basic configuration parameters
- Confirm we receive the data from the Kafka Producer written in Java

![Consumer.png](img%2FConsumer.png)

### property

- key.deserializer: org.apache.kafka.common.serialization.StringDeserializer
- value.deserializer: org.apache.kafka.common.serialization.StringDeserializer
- group.id: my-first-application
- auto.offset.reset: earliest
  - earliest: automatically reset the offset to the earliest offset
  - latest: automatically reset the offset to the latest offset
  - none: throw exception to the consumer if no previous offset is found for the consumer's group

## Kafka Consumer - Graceful shutdown

- Ensure we have code in place to respond to termination signals 
- 
