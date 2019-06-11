package spark.to.s3;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import spark.to.s3.processor.census.spark.SparkProcessor;
import spark.to.s3.utils.CommonUtil;
import spark.to.s3.utils.S3Util;

/**
 * Driver class to show how to read in Census data, process it, write it to an S3 bucket and then retrieve that data
 * once again.
 */
public class SparkToS3Driver {
    // Constants used by the class
    private static final String APP_NAME = "java-spark";
    private static final String LOCAL_MASTER = "local[*]";
    private static final String SINK_BUCKET_KEY = "SINK_BUCKET";
    private static final String SOURCE_CENSUS_DATA_KEY = "CENSUS_DATA";
    private static final String DEFAULT_DATA = "s3a://spark-source-bucket/census_data.csv";
    private static final String DEFAULT_SINK_BUCKET = "spark-sink-bucket";

    /**
     * Main entry point for Spark data processing which will read in local/S3 Census data, processes it using Spark
     * then write the result out to a sink S3 bucket. Serving primarily to show examples of reading/writing data to S3.
     *
     * @param args String array for application arguments
     */
    public static void main(String[] args) {
        // Grab any system properties to drive the location of where data is pulled in and where it's going
        final String sourceDataPath = CommonUtil.retrieveSystemProperty(SOURCE_CENSUS_DATA_KEY, DEFAULT_DATA);
        // Sink bucket, S3 bucket to write data
        final String sinkBucket = CommonUtil.retrieveSystemProperty(SINK_BUCKET_KEY, DEFAULT_SINK_BUCKET);

        // Setup spark session
        SparkSession sparkSession = S3Util.createLocalS3SparkSession(APP_NAME, LOCAL_MASTER, true);

        // Read in data using SparkProcessor class
        System.out.println("Reading data from " + sourceDataPath);
        SparkProcessor sparkProcessor = new SparkProcessor(sparkSession, sourceDataPath);
        Dataset<Row> dataset = sparkProcessor.process();
        // Print out Dataset to console
        dataset.show();

        // Get path to S3 sink bucket which is where we write to
        final String s3DestinationPath = S3Util.buildS3BucketPath(sinkBucket);
        System.out.println("Writing Data to " + s3DestinationPath);
        // Write result CSV data
        sparkProcessor.writeCsvData(dataset, s3DestinationPath);

        // Read in S3 data that we just pushed and show it
        System.out.println("Reading in S3 data from " + s3DestinationPath);
        Dataset<Row> sinkDataset = S3Util.readS3CSVFile(sparkSession, s3DestinationPath);
        sinkDataset.show();
    }
}
