# Why should I care about topic configuration
- Brokers have defaults for all the topic configuration parameters
- These parameters impact **performance** and **topic behavior**
- Some topic may need different values than the defaults
  - Replication Factor
  - \# of Partitions 
  - Message size
  - Compression level
  - Log Cleanup Policy
  - Min Insync Replicas
  - Other configuration

# Partitions and Segments
- Topics are made of partitions
- Partitions are made of segments (files)!

![Segments.png](img%2FSegments.png)
- Only one segment is ACTIVE (the one data is being written to)
- Two segment settings:
  - `log.segment.bytes`: the max size of a single segment in bytes (default 1GB)
  - `log.segment.ms`: the time Kafka will wait before committing the segment if not full (1 week)

# Segments and Indexes
- Segments come with two indexes (files):
  - An offset to position index: helps Kafka find where to read from to find a message
  - A timestamp to offset index: help Kafka find messages with a specific timestamp

![Segment_And_Indexes.png](img%2FSegment_And_Indexes.png)

# Segments: Why should I care?
- A smaller `log.segment.bytes` (size, default: 1GB) means:
  - More segments per partitions
  - Log Compaction happens more often
  - BUT Kafka must keep more files opened (Error: Too many open files)
- Ask yourself: how fast will I have new segments based on throughput?
- A smaller `log.segment.ms` (time, default 1 week) means:
  - You set a max frequency for log compaction (more frequent triggers)
  - Maybe you want daily compaction instead weekly?
- Ask yourself: how often do I need log compaction to happen?

# Log Cleanup Policies
- Many Kafka cluster make data expire, according to a policy
- That concept is called log cleanup

Policy1: `log.cleanup.policy=delete` (Kafka default for all use topics)
- Delete based on age of data (default is a week)
- Delete based on max size of log (default is -1=infinite)
Policy2: `log.cleanup.policy=compact` (Kafka default for topic __consumer_offsets)
- Delete based on keys of your messages
- Will delete old duplicate keys **after** the active segment is committed
- Infinite time and space retention

# Log Cleanup: Why and When?
- Deleting data from Kafka allows you to:
  - Control the size of the data on the disk, delete obsolete data
  - Overall: Limit maintenance work on the Kafka Cluster
- How often does long cleanup happen?
  - Long cleanup happens on your partition segments!
  - Smaller/ More segments means that log cleanup will happen more often!
  - Log cleanup shouldn't happen too often => takes CPU and ram resources
  - The cleaner check for work every 15 seconds (`log.cleaner.backoff.ms`)

# Log Cleanup Policy: Delete
- `log.retention.hours`: number of hours to keep for (default is 168 - one week)
  - Higher number means more disk space
  - Lower number means that fewer data re retained (if your consumers are down for too long, 
    they can miss data)
  - Other parameters allowed: `log.ertension.ms, log.retension.minutes` (smaller unit has precedence)
- `log.retension.bytes`:
  - Max size in Bytes for each partition (default is -1 - infinite) 
  - Useful to keep the size of a log under a threshold

![Log_Cleanup_Policy_Delete.png](img%2FLog_Cleanup_Policy_Delete.png)
Use cases - two common pair of options:
- One week of retention:
  - `log.retention.hours=168` and `log.retention.bytes=-1`
- Infinite time retention bounded by 500MB:
  - `log.retention.ms=-1` and `log.retention.bytes=524288000` 

# Log Cleanup Policy: Compact
- Log compaction ensures that your log contains at least the last known value for a specific
  key within a partition
- Very useful if we just require a SNAPSHOT instead of full history (such as for a data table in a database)
- The idea is that we only keep the latest "update" for a key in our log

# Log Compaction: Example
- Our topic is: employee-salary
- We want to keep the most recent salary for our employees
![Compaction_EG.png](img%2FCompaction_EG.png)

# Log Compaction Guarantee
- **Any consumer that is reading from the tail of a log (most current data) will still see all the message
  sent to the topic**
- Ordering of message it kept, log compaction only removes some messages, but does not re-order them
- The offset of a message is immutable (it never changes). Offsets are just skipped if a message is missing
- Deleted records can still be seen by consumers for a period of `delete.retention.ms` (default 24 hours)

# Log Compaction Myth Busting
- It doesn't prevent you from pushing duplicate data to Kafka
  - De-duplication is done after a segment is committed
  - Your consumers will still read from tail as soon as the data arrives
- It doesn't prevent you from reading duplicate data from Kafka
  - Some points as above
- Log Compaction can fail from time to time
  - It is an optimization and it the compaction thread might crash
  - Make sure you assign enough memory to it and that it gets triggered
  - Restart Kafka if log compaction is broken
- You can't trigger Log Compaction using an API call

# Log Compaction - How it works
![Compaction_How.png](img%2FCompaction_How.png)

- Log compaction `log.cleanup.policy=compact` is impacted by:
  - `segment.ms (default 7 days)`: Max amount of time to wait to close active segment
  - `segment.bytes (default 1G)`: Max size of segment
  - `min.compaction.lag.ms (default 0)`: how long to wait before message can be compacted
  - `delete.retention.lag.ms (default 24 hours)`: wait before deleting data marker for compaction
  - `min.cleanable.dirty.ratio (default 0.5)`: higher => less, more efficient cleaning.Lower => opposite

# `unclean.leader.election.enable`
- If all your In Sync Replicas go offline (but you still have out of sync replicas up), you have the following
  options:
  - Wait for an ISR to come back online (default)
  - Enable `unclean.leader.election.enable=true` and start producing to non ISR partitions
- If you enable `unclean.leader.election.enable=true`, you improve availability, but you will lose data because
  other messages on ISR will be discarded when they come back online and replicate data from the new leader
- Overall, this is very **dangerous setting**, and it's implications must be understood fully before enabling it
- Use cases include metrics collection, log collection, and other cases where data loss is somewhat acceptable at the
  trade-off availability.

# Large Message in Apache Kafka
- Kafka has a default of 1 MB per message in topics, as large message are considered inefficient and an anti-pattern
- Two approaches to sending large messages:
  - Using an external store: store messages in HDFS, Amazon S3, Google Cloud Storage, etc... and send a reference of 
    that message to Apache Kafka
  - Modify kafka parameters: must change broker, producer and consumer settings

## Option 1: Large Message using External Store
- Store the large message (e.g. video, archive file, etc...) outside of Kafka
- Send a reference of that message to Kafka
- Write custom code at the producer/ consumer level to handle this pattern
- 
![External_Store.png](img%2FExternal_Store.png)

## Option 2: Sending large messages in Kafka (ex:10 MB)
- **Topic-wise, Kafka-side**, set max message size to 10MB
  - Broker side: modify `message.max.bytes`
  - Topic side: modify `max.message.bytes`
  - **Warning: the settings have similar but different name; this is not a typo**
- **Broker-wise**, set max replication fetch size to 10MB
  - `replica.fetch.max.bytes=10485880` (in server.properties)
- **Consumer-side**, must increase fetch size of the consumer will crash:
  - `max.partition.fetch.bytes=10485880`
- **Producer-side**, must increase max request size
  - `max.request.size=10485880` 
