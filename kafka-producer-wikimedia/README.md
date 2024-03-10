# Producer Acknowledgements Deep Dive

## acks

- Producers can choose to receive acknowledgment of data writes:
    - `acks=0` : Producer will not wait for acknowledgment (possible data loss)
    - `acks=1` : Producer will wait for leader acknowledgment (limited data loss)
    - `acks=all` : Producer will wait for leader and replicas acknowledgment (no data loss)

![acks.png](..%2Fimg%2FAcks.png)

### Producer: acks=0

- When `acks=0` producers consider messages as "written successfully" the moment the message was sent without
  waiting for the broker to accept it at all
![acks0.png](..%2Fimg%2FAcks0.png)
- If the broker goes offline or an exception happens, we won't know and **will lose data**
- Useful for data where it's okay to potentially lose message, such as metrics collection
- Produces the highest throughput setting because the network overhead is minimized

### Producer: acks=1

- When `acks=1` producers will consider a message "written successfully" when the message was acknowledged by
  only the leader
- Default for kafka v1.0 to v2.8
![acks1.png](..%2Fimg%2FAcks1.png)
- Leader response is required, but replication is not a guarantee as it happens in the background
- If the leader broker goes offline unexpectedly but replicas haven't replicated the data yet, 
  we have a data loss
- If an ack is not received, the producer may retry the request

### Producer: acks=all (ackes=-1)

- When `acks=all` producers will consider a message "written successfully" when the message is accepted by 
  all in-sync replicas (ISR).
- Default for kafka 3.0+

![acks_all.png](..%2Fimg%2FAcks_all.png)

### Producer acks=all & min.insync.replicas

- The leader replica for a partition checks to see if there are enough in-sync replicas for safely
  writing the message (controlled by the broker setting `min.insync.replicas`)
  - `min.insync.replicas=1` : only the broker leader needs to successfully ack
  - `min.insync.replicas=2` : at least the broker leader and one replica need to ack

![min_isync_replica.png](..%2Fimg%2FMin_isync_replica.png)

### Kafka Topic Availability

- Availability: (considering RF=3)
  - `acks=0 & acks=1` : if one partition is uop and considered an ISR, the topic will be available for writes
  - `acks=all` : 
    - `min.insync.replicas=1` (default): the topic must have at least 1 partition up as an ISR (that includes the leader)
      and so we can tolerate 2 brokers going down
    - `min.insync.replicas=2` : the topic must have at least 2 partitions up, and therefore we can tolerate at most one 
      broker being down (in the case of replication factor 3), and we have the guarantee that for every write, the data 
      will be at least written twice
    - `min.insync.replicas=3` : this wouldn't make such sense for a corresponding replication factor of 3, and we couldn't
      tolerate any broker going down
    - in summary, when `acks=all` with a `replication.factor=N` and `min.insync.replicas=M`, we can tolerate at most 
      N-M brokers going down for topic availability purposes
  - `acks=all` and `min.insync.replicas=2` is the most popular option for data durability and availability and allows 
    you to withstand at most the loss of one Kafka broker

## Producer Retries

- In case of transient failures, developers are expected to handle exceptions, otherwise the data will be lost
- Example of transient failure:
  - NOT_ENOUGH_REPLICAS (due to `min.insync.replicas` setting)
- There is a "retries" setting
  - default to 0 for Kafka version <= 2.0
  - defaults to 2147483647 for Kafka version >= 2.1
- The `retry.backoff.ms` setting is by default 100ms

### Producer Timeouts

- If retries>0, for example retries=2147483647, retries are bounded by a timeout
- Since Kafka 2.1, you can set: `delivery.timeout.ms=120000 == 2min`
- Records will be failed of they can't be acknowledged within `delivery.timeout.ms`

![Delivery_Timeout.png](..%2Fimg%2FDelivery_Timeout.png)

### Producer Retries: Warning for old version of Kafka

- If you are not using an idempotent producer(not recommend - old Kafka):
  - In cse of **retries**, there is a chance that messages will be sent out of order (if a batch has failed
    to be sent)
  - **If you rely on key-based ordering, that can be an issue**
- For this, you can set the setting while controls how many produce requests can be made in parallel:
  `max.in.flight.requests.per.connection`
  - Default: 5
  - Set it to 1 if you need to ensure ordering (may impact throughput)
- In Kafka >= 1.0.0, there's a better solution with idempotent producers!

### Idempotent Producer

- The Producer can introduce duplicates messages in Kafka due to network errors

![Idempotent_Producer.png](..%2Fimg%2FIdempotent_Producer.png)
- In Kafka >= 0.11, you can define a "idempotent producer" which won't introduce duplicates on network error

![Idempotent_Producer_Old_Version.png](..%2Fimg%2FIdempotent_Producer_Old_Version.png)

- Idempotent producer are great to guarantee a stable and safe pipeline!
- **They are the default since Kafka 3.0, recommend to use them**
- They come with:
  - `reties=Integer.MAX_VALUE`(2^31-1=2147483647)
  - `max.in.flight.requests=1` (Kafka == 0.11) or
  - `max.in.flight.requests=5` (Kafka >= 1.0 - high performance & keep ordering - KAFKA-5494)
  - `acks=all`
- These settings are applied automatically after you producer has started if not manually set
- Just set: `producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true")`

# Kafka Producer defaults

- Since Kafka 3.0, the producer is "safe" by default:
  - `acks=all` (-1) 
  - `enable.idempotence=true`
- With Kafka 2.8 and lower, the producer by default comes with:
  - `acks=1` 
  - `enable.idempotence=false`

# Safe Kafka Producer - Summary
- Since Kafka 3.0, the producer is "safe" by default, otherwise, upgrade your clients or set the following settings
- `acks=all`
  - Ensure data is properly replicated before an ack is received
- `min.insync.replicas=2` (broker/ topic level)
  - Ensure two brokers in ISR at least have the data after an ack
- `enable.idempotence=true`
  - Duplicates are not introduced due to network retries
- `retries=MAX_INT` (producer level)
  - Retry until the `delivery.timeout.ms` is reached
- `delivery.timeout.ms=120000` 
  - Fail after retrying for 2 minutes
- `max.in.flight.requests.per.connection=5`
  - Ensure maximum performance while keeping message ordering
