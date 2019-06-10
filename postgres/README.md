# postgres
Creates a Docker container that contains a PostgreSQL instance.

- **Current Execution Flow**
  - By executing the script located at `$PROJECT_DIR/postgres/scripts/build_postgres.sh` it will perform the actions listed below:
    - Run the PostgreSQL Docker container
    - Create a Database in PostgreSQL called `example_db`.
    - Create a table called `example_tbl` if it does not exist in the `example_db` and insert some dummy data.

- **References**
  - Logs can be found locally at `$PROJECT_DIR/postgres/resources/tmp`
  - PostgreSQL is exposed on port `54320`. Connect to it either through the command line by executing `docker exec -it postgres psql -U postgres` or through the IDE of your choice.
