# postgres-kafka-cdc
Highlights data streaming and change data capture basics by creating local Docker containers that run [Zookeeper](https://zookeeper.apache.org/), [Kafka](https://kafka.apache.org/) and [PostgreSQL](https://www.postgresql.org/). The PostgreSQL instance has [Debezium](https://debezium.io/) configured to perform change data capture on the database to push changes to a Kafka topic.

### Current Execution Flow
By executing the script located at `$PROJECT_DIR/postgres-kafka-cdc/scripts/build_kafka_cdc.sh` it will perform the actions listed below:
- Check to see if Zookeeper, Kafka or PostgreSQL instances are up and running and tears them down if so.
- Ensure that Debezium connector resources are still available to download which is a necessity for the CDC portion to work.
- Build three Docker containers: Zookeeper, Kafka and PostgreSQL using `docker-compose` which will inherently create the `test` topic.
  - Note, container names are explicitly set in the `docker-kafka-compose.yml` file to `kafka_docker`, `zookeeper_docker` and `deb_postgres`.
- After the containers are running they will be further configured to do the following:
  - Create a plugin directory `/kafka/connect` in Kafka container to hold the Debezium connector resources.
  - Download `tar` file that contains connector resources to Kafka containers plugin directory and unzip the data.
  - Copy `$PROJECT_DIR/postgres-kafka-cdc/resources/postgres/postgres_worker.properties` file to the Kafka container location `/opt/kafka/config/postgres_worker.properties`. This property file is a slightly modified Kafka `connect-standalone.properties` file that specifies where the connect plugins live, i.e. `/kafka/connect` and what key/value converters to use which for simplicity are now set to `org.apache.kafka.connect.json.JsonConverter` but Avro can easily be introduced as well!
  - Copy `$PROJECT_DIR/postgres-kafka-cdc/resources/postgres/postgres_connector.properties` file to the Kafka container location `/opt/kafka/config/postgres_connector.properties`. This property file contains data that Debezium's PostgreSQL connector uses to tell what databse to connect to, what table to listen to and so on. For more information about these properties please refer to [Debezium's PostgreSQL connector properties documentation](https://debezium.io/docs/connectors/postgresql/#connector-properties).
- Next, the standalone Kafka connector will be started and log the output to the `$PROJECT_DIR/postgres-kafka-cdc/resources/tmp` directory with the log name being in the `kafka-connect-log_<DATE-IN-MILLIS>.log` format.
- From here a database table in PostgreSQL will be created named `example_db`.
- After database creation is done a table will be created, `example_tbl` and a randomized record inserted to be picked up by Debezium.
- Finally, all topics known to Kafka will be printed to the console and then a Kafka console consumer is created on the topic `example_db.public.example_tbl` which should contain the CDC message. The structure of this message is explained in Debezium's [streaming PostgreSQL changes documentation](https://debezium.io/docs/connectors/postgresql/#streaming-changes).
  - Note, to exit the console consumer press `CTRL + C`.

### Basic Kafka Commands
With the Kafka container running there is no need for you to install Kafka binaries/scripts locally you just need to utilize the container! You can invoke Kafka commands from the terminal either through `docker exec -it <COMMAND>` or by opening a bash shell in the container. If you would like to open a bash shell that connects to the Kafka container run `docker exec -it kafka_docker /bin/bash` from the command line and you will have a bash shell opened up for you. To exit, press `CTRL + D` and it will exit the running bash shell.

Here are a few examples you can use to work with Kafka without opening a bash shell. These commands still work in the shell just remove the `docker exec -it kafka_docker` portion of the command! For a more detailed reference refer to [Kafkas Quickstart Documentation](https://kafka.apache.org/quickstart).
- **Create a Topic**: `docker exec -it kafka_docker /opt/kafka/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic <TOPIC-NAME>`
- **List Topics**: `docker exec -it kafka_docker /opt/kafka/bin/kafka-topics.sh --list --bootstrap-server localhost:9092`
- **Start a Console Consumer on Topic test from the Beginning**: `docker exec -it kafka_docker /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning`
  - To exit the console consumer press `CTRL + C`.
- **Start a Console Producer on Topic test**: `docker exec -it kafka_docker /opt/kafka/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test`
  - To exit the console producer press `CTRL + C`.

#### References
  - Logs can be found locally at `$PROJECT_DIR/postgres-kafka-cdc/resources/tmp`
    - Log files with the format `postgres-kafka-cdc_<DATE-IN-MILLIS>.log` contain the Docker Compose logs.
    - Log files with the format  `kafka-connect-log_<DATE-IN-MILLIS>.log` contain the Kafka connector logs.
  - Learn more about Kafka by visiting their site [here](https://kafka.apache.org/).
  - For a more in depth look at Debezium's PostgreSQL connector look at their documentation which can be found [here](https://debezium.io/docs/connectors/postgresql/).
