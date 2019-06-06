# Weather Alert Service
This service is intended to display an end-to-end solution for processing Weather Alert data using a local testing environment that includes a multitude of cloud components while showing other useful examples interacting with different APIs, SDKs and frameworks.

### Current Application Flow
- Pulls in Weather Alert Atom Feed Data from weather.gov either through a REST API call or on a preconfigured schedule if enabled.
- Transforms Weather Alert Data and sends it to a Kafka topic if enabled.
- Kafka Consumer/Listener will retrieve topic data and push it to Elasticsearch if enabled. The way in which the data is indexed will automatically either insert a new document or update an existing one if it exists.
- Transforms the Weather Alert data again into CSV format and pushes it to an S3 source bucket. 
- The source bucket where the Alert CSV data is pushed to has a AWS Lambda function tied to it to copy the contents into a predefined sink bucket.
  - This Lambda function is worth checking out as well and is located in the `$BASE_DIR/localize-it/com-alert-lambda` directory. It mixes both Java and Kotlin code to show how the two languages can work hand in hand even in a cloud environment!
- [Swagger UI](http://localhost:8081/swagger-ui.html) can be used to run REST endpoints from your browser.
  - **Note**: When running the end-to-end process the code will attempt to resolve all configurations and environmental dependencies such as Lambda function and S3 bucket creation. However, this does require a running LocalStack instance which is defined below. 

## Microservice Components
Section outlines different aspects of this services components in greater detail to clarify what is being used internally at a high level.

### General
- **Spring Boot**: Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications that you can "just run". This framework is very popular to run as Microservices because Spring Boot creates portable code that can easily be pushed to a Docker container. 
- **REST Interface**: This application uses the Representational State Transfer (REST) software architectural style. This defines a set of constraints to be used for creating Web services. Web services that conform to the REST architectural style, called RESTful Web services (RWS), provide interoperability between computer systems on the Internet. RESTful Web services allow the requesting systems to access and manipulate textual representations of Web resources by using a uniform and predefined set of stateless operations. Other kinds of Web services, such as _SOAP Web services_, expose their own arbitrary sets of operations.
- **Scheduled Processing**: Service will process alert data on a set interval/schedule if enabled by setting the `weather.alert.scheduler.enabled` property to `true`. This offers a chance to show how to use Spring's `@EnableScheduling` and `@Scheduled` annotations in a straightforward manner. Also, the full end-to-end process can be kicker off using the REST API.
- [Swagger UI](http://localhost:8081/swagger-ui.html) to see and run REST endpoints in your browser after the project is built and run.
- Automated [AsciiDoc](http://asciidoc.org/) documentation generation for REST endpoints is created based upon Swagger API annotations in each Controller class. This way if you have good code comments your documentation will also be ready to go! Using some Gradle/Spring magic this information can be found after building and running the project and navigating in your browser [here](http://localhost:8081/index.html).
- Custom [JsonSchema2Pojo](https://github.com/joelittlejohn/jsonschema2pojo) extension is being used to generate Plain Old Java Objects (POJOs) which can be pushed to common repositories/pushed to services so payloads are up to date at all times. This is customized to produce Springfox related annotations on the object fields along with any JPA specifications if needed which isn't included out of the box. Check out the `$MICROSERVICE_PROJECT_DIR/src/main/resources/schema` directory to see the Objects that we create.
- **Testing**: Utilizes JUnit, Mockito and Spring Boot test runners to show how application code can be tested in a variety of ways. This will be expanded upon in future iterations to include Cucumber tests and Sonar static code analysis.
  - [JaCoCo](https://www.eclemma.org/jacoco/) is being used currently to build test reports for this services code coverage. To view the report build the project and open `$BASE_DIR/localize-it/com-alert-microservice/build/reports/tests/test/index.html` in your browser of choice.

### Amazon Web Services (AWS)
- **LocalStack**: A fully functional local AWS cloud stack used to develop and test cloud applications offline which when running locally we will utilize. [LocalStack](https://github.com/localstack/localstack) can help organizations avoid expensive overhead when developing new cloud based solutions and allow developers to mimic their deployment environment as accurately as possible. LocalStack comes with a web-console that can be viewed in your browser at [http://localhost:8080](http://localhost:8080). Components we utilize from LocalStack include:
  - **S3 (Simple Storage Service)**: Scalable object storage service that integrates with many AWS services and solutions. S3 stores your data as objects that consist of data and optional metadata that describes the file within buckets.
    - This application includes AWS S3 Controller and Service classes to help show how you can use the AWS Java SDK to perform general S3 operations.
    - Basic S3 bucket/file listings can be seen in your browser by heading to [http://localhost:4572](http://localhost:4572)
    - The default path to the weather alert sink bucket is [http://localhost:4572/alert-source-bucket](http://localhost:4572/alert-source-bucket) while the path to the sink bucket is [http://localhost:4572/alert-sink-bucket](http://localhost:4572/alert-sink-bucket) 
  - **Lambda**: Serverless code executor that allows you to run code for virtually any type of application or backend service with zero administration over server instances. These functions can be event driven and triggered by AWS services or direct calls.
    - This application includes AWS Lambda Controller and Service classes to help show how you can use the AWS Java SDK to perform general Lambda operations.
    - URL for local AWS Lambda is [http://localhost:4574](http://localhost:4574) but for an up to date list of Lambdas use the LocalStack web-console or utilize the applications Lambda REST controller to get information. 
  - **Elasticsearch**: Elasticsearch is a search engine based on the Lucene library. It provides a distributed, multitenant-capable full-text search engine with an HTTP web interface and schema-free JSON documents.
    - Configuration and Service classes provided in this repository to show how to setup, search and index data into Elasticsearch. This utilizes Elasticsearch's High Level REST Client so verbose query creation can be shown including fuzzy search capabilities.
    - Can be toggled on/off setting the `weather.alert.elasticsearch.enabled` property to `true` or `false`.
    - Local endpoint to interact with Elasticsearch is [http://localhost:4571](http://localhost:4571)

### Kafka
- Kafka is used for building real-time data pipelines and streaming apps. It is horizontally scalable, fault-tolerant, wicked fast, and runs in production in thousands of companies. Think of all the issues you had with messaging queues and breathe a sigh of relief that Kafka can be used in its place!
- Functionality can be toggled on/off setting the `weather.alert.kafka.enabled` property to `true` or `false`
- Utilizes generic `Avro` serializer/deserializer to transmit and parse object data over Kafka.
- Producers use Spring's `KafkaTemplate` to produce messages because it gives us a generic interface to interact with when sending messages to a topic.
- Consumers utilize Spring's `@KafkaListener` annotation to easily route in Weather Alert Data from a Kafka topic and perform an operation on the data which in this case is sending it to Elasticsearch.  

### Installing Necessary Dependencies
_Dependencies can also be installed through other mechanisms but we primarily use `brew` here, just go with what's easiest for you! If you do not have Homebrew on your machine try executing the following command to install it `$ /usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"`_

#### Option 1 Install Dependencies on Your Own

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
If you are running this project on a Mac then you can execute the _experimental_ install script located here, `$BASE_DIR/localize-it/general/scripts/install.sh`. This script is a work in progress so all dependencies may not be installed properly. If failures occur differ to the _Option 1_ self install to ensure everything is working as expected.

## Running Application Components
It is suggested that you run the `$BASE_DIR/localize-it/weather-alerts/scripts/run_weather_alert.sh` script from the command line so it can handle building necessary resources and check the local environment. However, you can start the components individually as described below:

### External Components
#### LocalStack
- From the command line execute `$ docker-compose -f $BASE_DIR/localize-it/weather-alerts/resources/docker/docker-localstack-compose.yml up` 
- If this command does not work properly try running `TMPDIR=/private$TMPDIR docker-compose -f $BASE_DIR/localize-it/weather-alerts/resources/docker/docker-localstack-compose.yml up` 

#### Start Kafka 
- From the command line execute `$ docker-compose -f $BASE_DIR/localize-it/weather-alerts/resources/docker/docker-kafka-compose.yml up`
  - When using the Docker Compose route it will create a topic called `test` by default for you to mess around with but will not be used by this service.
  - You may also install and run Kafka manually using their [quickstart documentation](https://kafka.apache.org/quickstart).
  
### Local Components
#### Build AWS Lambda Project
- Change into the `$BASE_DIR/localize-it/com-alert-lambda` directory.
- Execute `./gradlew clean build` to build the application.

#### Build and Run Microservice
- Change into the `$BASE_DIR/localize-it/com-alert-microservice` directory.
- Execute `./gradlew clean build` to build the application. 
- After that has completed run the following if you want to set the AWS Lambda JAR environment variable explicitly `./gradlew bootRun "-DLAMBDA_JAR_PATH=<PATH-TO>/com-alert-lambda/build/libs/com-alert-lambda-0.1.jar"` or if you've set this up in the `application.properties` simply run `./gradlew bootRun`
- Once running it should deploy to [http://localhost:8081](http://localhost:8081)
  - Note, you can go into your browser of choice and and navigate the [Swagger UI](http://localhost:8081/swagger-ui.html) to see and run REST endpoints directly once the application is running.
  - To terminate the running application send an interrupt signal to the terminal by pressing `CTRL + C`.

## References

### Local URLs
- [LocalStack AWS Elasticsearch Endpoint](http://localhost:4571)
- [LocalStack AWS S3 Endpoint](http://localhost:4572)
- [LocalStack AWS Lambda Endpoint](http://localhost:4574)
- [Swagger UI for Weather Alert Service](http://localhost:8081/swagger-ui.html)
- [Automated Weather Alert Service Documentation](http://localhost:8081/index.html)

### General Documentation
- [Homebrew Install Documentation](https://docs.brew.sh/Installation)
- [Pythons Install Documentation](https://wiki.python.org/moin/BeginnersGuide/Download)
- [Dockers Install Documentation](https://docs.docker.com/compose/install/)
- [Kafka Install Documentation](https://kafka.apache.org/quickstart)
- [AWS CLI Install Documentation](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html)
- [LocalStack Git Repository](https://github.com/localstack/localstack)
- [LocalStack AWS Local CLI Git Repository](https://github.com/localstack/awscli-local)
- [JsonSchema2Pojo Git Repository](https://github.com/joelittlejohn/jsonschema2pojo)