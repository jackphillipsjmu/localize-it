# Elasticsearch and Kibana
Creates two Docker containers that contain an Elasticsearch and Kibana instance for use locally.

## What is Elasticsearch?
Elasticsearch is a search engine based on the Apache Lucene library. It provides a distributed, multitenant-capable full-text search engine with an HTTP web interface and schema-free JSON documents. Originally designed to be an open-source Splunk it has expanded to being an absurdly fast indexing NoSQL data store. For more information, checkout the [Elasticsearch Wikipedia Page](https://en.wikipedia.org/wiki/Elasticsearch) for a brief overview.

## What is Kibana?
Kibana is an open source data visualization plugin for Elasticsearch. It provides visualization capabilities on top of the content indexed on an Elasticsearch cluster. Users can create bar, line and scatter plots, or pie charts and maps on top of large volumes of data. For more information, checkout the [Kibana Wikipedia Page](https://en.wikipedia.org/wiki/Kibana) for a brief overview.

### Current Execution Flow
By executing the script located at `$PROJECT_DIR/elasticsearch-kibana/scripts/build_elastic_kibana.sh` it will perform the actions listed below:
- Check to see that Docker is running. If it is not, an error message will be presented and the script will terminate.
- Build two Docker containers: Elasticsearch and Kibana using `docker-compose`.
  - Note, container names are explicitly set in the `docker-es-kibana-compose.yml` file to `elasticsearch` and `kibana`.
- Once the Docker containers are up and running you can open up your browser of choice and navigate to either the running [Elasticsearch instance](http://localhost:9200) or for a more user friendly experience the [Kibana instance](http://localhost:5601).
  - When you navigate to either URL you may be prompted for user credentials. These are set to the traditional Elasticsearch defaults of `elastic:changeme`

### Docker CLI Reference
Section outlines a handful of useful Docker CLI functions for interacting with Docker.

#### Explore Containers
- **List all Running Docker Containers** from the command line by executing `docker ps`
- **List all Docker Containers _(running or not)_** from the command line by executing `docker ps -a`

#### Stop Containers
- **Stop all running Docker Containers** from the command line by executing `docker stop $(docker ps -aq)`
- **Stop a running Docker Container by name** from the command line by executing `docker stop <CONTAINER-NAME>` where in this case `<CONTAINER-NAME>` can be replaced with `elasticsearch` or `kibana`.

#### Remove Containers
- **Remove all stopped Docker Containers** from the command line by executing `docker rm $(docker ps -aq)`
- **Remove a stopped Docker Container by name** from the command line by executing `docker stop <CONTAINER-NAME>` where in this case `<CONTAINER-NAME>` can be replaced with `elasticsearch` or `kibana`.

### Elasticsearch Reference
Section outlines a handful of Elasticsearch commands that can be used with cURL. In addition, Kibana's Developer Tools provides a scratchpad to perform requests to Elasticsearch. Please note that `GET` functions that do not have a corresponding query body can be done using your browser too!

- **Index** data to sample index `sample_idx` with JSON data `{"foo" : "bar"}` by using this cURL command from the command line
`curl -X POST -u elastic:changeme -H "Content-Type: application/json" -d '{"foo" : "bar"}' http://localhost:9200/sample_idx/_doc`
  - Elasticsearch will generate a unique identifier for you when indexing data if you do not have one. If you would like index a document with an explicit `ID` you can! Just modify the URL in the above command to include the ID as the last parameter, ex. `curl -X POST -u elastic:changeme -H "Content-Type: application/json" -d '{"foo" : "bar"}' http://localhost:9200/sample_idx/_doc/1` where in this case the ID is `1`. This can be useful especially when you need to tie back data to a traditional RDBMS primary key or some other identifier.
- **Retrieve "all"** indexed data from the `sample_idx` created above by executing `curl -X GET -u elastic:changeme http://localhost:9200/sample_idx/_search` from the command line. This is similar to a `SELECT *` in SQL but without a `size` parameter specified will limit the result to 10 documents to be returned. To specify the size you can append the `size` parameter to the URL, for example to get 100 possible documents to be returned execute `curl -X GET -u elastic:changeme http://localhost:9200/sample_idx/_search?size=100`. These commands can also be done in your browser by navigating to `http://localhost:9200/sample_idx/_search` or `http://localhost:9200/sample_idx/_search?size=100` for the `size` flavor of searching.
- **Query for document** from the sample index where the key `foo` has a value of `bar` by executing `curl -X GET -u elastic:changeme http://localhost:9200/sample_idx/_search?q=foo:bar` from the command line. This command can also be done in your browser by navigating to `http://localhost:9200/sample_idx/_search?q=foo:bar`
  - Note, like the other `_search` queries Elasticsearch by default limits returned results to 10 documents. To specify a higher number include a `size` parameter in the query. For example, the URL for both the cURL and browser command could change to `http://localhost:9200/sample_idx/_search?size=100;q=foo:bar` to return potentially 100 documents in the response.
- **Query for document by ID** by executing the cURL command `curl -X GET -u elastic:changeme http://localhost:9200/sample_idx/_doc/<ID>` or in your browser using the URL `http://localhost:9200/sample_idx/_doc/<ID>` replacing the `<ID>` with the Elasticsearch document ID.
  - When indexing Elasticsearch data by default if no ID is provided then one will be automatically created for you. This ID whether you explicitly set it or not can be found in the documents `_id` field.

#### References
- Logs can be found locally at `$PROJECT_DIR/elasticsearch-kibana/resources/tmp` with the naming convention of these files being `elastic_kibana_<DATE-IN-MILLIS>.log`
- Learn more about Elasticsearch by visiting their site [here](https://www.elastic.co).
- Local Elasticsearch Instance runs on [http://localhost:9200](http://localhost:9200).
- Local Kibana Instance runs on [http://localhost:5601](http://localhost:5601).
- Username for the instances is `elastic` and the password is `changeme`
