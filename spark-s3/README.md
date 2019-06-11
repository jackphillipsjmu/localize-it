# spark-s3
Using Apache Spark this project will pull in an S3 CSV file that contains Census data, transform it, then pushes it to an S3 sink bucket afterwards. This builds upon another project that processes Census data, `simple-s3-lambda`, but does the processing using Spark! For a refresher, here is what happens under the hood in terms of data transformation:
- Remove any line items that have a TotalPop of zero.
- For each line item, sum the columns Hispanic, White, Black, Native, Asian and Pacific and subtract the sum from 100. Put this number in a new column named OtherEthnicity.
- For each line item, the following columns will be dropped: `TotalPop`, `Citizen`, `Income`, `IncomeErr`, `IncomePerCap`, `IncomePerCapErr`, `Poverty`, `ChildPoverty`, `Drive`, `Carpool`, `Transit`, `Walk`, `OtherTransp`, `WorkAtHome`, `MeanCommute`.

- **Current Execution Flow**
  - By executing the script located at `$BASE_DIR/spark-s3/scripts/build_spark_to_s3.sh` it will perform the actions listed below:
    - Check environment to ensure that Docker and LocalStack are running and attempt to resolve what it can.
    - Removes any existing S3 source/sink buckets (`spark-source-bucket`, `spark-sink-bucket`) and recreates them to ensure that we have a fresh run with each build.
    - Pushes `$BASE_DIR/spark-to-s3/src/main/resources/census_data.csv` to `spark-source-bucket`.
    - Builds Spark Gradle project and produces a _"fat/shadow JAR"_ to package all of our dependencies together.
    - Runs Spark code which is printed out to the console.
- **References**
  - [LocalStack Web Console](http://localhost:8080)
  - [LocalStack AWS S3 Endpoint](http://localhost:4572)
  - Logs can be found locally at `$BASE_DIR/spark-to-s3/resources/tmp`
