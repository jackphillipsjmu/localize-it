# simple-s3-lambda
Creates local S3 buckets and Lambda function to operate on CSV Census data that is pushed to a S3 sink bucket which is in turn pulled in via a AWS Lambda function. The functions code will transform the data as follows:
- Remove any line items that have a TotalPop of zero.
- For each line item, sum the columns Hispanic, White, Black, Native, Asian and Pacific and subtract the sum from 100. Put this number in a new column named OtherEthnicity.
- For each line item, the following columns will be dropped: `TotalPop`, `Citizen`, `Income`, `IncomeErr`, `IncomePerCap`, `IncomePerCapErr`, `Poverty`, `ChildPoverty`, `Drive`, `Carpool`, `Transit`, `Walk`, `OtherTransp`, `WorkAtHome`, `MeanCommute`.

After the transformation is complete the result data is pushed to a predefined S3 sink bucket.

- **Current Execution Flow**
  - By executing the script located at `$PROJECT_DIR/simple-s3-lambda/scripts/run_s3_lambda.sh` it will perform the actions listed below:
    - Check environment to ensure that Docker and LocalStack are running prompting the user to start each if necessary (_Currently Mac OS only for Docker start functionality_).
    - Removes any existing S3 source/sink buckets and Lambda functions with the name `s3-lambda`.
    - Builds AWS S3 Lambda Java Project to be used in LocalStack.
    - Creates default S3 source and sink buckets along with Lambda function that will be configured using a JSON config file located at `$PROJECT_DIR/simple-s3-lambda/resources/config/s3_notification.json`
    - Copies example CSV file `$BASE_FIR/simple-s3-lambda/resources/sample_data/census_sample_data.csv` to [S3 source bucket](http://localhost:4572/source-bucket) which in turn invokes the Lambda function to transform and push modified file to the [S3 sink bucket](http://localhost:4572/sink-bucket).
- **References**
  - [LocalStack Web Console](http://localhost:8080)
  - [LocalStack AWS S3 Endpoint](http://localhost:4572)
  - [LocalStack AWS Lambda Endpoint](http://localhost:4574)
  - Logs can be found locally at `$PROJECT_DIR/simple-s3-lambda/resources/tmp`