# localize-it
Repository intended to show how to setup and perform local development for both simple and complex software solutions. This helps reduce costs running things within an external environment while attempting to mitigate the classic _"works on my machine"_ paradigm. This repository will be updated to include more functionality in the future but of coarse feel free to add new additions and feature requests!

## Examples Demonstrated in this Repository
### Spring Boot Microservice Docker Deploy ([simple-springboot-docker](simple-springboot-docker))
Builds and Deploys a Dockerized Spring Boot Microservice that includes in-memory audit logging of REST endpoints.

### Weather Alert Service ([weather-alerts](weather-alerts))
This service is intended to display an end-to-end solution for processing Weather Alert data using a local testing environment that includes a multitude of cloud components while showing other useful examples interacting with different APIs, SDKs and frameworks.

### Jenkins ([simple-jenkins](simple-jenkins))
Builds a Jenkins instance that has the ability to pull in a GitHub project, build and run it using Blue Ocean's Jenkins Docker image.

### S3 Lambda ([simple-s3-lambda](simple-s3-lambda))
Creates local S3 buckets and Lambda function to operate on CSV Census data that is pushed to a S3 sink bucket which is in turn pulled in via a AWS Lambda function. This is similar to the [D&A Serverless Lambda Challenge](https://techchallenge.captechlab.com/data-engineering/Serverless.Assignment-1.Lambda-Introduction)

### Spark _(Coming Soon!)_
Code is written just need to package it nicely :)

### Future Examples to Implement
- **Sonar**: Performs static code analysis to ensure code quality.
- **GraphQL**: Query language for APIs and a runtime for fulfilling those queries with your existing data. GraphQL provides a complete and understandable description of the data in your API, gives clients the power to ask for exactly what they need and nothing more, makes it easier to evolve APIs over time, and enables powerful developer tools.
- **API Gateway**: Both AWS and Netflix flavors.
- **Jenkins Instances with Master/Slave Communication**: To supply a full fledged local Jenkins environment.

## Installing Necessary Dependencies
_Dependencies can also be installed through other mechanisms but we primarily use `brew` in this repository, just go with what's easiest for you! If you do not have Homebrew on your machine try executing the following command to install it `$ /usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"` or differ to the installation script explained under "Option 2 Install Dependencies Using Script"_

### Option 1 Install Dependencies on Your Own
**Docker/Docker Compose**
- With Homebrew installed, from the command line run `$ brew install docker docker-compose docker-machine xhyve docker-machine-driver-xhyve`
- Execute `$ docker` from the command line to see if things are installed on your machine.
- To install Docker/Docker Compose manually refer to [Dockers install documentation](https://docs.docker.com/compose/install/).

**AWS CLI (Local and Typical Clients)**
- Ensure you have Python installed on your machine with `pip`
  - Check out [Pythons installation instructions](https://wiki.python.org/moin/BeginnersGuide/Download) if you're installing Python for the first time.
- For Mac's execute the following from the command line to retrieve and install the AWS Local CLI which is beneficial for testing = `$ sudo pip install awscli-local --upgrade --ignore-installed six`
  - Or use a simpler command in other non-Mac environments = `$ sudo pip install awscli-local`
- Install the typical AWS CLI by executing `$ brew install awscli` or with use the `pip3` flavor install by executing `$ pip3 install awscli --upgrade --user`
- For further AWS CLI installation instructions/references check out [AWS CLI Installation Documentation](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html).

#### Option 2 Install Dependencies Using Script (_Experimental_)
If you are running this project on a Mac then you can execute the _experimental_ install script located here, `$PROJECT_DIR/general/scripts/install.sh`. This script is a work in progress so all dependencies may not be installed properly. If failures occur differ to the _Option 1_ self install to ensure everything is working as expected.

Currently, the script will attempt to install the following:
- Homebrew
- Docker
- AWS CLI
- AWS Local CLI

Functions to help install the following dependencies also exist in the script:
- Scala
- Spark
- Jupyter
