package spark.to.s3.utils;

import com.amazonaws.services.s3.internal.SkipMd5CheckStrategy;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import spark.to.s3.processor.exception.DataProcessingException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to help with Spark + S3 operations.
 */
public final class S3Util {
    // Class constants to be used by underlying code
    private static final String APP_NAME = "java-spark";
    private static final String LOCAL_MASTER = "local[*]";
    private static final String S3_A_PREFIX = "s3a://";

    /**
     * Private default constructor, singleton.
     */
    private S3Util() {
        // No instantiation, just use the methods below!
    }

    /**
     * Creates a local Spark S3 session for use when reading S3 data
     *
     * @param appName   application name for Spark Session
     * @param masterUrl URL for Spark master node
     * @param isLocal   Boolean to dictate if this is a local process or not
     * @return SparkSession to use when operating on S3 data
     */
    public static SparkSession createLocalS3SparkSession(final String appName,
                                                         final String masterUrl,
                                                         final Boolean isLocal) {
        // Ensure we have values and populate with defaults if necessary
        final String applicationName = CommonUtil.defaultIfNull(appName, APP_NAME);
        final String master = CommonUtil.defaultIfNull(masterUrl, LOCAL_MASTER);
        final Boolean local = CommonUtil.defaultIfNull(isLocal, true);

        // Base Spark Session Builder
        SparkSession sparkSession = SparkSession.builder()
                .appName(applicationName)
                .master(master)
                .getOrCreate();
        // Set log level to ERROR so we aren't clogging up our log data
        sparkSession.sparkContext().setLogLevel("ERROR");

        // If we have a local setup then ensure the spark session knows where to look for our data
        return local ? localS3Setup(sparkSession) : sparkSession;
    }

    /**
     * Sets up local S3 configuration in Spark Session
     *
     * @param sparkSession entry point to programming Spark with Datasets
     * @return SparkSession with S3 configurations set
     */
    private static SparkSession localS3Setup(SparkSession sparkSession) {
        // Sanity null checks, in Kotlin if there is no elvis operator on the parameter then it must not be null
        CommonUtil.ifNullThrowException(sparkSession, new DataProcessingException("Cannot Setup Local Spark S3 Configuration!"));

        // Disable MD5 Checking which is necessary at this time when using LocalStack
        // check out https://github.com/localstack/localstack/pull/1120 for more info
        System.setProperty(SkipMd5CheckStrategy.DISABLE_GET_OBJECT_MD5_VALIDATION_PROPERTY, "true");
        System.setProperty(SkipMd5CheckStrategy.DISABLE_PUT_OBJECT_MD5_VALIDATION_PROPERTY, "true");

        // Declare map of necessary variables to apply to Hadoop configuration
        Map<String, String> variableMap = new HashMap<>();
        variableMap.put("fs.s3a.access.key", "test");
        variableMap.put("fs.s3a.secret.key", "test");
        variableMap.put("fs.s3a.endpoint", "http://localhost:4572");
        variableMap.put("fs.s3a.path.style.access", "true");
        // Helps to get over MD5 hurdle with LocalStack
        variableMap.put("fs.s3a.multiobjectdelete.enable", "false");

        // Need to specify HDFS and file implementations for Hadoop since it cannot resolve them by itself.
        // If you're running this in an IDE this portion is not necessary, however, if you build JAR to use
        // then these will need to be specified
        variableMap.put("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
        variableMap.put("fs.file.impl", "org.apache.hadoop.fs.LocalFileSystem");

        // Append Hadoop configuration properties
        variableMap.forEach((key, value) -> sparkSession.sparkContext().hadoopConfiguration().set(key, value));
        // return reference to SparkSession
        return sparkSession;
    }

    /**
     * Reads a file from S3 and returns a Dataset of Row objects
     *
     * @param sparkSession entry point to programming Spark with Datasets
     * @param s3Path       path to the S3 file
     * @return Dataset containing S3 CSV data
     */
    public static Dataset<Row> readS3CSVFile(SparkSession sparkSession, final String s3Path) {
        // Sanity null checks, in Kotlin if there is no elvis operator on the parameter then it must not be null
        CommonUtil.ifNullThrowException(sparkSession, new DataProcessingException("Cannot Read CSV File from S3 due to a null SparkSession"));
        CommonUtil.ifNullThrowException(s3Path, new DataProcessingException("Cannot Read CSV File from null S3 file path"));
        // Read in the CSV data, it is assumed a header is present in the file
        return sparkSession.read().option("header", "true").csv(s3Path);
    }

    /**
     * Builds path to S3 bucket and attempts to resolve issues with not providing the "s3a" prefix amongst other things.
     *
     * @param bucketName String the bucket name to build a path to, ex. spark-sink-bucket
     * @return String path to S3 bucket, ex. s3a://spark-sink-bucket/output_1560269886384.csv
     */
    public static String buildS3BucketPath(final String bucketName) {
        // No bucket name, throw exception
        if (CommonUtil.isNullOrEmpty(bucketName)) {
            throw new DataProcessingException("Cannot build S3 Bucket path from null/empty data");
        }
        // Build generated file name and then build path to it
        final String fileName = "/output" + new Date().getTime() + ".csv";
        return bucketName.startsWith(S3_A_PREFIX) ? bucketName + fileName : S3_A_PREFIX + bucketName + fileName;
    }
}
