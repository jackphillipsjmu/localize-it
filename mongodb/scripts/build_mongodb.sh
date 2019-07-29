#!/usr/bin/env bash
#
# Script to spin up MongoDB Docker instance which then has an example
# database and table created to set things up initially.
#

# Properties used by underlying script functions
DOCKER_COMPOSE_PATH=../resources/docker-mongodb-compose.yml
MONGODB_DATALOAD_PATH=../resources/json/data_load.js
LOG_DIR=../resources/tmp
CONTAINER_NAME=mongo
DB_NAME=admin

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


# Checks the provided String to see if it is empty or not
#
# param1 = value to check for emptiness
# return true if empty, false otherwise
function isEmpty {
  if [ -z "$1" ]; then
      return
  else
      false
  fi
}


# Checks to see if a Docker container exists with the specified name
#
# param1 = name of Docker container to check existance of
# return true if container exists, false otherwise
function containerExists {
  # Grab first argument and check if the container exists
  if ! isEmpty "$(docker container ls -q -a -f name=$1)"; then
    return
  else
    false
  fi
}

# Function that will force remove the specified Docker container if it exists
#
# param1 = Docker container name to destroy
function destroyDockerContainer {
  # Grab first argument
  local containerName=$1
  # Check to see if we have a docker container to destroy then do so if it does
  if containerExists $containerName; then
    echo "= INFO: Destroying Docker Container $containerName ="
    docker rm -f $containerName
  else
    echo "= INFO: Nothing to destroy, container $containerName was not found ="
  fi
}

# Runs the provided Docker compose file and pipes out log data to file
#
# param1 = Path to the Doocker compose file
function runDockerCompose {
  local dockerFile=$1
  echo "= INFO: Running Docker Compose on file $dockerFile ="
  LOG_FILE_PATH="$LOG_DIR/mongodb_`date +%s`.log"
  echo "= INFO: Logs for this Docker container can be found in $LOG_FILE_PATH ="
  # Run docker compose
  docker-compose -f $dockerFile up --force-recreate --build > "$LOG_FILE_PATH" 2>&1 &
}

# Creates DB and Table in MongoDB. This can also be done in the docker-compose
# file as well just modify the YAML file to read something like this
#   volumes:
#     - ./init.sql:/docker-entrypoint-initdb.d/init.sql
#
function setupExampleDatabaseAndTable {
  # Create database
  echo "= INFO: Creating Database $DB_NAME ="
  docker exec $CONTAINER_NAME mongo --eval "db = db.getSiblingDB('admin'); db.createCollection('companies'); db.companies.insert([{'name':'John'}]); db.companies.insert([{'name':'Smith'}]); db.companies.insert([{'name':'Doe'}]); db.companies.find()"
}

# Checks to see if docker is running and if so will build the docker instance
function buildMongodb {
  # Create Log directory if it does NOT exist
  mkdir -p $LOG_DIR
  # If Docker is running then build the image
  if dockerRunning; then
    # Remove docker MongoDB container then rerun
    destroyDockerContainer $CONTAINER_NAME
    runDockerCompose $DOCKER_COMPOSE_PATH
    # Wait unti MongoDB is up
    mongodbUp=false
    while ! $mongodbUp; do
      if grep -q "waiting for connections on port" "$LOG_FILE_PATH"; then
        echo "= INFO: MongoDB is up! ="
        mongodbUp=true
      else
        echo "= INFO: MongoDB is not up yet, sleeping until it can accept connections. ="
        sleep 5
      fi
    done
  else
    echo "= INFO: Docker is NOT running! Please run Docker then re-execute script. ="
    exit 1
  fi
  # Create example DB and table
  setupExampleDatabaseAndTable
}

# Run Dockerized Mongodb instance
buildMongodb
