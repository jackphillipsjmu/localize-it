package spark.to.s3.processor.census.spark;

import org.apache.spark.sql.*;
import scala.collection.JavaConverters;
import spark.to.s3.processor.DataProcessor;
import spark.to.s3.processor.census.data.CensusRecord;
import spark.to.s3.processor.exception.DataProcessingException;
import spark.to.s3.utils.CommonUtil;

import java.util.Arrays;

/**
 * Class responsible for handling CSV Census data with Spark
 * <p>
 * What this class will do processing the data includes:
 * <p>
 * - Remove any line items that have a TotalPop of zero
 * - For each line item, sum the columns Hispanic, White, Black, Native, Asian and Pacific and subtract the sum from 100.
 * Put this number in a new column named OtherEthnicity.
 * - For each line item, drop all columns not found in the output file format in Appendix B.
 */
public class SparkProcessor implements DataProcessor<Dataset<Row>> {
    // Final values that must always be passed in when creating this processor
    private final SparkSession sparkSession; // Allows Spark operations to occur
    private final String dataPath;           // Path to Data that will be processed

    String[] DROP_COLUMNS = new String[]{
            "TotalPop", "Citizen", "Income", "IncomeErr", "IncomePerCap", "IncomePerCapErr", "Poverty",
            "ChildPoverty", "Drive", "Carpool", "Transit", "Walk", "OtherTransp", "WorkAtHome", "MeanCommute"
    };

    /**
     * Constructor for Spark Processor if either parameter is null a {@link DataProcessingException} will be thrown.
     *
     * @param sparkSession entry point to programming Spark with the Dataset and DataFrame API.
     * @param dataPath     String path to the data this class will be processing
     */
    public SparkProcessor(SparkSession sparkSession, String dataPath) {
        // Sanity checks for null values
        CommonUtil.ifNullThrowException(sparkSession, new DataProcessingException("Cannot process Spark data without a valid Spark Session"));
        CommonUtil.ifNullThrowException(dataPath, new DataProcessingException("Cannot perform Spark process without a valid path to the data"));
        this.sparkSession = sparkSession;
        this.dataPath = dataPath;
    }

    /**
     * Processes Census data and returns the resulting {@link Dataset}
     *
     * @return Dataset of Row objects that are the result of a handful of simple data operations
     */
    @Override
    public Dataset<Row> process() {
        // Read in CSV file to typed Dataset, not necessary but shows how we can operate on the data outside
        // of using a Spark Row object
        Dataset<CensusRecord> initialDataSet = readToTypedSet(sparkSession, dataPath, CensusRecord.class);

        // Using the data that we read in from the CSV file filter for rows with a total population greater than zero
        // and append a new column with the absolute value of the sum of each ethnicity column minus 100.
        // Then, drop columns that are not needed in the final output
        return initialDataSet.filter(initialDataSet.col("TotalPop").gt(0))
                .withColumn("OtherEthnicity",
                        functions.abs(initialDataSet.col("Hispanic")
                                .plus(initialDataSet.col("White"))
                                .plus(initialDataSet.col("Black"))
                                .plus(initialDataSet.col("Native"))
                                .plus(initialDataSet.col("Asian"))
                                .plus(initialDataSet.col("Pacific"))
                                .minus(100)))
                .drop(JavaConverters.asScalaBuffer(Arrays.asList(DROP_COLUMNS)).seq());
    }

    /**
     * Writes CSV data that is contained within the provided {@link Dataset} to the S3 sink bucket.
     *
     * @param dataset         Dataset strongly typed collection of domain-specific objects that can be transformed in parallel
     *                        using functional or relational operations.
     * @param destinationPath String where to write to, ex. s3a://spark-sink-bucket/result.csv
     * @return String where the data was written to
     */
    public void writeCsvData(final Dataset<Row> dataset, final String destinationPath) {
        // Usually you should AVOID using the coalesce function unless you need to since it can have unintended
        // side effects due to how spark will shuffle/not-shuffle data around. However, since we want things all in
        // one result file we coalesce to 1.
        dataset.coalesce(1).write()
                .option("header", "true")
                .mode(SaveMode.Overwrite)
                .csv(destinationPath);
    }

    /**
     * Helper method to read a CSV file to a Spark Dataset with a corresponding Object populated with the data
     *
     * @param spark {@link SparkSession} The entry point to programming Spark with the Dataset and DataFrame API.
     * @param clazz {@link Class} instances of the class {@code Class} represent classes and interfaces in a running
     *              Java application. This will be the class that we attempt to parse the CSV data into.
     * @param <T>   Generic Type
     * @return Dataset populated with Objects of class T
     */
    private <T> Dataset<T> readToTypedSet(SparkSession spark, final String pathToData, Class<T> clazz) {
        return spark.read().option("header", "true").csv(pathToData).as(Encoders.bean(clazz)).limit(10);
    }
}
