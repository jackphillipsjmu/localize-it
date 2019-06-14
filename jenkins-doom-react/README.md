# jenkins-doom-react
Builds a Jenkins instance that has the ability to pull in a local [React](https://reactjs.org/) GitHub project, build and run it using Blue Ocean's Jenkins Docker image. For fun, the React application utilizes [DOSBox](https://www.dosbox.com/) to let you play [Doom](https://doom.fandom.com/wiki/Doom) in your browser!

### Current Execution Flow
- By executing the script located at `$PROJECT_DIR/jenkins-doom-react/scripts/build_jenkins.sh` it will perform the actions listed below:
  - Builds Jenkins Docker image and waits for it to be spun up before continuing.
  - Once Jenkins Docker image is up the admin password will be printed to the terminal for use as you see fit.
  - Next, the React project located in `$PROJECT_DIR/jenkins-doom-react/resources/web/simple-node-js-react-npm-app` will be moved to the local Jenkins home directory so it can be pulled into Jenkins.
  - Git `init`, `add` and `commit` calls are made within the Jenkins `home/simple-node-js-react-npm-app` directory to ensure that it can be picked up by Jenkins SCM.
  - A user is created with the credentials `jenkins:password` that you may use to log into the Jenkins web console.
  - Jenkins job is created to build and run the web application and is then kicked off.
  - Terminal displays status of the web-application and will notify you when it's done. Or you can monitor the job yourself within the [Jenkins doom-react Job](http://localhost:8082/job/doom-react).
  - Once the job is complete go to the [React Web App](http://localhost:3000) to view the deployed application!
  - To kill the application you can go into the running Jenkins job and through the `Paused for Input` (suggested path), `Blue Ocean` or `Console Output` links click the `Proceed` button.

### Building the Web-App Locally
If you want to play around with the Web-App locally and deploy the changes you can! Just follow the instructions below.
#### Required Dependencies
To build the application you must have `npm` installed. To do this follow installation instructions provided by [npm](https://www.npmjs.com/get-npm) or use Hombrew to install the necessary dependencies like so:
- From the terminal execute `$ brew update` to get the latest from Homebrew.
- Next, execute `$ brew install node` to install `Node.js` and `npm`.
- Verify the installation by executing `$ node -v` in a terminal to display the version of `Node.js` that is installed. Execute `$ npm -v` to display the `npm` version that is installed.

#### Running the Application
- From the terminal change into the `$PROJECT_DIR/jenkins-doom-react/resources/web/simple-node-js-react-npm-app` directory.
- Execute the `$ npm install` command to install necessary dependencies. Since this is an older React project you can ignore the warnings `npm` shows for now.
- Build the `Node.js`/`React` application by executing `$ npm run build` from the terminal.
- Run the application by executing `$ npm start` this will start the application and attempt to open a window in your browser which you may need to give the proper permissions to do so (should prompt for input automatically).
- Go to [http://localhost:3000/](http://localhost:3000/) to see the running application.

#### References
- [Jenkins Web Console](http://localhost:8082)
- [React Web App](http://localhost:3000)
- [npm](https://www.npmjs.com/)
- [Node.js](https://nodejs.org/en/)
- Logs can be found locally at `$PROJECT_DIR/jenkins-doom-react/resources/tmp` with the naming convention of these files being `jenkins_<DATE-IN-MILLIS>.log`.