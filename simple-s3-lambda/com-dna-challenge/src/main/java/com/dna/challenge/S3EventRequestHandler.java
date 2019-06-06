package com.dna.challenge;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.dna.challenge.data.processor.s3.CensusCSVDataProcessor;
import com.dna.challenge.data.processor.exception.DataProcessingException;
import com.dna.challenge.util.CommonUtil;
import com.dna.challenge.util.S3Util;

import java.io.InputStream;

/**
 * Lambda request handlers implement AWS Lambda Function application logic using plain old java objects
 * as input and output. S3Event is the input parameter type, String is the output type.
 */
public class S3EventRequestHandler implements RequestHandler<S3Event, String> {
    // Constant values for S3 event
    // Environment variable keys
    private static final String SERVICE_ENDPOINT_KEY = "S3_SERVICE_ENDPOINT";
    private static final String SIGNING_REGION_KEY = "S3_SIGNING_REGION";
    private static final String SINK_BUCKET_KEY = "SINK_BUCKET";
    // Default values if nothing is present in the environment variables
    private static final String LOCAL_SINK_BUCKET = "sink-bucket";

    /**
     * Handles a Lambda Function request
     *
     * @param input   The Lambda Function input, i.e. an S3Event
     * @param context The Lambda execution environment context object.
     * @return The Lambda Function output
     */
    @Override
    public String handleRequest(S3Event input, Context context) {
        // Sanity checks before processing
        CommonUtil.ifNullThrowException(input, new DataProcessingException("ERROR: Cannot process null S3 Event!"));
        // If we have data then proceed with processing
        if (input.getRecords().isEmpty()) {
            throw new DataProcessingException("ERROR: Could not evaluate S3 Event " + input.toString());
        }
        // Grab the first value from the event record list, if null is returned, throw an exception
        S3EventNotification.S3EventNotificationRecord record = CommonUtil.ifNullThrowException(
                input.getRecords().stream().collect(CommonUtil.singletonCollector()),
                new DataProcessingException("ERROR: No Records obtained from S3 Event!"));

        // Pull out necessary information from S3 record
        final String sourceBucket = record.getS3().getBucket().getName();
        final String sourceKey = record.getS3().getObject().getKey();
        // Retrieve Environment variables that establish where to look for the S3 file, or, use defaults if they
        // do not exist
        final String serviceEndpoint = CommonUtil.retrieveEnvironmentVariable(SERVICE_ENDPOINT_KEY, S3Util.LOCAL_SERVICE_ENDPOINT);
        final String signingRegion = CommonUtil.retrieveEnvironmentVariable(SIGNING_REGION_KEY, S3Util.LOCAL_SIGNING_REGION);
        final String sinkBucket = CommonUtil.retrieveEnvironmentVariable(SINK_BUCKET_KEY, LOCAL_SINK_BUCKET);
        // Grab input stream from AWS S3, i.e. the file to process
        InputStream inputStream = S3Util.retrieveBucketInputStream(serviceEndpoint, signingRegion, sourceBucket, sourceKey);
        // Instantiate processor with S3 input stream
        CensusCSVDataProcessor censusCSVDataProcessor = new CensusCSVDataProcessor(inputStream);
        // Perform data processing
        String processorResult = censusCSVDataProcessor.process();
        // Copy the processed result to a new S3 bucket and return its E-Tag for traceability
        return S3Util.copyToBucket(serviceEndpoint, signingRegion, sinkBucket, "MODIFIED-" + sourceKey, processorResult).getETag();
    }
}
