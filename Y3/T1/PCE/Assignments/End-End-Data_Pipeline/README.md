Got it — plain, easy-to-read data flow. No files, just the flow and what each part does.

Microservices (A/B/C) → NiFi → Kafka → (1) HDFS (raw) and (2) Consumers (Spark/Hive) → Processed storage → BI / Dashboards

Short version, step-by-step:

1. **Microservices (producers)**

   * Generate logs/events (JSON, CLF, HTTP webhook).
   * Write to local log files or send HTTP events.

2. **NiFi (collector & normalizer)**

   * Reads/tails log files or listens to HTTP.
   * Parses and normalizes records to a common schema.
   * Publishes normalized records to Kafka topics.

3. **Kafka (message bus)**

   * Receives records on topics (e.g., `service-logs-raw`).
   * Acts as durable buffer so producers and consumers are decoupled.

4. **Two sinks from Kafka**
   A. **NiFi / Kafka Connect → HDFS**

   * Sink writes raw data files (JSON/Parquet) into HDFS, partitioned by date/service.
   * Purpose: persistent raw storage for audits and reprocessing.
     B. **Consumers (Spark streaming / Kafka Streams)**
   * Consume topic data in near real-time.
   * Transform, enrich, aggregate; output processed datasets.

5. **Processing & Query layer (Spark / Hive)**

   * Read raw files from HDFS or stream from Kafka.
   * Clean, deduplicate, compute aggregates (e.g., p95 latency), write results as partitioned Parquet or Hive tables.

6. **BI / Dashboard (Superset / Grafana)**

   * Connect to processed tables.
   * Visualize metrics, run SQL queries, and take demo screenshots.

7. **Extras (optional but useful)**

   * **Postgres**: store pipeline metadata (topic names, job runs).
   * **Monitoring**: Prometheus + Grafana for NiFi/Kafka metrics and alert/back-pressure.
   * **Load generator**: script to simulate traffic for testing/demo.

What to show in assignment/demo (minimum, in this order):

* Microservice log sample.
* NiFi flow screenshot + proof NiFi publishes to Kafka.
* Kafka topic list and offsets.
* HDFS directory listing showing raw files.
* Spark/Hive query and result (screenshot).
* One dashboard or chart screenshot.
* Short recovery test: stop Kafka/NiFi and show messages persist/resume.

That’s the whole pipeline in a readable flow. Want this condensed into one-page text you can paste into your assignment?
