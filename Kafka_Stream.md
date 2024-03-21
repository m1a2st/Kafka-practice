# Kafka Streams Introduction
- You want tp dp the following from the `wikimedia.recentchange` topic:
  - Count the number of times a change was created by a bot versus a human
  - Analyze number of changes per website (ru.wikipedia.org vs en.wikipedia.org)
  - Number of edits per 10-seconds as a time series
- With the Kafka Producer and Consumer. you can achieve, but it's very low level and not
  development friendly

![Kafka_Stream.png](img%2FKafka_Stream.png)

# What is Kafka Streams?
- Easy **data processing and transformation library** within Kafka
  - Standard Java Application
  - No need to create a separate cluster
  - Highly scalable, elastic and fault-tolerant
  - Exactly-Once Capabilities
  - One record at a time processing (no batching)
  - Works for any application size

# The need for a schema registry
- Kafka takes bytes as an input and publishes them
- No data verification
- What if the producer sands bad data?
- What if a field gets renamed?
- What if the data format changes from one day to another?
- **The consumer break!**

![Stream_Schema.png](img%2FStream_Schema.png)

- We need data to be self describable
- We need to be able to evolve data without breaking downstream consumers
- **We need schemas... and a schema registry**
- What if the Kafka Brokers were verify the message they receive?
- It would break what makes Kafka so good:
  - Kafka doesn't parse or even read your data (no CPU usage)
  - Kafka takes bytes as an input without even loading them into memory (that's called zero copy)
  - Kafka distributes bytes
  - As far Kafka is concerned, it doesn't even know if your data is an integer, a string etc
- The schema Registry must be a separate components
- Producers and Consumers need to be able to talk to it
- The Schema Registry must be able to reject bad data
- A common data format must be agreed upon
  - It needs to support schemas 
  - It needs to support evolution
  - It needs to be lightweight
- Enter... the Schema Registry
- And Apache Avro as the data format (Protobuf, JSON Schema also supported)

# Pipeline without Schema Registry
![Pipeline_Without_Schema.png](img%2FPipeline_Without_Schema.png)

# Schema Registry - Purpose

- Store and retrieve the schemas for Producers/ Consumers
- Enforce Backward/ Forward/ Full compatibility on topics
- Decrease the size of the payload of data sent to Kafka
- 
![Schema_Registry_Purpose.png](img%2FSchema_Registry_Purpose.png)

# Schema Registry: gotchas
- Utilizing a schema registry has a lot of benefits 
- BUT it implies you need to 
  - Set it up well
  - Make sure it's highly available
  - Partially change the producer and consumer code
- Apache Avro as a format is awesome but has a learning curve
- Other formats include Protobuf and JSON Schema
- The Confluent Schema Registry is free and source-available
- Other open-source alternative may exist





