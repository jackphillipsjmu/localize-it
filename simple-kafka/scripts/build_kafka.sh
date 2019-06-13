#!/usr/bin/env bash
#
# Script will setup a Dockerized Kafka instance.
#

# Script "constants"
readonly LOCAL_KAFKA_PORT=9092
readonly DOCKER_FILE_DIR=../resources/docker/
readonly KAFKA_DOCKER_FILE=docker-kafka-compose.yml
readonly LOG_DIR=../resources/tmp
readonly CONTAINER_NAME=kafka_docker

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

# Runs Dockerized Kafka and pipes output logs to a file
function startKafka {
  if processRunningOnPort $LOCAL_KAFKA_PORT; then
    echo "= INFO: A process is already running on port $LOCAL_KAFKA_PORT will not start Kafka ="
  else
    local logFile="$LOG_DIR/kafka_`date +%s`.log"
    echo "= INFO: Running Docker Compose file $KAFKA_DOCKER_FILE and logging output to $logFile="
    docker-compose -f $DOCKER_FILE_DIR$KAFKA_DOCKER_FILE up --force-recreate > "$logFile" 2>&1 &
  fi
}

# Builds and runs local Kafka instance in a Docker container
function runKafka {
  # Create log directory if it does NOT exist
  mkdir -p $LOG_DIR
  # Ensure that Docker is running then start Kafka
  if dockerIsRunning; then
    startKafka
  else
    echo "= ERROR: Please start Docker to run script ="
    exit 1
  fi
}

# Run Local Dockerized Kafka
runKafka
