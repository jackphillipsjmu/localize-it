# simple-springboot-docker
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