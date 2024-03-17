# Controlling Consumer Liveliness
- Consumers in a Group talk to a Consumer Groups Coordinate
- To detect consumers that are "down", there is a "heartbeat" mechanism and a "poll" mechanism
- **To avoid issues, consumers are encouraged to process data fast and poll often**
![Controlling_Consumer_Liveliness.png](..%2Fimg%2FControlling_Consumer_Liveliness.png)

# Consumer Heartbeat Thread
- `heartbeat.interval.ms` (default 3 seconds):
  - How often to send heartbeats
  - Usually set to 1/3rd of `session.timeout.ms`
- `session.timeout.ms` (default 45 seconds kafka 3.0+, before 10 seconds):
  - Heartbeats are sent periodically to the broker
  - If no heartbeat is sent during that period, the consumer is considered dead
  - Set even lower to faster consumer rebalances
**Take-away: This mechanism is used to detect a consumer application being down**

# Consumer Poll Thread
- `max.poll.interval.ms` **(default 5 minutes)**:
  - Maximum amount of time between two `.poll()` calls before declaring the consumer dead
  - This is relevant for Big Data frameworks like Spark in case the processing takes time
**Take-away: This mechanism is used to detect a data processing issue with the consumer (consumer is "stuck")**
- `max.poll.records` **(default 500)**:
  - Controls how many records to receive per poll request
  - Increase if your message are very small and have a lot of available RAM
  - Good to monitor how many records are polled per request
  - Lower if it takes you too much time to process records
  
# Consumer Poll Behavior
- `fetch.min.bytes` **(default 1 byte)**:
  - Controls how much data you want to pull at least on each request
  - Helps to improve throughput and decreasing request number
  - At the cost of latency
- `fetch.max.wait.ms` **(default 500 ms)**:
  - The maximum amount of time the Kafka broker will block before answering the  fetch request if there isn't
    sufficient data to immediately satisfy the requirement given by `fetch.min.bytes`
  - This means that until the requirement of `fetch.min.bytes` to be satisfied, you will have up to 500 ms of 
    latency before the fetch returns data to the consumer (e.g. introducing a potential delay to be more efficient in 
    requests)

# Default Consumer behavior with partition leaders
- Kafka Consumers by default will read from the leader broker for a partition
- Possibly higher latency (multiple data centre), + high network charges ($$$)
- Example: Data centre === Availability Zone(AZ), you pay for Cross AZ network charges

![Partition_Leader.png](..%2Fimg%2FPartition_Leader.png)

# Kafka Consumer Replica Fetching (Kafka 2.4+)
- Since Kafka 2.4, it is possible to configure consumers to read from **the closet replica**
- This may help improve latency, and also decrease network costs if using the cloud

![Replica_Fetching.png](..%2Fimg%2FReplica_Fetching.png)

# Consumer Rack Awareness (v2.4+) - How to Set up
- Broker setting:
  - Must be version Kafka v2.4+
  - `rack.id` config must be set to the data centre ID (ex: AZ ID in AWS)
  - Example for AWS: AZ ID `rack.id=us-east-1a`
  - `replica.selector.class` must be set to `org.apache.kafka.common.replica.RackAwareReplicaSelector`
- Consumer client setting:
  - Set `client.rack` to the data centre ID the consumer is launched on
