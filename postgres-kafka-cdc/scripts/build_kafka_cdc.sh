#!/usr/bin/env bash
#
# Script will setup a Dockerized Kafka instance
#

# Script "constants"
# Ports that are exposed by Docker containers
readonly LOCAL_KAFKA_PORT=9092
readonly LOCAL_POSTGRES_PORT=5432
# Paths to resources necessary to get everything up and running
readonly DOCKER_FILE_DIR=../resources/docker/
readonly DOCKER_FILE=docker-all-compose.yml
readonly LOG_DIR=../resources/tmp
# Names of Docker containers that will be created
readonly KAFKA_CONTAINER_NAME=kafka_docker
readonly ZOOKEEPER_CONTAINER_NAME=zookeeper_docker
readonly POSTGRES_CONTAINER_NAME=deb_postgres
# Example DB and Table name to push to Postgres
readonly DB_NAME=example_db
readonly TABLE_NAME=example_tbl
# Kafka -> Postgres Connector related variables
# PG_WORKER = Modified Kafka connect-standalone.properties that specifies where the connect plugins live
readonly PG_WORKER=../resources/postgres/postgres_worker.properties
# PG_CONNECTOR = Connector properties to tell the Debezium Postgres connector what to do
readonly PG_CONNECTOR=../resources/postgres/postgres_connector.properties
# use the property files
readonly DOCKER_KAFKA_CONFIG_DIR=/opt/kafka/config
readonly KAFKA_PG_OUT="$KAFKA_CONTAINER_NAME:$DOCKER_KAFKA_CONFIG_DIR/postgres_connector.properties"
readonly KAFKA_WORKER_PG_OUT="$KAFKA_CONTAINER_NAME:$DOCKER_KAFKA_CONFIG_DIR/postgres_worker.properties"
# Debezium URL to download in Kafka Docker container checks in script are there
# to ensure this exists but if it is ever moved/you want to upgrade the version
# then this will need to be updated.
readonly DEBEZIUM_URL=https://repo1.maven.org/maven2/io/debezium/debezium-connector-postgres/0.9.5.Final/debezium-connector-postgres-0.9.5.Final-plugin.tar.gz
# Kafka topic name built by Debezium connector
readonly EXAMPLE_TOPIC=$DB_NAME.public.$TABLE_NAME

# Helper function to check if a process is running on a specific port.
# If something is running on the port it will return true, otherwise, false.
#
# Example Use:
# if processRunningOnPort $PORT; then echo "Running"; else echo "Not Running"; fi
#
# param1 = Port to check to see if a process is running on it
# return "true" if a process is running on the port, "false" otherwise
function processRunningOnPort {
  if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null ; then
    true
  else
    false
  fi
}

# Checks to see if Docker is running currently
#
# Example Use:
# if dockerIsRunning; then echo "Running"; else echo "Not Running"; fi
function dockerIsRunning {
  docker_state=$(docker info >/dev/null 2>&1)
  if [[ $? -ne 0 ]]; then
    false
  else
    true
  fi
}

# Checks to see if a Docker running container exists with the specified name.
#
# Example Use:
# if runningContainerExists $NAME; then echo "Exists"; else echo "Doesn't exist"; fi
#
# param1=name of Docker container to check existance of
# return=true if container exists, false otherwise
function runningContainerExists {
  # Grab first argument and check if the container exists
  # -q = quiet
  # -f = filter (by name in this case)
  if [ -z "$(docker container ls -q -f name=$1)" ]; then
      false
  else
      true
  fi
}

# Executes a command in a Docker container, if the container does NOT exist
# the script will exit.
#
# param1 = Container name to run command inside of
# param2 = Command to run in container
function executeCommandInContainer {
  local containerName=$1
  local command=$2
  if runningContainerExists $containerName; then
    docker exec -i $containerName $command
  else
    echo "= ERROR: No running container exists for $containerName ="
    exit 1
  fi
}

# Builds and runs Postgres and Kafka Docker containers
function buildContainers {
  local logFile="$LOG_DIR/postgres-kafka-cdc_`date +%s`.log"
  echo "= INFO: Running Docker Compose file $DOCKER_FILE and logging output to $logFile="
  docker-compose -f $DOCKER_FILE_DIR$DOCKER_FILE up --force-recreate > "$logFile" 2>&1 &
}

# Stops and removes a Docker container by its name.
#
# Example Use:
# stopAndRemoveContainer $CONTAINER_NAME
#
# param1 = Name of Docker container to stop and remove
function stopAndRemoveContainer {
  local containerName=$1
  if runningContainerExists $containerName; then
    echo "= INFO: Stopping container $containerName ="
    docker stop $containerName
  else
    echo "= INFO: No running container $containerName found! No need to stop Container ="
  fi

  echo "= INFO: Removing container $containerName ="
  docker rm $containerName
}

# Stops and removes Kafka and Postgres containers
function cleanseDocker {
  stopAndRemoveContainer $KAFKA_CONTAINER_NAME
  stopAndRemoveContainer $ZOOKEEPER_CONTAINER_NAME
  stopAndRemoveContainer $POSTGRES_CONTAINER_NAME
}

# Wait until Kafka/Postgres are up
function waitForDocker {
  while ! processRunningOnPort $LOCAL_KAFKA_PORT; do
    echo "= INFO: Waiting for Kafka to start ="
    sleep 5
  done

  while ! processRunningOnPort $LOCAL_POSTGRES_PORT; do
    echo "= INFO: Waiting for PostgreSQL to start ="
    sleep 5
  done
}

# Creates an example DB Table in Postgres to be subject to CDC
function createExampleTable {
  # Create example table
  echo "= INFO: Creating Table $TABLE_NAME ="
  docker exec -it $POSTGRES_CONTAINER_NAME psql -U postgres -c "CREATE TABLE IF NOT EXISTS $TABLE_NAME (id serial PRIMARY KEY, num integer, data varchar);"
}

# Creates a randomized Postgres record to insert into the example table
function insertRandomRecord {
  echo "= INFO: Inserting sample data into $TABLE_NAME ="
  # Generate number between 1 and 99
  local randomNum=${RANDOM:0:2}
  local sqlInsert="INSERT INTO $TABLE_NAME (num, data) VALUES ($randomNum, 'Database Record $randomNum');"
  echo "= INFO: Performing insertion -> $sqlInsert ="
  docker exec -it $POSTGRES_CONTAINER_NAME psql -U postgres -c "$sqlInsert"
}

# Validates that the URL provided to the function exists
#
# param1 = URL to validate
# return=true if URL exists/is valid, false otherwise
function validateUrl() {
  # Make sure the endpoint exists and account for servers refusing HEAD requests
  # this can be done as well if the server doesn't care about HEAD requests
  # like so: curl --output /dev/null --silent --head --fail "$1"
  if curl --output /dev/null --silent --fail -r 0-0 "$1"; then
    true
  else
    false
  fi
}

# Works inside Docker containers to setup Kafka Postgres connector by
# creating a plugin directory, downloading necessary dependencies, copying
# over property files and finally running the standalone connector.
function setupKafkaPostgresConnector {
  # Create plugin directory in Kafka container
  executeCommandInContainer $KAFKA_CONTAINER_NAME "mkdir -p /kafka/connect"
  # Download tar file that contains connector JARs to Kafka container
  getDebeziumPostgresCmd="wget -O /kafka/connect/debezium-connector-postgres.tar.gz -c $DEBEZIUM_URL"
  executeCommandInContainer $KAFKA_CONTAINER_NAME "$getDebeziumPostgresCmd"
  # Extract JARs from previous download into the recently created plugin directory
  untarCmd="tar -zxvf /kafka/connect/debezium-connector-postgres.tar.gz -C /kafka/connect/"
  executeCommandInContainer $KAFKA_CONTAINER_NAME "$untarCmd"
  # Copy necessary configuration files to Kafka container
  docker cp $PG_WORKER $KAFKA_WORKER_PG_OUT
  docker cp $PG_CONNECTOR $KAFKA_PG_OUT
  # Start standalone Kafka connector and log output to tmp directory
  local kafkaConnectLog="$LOG_DIR/kafka-connect-log_`date +%s`.log"
  echo "= INFO: Starting Kafka Postgres Connector and Logging output to $kafkaConnectLog ="
  standalonePostgresKafkaCmd="/opt/kafka/bin/connect-standalone.sh /opt/kafka/config/postgres_worker.properties /opt/kafka/config/postgres_connector.properties"
  executeCommandInContainer $KAFKA_CONTAINER_NAME "/opt/kafka/bin/connect-standalone.sh /opt/kafka/config/postgres_worker.properties /opt/kafka/config/postgres_connector.properties" > "$kafkaConnectLog" 2>&1 &
}

# Ensures that everything is in order before attempting to run the rest of
# the script
function checkEnvironment {
  # Create Log directory if it does NOT exist
  mkdir -p $LOG_DIR

  # Ensure that Docker is running
  if ! dockerIsRunning; then
    echo "= ERROR: Please start Docker to run script ="
    exit 1
  fi

  # Check that the Debezium Connector code is still alive
  if validateUrl $DEBEZIUM_URL; then
    echo "= INFO: Debezium Postgres Connector still alive! ="
  else
    echo "= ERROR: $DEBEZIUM_URL no longer exists! Cannot download dependencies ="
    exit 1
  fi
}

function listKafkaTopics {
  # Retrieve Kafka topics from container
  topics=$(executeCommandInContainer $KAFKA_CONTAINER_NAME "/opt/kafka/bin/kafka-topics.sh --list --bootstrap-server localhost:9092")
  # if there are no topics then say so, otherwise, print them out
  if [ -z $topics ]; then
      echo "= INFO: No Kafka topics present ="
  else
      echo "= INFO: Kafka Topics $topics ="
  fi
}

# Starts a Kafka consumer on the provided topic
function startConsoleConsumer {
  local topicName=$1
  if [ -z $topicName ]; then
    echo "= ERROR: Cannot start Kafka Consumer on a empty topic! ="
    exit 1
  else
    echo "= INFO: Starting Consumer on Topic $topicName (press CTRL + C to exit) ="
    executeCommandInContainer $KAFKA_CONTAINER_NAME "/opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic $topicName --from-beginning"
  fi
}

# Ensure everything is good to go before continuing script
checkEnvironment
# Stop and remove containers to have a fresh run
cleanseDocker
# Build and start Kafka and Postgres containers
buildContainers
# Wait until both Kafka and Postgres are running
waitForDocker
# Create an example table in the Postgres DB
setupKafkaPostgresConnector
# Setup our data
createExampleTable
insertRandomRecord

# Helper functions to interact with Kafka in container
listKafkaTopics
# Start Kafka Console Consumer
startConsoleConsumer $EXAMPLE_TOPIC
