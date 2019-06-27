#!/usr/bin/env bash
#
# Script to spin up Elasticsearch and Kibana
#

# Properties used by underlying script functions
# readonly DOCKER_COMPOSE_PATH=../resources/docker/docker-elastic-kibana-compose.yml
readonly DOCKER_COMPOSE_PATH=../resources/docker/docker-es-kibana-compose.yml
readonly LOG_DIR=../resources/tmp

# What version of ES and Kibana to use
# this will be picked up by the underlying Docker file(s)
export ELK_VERSION=7.1.1

# Checks to see if Docker is running currently
#
# # return true if Docker is running, false otherwise
function dockerRunning {
  docker_state=$(docker info >/dev/null 2>&1)
  if [[ $? -ne 0 ]]; then
    false
  else
    echo "= INFO: Docker is Running ="
    return
  fi
}

# Checks to see if a file exists using the specified path.
# NOTE: This will only check files and NOT directories.
#
# Example Use:
# if fileExists "$FILE"; then echo "Exists!"; else echo "Does NOT Exist"; fi
#
# param1 = Path to file to check for existence
# return "true" if the file exists, "false" otherwise
function fileExists {
  if [ -f "$1" ]; then
    true
  else
    false
  fi
}

# Runs the provided Docker compose file and pipes out log data to file
#
# param1 = Path to the Doocker compose file
function runDockerCompose {
  local dockerFile="$1"
  echo "= INFO: Running Docker Compose on file $dockerFile ="
  local logFilePath="$LOG_DIR/elastic_kibana_`date +%s`.log"
  echo "= INFO: Logs for this Docker container can be found in $logFilePath ="
  # Run docker compose
  docker-compose -f $dockerFile up --force-recreate --build > "$logFilePath" 2>&1 &
}

function runElasticsearchAndKibana {
  echo "= INFO: Using Elasticsearch and Kiban Version $ELK_VERSION ="
  # Create Log directory if it does NOT exist
  mkdir -p $LOG_DIR
  # If Docker is running then build the image
  if dockerRunning; then
    # Run Dockerized Elasticsearch and Kibana
    runDockerCompose $DOCKER_COMPOSE_PATH
  else
    echo "= ERROR: Docker is NOT running please run Docker then Retry ="
    exit 1
  fi
}

# Run Dockerized ES and Kibana instances
runElasticsearchAndKibana
