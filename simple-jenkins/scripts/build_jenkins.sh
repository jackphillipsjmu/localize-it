#!/bin/bash
#
# Script to build a dockerized jenkins instance that has the ability to
# pull in a GitHub project, build it and run it.
#
JENKINS_CONTAINER_NAME=simple_jenkins
JENKINS_DOCKER_FILE=../resources/docker/docker-jenkins-compose.yml
LOG_DIR=../resources/tmp
JENKINS_UI_PORT=8082
LOCAL_JENKINS_HOME=../resources/docker/home
LOCAL_JENKINS_DATA=../resources/docker/jenkins_data
LOCAL_SECRET_PATH=$LOCAL_JENKINS_DATA/secrets/initialAdminPassword

JENKINS_CLI_JAR=$LOCAL_JENKINS_DATA/war/WEB-INF/jenkins-cli.jar
ALIVE_MICROSERVICE_JOB_PATH=../resources/jobs/gradle_local_keep_alive.xml
DESTROY_JOB_PATH=../resources/jobs/destroy_gradle_local.xml
JENKINS_URL=http://localhost:8082

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

# Runs Jenkins Docker container and pipes output to log
function runDocker {
  local dockerFile=$1
  echo "= INFO: Running Docker Compose on file $dockerFile ="
  echo "= INFO: Logs for this Docker container can be found in the $LOG_DIR directory ="
  docker-compose -f $dockerFile up --force-recreate > "$LOG_DIR/jenkins_`date +%s`.log" 2>&1 &
}

# Shows how to execute a command in a Docker container
function executeCommandInContainer {
  local containerName=$1
  local command=$2
  echo "= INFO: Executing Command $command on Docker Container $containerName"
  docker exec -it $containerName $command
}

# Shows how to open a bash shell in docker container
function openBashContainerTerminal {
  local containerName=$1
  echo "= INFO: Opening Bash Shell for container $containerName"
  # Check if the container exists and then run operation
  if containerExists $containerName; then
    docker exec -it $containerName /bin/bash
  else
    echo "= ERROR: Cannot open shell container $containerName does not exist! ="
  fi
}

# Calls the underlying docker command to retrieve the admin password that is
# present on the Jenkins instance to show how to do this operation without
# relying on the Jenkins local filesystem that is being shared
function printInitialJenkinsPassword {
  local containerName=$1
  local catCmd="cat /var/jenkins_home/secrets/initialAdminPassword"
  echo "= INFO: Retrieving Password for Container $containerName ="

  # If Jenkins UI is running then retrieve the password
  if processRunningOnPort $JENKINS_UI_PORT; then
    jenkinsPasswd="$(docker exec -it $containerName $catCmd)"
    echo "Password = $jenkinsPasswd"
  else
    echo "= ERROR: Jenkins is not running! Cannot retrieve Jenkins password ="
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

# Function that will stop the specified Docker container if it exists and
# is currently running
#
# param1=Docker container name to stop
function stopDockerContainer {
  # Grab first argument
  local containerName=$1
  # Check to see if we have a docker container to stop
  if containerExists $containerName; then
    if [ "$(docker ps -aq -f status=running -f name=$containerName)" ]; then
        echo "= INFO: Stopping Container $containerName ="
        docker container stop $containerName
      else
        echo "= INFO: Container $containerName is NOT running ="
    fi
  else
    echo "= INFO: Nothing to stop, container $containerName was not found ="
  fi
}

# Removes the provided directory if it exists
function removeDirectory {
  local dirToRemove=$1
  echo "= INFO: Removing $dirToRemove ="
  if [ -d "$dirToRemove" ]; then rm -Rf $dirToRemove; fi
}

# Build Jenkins Docker container and retrieve credential information
# to log into console if other users are not established later on
function buildJenkins {
  echo "= INFO: Building Jenkins ="
  # Run Jenkins Docker
  runDocker $JENKINS_DOCKER_FILE
  # Wait until we can retrieve the password
  # Print password to terminal/wait until it exists
  while [ ! -f "$LOCAL_SECRET_PATH" ]; do
      echo "= INFO: Admin Secret Not Available! Sleeping until it can be attained ="
      sleep 15
  done
  # Print password to console
  printInitialJenkinsPassword $JENKINS_CONTAINER_NAME
}

# Destroy all resources for a fresh deployment of a Jenkins instance
function cleanJenkinsAndBuild {
  echo "= INFO: Cleaning Jenkins (NOTE: it take several minutes to retrieve initial Jenkins Admin Password) ="
  # Destroy Jenkins Container
  destroyDockerContainer $JENKINS_CONTAINER_NAME
  # Remove old directories for fresh run
  removeDirectory $LOCAL_JENKINS_HOME
  removeDirectory $LOCAL_JENKINS_DATA
  # Build Jenkins using Docker
  buildJenkins
}

# Creates a Jenkins user
function createUser {
  echo "= INFO: Creating user $1 with password $2 ="
  echo 'jenkins.model.Jenkins.instance.securityRealm.createAccount('\"$1\"', '\"$2\"')' | java -jar $JENKINS_CLI_JAR -auth $3:$4 -s $JENKINS_URL groovy =
}

# Creates and runs example Jenkins job that builds and runs spring boot service
function createAndRunJenkinsJob {
  ADMIN_USER=admin
  ADMIN_PASS=$(cat $LOCAL_SECRET_PATH)
  JENKINS_USER="jenkins"
  JENKINS_USER_PASS="password"

  # Create another user that has easier credentials to remember ;)
  createUser $JENKINS_USER $JENKINS_USER_PASS $ADMIN_USER $ADMIN_PASS

  # Create job to build and run spring boot project
  local jobName="gradle-alive-local"
  echo "= INFO: Exporting Jenkins Job $jobName located at $ALIVE_MICROSERVICE_JOB_PATH"
  java -jar $JENKINS_CLI_JAR -s $JENKINS_URL -auth $ADMIN_USER:$ADMIN_PASS create-job $jobName < $ALIVE_MICROSERVICE_JOB_PATH
  # Execute jon
  echo "= INFO: Running Jenkins Job $jobName"
  java -jar $JENKINS_CLI_JAR -s $JENKINS_URL -auth $ADMIN_USER:$ADMIN_PASS build $jobName

  echo "= INFO: Jenkins Job progress can be viewed at $JENKINS_URL/job/gradle-alive-local ="
  echo "= INFO: Once complete, you may use the deployed service at http://localhost:8085/swagger-ui.html ="
  echo "= INFO: To terminate the job go to $JENKINS_URL/job/gradle-alive-local and click the red X button next to running build ="
}

# Main function to build/run/clean Jenkins related operations
function executeJenkins {
  # Create Log directory if it does NOT exist
  mkdir -p $LOG_DIR
  echo -n "Would you like to Clean and Build Jenkins (y/n)? "
  read cleanBuildAnswer
  if [ "$cleanBuildAnswer" != "${cleanBuildAnswer#[Yy]}" ] ;then
    cleanJenkinsAndBuild
  else
    buildJenkins
  fi

  # Check if they would like to create and run a Jenkins example job
  echo -n "Would you like to Create and Run the Jenkins Microservice Job (y/n)? "
  read jenkinsJobAnswer
  if [ "$jenkinsJobAnswer" != "${jenkinsJobAnswer#[Yy]}" ] ;then
    createAndRunJenkinsJob
  else
    echo "= INFO: Using the admin password please go to your Jenkins server at $JENKINS_URL and start using your instance! ="
  fi
}

# Call main function
executeJenkins
