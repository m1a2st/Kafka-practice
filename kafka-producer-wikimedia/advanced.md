# `max.block.ms` & `buffer.memory`

- If the producer produces faster than the broker can take, the records will be buffered in memory
- `buffer.memory=33554432` (32MB): the size of the send buffer
- That buffer will fill up over time and empty back down when the throughput to the broker increases
- If that buffer is full (all 32MB), then the `.send()` method start block (won't return right away)
- `max.block.ms=60000` (60s): the time the `.send()` will block until throwing an exception.
  Exceptions are thrown when:
  - The producer has filled up buffer
  - The broker is not accepting any new data
  - 60 seconds has elapsed
- If you hit an exception hit that usually means your brokers are down or overloaded as they can't respond 
  to requests
