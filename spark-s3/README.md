# spark-s3
Using Apache Spark this project will pull in a S3 CSV file that contains Census data, transform it, then pushe it to an S3 sink bucket afterwards. This builds upon another project that processes Census data, `simple-s3-lambda`, but does the processing using Spark! For a refresher, here is what happens under the hood in terms of data transformation:
- Remove any line items that have a TotalPop of zero.
- For each line item, sum the columns Hispanic, White, Black, Native, Asian and Pacific and subtract the sum from 100. Put this number in a new column named OtherEthnicity.
- For each line item, the following columns will be dropped: `TotalPop`, `Citizen`, `Income`, `IncomeErr`, `IncomePerCap`, `IncomePerCapErr`, `Poverty`, `ChildPoverty`, `Drive`, `Carpool`, `Transit`, `Walk`, `OtherTransp`, `WorkAtHome`, `MeanCommute`.

## Current Execution Flow
- By executing the script located at `$PROJECT_DIR/spark-s3/scripts/build_spark_to_s3.sh` it will perform the actions listed below:
  - Check environment to ensure that Docker and LocalStack are running and attempt to resolve what it can.
  - Removes any existing S3 source/sink buckets (`spark-source-bucket`, `spark-sink-bucket`) and recreates them to ensure that we have a fresh run with each build.
  - Pushes `$PROJECT_DIR/spark-to-s3/src/main/resources/census_data.csv` to `spark-source-bucket`.
  - Builds Spark Gradle project and produces a _"fat/shadow JAR"_ to package all of our dependencies together.
  - Runs Spark code which is printed out to the console.

### What's Happening Where?
- `spark.to.s3.SparkToS3Driver`: Class that is the main entry point of the code and will attempt to retrieve System properties to override internal default values. This calls the underlying classes to read, process and write out the data.
- `spark.to.s3.processor.census.spark.SparkProcessor`: Responsible for reading in the CSV data, transforming and writing it out using Spark. However, this is agnostic of S3 since the Spark operations performed have no tie to it making it pretty portable.  
- `spark.to.s3.utils.S3Util`: Utility class to work with S3 and setup how Spark is configured to do so.

### References
- [LocalStack Web Console](http://localhost:8080)
- [LocalStack AWS S3 Endpoint](http://localhost:4572)
- Logs can be found locally at `$PROJECT_DIR/spark-to-s3/resources/tmp`


### Gotchas
