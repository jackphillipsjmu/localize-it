#!/bin/bash
#
# Script to help spin up and execute AWS Operations Locally
#
# General Information:
# - Local S3 UI = http://localhost:4572
# - LocalStack Web Console = http://localhost:8080/#/infra
#
# Requirements:
# - Docker and Docker Compose
# - LocalStack (will run in Docker environment)
# - AWS CLI and AWS Local CLI (ignores security roles)
# - Java JDK 1.8 (lambda code)
#
# Installing AWS CLI Locally:
# - Have Python installed on your machine with pip
# - For Mac's execute the following from the command line
#    $ sudo pip install awscli-local --upgrade --ignore-installed six
# - Or use a simpler command in other environemnts
#    $ sudo pip install awscli-local
#
# References:
# Docker = https://www.docker.com/
# LocalStack = https://github.com/localstack/localstack
# AWS CLI Local = https://github.com/localstack/awscli-local
#

# Report the usage of uninitialized variables
set -u

# ENVIRONMENT VARIABLES
# Necessary for local CLI operations but doesn't actually do much
export AWS_ACCESS_KEY_ID=foo
export AWS_SECRET_ACCESS_KEY=foo
export AWS_DEFAULT_REGION=us-east-1
# S3
S3_ENDPOINT_URL="http://localhost:4572"
SOURCE_S3_BUCKET="source-bucket"
SOURCE_BUCKET_CONFIG_PATH=file://../resources/config/s3_notification.json
S3_SOURCE_BUCKET_PATH="s3://$SOURCE_S3_BUCKET/test_`date +%s`.csv"
SINK_S3_BUCKET="sink-bucket"
S3_ROLE=arn:aws:iam::123456:role/irrelevant
# Lambda Properties
LAMBDA_FUNCTION_NAME="s3-lambda"
LAMBDA_EVENT_HANDLER=com.dna.challenge.S3EventRequestHandler
LAMBDA_PROJECT_PATH=../com-dna-challenge
LAMBDA_JAR_PATH=../resources/tmp/com-dna-challenge.jar
# Where the data we will push to S3 lives
INIT_DATA_PATH="../resources/sample_data/census_sample_data.csv"
LOCAL_STACK_UI_PORT=8080
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

# Runs Dockerized LocalStack and pipes output logs to a file
function startLocalStack {
  # Note this way of starting local stack is Mac specific
  # if you would like to run it on other machines try the simpler command
  # $ docker-compose up
  LOCAL_STACK_COMPOSE_PATH="../resources/docker/docker-localstack-compose.yml"
  TMPDIR=/private$TMPDIR docker-compose -f $LOCAL_STACK_COMPOSE_PATH up > "$LOG_DIR/localstack_`date +%s`.log" 2>&1 &
}

# Build JAR that will be pushed to AWS Lambda
function buildAndProcessLambdaProject {
  echo "= INFO: Building Lambda Project at $LAMBDA_PROJECT_PATH ="
  ( cd $LAMBDA_PROJECT_PATH ; ./gradlew clean build )

  local builtJar=$LAMBDA_PROJECT_PATH/build/libs/com-dna-challenge-0.1.jar
  # Check that we have our Lambda JAR and exit if it does not exist
  if [ ! -f "$builtJar" ]; then
    echo "= ERROR: Lambda JAR does not exist! Exiting ="
    exit 1
  fi

  echo "= INFO: Copying Built Lambda Project JAR to $LAMBDA_JAR_PATH ="
  # mv $LAMBDA_PROJECT_PATH/build/libs/com-dna-challenge-0.1.jar $LAMBDA_JAR_PATH
  cp $LAMBDA_PROJECT_PATH/build/libs/com-dna-challenge-0.1.jar $LAMBDA_JAR_PATH
}

# Push Configuration to S3 Bucket for notification events
#
# param1 = S3 Bucket Name
# param2 = S3 Configuration file
function setBucketConfig {
  echo "= INFO: Setting Bucket Configuration for $1 from file $2 at endpoint $S3_ENDPOINT_URL"
  aws --endpoint-url=$S3_ENDPOINT_URL s3api put-bucket-notification-configuration --bucket $1 --notification-configuration $2
}

# Helper function to retrieve S3 Bucket configuration
#
# param1 = S3 Bucket Name
function getBucketConfig {
  echo "= INFO: Retrieving Bucket Configuration for $1 ="
  aws --endpoint-url=$S3_ENDPOINT_URL s3api get-bucket-notification-configuration --bucket $1
}

# Helper function that lists S3 Buckets
# used primarily as an example for using awslocal to perform the operation
function listBuckets {
  echo "= INFO: Fetching S3 Bucket List ="
  # Could not connect to the endpoint URL: "http://localhost:4572/"
  aws --endpoint-url=$S3_ENDPOINT_URL s3 ls
}

# Helper function that lists AWS Lambda Functions
# used primarily as an example for using awslocal to perform the operation
function listLambdas {
  echo "= INFO: List of Lambda Functions ="
  awslocal lambda list-functions
}

# Copies a local file to the specified S3 Bucket or vice versa
#
# param1 = path to source
# param2 = path to destination
function copyFileToBucket {
  echo "= INFO: Copying $1 to $2 = "
  aws --endpoint-url=$S3_ENDPOINT_URL s3 cp $1 $2
}

# Copies a file in a S3 bucket to the local filesystem
# used primarily as an example for using awslocal to perform the operation
function copyBucketFileToLocal {
  echo "= INFO: Copying $1 to $2 = "
  awslocal --endpoint-url=$S3_ENDPOINT_URL s3 cp $1 $2
}

# Explicitly invokes Lambda function which may help testing efforts
# used primarily as an example for using awslocal to perform the operation
function invokeLambdaFunction {
  echo "INFO: Invoking Lambda Function $1"
  local timestamp=`date +%s`
  awslocal lambda invoke --function-name $1 --payload '{"firstName":"John","lastName" : "Doe"}' outputfile_$timestamp.txt
}

# Creates a AWS Lambda Function
# TODO: Make LAMBDA_JAR_PATH a parameter
#
# param1 = Lambda function Name
# param2 = Lambda Handler
# param3 = Lambda JAR Path
# param4 = AWS ARN Role
function lambdaCreateFunction {
  echo "= INFO: Creating Lambda Function with name $1 with code from $2 using JAR $3 and role $4="

  awslocal lambda create-function \
    --function-name $1 \
    --zip-file fileb://$3 \
    --handler $2 \
    --runtime java8 \
    --role $4 \
    --timeout 10
}

# Removes AWS Lambda Function
#
# param1 = Name of function to remove
function deleteFunction {
  echo "= INFO: Deleting Function $1 (ignore error messages if no function is present) ="
  awslocal lambda delete-function --function-name $1
}

# Create a S3 bucket
#
# param1 = Bucket path and name to create
function createBucket {
  echo "= INFO: Creating Bucket $1 ="
  aws --endpoint-url=$S3_ENDPOINT_URL s3 mb s3://$1
}

# Removes a S3 bucket.
# NOTE: This will force remove everything so be aware of data loss!
#
# param1 = Bucket path and name to remove
function removeBucket {
  echo "= INFO: Removing Bucket $1 (ignore error messages if no bucket is present) ="
  aws --endpoint-url=$S3_ENDPOINT_URL s3 rb s3://$1 --force
}

# Attach an ACL to the bucket so it is readable
#
# param1 = name of the createBucket
# param2 = ACL value
function attachACLToBucket {
  echo "= INFO: Attaching ACL To S3 Bucket ="
  aws --endpoint-url=$S3_ENDPOINT_URL s3api put-bucket-acl --bucket $1 --acl public-read
}

# Removes resources for a clean run later on
function clearResources {
  echo "= INFO: Clearing Resources ="
  # Remove buckets that may or may not exist
  removeBucket $SOURCE_S3_BUCKET
  removeBucket $SINK_S3_BUCKET
  # Remove AWS Lambda Function
  deleteFunction $LAMBDA_FUNCTION_NAME
}

# Creates necessary AWS resources
function createAWSResources {
  echo "= INFO: Creating AWS Resources ="
  lambdaCreateFunction $LAMBDA_FUNCTION_NAME $LAMBDA_EVENT_HANDLER $LAMBDA_JAR_PATH $S3_ROLE
  # Create S3 Buckets
  createBucket $SOURCE_S3_BUCKET
  createBucket $SINK_S3_BUCKET
  # Setup S3 Bucket Configurations
  setBucketConfig $SOURCE_S3_BUCKET $SOURCE_BUCKET_CONFIG_PATH
  # Push data to S3 source bucket
  copyFileToBucket $INIT_DATA_PATH $S3_SOURCE_BUCKET_PATH
}

# Builds local execution environment
function buildEnvironment {
  # Create Log directory if it does NOT exist 
  mkdir -p $LOG_DIR
  checkEnvironment
  echo "= INFO: Building Environment ="
  clearResources
  buildAndProcessLambdaProject
  createAWSResources
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
  # Make sure that Docker is running on the machine
  checkDockerIsRunning

  # Check that LocalStack is running
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
}


# Pulls in file from S3 Bucket to Local Filesystem
#
# Calling function Example:
# pullS3FileToLocal "../resources/tmp/" "sink-bucket/MODIFIED-test_1557840642.csv"
#
# param1 = local file path to put data into ex. ./tmp
# param2 = S3 file path to retrieve data from ex. bucket/foo.csv
function pullS3FileToLocal {
  local outputFile=$1
  local sinkFile=$2
  copyFileToBucket "s3://$sinkFile" $outputFile
}

# Build and Run S3 Lambda Example
buildEnvironment
