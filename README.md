# Kafka practice

## Kafka Producer: Java API - Basic

- View basic configuration parameters
- Confirm we receive the data in a Kafka Console Consumer

### Environment variables:

| Variable          | Value          |
|-------------------|----------------|
| bootstrap_servers | localhost:9092 |
| username          | user           |
| pwd               | password       |

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

## Kafka Consumer: Java API - Consumer Groups

- Make your consumer in Java consume data as part of a consumer group
- Observer partition rebalanced mechanisms

![Consumer_Group.png](img%2FConsumer_Group.png)

## Consumer Groups and Partition Rebalanced

- Moving partitions between consumers is called a rebalanced
- Reassignment of partitions happened when a consumer leaves or joins a group
- It also happens if an administrator adds new partitions into a topic

![Partition_Rebalanced.png](img%2FPartition_Rebalanced.png)

### Eager Rebalanced 

- All consumes stop, give up their membership of partitions
- They rejoin consumer group and get a new partition assignment
- Problems:
  - During a short period of time, the entire consumer group stops processing
  - **Consumers don't necessarily get the same partitions they had before**

![Eager_Rebalanced.png](img%2FEager_Rebalanced.png)

### Cooperative Rebalanced (Incremental Rebalanced)

- Reassigning a small subset of the partitions from one consumer to another
- Other consumers that don't have reassigned partitions can still process uninterrupted
- Can go through several iterations to find a **stable** assignment (hence "incremental")
- Avoids **stop the world** events where all consumers stop processing data

![Cooperative_Rebalance.png](img%2FCooperative_Rebalance.png)

## Cooperative Rebalanced, how to use

- Kafka Consumer: `partition.assignment.strategy`
  - RangeAssignor: assign partitions on a per-topic basis (can lead to imbalance)
  - RoundRobin: assign partitions on a per-topic in round-robin fashion, optimal balance
  - StickyAssignor: balance like RoundRobin, and then minimises 
    partition movements when consumer join/ leave the group in order to minimise movements
  - **CooperativeStickyAssignor**: rebalanced strategy is identical to StinkyAssignor but supports
    cooperative rebalances and therefore consumers can keep pn consuming from the topic
  - The default assignor is [RangeAssignor, CooperativeStickyAssignor], which will use the RangeAssignor
    by default, but allows upgrading to the CooperativeStickyAssignor with just a single rolling bounce 
    that removes the RangeAssignor from the list
- **Kafka Connect**: already implemented (enabled by default)
- **Kafka Streams**: turned on by default using StreamPartitionAssignor

## Static Group Membership

- By default, when a consumer leaves a group, its practitioners are revoke and re-assigned
- If it joins back, it will have a new "member ID" and new partitions assigned
- If you specify `group.instance.id` it makes the consumer a **static member** 
- Upon leaving, the consumer has up to `session.timeout.ms` to join back and get back its partitions
  (else they will be re-assigned), without triggering a rebalanced
- This is helpgul when consumers maintain local state and cache(to avoid re-building the cache)

![Static_Group_Member.png](img%2FStatic_Group_Member.png)
