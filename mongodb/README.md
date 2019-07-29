# MongoDB
MongoDB is a document-oriented, NoSQL database providing high availability and scalability. This folder creates a Docker container containing a MongoDB instance.

- **Current Execution Flow**
  - By executing the script located at `$PROJECT_DIR/mongodb/scripts/build_mongodb.sh`, it will perform the actions listed below:
    - Run the MongoDB Docker container with port 27017 exposing MongoDB and port 27018 exposing Mongo-Express, a web UI for MongoDB.
    - Create a database in MongoDB called `admin`.
    - Create a collection called `companies` and insert some dummy data.

- **Basic MongoDB Commands**
You can use MongoDB commands from your terminal by using `docker exec -it mongo <COMMAND>` or by opening a bash shell in the container. If you would like to open the MongoDB shell that connects to MongoDB, run `docker exec -it mongo mongo` from the command line. To exit, press `CTRL + D` and it will exit the running MongoDB shell. If you prefer to use a web UI to interact with MongoDB data, you can connect to it via opening [Mongo-Express](http://localhost:27018) on a web browser.

Here are a few examples of MongoDB commands. Refer to the MongoDB [Documentation](https://docs.mongodb.com/manual/) for more detail.
- **Open the MongoDB shell**: `docker exec -it mongo mongo`. Once you get into the MongoDB shell, you can use below command lines to build databases in MongoDB.
  - **Print a list of all databases on the server**: `show dbs`
  - **Switch current database to <db>**: `use <DB_NAME>`
  - **Print a list of all collections for current database***: `show collections`
  - **Insert a new document into a collection (if the collection does not exist, this method will create the collection)**: `db.<COLLECTION_NAME>.insert([{'<KEY_1>':'<VALUE_1>','<KEY_2>':'<VALUE_2>'}]);`
  - **Selects documents in a collection or view and returns a cursor to the selected documents.**: `db.<COLLECTION_NAME>.find()`
  - To exit the console consumer press `CTRL + C`.

- **References**
  - MongoDB is exposed on port `27017`. Connect to it by executing `docker exec -it mongodb bash` then by executing `mongo`.
  - Mongo-Express, a web-based MongoDB admin interface, is expoed on port `27018`.  Connect to it via (http://localhost:27018)
  - Logs can be found locally at `$PROJECT_DIR/mongodb/resources/tmp`