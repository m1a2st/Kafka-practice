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
