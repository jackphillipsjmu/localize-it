#!/bin/bash
#
# Script will setup and run the necessary components to perform E2E processing
# of Weather Alert data.
#
LAMBDA_PROJECT_PATH="../com-alert-lambda"
LAMBDA_JAR_PATH="$LAMBDA_PROJECT_PATH/build/libs"
LAMBDA_JAR_NAME="/com-alert-lambda-0.1.jar"
ALERT_SERVICE_PATH="../com-alert-microservice"
LOCAL_STACK_UI_PORT=8080
LOCAL_KAFKA_PORT=9092
LOCAL_WEATHER_SERVICE_PORT=8081
DOCKER_FILE_DIR=../resources/docker/
KAFKA_DOCKER_FILE=docker-kafka-compose.yml
LOG_DIR=../resources/tmp

# Kills the process running on the port provided to the function
function killProcessOnPort {
  local portNumber=$1
  echo "= INFO: Killing process running on port $portNumber"
  lsof -i tcp:${portNumber} | awk 'NR!=1 {print $2}' | xargs kill
}

# Runs Dockerized Kafka and pipes output logs to a file
function startKafka {
  echo "= INFO: Running Docker Compose file $KAFKA_DOCKER_FILE ="
  docker-compose -f $DOCKER_FILE_DIR$KAFKA_DOCKER_FILE up > "$LOG_DIR/kafka_`date +%s`.log" 2>&1 &
}

# Runs Dockerized LocalStack and pipes output logs to a file
function startLocalStack {
  # Note this way of starting local stack is Mac specific
  # if you would like to run it on other machines try the simpler command
  # $ docker-compose up
  LOCAL_STACK_COMPOSE_PATH="../resources/docker/docker-localstack-compose.yml"
  TMPDIR=/private$TMPDIR docker-compose -f $LOCAL_STACK_COMPOSE_PATH up > "$LOG_DIR/localstack_`date +%s`.log" 2>&1 &
}

# Helper function to check if a process is running on a specific port.
# If something is running on the port it will return true, otherwise, false.
#
# param1 = port to check if a process is running on it
function processRunningOnPort {
  if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null ; then
    return
  else
    false
  fi
}

# Checks the current environment to ensure that everything is as it should be.
# If necessary dependencies aren't met then the script will terminate with
# a relevant error message.
function checkEnvironment {
  echo "= INFO: Checking Environment Setup ="
  # Create Log directory if it does NOT exist
  mkdir -p $LOG_DIR
  # If no process is running on the LocalStack UI port then display error
  # then prompt user if they would like to run LocalStack, if so, then run it
  # otherwise exit the script
  if ! processRunningOnPort $LOCAL_STACK_UI_PORT; then
    echo "= ERROR: LocalStack is not running! Please run the LocalStack setup before continuing ="
    echo -n "Would you like to run LocalStack now? (y/n)? "
    read localstack_answer
    # evaluate user input
    if [ "$localstack_answer" != "${localstack_answer#[Yy]}" ] ;then
      echo "= INFO: Running LocalStack ="
      startLocalStack
      echo "= INFO: Sleeping 15 seconds to let LocalStack start ="
      sleep 15
    else
      echo "= INFO: Exiting ="
      exit 1
    fi
  fi

  # Check that Kafka is running then prompt user if they would like to run Kafka,
  # if so, then run it, otherwise exit the script
  if ! processRunningOnPort $LOCAL_KAFKA_PORT; then
    echo "= ERROR: Kafka is not running! Please run the Kafka setup before continuing ="
    echo -n "Would you like to run Kafka now? (y/n)? "
    read kafka_answer
    # evaluate user input
    if [ "$kafka_answer" != "${kafka_answer#[Yy]}" ] ;then
      echo "= INFO: Running Kafka ="
      startKafka
      echo "= INFO: Sleeping 15 seconds to let Kafka start ="
      sleep 15
    else
      echo "= INFO: Exiting ="
      exit 1
    fi
  fi
}

# Builds Lambda and Microservice Gradle Projects
function buildAndProcessGradleProjects {
  echo "= INFO: Building Lambda Project ="
  ( cd $LAMBDA_PROJECT_PATH ; ./gradlew clean build )

  echo "= INFO: Building Microservice Project ="
  ( cd $ALERT_SERVICE_PATH ; ./gradlew clean build )
}

# Runs the Spring Boot Gradle Microservice
function runMicroservice {
  echo "= INFO: Running Microservice ="
  # Retrieve the Lambda JAR path to pass as an environment variable to Spring
  local localLambdaJarPath="$( cd $LAMBDA_JAR_PATH ; /bin/pwd )"$LAMBDA_JAR_NAME
  # Run microservice using gradle
  ( cd $ALERT_SERVICE_PATH ; ./gradlew bootRun "-DLAMBDA_JAR_PATH=$localLambdaJarPath")
}

# Removes all establised AWS resources if they exist
function clearResources {
  echo "= INFO: Removing Resources if Needed (ignore error messages) ="
  awslocal lambda delete-function --function-name weather-alert-lambda
  aws --endpoint-url=http://localhost:4572 s3 rb s3://alert-source-bucket --force
  aws --endpoint-url=http://localhost:4572 s3 rb s3://alert-sink-bucket --force
  # Kill running weather alert microservice/other process on port 8081
  killProcessOnPort $LOCAL_WEATHER_SERVICE_PORT
}

checkEnvironment
clearResources
buildAndProcessGradleProjects
runMicroservice
