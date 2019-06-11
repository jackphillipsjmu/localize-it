#!/usr/bin/env bash
#
# Script to do the following functionality:
#   - Check to see if Docker and LocalStack are running.
#   - Build Java Gradle Spark Project.
#   - Create S3 Source Bucket to hold initial Census CSV data.
#   - Create S3 Sink Bucket to hold the result Census CSV data.
#   - Run Spark process to pull Source S3 data, transform and write to Sink Bucket.
#

# Local resources
readonly SPARK_PROJECT_PATH=../spark-to-s3
readonly SPARK_PROJECT_JAR=$SPARK_PROJECT_PATH/build/libs/spark-to-s3-all.jar
readonly LOG_DIR=../resources/tmp
readonly INIT_DATA_PATH="../spark-to-s3/src/main/resources/census_data.csv"

# S3 Variables
readonly S3_ENDPOINT_URL="http://localhost:4572"
readonly SOURCE_BUCKET="spark-source-bucket"
readonly SOURCE_DATA_PATH="$SOURCE_BUCKET/census_data.csv"
readonly SINK_BUCKET="spark-sink-bucket"

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

# Runs Dockerized LocalStack and pipes output logs to a file
function startLocalStack {
  # Note this way of starting local stack is Mac specific
  # if you would like to run it on other machines try the simpler command
  # $ docker-compose up
  LOCAL_STACK_COMPOSE_PATH="../resources/docker/docker-localstack-compose.yml"
  TMPDIR=/private$TMPDIR docker-compose -f $LOCAL_STACK_COMPOSE_PATH up > "$LOG_DIR/spark-to-s3_`date +%s`.log" 2>&1 &
}

# Create a S3 bucket
#
# param1 = Bucket path and name to create
function createBucket {
  echo "= INFO: Creating Bucket $1 ="
  aws --endpoint-url=$S3_ENDPOINT_URL s3 mb s3://$1
}

# Build Spark project using Gradle
function buildSparkProject {
  echo "= INFO: Building Spark Project at $SPARK_PROJECT_PATH ="
  # Make sure we can run gradlew by making it executable
  chmod +x $SPARK_PROJECT_PATH/gradlew
  # Rub gradle clean build in project directory
  ( cd $SPARK_PROJECT_PATH ; ./gradlew clean build )
}

# Ensures that everything is up and running as it should be
function checkEnvironment {
  # Create Log directory if it does NOT exist
  mkdir -p $LOG_DIR
  # Check docker is running
  if ! dockerRunning; then
    echo "= ERROR: Docker is NOT running! Start Docker then re-execute script ="
    exit 1
  else
    # Check LocalStack, if its NOT up then start it
    if ! urlIsUp $S3_ENDPOINT_URL; then
      echo "= INFO: LocalStack is not running! Starting now ="
      startLocalStack
      # Wait for web app to be running to notify user via the terminal so they
      # do not need to check themselves
      while ! urlIsUp $S3_ENDPOINT_URL; do
        echo "= INFO: Waiting for $S3_ENDPOINT_URL to be up ="
        sleep 10
      done
    fi
  fi
}

# Copies a local file to the specified S3 Bucket or vice versa
#
# param1 = path to source
# param2 = path to destination
function copyFileToBucket {
  echo "= INFO: Copying $1 to $2 = "
  aws --endpoint-url=$S3_ENDPOINT_URL s3 cp $1 $2
}

# Removes a S3 bucket.
# NOTE: This will force remove everything so be aware of data loss!
#
# param1 = Bucket path and name to remove
function removeBucket {
  echo "= INFO: Removing Bucket $1 (ignore error messages if no bucket is present) ="
  aws --endpoint-url=$S3_ENDPOINT_URL s3 rb s3://$1 --force
}

# Removes and creates necessary AWS resources in S3
function refreshAwsResources {
  # Remove and create source bucket
  removeBucket $SOURCE_BUCKET
  createBucket $SOURCE_BUCKET
  # Remove and create sink bucket
  removeBucket $SINK_BUCKET
  createBucket $SINK_BUCKET
  # Send initial census data to source bucket
  copyFileToBucket $INIT_DATA_PATH "s3://$SOURCE_DATA_PATH"
}

# Run the JAR that is packaged up with our Spark code
function runJar {
  # java -jar -DSINK_BUCKET=$SINK_BUCKET -DCENSUS_DATA=$"s3a://$SOURCE_DATA_PATH" $SPARK_PROJECT_JAR
  java -jar -DSINK_BUCKET=$SINK_BUCKET $SPARK_PROJECT_JAR

}

# Check, build and run Spark process
checkEnvironment
buildSparkProject
refreshAwsResources
runJar
