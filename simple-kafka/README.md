# simple-kafka
Creates a Docker container that contains a Kafka instance and creates a topic `test` to play around with.

### Current Execution Flow
By executing the script located at `$PROJECT_DIR/simple-kafka/scripts/build_kafka.sh` it will perform the actions listed below:
- Check to see if anything is running on port `9092` which is the port exposed by Kafka. If nothing is running on the port the script will continue.
- Build two Docker containers (Zookeeper and Kafka) using `docker-compose` which will inherently create the `test` topic.
  - Note, container names are explicitly set in the `docker-kafka-compose.yml` file to `kafka_docker` and `zookeeper_docker`.

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
  - Logs can be found locally at `$PROJECT_DIR/simple-kafka/resources/tmp`
  - Learn more about Kafka by visiting their site [here](https://kafka.apache.org/).
