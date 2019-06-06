#!/bin/bash
#
# Script to setup and deploy a Spring Boot microservice to Docker locally
# NOTE: Some operations in this script are MacOS specified but the proper
# checks are performed to ensure everything is working as intended.
#

# Report the usage of uninitialized variables
set -u

# Global variables used in script
MICROSERVICE_DIR=../com-example-microservice
MICROSERVICE_PORT=8089
DOCKER_CONTAINER_NAME=simple_springboot_docker
LOG_DIR=../resources/tmp

# Checks the provided String to see if it is empty or not
#
# param1=value to check for emptiness
# return=true if empty, false otherwise
function isEmpty {
  if [ -z "$1" ]; then
      return
  else
      false
  fi
}

# Checks the operating system to determine if it is supported or not
function isOperatingSystemSupported {
  echo "= INFO: Checking Operating System ="
  if [[ "$OSTYPE" == "darwin"* ]]; then
    # Mac OSX
    echo "= INFO: OS $OSTYPE supported! ="
    return
  else
    echo "= ERROR: $OSTYPE is not supported at this time! ="
    false
  fi
}

# Checks to see if Docker is running currently
function dockerRunning {
  docker_state=$(docker info >/dev/null 2>&1)
  if [[ $? -ne 0 ]]; then
    false
  else
    echo "= INFO: Docker is Running ="
    return
  fi
}

# Checks that Docker is running and prompts the user if it is not if they would
# like to start it. If so, a OS check is made to ensure it can be started
# in the appropriate manner. If all else fails, the script will exit.
function checkDockerIsRunning {
  if ! dockerRunning; then
    echo "= ERROR: Docker is NOT Running! ="
    echo -n "Would you like to run Docker now? (y/n)? "
    read docker_answer
    if [ "$docker_answer" != "${docker_answer#[Yy]}" ] ;then
      if isOperatingSystemSupported; then
        echo "= INFO: Running Docker ="
        open --hide --background -a Docker
        # Sleep until Docker is running
        dockerUp=false
        while ! $dockerUp; do
          if dockerRunning; then
            echo "= INFO: Docker is up! Continuing Build Process ="
            dockerUp=true
          else
            echo "= INFO: Waiting for Docker to Start ="
            sleep 10
          fi
        done
      else
        exit 1
      fi
    else
      echo "= INFO: Exiting, Docker is NOT Running ="
      exit 1
    fi
  fi
}

# Checks to see if a Docker container exists with the specified name
#
# param1=name of Docker container to check existance of
# return=true if container exists, false otherwise
function containerExists {
  # Grab first argument and check if the container exists
  if ! isEmpty "$(docker ps -q -f name=$1)"; then
    return
  else
    false
  fi
}

# Function that will force remove the specified Docker container if it exists
#
# param1=Docker container name to destroy
function destroyDockerContainer {
  # Grab first argument
  local containerName=$1
  # Check to see if we have a docker container to destroy then do so if it does
  if containerExists $containerName; then
    echo "= INFO: Destroying Docker Container $containerName"
    docker rm -f $containerName
  else
    echo "= INFO: Nothing to destroy, container $containerName was not found ="
  fi
}

# Builds Gradle Project using the provided parameters
#
# param1=Path to Gradle project to build
function buildAndProcessGradleProjects {
  echo "= INFO: Building Microservice Project at $1 ="
  ( cd $1 ; ./gradlew clean build )
}

# Runs Docker Compose using the provided parameters
#
# param1=Docker Compose file path
# param2=Path to Log
function startDockerCompose {
  echo "= INFO: Running Docker Compose file $1 and Logging to $2 ="
  docker-compose -f $1 up --build > "$2/microservice_`date +%s`.log" 2>&1 &
}

# Prints out potentially beneficial information for users
function printInformation {
  echo "= INFO: Spring Boot Service Running! ="
  echo "= INFO: http://localhost:$MICROSERVICE_PORT/index.html contains Service Documentation ="
  echo "= INFO: http://localhost:$MICROSERVICE_PORT/swagger-ui.html has REST Endpoints you can call in your browser ="
}

# Run necessary functions to get the microservice deployed to Docker
# Create Log directory if it does NOT exist
mkdir -p $LOG_DIR
checkDockerIsRunning
destroyDockerContainer $DOCKER_CONTAINER_NAME
buildAndProcessGradleProjects $MICROSERVICE_DIR
startDockerCompose $MICROSERVICE_DIR/docker-compose.yml $LOG_DIR
printInformation
