# Delivery Semantic - At Most Once

- **At most once**: offsets are committed as soon as the message batch is received. If the processing goes wrong,
  the message will be lost (it won't be read again).

![At_Most_Once.png](..%2Fimg%2FAt_Most_Once.png)

# Delivery Semantic - At Least Once
- **At least once**: offsets are committed after the message is processed. If the processing goes wrong,
  the message will be read again. This can result in duplicate processing of messages. Make sure your processing 
  is **idempotent** (i.e. processing again the message won;t impact your systems)

![At_Least_Once.png](..%2Fimg%2FAt_Least_Once.png)

# Delivery Semantic - Exactly Once

- **Exactly once**: can be achieved for Kafka=>Kafka workflows using the Transactional API (easy with Kafka Streams API).
  For Kafka => Sink workflows, use an idempotent consumers.
  
## bottom line:
** for most applications you should use "at least once processing" (we'll see in practice how to do it) and ensure your 
transformations/ processing are idempotent. **

# Consumer Offset Commit Strategies

- There are two most common patterns for committing offsets in a consumer application.
- 2 strategies:
  - (easy) `enable.auto.commit=true` & synchronous processing of batches
  - (medium) `enable.auto.commit=false` & manual commit of offsets

## Kafka Consumer - Auto Offset Commit Behavior

- In the java Consumer API, offsets are regularly committed
- Enable at-least once reading scenario by default (under conditions)
- Offsets are committed when you call `.poll()` and `auto.commit.interval.ms` has elapsed
- Example: `auto.commit.interval.ms=5000` and `enable.auto.commit=true` => will commit

- **Make sure messages are all successfully processed before you call `.poll()` again**
  - If you don't, you will not be in at-least-once reading scenario 
  - In that (rare) case, you must disable `enable.auto.commit` and most likely most processing to a separate thread, and 
    then from time-to time call `.commitSync()` or `.commitAsync()` with the correct offsets manually (advanced)
  - 
![Auto_Offset.png](..%2Fimg%2FAuto_Offset.png)

### Consumer Offset Commits Strategies

- `enable.auto.commit=true` & synchronous processing of batches
```java
while (true) {
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
    doSomethingSynchronous(batch);
}
```
- With auto-commit, offsets will be committed automatically for you at regular interval (auto.commit.interval.ms 
  **by default**) every-time you call `.poll()`
- If you don't use synchronous processing, you will be in "at-most-once" behavior because offsets will be committed 
  before your data is processing

- `enable.auto.commit=false` & synchronous processing of batches
```java
while (true) {
    batch += consumer.poll(Duration.ofMillis(100));
    if isReady(batch) {
        doSomethingSynchronous(batch);
        consumer.commitSync();
    }
}
```
- You control when you commit offsets and what's the condition for committing them
- Example: accumulating records into a buffer and then flushing the buffer to a database + committing offsets 
  asynchronously them

- `enable.auto.commit=false` & storing offsets externally
- **This is advanced**
  - You need to assign partitions to your consumer and at launch manually using `.seek()` API 
  - You need to model and store your offsets in a database table for example
  - You need to handle the cases where rebalances happen (`ConsumerRebalanceListener` interface)
- Example: if you need exactly once processing and cna;t find any way to do idempotent processing, then you "process
  data" + "commit offsets" as part of a single transaction.

# Consumer Offset Reset Behavior

- A consumer is expected to read from a log continuously.
![Consumer_Offset_Reset.png](..%2Fimg%2FConsumer_Offset_Reset.png)
- But if your application ahs a bug, your consumer can be down
- If Kafka has a retention of 7 days, and your consumer is down for more than 7 days, the offsets are "invalid"
- The behavior for the consumer is to then use:
  - `auto.offset-reset=lastest`: will read from the end of the log
  - `auto.offset-reset=earliest`: will read from the start of the log
  - `auto.offset-reset=none`: will throw exception if no offset is found
- Additionally, consumer offsets be lost:
  - If a consumer hasn't read new data in 1 day (Kafka < 2.0)
  - If a consumer hasn't read new data in 7 day (Kafka >= 2.0)
- This can be controlled by the broker setting `offset.retention.minutes`

## Replaying data for Consumer

- To replay data for a consumer group:
  - Take all the consumers from a specific group down
  - Use  Kafka-consumer-groups command to set to what you want
  - Restart consumers
- **Bottom line:**
- Set proper data retention period & offset retention period
- Ensure the auto reset behavior is the one you expect /want
- Use replay capability in case of unexpected behavior
