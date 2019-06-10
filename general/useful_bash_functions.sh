#!/bin/bash

# When this option is on, when any command fails (for any of the reasons listed
# in Consequences of Shell Errors or by returning an exit status greater
# than zero), the shell immediately shall exit, as if by executing the exit
# special built-in utility with no argument.
set -o errexit
# Shorthand equivelant of set -o errexit
set -e

# Detects uninitialised variables in your script (and exits with an error).
# However, this will reject environment variables which isn't ideal in
# certain situations.
set -o nounset
# Shorthand equivelant of set -o nounset
set -u

# The shell shall write to standard error a trace for each command after it
# expands the command and before it executes it.
set -o xtrace
# Shorthand equivelant of set -o xtrace
set -x

# Checks the provided String to see if it is empty or not
#
#
# Example Use:
# if isEmpty $STR_TO_CHECK; then echo "Empty!"; else echo "Not Empty!"; fi
#
# param1 = Value to check for emptiness
# return = "true" if empty, "false" otherwise
function isEmpty {
  if [ -z "$1" ]; then
      return
  else
      false
  fi
}

# Helper function to check if a process is running on a specific port.
# If something is running on the port it will return true, otherwise, false.
#
# Example Use:
# if processRunningOnPort $PORT; then echo "Running"; else echo "Not Running"; fi
#
# param1 = Port to check to see if a process is running on it
# return "true" if a process is running on the port, "false" otherwise
function processRunningOnPort {
  if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null ; then
    return
  else
    false
  fi
}

# Kills the process running on the port provided to the function
#
# Example Use:
# killProcessOnPort 8080
#
# param1 = Port to kill process that is running on it
function killProcessOnPort {
  local portNumber=$1
  lsof -i tcp:${portNumber} | awk 'NR!=1 {print $2}' | xargs kill
}

# Checks the current Operating System and prints it out to the terminal.
#
# Example Use:
# getOperatingSystem
function getOperatingSystem {
  case "$OSTYPE" in
    solaris*) echo "SOLARIS" ;;
    darwin*)  echo "OSX" ;;
    linux*)   echo "LINUX" ;;
    bsd*)     echo "BSD" ;;
    msys*)    echo "WINDOWS" ;;
    *)        echo "unknown: $OSTYPE" ;;
  esac
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

# Checks to see if a file exists using the specified path.
# NOTE: This will only check files and NOT directories.
#
# Example Use:
# if fileExists "$FILE"; then echo "Exists!"; else echo "Does NOT Exist"; fi
#
# param1 = Path to file to check for existence
# return "true" if the file exists, "false" otherwise
function fileExists {
  if [ -f "$1" ]; then
    return
  else
    false
  fi
}

# Checks to see if a directory exists using the specified path.
# NOTE: This will only check directories and NOT files.
#
# Example Use:
# if directoryExists "/Users"; then echo "true"; else echo "false"; fi
#
# param1 = Path to directory to check for existence
# return "true" if the file exists, "false" otherwise
function directoryExists {
  if [ -d "$1" ]; then
    return
  else
    false
  fi
}

# Checks to see if a file or directory exists using the specified path.
#
# Example Use:
# if fileOrDirectoryExists "/Users"; then echo "true"; else echo "false"; fi
#
# param1 = Path to file or directory to check for existence
# return "true" if the file exists, "false" otherwise
function fileOrDirectoryExists {
  if [ -e "$1" ]; then
    return
  else
    false
  fi
}

# Prompts user for input and then checks the answer to see if it starts with
# a 'y' or 'Y' and echos the "positive" response. Otherwise it will echo the
# "negative" response.
#
# Example Use:
# promptUser "Do you like bash?"
#
# param1 = Prompt to send to user
function promptUser {
  echo -n "$1"
  read answer
  if [ "$answer" != "${answer#[Yy]}" ] ;then
    echo "Positive Response From User $answer"
  else
    echo "Negative Response From User $answer"
  fi
}
