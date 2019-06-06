#!/bin/bash
#
# Script to help install dependencies used in the localize-it repository.
#

# Check to see if a command/dependency exists and return either 0 for true
# or 1 for false if it exists
function checkDependency {
  local cmdToRun=$1
  local dependencyName=$2
  echo "= INFO: Checking $dependencyName Installation ="
  # If the command does NOT exist then return false, otherwise, return true
  if ! [ -x "$(command -v $cmdToRun)" ]; then
    echo "= INFO: $dependencyName is not installed! Will attempt installation now ="
    return 1
  else
    return 0
  fi
}

# Installs Homebrew if necessary
function installHomebrew {
  if ! checkDependency "brew" "Homebrew"; then
    /usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
  fi
}

# Installs Docker if necessary
function installDocker {
  if ! checkDependency "docker" "Docker"; then
    brew install docker docker-compose docker-machine xhyve docker-machine-driver-xhyve
  fi
}

# Installs Scala if necessary
# CURRENTLY NOT INCLUDED IN MAIN INSTALL!
# RUN THIS FUNCTION ON YOUR OWN TO MAKE SURE YOU HAVE THE RESOURCE!
function installScala {
  if ! checkDependency "scala -version" "Scala"; then
    brew install scala
  fi
}

# Installs Apache Spark if necessary
# CURRENTLY NOT INCLUDED IN MAIN INSTALL!
# RUN THIS FUNCTION ON YOUR OWN TO MAKE SURE YOU HAVE THE RESOURCE!
function installSpark {
  if ! checkDependency "spark-shell -h" "Spark"; then
    brew install apache-spark
  fi
}

# Installs AWS Local CLI if necessary
function installAWSLocalCLI {
  if ! checkDependency "awslocal s3" "AWS Local CLI"; then
    sudo pip install awscli-local --upgrade --ignore-installed six
  fi
}

# Installs AWS CLI if necessary
function installAWSCLI {
  if ! checkDependency "aws" "AWS CLI"; then
    brew install awscli
  fi
}

# Installs Jupyter Notebook if necessary
# CURRENTLY NOT INCLUDED IN MAIN INSTALL!
# RUN THIS FUNCTION ON YOUR OWN TO MAKE SURE YOU HAVE THE RESOURCE!
function installJupyter {
  if ! checkDependency "jupyter" "Jupyter Notebook"; then
    brew install jupyter
  fi
}

# Checks the operating system to determine if it is supported or not
function checkOperatingSystem {
  if [[ "$OSTYPE" == "darwin"* ]]; then
    # Mac OSX
    echo "= INFO: OS $OSTYPE supported! ="
    return
  else
    echo "ERROR: Installation process for $OSTYPE is not implemented at this time!"
    false
  fi
}

# Checks that required dependencies exist and installs them if needed
function checkAndInstallDependencies {
  # Homebrew
  installHomebrew
  # Container
  installDocker
  # AWS
  installAWSLocalCLI
  installAWSCLI
}

echo "= INFO: Checking Operating System ="
if ! checkOperatingSystem; then
  echo "= ERROR: Installation process for $OSTYPE is not implemented at this time! ="
  exit 1
else
  checkAndInstallDependencies
fi

echo "= INFO: Process Complete! ="
