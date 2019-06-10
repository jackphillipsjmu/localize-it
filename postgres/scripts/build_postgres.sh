#!/usr/bin/env bash
#
# Script to spin up PostgreSQL Docker instance which then has an example
# database and table created to set things up initially.
#

# Properties used by underlying script functions
DOCKER_COMPOSE_PATH=../resources/docker-postgres-compose.yml
LOG_DIR=../resources/tmp
CONTAINER_NAME=postgres
DB_NAME=example_db
TABLE_NAME=example_tbl


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
  LOG_FILE_PATH="$LOG_DIR/postgres_`date +%s`.log"
  echo "= INFO: Logs for this Docker container can be found in $LOG_FILE_PATH ="
  # Run docker compose
  docker-compose -f $dockerFile up --force-recreate --build > "$LOG_FILE_PATH" 2>&1 &
}

# Creates DB and Table in Postgres. This can also be done in the docker-compose
# file as well just modify the YAML file to read something like this
#   volumes:
#     - ./init.sql:/docker-entrypoint-initdb.d/init.sql
#
function setupExampleDatabaseAndTable {
  # Create database
  echo "= INFO: Creating Database $DB_NAME ="
  docker exec -it $CONTAINER_NAME psql -U postgres -c "create database $DB_NAME"
  # List Postgres DB's
  echo "= INFO: Listing Databases $DB_NAME ="
  docker exec -it $CONTAINER_NAME psql -U postgres -c "\list"
  # Create example table
  echo "= INFO: Creating Table $TABLE_NAME ="
  docker exec -it $CONTAINER_NAME psql -U $CONTAINER_NAME -c "CREATE TABLE IF NOT EXISTS $TABLE_NAME (id serial PRIMARY KEY, num integer, data varchar);"
  # Insert data
  echo "= INFO: Inserting sample data into $DB_NAME ="
  docker exec -it $CONTAINER_NAME psql -U $CONTAINER_NAME -c "INSERT INTO $TABLE_NAME (num, data) VALUES (1, 'A Database Record');"
  # Show the data that is in the table
  echo "= INFO: Data in $TABLE_NAME ="
  docker exec -it $CONTAINER_NAME psql -U $CONTAINER_NAME -c "SELECT * FROM $TABLE_NAME;"
}

# Checks to see if docker is running and if so will build the docker instance
function buildPostgres {
  # Create Log directory if it does NOT exist
  mkdir -p $LOG_DIR
  # If Docker is running then build the image
  if dockerRunning; then
    # Remove docker postgres container then rerun
    destroyDockerContainer $CONTAINER_NAME
    runDockerCompose $DOCKER_COMPOSE_PATH
    # Wait unti Postgres is up
    postgresUp=false
    while ! $postgresUp; do
      if grep -q "database system is ready to accept connections" "$LOG_FILE_PATH"; then
        echo "= INFO: PostgreSQL is up! ="
        postgresUp=true
      else
        echo "= INFO: PostgreSQL is not up yet, sleeping until it can accept connections. ="
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

# Run Dockerized PostgreSQL instance
buildPostgres
