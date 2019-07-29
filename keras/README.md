# Keras
Keras is a high-level API to build and train deep learning models in python. Keras was designed to enable fast experimentation with deep neural networks and runs on top of Tensorflow, Theano, Microsoft Cognitive Toolkit, or PlainML. This folder creates a Docker container running a Jupyter instance with Keras and Tensorflow by utilizing [Jupyter Tensorflow docker stack](https://jupyter-docker-stacks.readthedocs.io/en/latest/using/specifics.html#tensorflow). For Keras Documentation, refer to [Keras Official Website](https://keras.io/).

- **Current Execution Flow**
- By executing the script located at `$PROJECT_DIR/keras/scripts/build_jupyter_keras.sh`, the shell script will create a local Jupyter instance on port 8888 using the following steps:
  - Check to verify that Docker is running. Then, build a Docker container using `docker-keras-compose.yml`.
  - **Example Console Output**: If your browser does not open automatically, you can paste the URL displayed on the console output. Example: `= INFO: You may access the Jupyter Notebook in your Browser at http://localhost:8888/?token=<TOKEN> =`
  - Keras example notebook located at `$PROJECT_DIR/keras/resources/example/notebooks` will be copied to the Docker container volume at `$PROJECT_DIR/keras/resources/docker/volume`. This allows your work to be saved when you start, restart, or shutdown the container.

- **Run the Example Notebook**
- In the Jupyter notebook instance, click on "Keras_Basics.ipynb" located in the "example" folder.
- Run each cell sequentially to build the Keras image classifier.

- **References**
  - Jupyter notebook is exposed on port `8888`. If the notebook does not open automatically, connect to it by opening Jupyter notebook in your web browser with the correct token value the logs (http://localhost:8888/?token=<TOKEN>). The token can be found in the logfile at `$PROJECT_DIR/keras/resources/tmp`.
  - More Keras example codes can be found [here](https://github.com/keras-team/keras/tree/master/examples).