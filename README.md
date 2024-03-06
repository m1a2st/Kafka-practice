# Kafka practice

## Kafka Producer: Java API - Basic

- View basic configuration parameters
- Confirm we receive the data in a Kafka Console Consumer

### Environment variables:

| Variable | Value |
| --- | --- |
| bootstrap_servers | localhost:9092 |
| username | user |
| pwd | password |

### Create a topic:

```bash
kafka-topics --command-config playground.config --bootstrap-server <bootstrap_servers> --topic demo_java --create --partitions 3
```

### Consume the topic:

```bash
kafka-console-consumer --command-config playground.config --bootstrap-server <bootstrap_servers> --topic demo_java --from-beginning
```

## Kafka Producer: Java API - Callbacks
