# Keras
Keras is a high-level API to build and train deep learning models in python. Keras was designed to enable fast experimentation with deep neural networks and runs on top of Tensorflow, Theano, Microsoft Cognitive Toolkit, or PlainML. This folder creates a Docker container running a Jupyter instance as well as deep learning libraries such as Keras and Tensorflow by utilizing [Tensorflow Jupyter docker stack](https://jupyter-docker-stacks.readthedocs.io/en/latest/using/specifics.html#tensorflow). For more information on Keras, refer to [Keras Official Website](https://keras.io/).

- **Current Execution Flow**
- By executing the script located at `$PROJECT_DIR/keras/scripts/build_jupyter_keras.sh`, the shell script will create a local Jupyter instance on [http://localhost:8888](http://localhost:8888) using the following steps:
  - Check to verify that Docker is running. Then, build a Docker container using `docker-compose`.
  - **Example Console Output**: If your browser does not open automatically, you can paste the URL displayed on the console output. Example: `= INFO: You may access the Jupyter Notebook in your Browser at http://localhost:8888/?token=<TOKEN> =`
  - Keras example notebook located at `$PROJECT_DIR/keras/resources/example/notebooks` will be copied to the Docker container volume at `$PROJECT_DIR/keras/resources/docker/volume`. This allows your work to be saved when you start, restart, or shutdown the container.

- **References**
  - Jupyter notebook is exposed on port `8888`. Connect to it by opening Jupyter notebook in your web browser with the correct token value.
  - Keras example codes can be found [here](https://keras.io/getting-started/sequential-model-guide/).
  - Logs can be found locally at `$PROJECT_DIR/keras/resources/tmp`