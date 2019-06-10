# simple-jenkins
Builds a Jenkins instance that has the ability to pull in a local React GitHub project, build and run it using Blue Ocean's Jenkins Docker image.

- **Current Execution Flow**
  - By executing the script located at `$PROJECT_DIR/jenkins-doom-react/scripts/build_jenkins.sh` it will perform the actions listed below:
    - Prompt user for a clean build of Jenkins instance. If this is done all current Jenkins resources will be destroyed so make sure this is what you want! If it's the first time running the script then this shouldn't matter either way.
    - Builds Jenkins Docker image and waits for it to be spun up before continuing.
    - Once Jenkins Docker image is up the admin password will be printed to the terminal for use as you see fit.
    - Next, the React project located in `$PROJECT_DIR/jenkins-doom-react/resources/web/simple-node-js-react-npm-app` will be moved to the local Jenkins home directory so it can be pulled into Jenkins.
    - Git `init`, `add` and `commit` calls are made within the Jenkins `home/simple-node-js-react-npm-app` directory to ensure that it can be picked up by Jenkins SCM.
    - A user is created with the credentials `jenkins:password` that you may use to log into the Jenkins web console.
    - Jenkins job is created to build and run the web application and is kicked off. If errors occur such as `Server returned HTTP response code: 503` then that means Jenkins didn't have enough time to catch its breath so rerun the job again and do not use the `clean build` option when prompted.

- **References**
  - [Jenkins Web Console](http://localhost:8082)
  - [React Web App](http://localhost:3000)
  - Logs can be found locally at `$PROJECT_DIR/jenkins-doom-react/resources/tmp`
