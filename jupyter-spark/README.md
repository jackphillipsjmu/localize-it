# Jupyter and Spark
Creates a Docker container that runs a Jupyter instance that supports Spark processing in `Python`, `R` and `Scala`. This is derived from Jupyters own [all-spark-notebook](https://jupyter-docker-stacks.readthedocs.io/en/latest/using/specifics.html#apache-spark) Docker Stack and is intended to get individuals started quickly in exploring both Spark and Jupyter.

### What is Jupyter?
Project Jupyter exists to develop open-source software, open-standards, and services for interactive computing across dozens of programming languages. For this instance, it offers you the ability to run code in Notebooks available in your browser so you don't need to fuss around with getting a typical IDE setup on your machine. For more information, check out [Jupyters Official Website](https://jupyter.org/).

### What is Spark?
Apache Spark is a unified analytics engine for big data processing, with built-in modules for streaming, SQL, machine learning and graph processing. For more information, check out [Sparks Official Website](https://spark.apache.org/).

### Getting Started
To spin things up up make sure you have Docker/Docker Compose installed locally on your machine. If you don't have it installed it's pretty straightforward just follow [Dockers installation documentation](https://docs.docker.com/compose/install/). From here, it is suggested you use the script located at `$PROJECT_DIR/jupyter-spark/scripts/build_jupyter_spark.sh` to handle some of the lift for you such as pulling out tokens for logging into Jupyter and setting up example Python + Spark notebooks. Of coarse, you can run the `docker-compose` command as well via the command line by executing `docker-compose -f $PROJECT_DIR/jupyter-spark/resources/docker/docker-jupyter-spark-compose.yml up`

#### What do I do after it's up and running?
Start having fun with Jupyter and Spark! Listed below are URL references for you to use to access the resources that have spun up for you locally.
- Local Jupyster Instance runs on [http://localhost:8888](http://localhost:8888) but you must provide a valid token to login which is available in the scripts console output or if running the `docker-compose` command yourself will be in the Docker console output.
- Local SparkUI (Spark Monitoring and Instrumentation UI) is available at [http://localhost:4040](http://localhost:4040) which provides information on what Spark is doing when it executes a job. This comes in handy to see what is running, identify bottlenecks and build understanding on how things are operating under the hood.
- The `$PROJECT_DIR/jupyter-spark/resources/docker/volume` directory ties to the Docker containers Home directory using a Docker data volume. This enables you to save your work so it is not destroyed when you shutdown and restart the container. Also, it provides an easy way to put your own local files onto the Docker container and retrieve files off of the container as well. Nifty huh?

### Current Script Execution Flow
By executing the script located at `$PROJECT_DIR/jupyter-spark/scripts/build_jupyter_spark.sh` it will perform the actions listed below:
- Check to see that Docker is running. If it is not, an error message will be presented and the script will terminate.
- Build a Docker container that contains a Jupyter instance using `docker-compose`.
  - Note, the container name is explicitly set in the `docker-jupyter-spark-compose.yml` file to `jupyter_spark_docker`.
- Once the Docker container is up and running it will execute commands on the Jupyter Docker instance to retrieve a URL to your local Jupyter notebook. This will be presented in the console as a URL with a token parameter appended. Also, it will check your current Operating System and attempt to open the Jupyter instance in your default browser.
  - **Example Console Output**: In the event your browser does not open automatically, copy the URL from the console output and paste it into your browser of choice! Example: `= INFO: You may access the Jupyter Notebook in your Browser at http://localhost:8888/?token=<TOKEN> =`
- Next, example Python Jupyter notebooks that do not currently exist and are unmodified located at `$PROJECT_DIR/jupyter-spark/resources/example/notebooks` will be copied over to a local data volume that ties to the container at `$PROJECT_DIR/jupyter-spark/resources/docker/volume`. This data volume enables you to save your work without it getting lost when you start, shutdown and restart the container. As stated above, this also provides you an easy way to put your local files onto the Docker container and retrieve files off of the container as well.

**Note**: You may have to make the script executable by giving it the proper privileges, i.e. execute `chmod +x $PROJECT_DIR/jupyter-spark/scripts/build_jupyter_spark.sh` from the command line.

### Docker CLI Reference
Section outlines a few potentially useful Docker CLI functions for interacting with Docker.

#### Explore Containers
- **List all Running Docker Containers** from the command line by executing `docker ps`
- **List all Docker Containers _(running or not)_** from the command line by executing `docker ps -a`

#### Stop Containers
- **Stop all running Docker Containers** from the command line by executing `docker stop $(docker ps -aq)`
- **Stop a running Docker Container by name** from the command line by executing `docker stop <CONTAINER-NAME>` where in this case `<CONTAINER-NAME>` can be replaced with `jupyter_spark_docker`.

#### Remove Containers
- **Remove all stopped Docker Containers** from the command line by executing `docker rm $(docker ps -aq)`
- **Remove a stopped Docker Container by name** from the command line by executing `docker stop <CONTAINER-NAME>` where in this case `<CONTAINER-NAME>` can be replaced with `jupyter_spark_docker`.

#### Interacting with Container
- **Start a Bash shell in a Docker Container** by executing `docker exec -it <CONTAINER-NAME> /bin/bash` from the command line. Note, to exit send an interrupt to the shell with `CTRL + D`.
- **Execute a shell command in a Docker Container** by executing `docker exec -it <CONTAINER-NAME> <COMMAND>` from the command line.
- **Copy a file from your host machine to a Docker Container** by executing `    docker cp <HOST-FILE> "<CONTAINER-NAME>:<CONTAINER-FILE-PATH>`

#### References
- If you have run the Docker containers using the script, logs can be found locally at `$PROJECT_DIR/jupyter-spark/resources/tmp` with the naming convention of these files being `jupyter_spark_<DATE-IN-MILLIS>.log`
- Examples of using `Python`, `R` and `Scala` in a Jupyter Notebook can be found [here](https://jupyter-docker-stacks.readthedocs.io/en/latest/using/specifics.html#apache-spark).
