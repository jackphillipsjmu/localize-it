
# Script Execution Steps:
# - Check Docker is running
# - Destroy existing Docker container with the name keras_docker
# - Move example Jupyter Notebooks from keras/resources/example into
# - local directory keras/resources/docker/volume which is connected to the Docker instance.
# - Execute Docker Compose to spin up Jupyter Docker instance
# - Get token from JSON file on Docker instance and print Jupyter URL
# - Attempt to open the Jupyter URL in the machines default browser if it is supported.
#
# References:
# - https://jupyter-docker-stacks.readthedocs.io/en/latest/using/specifics.html#apache-spark

# Properties used by underlying script functions
readonly DOCKER_COMPOSE_PATH=../resources/docker/docker-keras-compose.yml
readonly LOG_DIR=../resources/tmp
readonly CONTAINER_NAME=keras_docker
# Where to store Jupyter Notebooks and example(s)
readonly LOCAL_VOLUME_PATH=../resources/docker/volume
readonly LOCAL_VOLUME_EXAMPLE_PATH=$LOCAL_VOLUME_PATH
readonly EXAMPLE_NOTEPBOOKS_PATH=../resources/example
# Ports used by Docker
readonly JUPYTER_PORT=8888
# Security token info used to access Jupyter notebook
readonly TOKEN_PATH=/home/jovyan/.local/share/jupyter/runtime/nbserver-*.json
readonly TOKEN_KEY=token

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

# Checks to see if a Docker container exists with the specified name
#
# param1 = name of Docker container to check existance of
# return true if container exists, false otherwise
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
  LOG_FILE_PATH="$LOG_DIR/keras_`date +%s`.log"
  echo "= INFO: Logs for this Docker container can be found in $LOG_FILE_PATH ="
  # Run docker compose
  docker-compose -f $dockerFile up --force-recreate --build > "$LOG_FILE_PATH" 2>&1 &
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

# Helper function to check if a process is running on a specific port.
# If something is running on the port it will return true, otherwise, false.
#
# param1 = port to check if a process is running on it
function processRunningOnPort {
  # if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null ; then
  return
  # else
    # false
  # fi
}

# Retrieves JSON value that has a corresponding key
function getJsonValue {
  local jsonValue=$(echo $1 | grep -o '"'$2'": *"[^"]*"' | grep -o '"[^"]*"$')
  echo ${jsonValue##*|}
}

# Checks the current Operating System and attempts to open the default Browser
# with the provided URL
function openUrl {
  local url=$1
  echo "= INFO: Attempting to open URL $url ="
  case "$OSTYPE" in
    darwin*)  open $url ;;
    linux*)   xdg-open $url ;;
    msys*)    start $url ;;
    *)        echo "= ERROR: Cannot Open Browser with OS: $OSTYPE =" ;;
  esac
}

# Builds URL to Jupyter instance that can be accessed locally with security
# token attached to the URL
function printJupyterTokenURLAndOpenBrowser {
  # If there is no process running on the Jupyter default port then
  # wait until it is up
  if ! processRunningOnPort $JUPYTER_PORT; then
    # Sleep until Jupyter is running
    jupyterUp=false
    while ! $jupyterUp; do
      if processRunningOnPort $JUPYTER_PORT; then
        echo "= INFO: Jupyter is up! ="
        jupyterUp=true
      else
        echo "= INFO: Waiting for Jupyter to Start ="
        sleep 10
      fi
    done
  fi
  # Retrieve information that lives within the docker container concerning
  # security token credentials
  echo "= INFO: Retrieving Token for Authentication with Jupyter Notebook ="
  local tokenJson=$(docker exec $CONTAINER_NAME sh -c "cat $TOKEN_PATH")
  local tokenValue=$(getJsonValue "$tokenJson" $TOKEN_KEY | sed -e 's/^"//' -e 's/"$//')
  local localUrl=http://localhost:8888/?token=$tokenValue
  # Print out information
  echo "= INFO: You may access the Jupyter Notebook in your Browser at $localUrl ="
  # Try to open URL in default browser
  openUrl $localUrl
}

# Checks to see if docker is running and if so will build the docker instance.
# Also, this will handle creating necessary directories for logging and
# tying local data to the container.
function buildJupyterSpark {
  # Create Log directory and volume that ties to the container if they do NOT exist
  mkdir -p $LOG_DIR
  mkdir -p $LOCAL_VOLUME_EXAMPLE_PATH
  # Move examples to folder that will be connected to Docker container
  echo "= INFO: Copying Local Example Notebooks as Needed to Shared Volume ="
  cp -rn $EXAMPLE_NOTEPBOOKS_PATH $LOCAL_VOLUME_EXAMPLE_PATH
  # If Docker is running then build the image, otherwise, show error and exit
  if dockerRunning; then
    # Remove Docker Jupyter/Spark container then rerun
    destroyDockerContainer $CONTAINER_NAME
    runDockerCompose $DOCKER_COMPOSE_PATH
    # Extract and print URL to notebook and attempt to open it in the machines
    # default browser
    printJupyterTokenURLAndOpenBrowser
  else
    echo "= INFO: Docker is NOT running! Please run Docker then re-execute script. ="
    exit 1
  fi
}

# Call main function that will setup everything
buildJupyterSpark
