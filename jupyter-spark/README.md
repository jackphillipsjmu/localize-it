# Jupyter and Spark
Creates a Docker container that runs a Jupyter instance that supports Spark processing in `Python`, `R` and `Scala`. This is derived from Jupyters own [all-spark-notebook](https://jupyter-docker-stacks.readthedocs.io/en/latest/using/specifics.html#apache-spark) Docker Stack and is intended to get individuals started quickly in exploring both Spark and Jupyter.

### What is Jupyter?
Project Jupyter exists to develop open-source software, open-standards, and services for interactive computing across dozens of programming languages. For this instance, it offers you the ability to run code in Notebooks available in your browser so you don't need to fuss around with getting a typical IDE setup on your machine. For more information, check out [Jupyters Official Website](https://jupyter.org/).

### What is Spark?
Apache Spark is a unified analytics engine for big data processing, with built-in modules for streaming, SQL, machine learning and graph processing. For more information, check out [Sparks Official Website](https://spark.apache.org/).

### Getting Started
To spin things up up make sure you have Docker/Docker Compose installed locally on your machine. If you don't have it installed it's pretty straightforward just follow [Dockers Getting Started Guide](https://www.docker.com/get-started). From here, it is suggested you use the script located at `$PROJECT_DIR/jupyter-spark/scripts/build_jupyter_spark.sh` to handle some of the lift for you such as pulling out tokens for logging into Jupyter and setting up an example Python + Spark notebook. Of coarse, you can run the `docker-compose` command as well via the command line by executing `docker-compose -f $PROJECT_DIR/jupyter-spark/resources/docker/docker-jupyter-spark-compose.yml up`

#### What do I do after it's up and running?
Start having fun with Jupyter and Spark! Listed below are URL references for you to use to access the resources that have spun up for you locally.
- Local Jupyster Instance runs on [http://localhost:8888](http://localhost:8888) but you must provide a valid token to login which is available in the scripts console output or if running the `docker-compose` command yourself will be in the Docker console output.
- Local SparkUI (Spark Monitoring and Instrumentation UI) is available at [http://localhost:4040](http://localhost:4040) which provides information on what Spark is doing when it executes a job. This comes in handy to see what is running, identify bottlenecks and build understanding on how things are operating under the hood.

### Current Script Execution Flow
By executing the script located at `$PROJECT_DIR/jupyter-spark/scripts/build_jupyter_spark.sh` it will perform the actions listed below:
- Check to see that Docker is running. If it is not, an error message will be presented and the script will terminate.
- Build a Docker container that contains a Jupyter instance using `docker-compose`.
  - Note, the container name is explicitly set in the `docker-jupyter-spark-compose.yml` file to `jupyter_spark_docker`.
- Once the Docker container is up and running it will execute commands on the Jupyter Docker instance to retrieve a URL to your local Jupyter notebook. This will be presented in the console as a URL with a token parameter appended so you don't need to explicitly login just past it in your browser and go!
  - **Example Console Output**: `= INFO: You may access the Jupyter Notebook in your Browser at http://localhost:8888/?token=<TOKEN> =`
- Next, an example Python Jupyter notebook located at `$PROJECT_DIR/jupyter-spark/resources/example/Simple_Python.ipynb` will be copied over to the Docker instances `~/work` directory which you can find once you log into Jupyter.

**Note**: You may have to make the script executable by giving it the proper privileges, i.e. execute `chmod +x $PROJECT_DIR/jupyter-spark/scripts/build_jupyter_spark.sh` from the command line.

### Docker CLI Reference
Section outlines a handful of useful Docker CLI functions for interacting with Docker.

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
