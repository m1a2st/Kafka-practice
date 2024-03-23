# Partitions Count & Replication Factor
- The **two most important** parameters when creating a topic
- They impact performance and durability of the system overall

![Partition_Replication.png](img%2FPartition_Replication.png)

- It is best to get the parameters right the first time!
  - If the partitions count increase during a topic lifecycle, you will break your keys ordering guarantees
  - If the replication factor increases during a topic lifecycle, you put more pressure on your cluster,
    which can lead to unexpected performance decrease

# Choosing the Partitions Count
- Each partition can handle a throughput of a few MB/s (measure it for your setup!)
- More partition implies:
  - Better parallelism, better throughput
  - Ability to run more consumers in a group to scale (max as many consumers per group as partitions)
  - Ability to leverage more brokers if you have a large cluster
  - BUT more elections to perform for Zookeeper (if using Zookeeper)
  - BUT more files opened on Kafka
- **Guidelines:**
  - **Partitions per topic = MILLION-DOLLAR QUESTION**
    - (Intuition) Small cluster (<6 brokers): 3 x # brokers 
    - (Intuition) Big cluster (>12 brokers): 2 x # brokers
    - Adjust for number of consumers you need to run in parallel at peek throughput
    - Adjust for producer throughput (increase if super-high throughput or projected increase in the next 2 years)
  - **TEST! Every Kafka cluster will have different performance**
  - Don't systematically create topics with 1000 partitions

# Choosing the Replication Factor
- Should be at least 2, usually 3, maximum 4
- The higher the replication factor (N):
  - Better durability of your system (N-1 brokers can fail)
  - Better availability of your system (`N-min.insync.replicas` if producer acks=all)
  - BUT more replication (higher latency if acks=all)
  - BUT more disk space on your system (50% more if RF is 3 instead of 2)
- **Guidelines**
  - **Set it to 3 to get started** (you must have at least 3 brokers for that)
  - If replication performance is an issue, get a better broker instead of less RF
  - **Never set it to 1 in production**

# Cluster guidelines
- Total umber of partitions in the cluster:
  - Kafka with Zookeeper: max 200,000 partitions - Zookeeper Scaling limit
    - Still recommend a maximum of 4,000 partitions per broker (soft limit)
  - Kafka with KRaft: potentially millions of partitions
- If you need more partition in your cluster, add brokers instead
- If you need more than 200,000 partitions in your cluster (it will take to get there!), follow the Netflix model
  and create more Kafka clusters
- Overall, you don't need a topic with 1000 partitions to achieve high throughput, **Start at a reasonable number**
  and test the performance

# Topic Naming Conventions
- Naming a topic is "free-for-all"
- It's better to enforce guidelines in your cluster to ease management
- You are free ro come up with your own guideline
- `<message type>.<dataset name>.<data name>.<data format>`
- Message Type:
  - logging: For logging data (sl4j, syslog, etc)
  - queuing: For classical queuing use cases
  - tracking: For tracking events such as clicks, page views, ad views, etc
  - etl/ db: For ETL and CDC use cases such as database feeds
  - streaming: For intermediate topics created by stream processing pipelines
  - push: For data that's being pushed from offline (batch computation) environments into online environments
  - user: For user-specific data such as scratch and test topics
- The dataset name field is analogous to a table name in traditional RMDBS systems, through it's fine to include
  further dotted notation if developers wish to impose their own hierarchy withing the dataset namespace.
- The data format for example .avro, .json, .text, .protobuf, .csv, .log
- Use snake case
