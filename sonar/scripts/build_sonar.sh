#!/usr/bin/env bash
#
# Script to build a Dockerized Sonar instance with a PostgreSQL backend
# to support it.
#

# Paths to resources necessary to get everything up and running
readonly DOCKER_FILE_DIR=../resources/docker/
readonly DOCKER_FILE=docker-sonar-compose.yml
readonly LOG_DIR=../resources/tmp

readonly SONAR_CONTAINER_NAME=sonar
readonly SONAR_DB_CONTAINER_NAME=sonar_postgres
readonly DB_PORT=5432
readonly SONAR_PORT=9000

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

  # Check to see if a process is running on the Sonar port
  if processRunningOnPort $SONAR_PORT; then
    echo "= INFO: Sonar Port $SONAR_PORT is in use! ="
    # Ensure that the Sonar port is being used by the sonar container
    # if it is NOT then display error and exit script
    if ! runningContainerExists $SONAR_CONTAINER_NAME; then
      echo "= ERROR: $SONAR_CONTAINER_NAME is not running on Port $SONAR_PORT! Please stop the proocess on port $SONAR_PORT and re-run script ="
      exit 1
    fi
  fi
  # Everything worked out, tell the user
  echo "= INFO: Environment checks complete! ="
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
  stopAndRemoveContainer $SONAR_CONTAINER_NAME
  stopAndRemoveContainer $SONAR_DB_CONTAINER_NAME
}

# Builds and runs Postgres and Kafka Docker containers
function buildContainer {
  local logFile="$LOG_DIR/sonar_`date +%s`.log"
  echo "= INFO: Running Docker Compose file $DOCKER_FILE and logging output to $logFile ="
  docker-compose -f $DOCKER_FILE_DIR$DOCKER_FILE up --force-recreate > "$logFile" 2>&1 &
}

# Check the port Sonar is running/going to run on and sleep until it is up
# and running.
function waitForSonarToRun {
  while ! processRunningOnPort $SONAR_PORT; do
    echo "= INFO: Waiting for Sonar to start ="
    sleep 10
  done
  echo "= INFO: Sonar port is open! Go to http://localhost:9000 to view the Sonar instance ="
}

# Ensure everything is good to go before continuing script
checkEnvironment
# Stop and remove containers to have a fresh run
cleanseDocker
# Build and start Sonar and Postgres Sonar containers
buildContainer
# Wait for Sonar to be up
waitForSonarToRun
