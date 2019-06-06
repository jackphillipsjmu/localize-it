# simple-jenkins
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
