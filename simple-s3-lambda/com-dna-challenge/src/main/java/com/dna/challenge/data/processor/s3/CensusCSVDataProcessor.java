package com.dna.challenge.data.processor.s3;

import com.dna.challenge.data.processor.census.CensusConstants;
import com.dna.challenge.data.processor.census.CensusRecord;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.dna.challenge.data.processor.DataProcessor;
import com.dna.challenge.data.processor.exception.DataProcessingException;
import com.dna.challenge.util.CommonUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class responsible for handling CSV data pulled from an S3 Event in AWS.
 * <p>
 * What this class will do processing the data includes:
 * <p>
 * - Remove any line items that have a TotalPop of zero
 * - For each line item, sum the columns Hispanic, White, Black, Native, Asian and Pacific and subtract the sum from 100.
 * Put this number in a new column named OtherEthnicity.
 * - For each line item, drop all columns not found in the output file format in Appendix B.
 */
public class CensusCSVDataProcessor implements DataProcessor<String> {
    // Final variable because it is ALWAYS required when constructing this Object
    private final InputStream censusDataStream;

    /**
     * Constructor for {@link CensusCSVDataProcessor}
     *
     * @param censusDataStream InputStream that is pulled from S3 Object
     */
    public CensusCSVDataProcessor(InputStream censusDataStream) {
        // If the input stream is null then throw an exception immediately
        CommonUtil.ifNullThrowException(censusDataStream, new DataProcessingException("Cannot process Null Census Data Stream"));
        this.censusDataStream = censusDataStream;
    }

    /**
     * Processes Census CSV Data and returns a String that contains modified output data to push to AWS S3 Sink Bucket
     *
     * @return String result of processing CSV data
     */
    public String process() {
        List<CensusRecord> censusRecords;
        try {
            censusRecords = convert(censusDataStream, Charset.defaultCharset(), CensusRecord.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new DataProcessingException("ERROR: Exception occurred processing CSV data", e);
        }
        // Create an updated schema for use in output later on
        // this is needed because Jackson's CSV library does not have a proper
        // drop column function like Spark
        CsvSchema updatedSchema = createSchemaFromColumnNames(CensusConstants.RESULT_COLUMNS);

        // Filter out values with a total population of zero
        // then convert the data into the updated format with new column attached
        // finally, collect them all into a list
        List<String> resultRecords = censusRecords.stream()
                .filter(currentRecord -> currentRecord.getTotalPop() > 0)
                .map(currentRecord -> currentRecord.processOtherEthnicityColumn(CensusConstants.ETHNICITY_COL, updatedSchema))
                .collect(Collectors.toList());
        // append header string for output that will be pushed to S3 sink bucket
        resultRecords.add(0, String.join(",", Arrays.asList(CensusConstants.RESULT_COLUMNS)).concat(System.lineSeparator()));
        // Join everything in the list together and return
        return String.join("", resultRecords);
    }

    /**
     * Helper method to create a simple schema from 1..N column names provided.
     *
     * @param columnNames 1..N String objects that will be set in the schema as columns
     * @return CsvSchema built using the provided column names
     */
    private CsvSchema createSchemaFromColumnNames(String... columnNames) {
        CsvSchema.Builder builder = CsvSchema.builder();
        Arrays.asList(columnNames).forEach(builder::addColumn);
        return builder.build();
    }

    /**
     * Converts a InputStream to a List and parses it into the provided Class.
     *
     * @param inputStream InputStream containing parsable CSV content
     * @param charset     Charset to help encode/decode data
     * @param clazz       Class to parse input stream too
     * @param <T>         Generic Type
     * @return List of T objects parsed from the provided input stream
     * @throws IOException
     */
    private <T> List<T> convert(InputStream inputStream, Charset charset, Class<T> clazz) throws IOException {
        // Null sanity checks
        CommonUtil.ifNullThrowException(clazz, new DataProcessingException("Cannot Convert Input Stream from a NULL Class"));
        CommonUtil.ifNullThrowException(inputStream, new DataProcessingException("Cannot Convert NULL Input Stream to object"));
        // If the provided charset is null then get the default set for the current JVM environment
        charset = CommonUtil.defaultIfNull(charset, Charset.defaultCharset());
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            return bufferedReader.lines()
                    .skip(1L)
                    .map(line -> processRecord(line, clazz))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Converts the provided String into the Class.
     *
     * @param recordStr String CSV representation of object
     * @param clazz     Class to parse String into
     * @param <T>       Generic Type
     * @return T parsed from String to specified class
     */
    private <T> T processRecord(final String recordStr, final Class<T> clazz) {
        if (CommonUtil.isNotEmpty(recordStr)) {
            CsvMapper csvMapper = new CsvMapper();
            try {
                return csvMapper.readerWithTypedSchemaFor(clazz).readValue(recordStr);
            } catch (IOException e) {
                // Throw exception to tell invoking code that something bad happened
                throw new DataProcessingException(e);
            }
        }
        return null;
    }
}
