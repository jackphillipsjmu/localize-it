#!/usr/bin/env bash
#
# Script that contains common Docker functions you can use in your scripts.
#

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

# Runs Docker Compose File at the provided location
# This will first check for the existence of the docker-compose.yml file
# then perform the command or respond with an error message.
#
# Example Use:
# runDockerComposeFile $PATH_TO_FILE
function runDockerComposeFile {
  # Check to see if the file exists
  if [ -f "$1" ]; then
    docker-compose -f $1 up
  else
    echo "= ERROR: Docker Compose File $1 does not exist!"
  fi
}

# Runs Docker Compose File at the provided location and forces recreation.
# This will first check for the existence of the docker-compose.yml file
# then perform the command or respond with an error message.
#
# Example Use:
# runDockerComposeFileForceRecreate $PATH_TO_FILE
function runDockerComposeFileForceRecreate {
  # Check to see if the file exists
  if [ -f "$1" ]; then
    docker-compose -f $1 up --force-recreate
  else
    echo "= ERROR: Docker Compose File $1 does not exist!"
  fi
}

# Checks to see if a Docker container exists with the specified name even
# if the container is stopped.
#
# Example Use:
# if containerExists $NAME; then echo "Exists"; else echo "Doesn't exist"; fi
#
# param1=name of Docker container to check existance of
# return=true if container exists, false otherwise
function containerExists {
  # Grab first argument and check if the container exists
  # -q = quiet
  # -a = all containers
  # -f = filter (by name in this case)
  if [ -z "$(docker container ls -q -a -f name=$1)" ]; then
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

# Lists all RUNNING Docker Containers
#
# Example Use:
# listRunningDockerContainers
function listRunningDockerContainers {
  docker ps
}

# Lists all Docker Containers regardless if they're running or not
#
# Example Use:
# listAllDockerContainers
function listAllDockerContainers {
  docker ps -a
}

# Stops all RUNNING Docker Containers
#
# Example Use:
# stopAllRunningContainers
function stopAllRunningContainers {
  docker stop $(docker ps -aq)
}

# Removes all Docker Containers
#
# Example Use:
# removeAllContainers
function removeAllContainers {
  docker rm $(docker ps -aq)
}

# Removes all Docker images
#
# Example Use:
# removeAllImages
function removeAllImages {
  docker rmi $(docker images -q)
}

# Executes a command in a Docker container
#
# param1 = Container name to run command inside of
# param2 = Command to run in container
function executeCommandInContainer {
  local containerName=$1
  local command=$2
  if runningContainerExists $containerName; then
    docker exec -it $containerName $command
  else
    echo "= ERROR: No running container exists for $containerName ="
  fi
}

# Opens a bash shell in Docker container
#
# param1 = Container name to open Bash script inside
function openBashContainerTerminal {
  local containerName=$1
  # Check if the container exists and then run operation
  if runningContainerExists $containerName; then
    docker exec -it $containerName /bin/bash
  else
    echo "= ERROR: Cannot open shell container $containerName does not exist! ="
  fi
}
