#!/bin/bash
#
# Script to build a dockerized jenkins instance that has the ability to
# pull in a GitHub project, build it and run it.
#
# React + Jenkins = https://jenkins.io/doc/tutorials/build-a-node-js-and-react-app-with-npm/
# Doom in Browser = https://js-dos.com/DOOM/
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
REACT_JOB_PATH=../resources/jobs/doom_react_app.xml
JENKINS_URL=http://localhost:8082
WEB_APP_URL=http://localhost:3000
WEB_APP=../resources/web/simple-node-js-react-npm-app
JENKINS_APP=$LOCAL_JENKINS_HOME/simple-node-js-react-npm-app

WEB_APP_WORKSPACE=$LOCAL_JENKINS_DATA/workspace/doom-react

# Check operating system to ensure we can execute certain functions
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
function waitForDockerToRun {
  if ! dockerRunning; then
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
      echo "= ERROR: Cannot wait for Docker to run"
      exit 1
    fi
  fi
}

# Starts docker in background, currently is only supported for MacOS.
function startDocker {
  if ! dockerRunning; then
    echo "= INFO: Starting Docker ="
    open --hide --background -a Docker
  else
    echo "Docker is Running!"
  fi
}

# Restarts Dcoker if necessary, if docker is NOT running it will attempt to
# start it for you. Currently, this function is only supported for MacOS.
function restartDocker {
  if dockerRunning; then
    osascript -e 'quit app "Docker"'
    startDocker
  else
    echo "= INFO: Docker is not running ="
    startDocker
  fi
}

# Checks the provided String to see if it is empty or not
#
# param1 = value to check for emptiness
# return = true if empty, false otherwise
function isEmpty {
  if [ -z "$1" ]; then
      return
  else
      false
  fi
}

# Checks to see if a Docker container exists with the specified name
#
# param 1 = name of Docker container to check existance of
# return = true if container exists, false otherwise
function containerExists {
  # Grab first argument and check if the container exists
  if ! isEmpty "$(docker container ls -q -a -f name=$1)"; then
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
  LOG_FILE_PATH="$LOG_DIR/jenkins_`date +%s`.log"
  echo "= INFO: Logs for this Docker container can be found at $LOG_FILE_PATH ="
  docker-compose -f $dockerFile up --force-recreate --build >"$LOG_FILE_PATH" 2>&1 &
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
# param 1 = Docker container name to destroy
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
# param 1 = Docker container name to stop
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
#
# param1 = Directory to remove
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

  # Initialize counter and set it to zero
  counter=0

  # Sleep until Jenkins is running by inspecting file
  jenkinsUp=false
  while ! $jenkinsUp; do
    # Iterate counter by one
    counter=$((counter+1))

    # Check if mount issues are present in log file and attempt to recover
    if grep -q "device or resource busy" "$LOG_FILE_PATH"; then
      echo "= ERROR: Mounting issues found! Attempting to recover (Attempt #$counter) ="
      # Try to resolve 3 times then exit script
      if [ "$counter" -ge "3" ]; then
        echo "= ERROR: Cannot resolve mounting issues! Exiting script. ="
        exit 1
      fi
      # Destroy everything that we can concerning the Jenkins data/containers
      # and attempt to restart services
      removeDirectory $LOCAL_JENKINS_DATA
      removeDirectory $LOCAL_JENKINS_HOME
      destroyDockerContainer $JENKINS_CONTAINER_NAME
      restartDocker
      waitForDockerToRun
      runDocker $JENKINS_DOCKER_FILE
    fi
    # Check if log file contains the proper output to say that Jenkins is
    # up and running
    if grep -q "Jenkins is fully up and running" "$LOG_FILE_PATH"; then
      echo "= INFO: Jenkins is Running ="
      jenkinsUp=true
    else
      echo "= INFO: Jenkins is not yet running! Sleeping until its Running (Attempt #$counter) ="
      sleep 15
    fi
  done

  # Print password to console
  printInitialJenkinsPassword $JENKINS_CONTAINER_NAME
}

# Creates a Jenkins user
function createUser {
  echo "= INFO: Creating user $1 with password $2 ="
  echo 'jenkins.model.Jenkins.instance.securityRealm.createAccount('\"$1\"', '\"$2\"')' | java -jar $JENKINS_CLI_JAR -auth $3:$4 -s $JENKINS_URL groovy =
}

# Changes into the specified directory and runs the provided command.
# This is usefule if you don't want to hop into and out of a certain directory.
#
# Example Use:
# changeDirectoryAndRunCommand "/Users" "ls"
#
# param1 = Directory to temporarily change into
# param2 = Command to run in directory
function changeDirectoryAndRunCommand {
  ( cd $1 ; $2 )
}

function setupWebApp {
  # Copy the web-app to the local Jenkins home directory so it can be picked
  # up by the Jenkins pipeline
  cp -r $WEB_APP $LOCAL_JENKINS_HOME
  # Ensure Jenkins can execute the scripts by making them executable
  changeDirectoryAndRunCommand $JENKINS_APP/jenkins/scripts "chmod +x deliver.sh"
  changeDirectoryAndRunCommand $JENKINS_APP/jenkins/scripts "chmod +x kill.sh"
  changeDirectoryAndRunCommand $JENKINS_APP/jenkins/scripts "chmod +x test.sh"
  # Create local git repository and make a commit so it can be picked up
  # by Jenkins Git SCM
  changeDirectoryAndRunCommand $JENKINS_APP "git init"
  changeDirectoryAndRunCommand $JENKINS_APP "git add ."
  ( cd $JENKINS_APP ;  git commit -m "Initial Commit for Jenkins to be happy")
}

# Creates and runs example Jenkins job that builds and runs spring boot service
function createAndRunJenkinsJob {
  ADMIN_USER=admin
  ADMIN_PASS=$(cat $LOCAL_SECRET_PATH)
  JENKINS_USER="jenkins"
  JENKINS_USER_PASS="password"

  # Move React project to Jenkins home directory and establish local git repo
  setupWebApp

  # Create another user that has easier credentials to remember ;)
  createUser $JENKINS_USER $JENKINS_USER_PASS $ADMIN_USER $ADMIN_PASS

  # Create job to build and run spring boot project
  local jobName="doom-react"
  echo "= INFO: Exporting Jenkins Job $jobName located at $REACT_JOB_PATH"
  java -jar $JENKINS_CLI_JAR -s $JENKINS_URL -auth $ADMIN_USER:$ADMIN_PASS create-job $jobName < $REACT_JOB_PATH
  # Execute job
  echo "= INFO: Running Jenkins Job $jobName"
  java -jar $JENKINS_CLI_JAR -s $JENKINS_URL -auth $ADMIN_USER:$ADMIN_PASS build $jobName

  echo "= INFO: Jenkins Job progress can be viewed at $JENKINS_URL/job/$jobName ="
  echo "= INFO: Once complete, you may view the web app at http://localhost:3000 ="
}

# Uses curl to check if a URL is up (GET HTTP 200 Response) and return the
# appropriate "boolean" respones. This will also timeout at 3 seconds to not
# cause issues with hanging calls
#
# param1 = URL to check
# return "true" if URL is up, "false" otherwise
function urlIsUp {
  if curl -m 3 -s --head --request GET "$1" | grep "200 OK" > /dev/null; then
   echo "= INFO: $1 is UP ="
   return
  else
   echo "= INFO: $1 is DOWN ="
   false
  fi
}

# Main function to build/run/clean Jenkins related operations
function executeJenkins {
  # Check if the web-app is already up
  if urlIsUp $WEB_APP_URL; then
    echo "= ERROR: Web-App at $WEB_APP_URL appears to be running! Please terminate this and re-run the script. ="
    exit 1
  fi
  # Run Docker if it is not running already
  waitForDockerToRun
  # Create Log directory if it does NOT exist
  mkdir -p $LOG_DIR
  # Build Jenkins image
  buildJenkins

  echo "= INFO: Creating Jenkins Job for React Web-App ="
  createAndRunJenkinsJob
}

# Call functions to get local Dockerized Jenkins up and running
# then create and run the React web-app job
executeJenkins

# Wait for web app to be running to notify user via the terminal so they
# do not need to check the Jenkins job
while ! urlIsUp $WEB_APP_URL; do
  echo "= INFO: Waiting for $WEB_APP_URL to be up ="
  sleep 20
done

# Let them know we're good to go 
echo "= INFO: React Web-App is up at $http://localhost:3000 ="
