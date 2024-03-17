# Kafka Connect 

## Why Kafka Connect
- Programmers always want to import data from the same sources:
  - Databases, JDBC, Couchbase, GoldenGate, SAP HANA, Blockchain, Cassandra, DynamoDB, FTP, IOT, MongoDB, MQTT 
    RethinkDB, Salesforce, S3, SFTP, SQL Server, Twitter, etc.
- Programmers always want to store data in the same sinks:
  - S3, Elasticsearch, HDFS, JDBC, MongoDB, Cassandra, Couchbase, Redis, etc.
![Connect_Pipeline.png](img%2FConnect_Pipeline.png)

## Kafka Connect - Architecture Design
![Connect_Architecture.png](img%2FConnect_Architechture.png)

## Kafka Connect - High level
- **Source Connectors** to get data from Common Data Source
- **Sink Connectors** to publish that data in Common Data Stores
- Make it easy for non-experienced dev to quickly get their data reliably into Kafka
- Part of your ETL pipeline
- Scaling made easy from small pipelines to company-wide pipelines
- Other programmers may already have done a very good job: re-usable code!
- Connectors achieve fault tolerance, idempotence, distribution, ordering
