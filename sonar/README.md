# SonarQube
Creates two Docker containers that contain a SonarQube instance and a corresponding PostgreSQL backend to support it.

## What is SonarQube?
SonarQube provides the capability to not only show health of an application but also to highlight issues newly introduced. With a Quality Gate in place, you can fix the leak and therefore improve code quality systematically. Using static code analysis it can identify security risks, bugs and code smells that you can configure yourself!

### Current Execution Flow
By executing the script located at `$PROJECT_DIR/sonar/scripts/build_sonar.sh` it will perform the actions listed below:
- Check to see that Docker is running. If it is not, an error message will be presented and the script will terminate.
- Check to see if a process is running on Sonar port `9000`. If so, ensure that it is indeed the `sonar` container running so other processes aren't interrupted. If it is not, an error message will be presented and the script will terminate.
- Build two Docker containers: Sonar and PostgreSQL using `docker-compose`.
  - Note, container names are explicitly set in the `docker-sonar-compose.yml` file to `sonar` and `sonar_postgres`. The `sonar_postgres` image does not expose the PostgreSQL default port `5432` to your local machine so conflicts shouldn't occur with other running processes.
- Script will then wait for Sonar to be up and then display a message to the console saying it is running.
  - You can access the locally running Sonar console at [http://localhost:9000](http://localhost:9000).

#### References
- Logs can be found locally at `$PROJECT_DIR/sonar/resources/tmp` with the naming convention of these files being `sonar_<DATE-IN-MILLIS>.log`
- Learn more about SonarQube by visiting their site [here](https://www.sonarqube.org/).
