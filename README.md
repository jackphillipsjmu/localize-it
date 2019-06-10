# localize-it
Repository intended to show how to setup and perform local development for both simple and complex software solutions. This helps reduce costs running things within an external environment while attempting to mitigate the classic _"works on my machine"_ paradigm. This repository will be updated to include more functionality in the future but of coarse feel free to add new additions and feature requests!

## What's Inside?
Lots of stuff! So far there are examples showing the following tech stack components/languages but more are soon to come!

| **Component** | **Description** |
| :-----------: |-----------------|
| AWS S3        | Simple Storage Service (S3) stores your data as objects that consist of data and optional metadata that describes the file within buckets. |
| AWS Lambda    | Serverless code executor that allows you to run code for virtually any type of application or backend service with zero administration over server instances. These functions can be event driven and triggered by AWS services or direct calls. |
| Elasticsearch | Search engine based on the Lucene library. It provides a distributed, multitenant-capable full-text search engine with an HTTP web interface and schema-free JSON documents. Originally designed to be an open-source Splunk it has expanded to being an absurdly fast indexing NoSQL data store. |
| Kafka         | Used for building real-time data pipelines and streaming apps. It is horizontally scalable, fault-tolerant, wicked fast, and runs in production in thousands of companies. Think of all the issues you had with messaging queues and breathe a sigh of relief that Kafka can be used in its place! |
| Jenkins       | Open source automation server which enables developers around the world to reliably build, test, and deploy their software. |
| Spring Boot   | Makes it easy to create stand-alone, production-grade Spring based Applications that you can "just run". |
| Docker        | Container platform to securely build, share and run any application, anywhere. |
| Gradle        | Build automation system that is the "next iteration" of Maven |
| React         | Popular JavaScript library for building user interfaces |

| **Language** | **Description** |
|:---------:|-----------------|
| Kotlin    | The default Android programming language built on the JVM. Kotlin solves a bunch of issues seen in Java providing a enjoyable functional programming experience that is completely backwards compatible with Java. If you're from a Python/R/Scala background you'll feel right at home. |
| Java       | You know Java, in this repository we currently use Java `1.8`. |
| JavaScript | Used primarily for Node/Web related functionality. |
| Bash       | Unix shell and command language primarily used for setting up things in each project. |
| Groovy     | JVM based language primarily used in Gradle to build projects (Kotlin is quickly gaining traction over Groovy so hop on the Kotlin train!). |
| Python     | Interpreted, high-level, general-purpose programming language. Currently used primarily in conjunction with LocalStack's Local AWS CLI.  |

## Examples Demonstrated in this Repository

### Jenkins React Web-Application ([jenkins-doom-react](jenkins-doom-react))
Builds a Jenkins instance that has the ability to pull in a local [React](https://reactjs.org/) GitHub project, build and run it using Blue Ocean's Jenkins Docker image. For fun, the React application utilizes [DOSBox](https://www.dosbox.com/) to let you play Doom in your browser!

### Jenkins Spring Boot ([simple-jenkins](simple-jenkins))
Builds a Jenkins instance that has the ability to pull in a GitHub project, build and run it using Blue Ocean's Jenkins Docker image.

### Spring Boot Microservice Docker Deploy ([simple-springboot-docker](simple-springboot-docker))
Builds and Deploys a Dockerized Spring Boot Microservice that includes in-memory audit logging (H2) of REST endpoints.

### Weather Alert Service ([weather-alerts](weather-alerts))
Intended to display an end-to-end solution for processing Weather Alert data using a local testing environment that includes a multitude of cloud components while showing other useful examples interacting with different APIs, SDKs and frameworks.

### S3 Lambda ([simple-s3-lambda](simple-s3-lambda))
Creates local S3 buckets and Lambda function to operate on CSV Census data that is pushed to a S3 sink bucket which is in turn pulled in via a AWS Lambda function. This is similar to the [D&A Serverless Lambda Challenge](https://techchallenge.captechlab.com/data-engineering/Serverless.Assignment-1.Lambda-Introduction).

### Spark _(Coming Soon!)_
Code is written just need to package it nicely :)

### Future Examples to Implement

* [ ] **Sonar**: Performs static code analysis to ensure code quality.
* [ ] **GraphQL**: Query language for APIs and a runtime for fulfilling those queries with your existing data. GraphQL provides a complete and understandable description of the data in your API, gives clients the power to ask for exactly what they need and nothing more, makes it easier to evolve APIs over time, and enables powerful developer tools.
* [ ] **API Gateway**: Both AWS ([AWS API Gateway](https://aws.amazon.com/api-gateway/)) and Netflix ([Zuul](https://github.com/Netflix/zuul), [Eureka](https://github.com/Netflix/eureka)) flavors.
* [ ] **Jenkins Instances with Master/Slave Communication**: To supply a full fledged local Jenkins environment.
* [ ] **Kubernetes**: Open-source system for automating deployment, scaling, and management of containerized applications.
* [ ] **Web UI/Visualizations**: Libraries such as D3js, Echarts and Google Charts will most likely be explored.
* [ ] **Databases**: PostgreSQL, MySQL and other NoSQL databases.
* [ ] **All the things!**: This should be a living repository and updated to help others with their own local development.

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

- Currently, the script will attempt to install the following:
  - Homebrew
  - Docker
  - AWS CLI
  - AWS Local CLI

- Functions to help install the following dependencies also exist in the script:
  - Scala
  - Spark
  - Jupyter
