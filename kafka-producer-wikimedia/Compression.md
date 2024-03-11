# Message Compression at the Producer level

- Producer usually send data that is text-based, for example with JSON data
- In this case, it is important to apply compression to the producer
- Compression can be enabled at the Producer level and doesn't require any configuration change in the Brokers or in the
  Consumers
- `compression.type` can be set to `none` (default), `gzip`, `snappy`, `lz4`, `zstd` (Kafka 2.1)
- Compression is more effective the bigger the batch of message being sent to Kafka
- Benchmarks here: https://blog.cloudflare.com/squeezing-the-firehose/

![Compression.png](..%2Fimg%2FCompression.png)

## Message Compression

- The compressed batch has the following advantage:
  - Much smaller producer request size (compression ratio up to 4x!)
  - Faster to transfer data over the network => less latency
  - Better throughput
  - Better disk utilisation in Kafka (stored message on disk are smaller)
- Disadvantages (very minor):
  - Producer must commit some CPU cycles to compression
  - Consumer must commit some CPU cycles to decompression
- Overall:
  - Consider testing `snappy` or `lz4` for optimal speed/ compression ratio (test others too)
  - Consider tweaking `linger.ms` and `batch.size` to have bigger batches, and therefore more compression
    and higher throughput
  - Use compression in production

# Message Compression at the Broker/ Topic Level

- There is also a setting you can set at the broker level (all topic) or topic level
- `compression.type=producer` (default), the broker takes the compressed batch from the producer client and
  writes it directly to the topic's log file without recompressing the data
- `compression.type=none`: all batches are decompressed by the broker
- `compression.type=lz4`: (for example)
  - If it's matching the producer setting, data is stored on disk as is
  - If it's a different compression setting, batches are decompressed by the broker and then recompressed
    using the compression algorithm specified by the broker setting
- **Warning: if you enable broker-side compression, it will consume extra CPU style**

## linger.ms & batch.size

- By default, Kafka producer try to send records as soon as possible
  - It will have up to `max.in.flight.requests.pre.connection=5`, meaning up to 5 message batches being 
    in flight (being sent between the producer in the broker) at most
  - After this, if more messages must be sent while others are in flight, Kafka is smart and will start batching
    them before the next batch send
- This smart batching helps increase throughput while maintaining very low latency
  - Added benefit: batches have higher compression ratio so better efficiency
- Two settings to influence the batching mechanism
  - `linger.ms`: default is 0, how long to wait until we send a batch. Adding a small number for example 5ms helps
    add more messages in the batch at the expense of latency
  - `batch.size`: if a batch is filled before the `linger.ms`, increase the batch size 

![Batch_Size.png](..%2Fimg%2FBatch_Size.png)

### batch.size (default 16KB)

- Maximum number of bytes that will be included in a batch
- Increasing a batch size to something like 32KB or 64KB can help increase throughput and compression, throughput
  ,and efficiency of request
- Any message that is bigger than the batch size will not be batched
- A batch is allocated per partition, so make sure that you don't set it a number that;s too high, otherwise
  you will run waste memory
- (Note: You can monitor the average batch size using Kafka Producer Metrics)

## High Throughput Producer

- Increase `linger.ms` and `the producer will wait few milliseconds for the batches to fill up before sending them.
- If you are sending full batches and have memory to spare, you can increase `batch.size` and send larger batches
- Introduce some producer-level compression for more efficiency in sends
```java
// high throughput producer (at the expense of a bit of latency and CPU usage)
props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
props.put(ProducerConfig.LINGER_MS_CONFIG, 20);
props.put(ProducerConfig.BATCH_SIZE_CONFIG, 32*1024);
```

# Producer Default Partitioner when `key != null`

![Producer_Default_Partitioner.png](..%2Fimg%2FProducer_Default_Partitioner.png)

- **Key Hashing** is the process of determining the mapping of a key to a partition
- In the default Kafka partitioner, the keys are hashed using the murmur2 algorithm
```text
targetPartition = Math.abs(Utils.murmur2(keyBytes)) % (numPartitions - 1)
```
- This means that same key will go to the same partition (we already know this), and adding partitions to
  a topic will completely alter the formula
- It is most likely preferred to not override the behavior of the partitioner, but it is possible to do so 
  using `partitioner.class`
- When `key = null`, the producer has a **default partitioner** that varies:
  - Round Robin: for Kafka 2.3 and below
  - Sticky Partition: for Kafka 2.4 and above
- Sticky Partitioner improves the performance of the producer especially when high throughput the key is null

## Producer Default Partitioner Kafka <= v2.3 Round Robin Partitioner

- With Kafka <= v2.3, when there's no partition and no key specified, the default partitioner sends data in
  **round-robin** fashion
- This results in **more matches** (one batch per partition) and **smaller batches** (imagine with 100 partitions)
- Smaller batches lead to more requests as well as higher latency

![Round_Robin.png](..%2Fimg%2FRound_Robin.png)

## Producer Default Partitioner Kafka >= v2.4 Sticky Partitioner

- It would be better to have all the records sent to a single partition and not multiple partitions to improve batching
- The producer **sticky partitioner**:
  - We "stick" to a partition until the batch is full or `linger.ms` has elapsed 
  - After sending the batch, the partition that is sticky changes
- Larger batches and reduced latency (because larger requests, and `batch.size` more likely to be reached)
- Over time, records are still spread evenly across partitions

![Sticky_Partition.png](..%2Fimg%2FSticky_Partition.png)
