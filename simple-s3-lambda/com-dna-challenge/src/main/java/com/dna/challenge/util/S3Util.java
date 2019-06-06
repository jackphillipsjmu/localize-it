package com.dna.challenge.util;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import java.io.InputStream;

/**
 * Utility class to help with common AWS S3 Operations
 */
public final class S3Util {
    // Class constants to be used by underlying code
    public static final String LOCAL_SERVICE_ENDPOINT = "http://localhost:4572";
    public static final String LOCAL_SIGNING_REGION = "us-east-1";

    /**
     * Copies the provided String content to the specified S3 Bucket
     *
     * @param serviceEndpoint the service endpoint either with or without the protocol
     *                        (e.g. https://sns.us-west-1.amazonaws.com or sns.us-west-1.amazonaws.com)
     * @param signingRegion   the region to use for SigV4 signing of requests (e.g. us-west-1)
     * @param bucketName      name of the S3 bucket to copy/put file into
     * @param bucketKey       the key of the object to create.
     * @param content         data to encode and send to S3 bucket
     * @return {@link PutObjectResult} Contains the data returned by Amazon S3 from the putObject operation
     */
    public static PutObjectResult copyToBucket(final String serviceEndpoint,
                                               final String signingRegion,
                                               final String bucketName,
                                               final String bucketKey,
                                               final String content) {
        return S3Util.setupAmazonS3(serviceEndpoint, signingRegion, true)
                .putObject(bucketName, bucketKey, content);
    }

    /**
     * Retrieves a file from an S3 bucket and creates an {@link InputStream} from its contents.
     *
     * @param serviceEndpoint the service endpoint either with or without the protocol
     *                        (e.g. https://sns.us-west-1.amazonaws.com or sns.us-west-1.amazonaws.com)
     * @param signingRegion   the region to use for SigV4 signing of requests (e.g. us-west-1)
     * @param sourceBucket    name of the S3 bucket to pull input stream from
     * @param sourceKey       the key of the object to retrieve and create an input stream
     * @return {@link InputStream} constructed from contents contained in the provided S3 file
     */
    public static InputStream retrieveBucketInputStream(final String serviceEndpoint,
                                                        final String signingRegion,
                                                        final String sourceBucket,
                                                        final String sourceKey) {
        return S3Util.setupAmazonS3(serviceEndpoint, signingRegion, true)
                .getObject(new GetObjectRequest(sourceBucket, sourceKey))
                .getObjectContent();
    }

    /**
     * Creates a {@link AmazonS3} object which provides access to AWS S3 web services.
     * <p>
     * NOTE: This will build a localized {@link AmazonS3} object for use when testing against a locally running AWS
     * environment.
     *
     * @param serviceEndpoint        the service endpoint either with or without the protocol
     *                               (e.g. https://sns.us-west-1.amazonaws.com or sns.us-west-1.amazonaws.com)
     * @param signingRegion          the region to use for SigV4 signing of requests (e.g. us-west-1)
     * @param pathStyleAccessEnabled configures the client to use path-style access for all requests.
     * @return {@link AmazonS3} provides an interface for accessing the Amazon S3 web service.
     */
    private static AmazonS3 setupAmazonS3(final String serviceEndpoint,
                                          final String signingRegion,
                                          final boolean pathStyleAccessEnabled) {
        return setupAmazonS3(serviceEndpoint, signingRegion, pathStyleAccessEnabled, true);
    }

    /**
     * Creates a {@link AmazonS3} object which provides access to AWS S3 web services.
     *
     * @param serviceEndpoint         the service endpoint either with or without the protocol
     *                                (e.g. https://sns.us-west-1.amazonaws.com or sns.us-west-1.amazonaws.com)
     * @param signingRegion           the region to use for SigV4 signing of requests (e.g. us-west-1)
     * @param pathStyleAccessEnabled  configures the client to use path-style access for all requests.
     * @param chunkedEncodingDisabled configures the client to disable chunked encoding for all requests
     * @return {@link AmazonS3} provides an interface for accessing the Amazon S3 web service.
     */
    private static AmazonS3 setupAmazonS3(final String serviceEndpoint,
                                          final String signingRegion,
                                          final boolean pathStyleAccessEnabled,
                                          final boolean chunkedEncodingDisabled) {
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, signingRegion))
                // This should be set to true when running locally running a Amazon S3 instance
                .withPathStyleAccessEnabled(pathStyleAccessEnabled)
                // Configures the client to disable chunked encoding for all requests
                // set this value to true to avoid issues with local S3 bucket testing
                .withChunkedEncodingDisabled(chunkedEncodingDisabled)
                .build();
    }
}
