# localize-it
Repository intended to show how to setup and perform local development for both simple and complex software solutions. This helps reduce costs running things within an external environment while attempting to mitigate the classic _"works on my machine"_ paradigm. This repository will be updated to include more functionality in the future but of coarse feel free to add new additions and feature requests!

## Examples Demonstrated in this Repository
### Spring Boot Microservice Docker Deploy (simple-springboot-docker)
Builds and Deploys a Dockerized Spring Boot Microservice that includes in-memory audit logging of REST endpoints.
- **Current Execution Flow**
  - By executing the script located at `$PROJECT_DIR/simple-springboot-docker/scripts/build_simple_docker.sh` it will perform the actions listed below:
    - Check that Docker is running and if not will prompt the user if they would like to start Docker (_Currently Mac OS only for start functionality_).
    - Once verification that Docker is running existing Docker containers matching the name `simple_springboot_docker` will be removed to ensure you are always deploying the latest and greatest.
    - Spring Boot Gradle project is built and the underlying `docker-compose.yml` file is run.
    - Information about the service including Swagger UI and documentation will be printed to the terminal window.
- **References**
  - [Swagger UI](http://localhost:8089/swagger-ui.html) can be used to run REST endpoints from your browser.
  - [Auto Generated Documentation](http://localhost:8089/index.html) is used as reference to what the service offers.
  - Logs can be found locally at `$PROJECT_DIR/simple-springboot-docker/resources/tmp`

### Weather Alert Service (weather-alerts)
This service is intended to display an end-to-end solution for processing Weather Alert data using a local testing environment that includes a multitude of cloud components while showing other useful examples interacting with different APIs, SDKs and frameworks.
- **Current Application Flow**
  - Pulls in Weather Alert Atom Feed Data from weather.gov either through a REST API call or on a preconfigured schedule if enabled.
  - Transforms Weather Alert Data and sends it to a Kafka topic if enabled.
  - Kafka Consumer/Listener will retrieve topic data and push it to Elasticsearch if enabled. The way in which the data is indexed will automatically either insert a new document or update an existing one if it exists.
  - Transforms the Weather Alert data again into CSV format and pushes it to an S3 source bucket.
  - The source bucket where the Alert CSV data is pushed to has a AWS Lambda function tied to it to copy the contents into a predefined sink bucket.
    - This Lambda function is worth checking out as well and is located in the `$PROJECT_DIR/localize-it/com-alert-lambda` directory. It mixes both Java and Kotlin code to show how the two languages can work hand in hand even in a cloud environment!

- **References**
  - [Weather Alert Feed](https://alerts.weather.gov/cap/us.php?x=1)
  - [LocalStack Web Console](http://localhost:8080)
  - [LocalStack AWS Elasticsearch Endpoint](http://localhost:4571)
  - [LocalStack AWS S3 Endpoint](http://localhost:4572)
  - [LocalStack AWS Lambda Endpoint](http://localhost:4574)
  - [Automated Weather Alert Service Documentation](http://localhost:8081/index.html) _(service must be running!)_.
  - [Swagger UI](http://localhost:8081/swagger-ui.html) can be used to run REST endpoints from your browser _(service must be running!)_.
    - **Note**: When running the end-to-end process the code will attempt to resolve all configurations and environmental dependencies such as Lambda function and S3 bucket creation. However, this does require a running LocalStack instance which is defined below.
  - Logs can be found locally at `$PROJECT_DIR/weather-alerts/resources/tmp`

### Jenkins (simple-jenkins)
Builds a Jenkins instance that has the ability to pull in a GitHub project, build and run it using Blue Ocean's Jenkins Docker image.
- **Current Execution Flow**
  - By executing the script located at `$PROJECT_DIR/simple-jenkins/scripts/build_jenkins.sh` it will perform the actions listed below:
    - Prompt user for a clean build of Jenkins instance. If this is done all current Jenkins resources will be destroyed so make sure this is what you want! If it's the first time running the script then this shouldn't matter either way.
    - Builds Jenkins Docker image and waits for it to be spun up before continuing.
    - Once Jenkins Docker image is up the admin password will be printed to the terminal for use if you do not want to setup jobs via the bash script.
    - Prompt user to see if they would like to create and run the default Jenkins job, i.e. build and run a microservice located in a publicly facing GitHub repository.
      - If the user wants to create the Jenkins job then a user is created with the credentials `jenkins:password` and the microservice job is pushed to Jenkins and run.
      - Once the job is complete users can navigate to the services [Swagger UI](http://localhost:8085/swagger-ui.html) to run REST endpoints.
      - To check on the status of the job go to the [Jenkins Console that is building running the job](http://localhost:8082/job/gradle-alive-local). To terminate the deployed service simply click the red `x` in the Job's console view.
    - If the user does not want to run the default Jenkins job then simply go to the [Jenkins Web Console](http://localhost:8082) and provide the admin password that the script produces for you in the terminal and follow the setup instructions.
- **References**
  - [Jenkins Web Console](http://localhost:8082)
  - [Swagger UI](http://localhost:8085/swagger-ui.html) for running REST requests if Jenkins job has been run.
  - Logs can be found locally at `$PROJECT_DIR/simple-jenkins/resources/tmp`

### S3 Lambda (simple-s3-lambda)
Creates local S3 buckets and Lambda function to operate on CSV Census data that is pushed to a S3 sink bucket which is in turn pulled in via a AWS Lambda function. The functions code will transform the data as follows:
- Remove any line items that have a TotalPop of zero.
- For each line item, sum the columns Hispanic, White, Black, Native, Asian and Pacific and subtract the sum from 100. Put this number in a new column named OtherEthnicity.
- For each line item, the following columns will be dropped: `TotalPop`, `Citizen`, `Income`, `IncomeErr`, `IncomePerCap`, `IncomePerCapErr`, `Poverty`, `ChildPoverty`, `Drive`, `Carpool`, `Transit`, `Walk`, `OtherTransp`, `WorkAtHome`, `MeanCommute`.

After the transformation is complete the result data is pushed to a predefined S3 sink bucket.

- **Current Execution Flow**
  - By executing the script located at `$PROJECT_DIR/simple-s3-lambda/scripts/run_s3_lambda.sh` it will perform the actions listed below:
    - Check environment to ensure that Docker and LocalStack are running prompting the user to start each if necessary (_Currently Mac OS only for Docker start functionality_).
    - Removes any existing S3 source/sink buckets and Lambda functions with the name `s3-lambda`.
    - Builds AWS S3 Lambda Java Project to be used in LocalStack.
    - Creates default S3 source and sink buckets along with Lambda function that will be configured using a JSON config file located at `$PROJECT_DIR/simple-s3-lambda/resources/config/s3_notification.json`
    - Copies example CSV file `$BASE_FIR/simple-s3-lambda/resources/sample_data/census_sample_data.csv` to [S3 source bucket](http://localhost:4572/source-bucket) which in turn invokes the Lambda function to transform and push modified file to the [S3 sink bucket](http://localhost:4572/sink-bucket).
- **References**
  - [LocalStack Web Console](http://localhost:8080)
  - [LocalStack AWS S3 Endpoint](http://localhost:4572)
  - [LocalStack AWS Lambda Endpoint](http://localhost:4574)
  - Logs can be found locally at `$PROJECT_DIR/simple-s3-lambda/resources/tmp`


### Spark
**Coming Soon!** Code is written just need to package it nicely :)

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
