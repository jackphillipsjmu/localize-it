package com.alert.lambda.data.processor

import com.alert.lambda.exception.DataProcessingException
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.event.S3EventNotification
import com.amazonaws.services.s3.model.CopyObjectResult

/**
 *
 * Class that handles an [S3Event] to grab an object from a source bucket and place it in a sink bucket while
 * showing off some cool Kotlin things at the same time! This calls implements the RequestHandler AWS class which
 * represents AWS Lambda Function application logic using POJOs as input and output.
 *
 * Kotlin highlights of this code include:
 * - Companion Object
 * - Extension functions
 * - Data Class
 * - Nullability operators (safe call ?., elvis ?:)
 * - Java interoperability
 */
class AlertRequestHandler : RequestHandler<S3Event, String> {
    // Constants for class, think of "object" as a static block in your code. These can even be named as well
    // so you could have "companion object Constants" and call things inside via Constants.<YOUR-STATIC-CALL>
    companion object {
        // Environment variable keys
        const val SINK_BUCKET_ENV_KEY = "SINK_BUCKET"
        // Default Local AWS Constants
        const val LOCAL_SERVICE_ENDPOINT = "http://localhost:4572"
        const val LOCAL_SIGNING_REGION = "us-east-1"
        const val DEFAULT_SINK_BUCKET = "alert-sink-bucket"
    }

    /**
     * Handles a Lambda Function request which in this case will take an S3 file from a source bucket and
     * move it to a sink bucket.
     *
     * Notice that this differs from overriding a method in Java by using the keyword override over @Override
     *
     * @param input The Lambda Function input
     * @param context The Lambda execution environment context object.
     * @return The Lambda Function output
     */
    override fun handleRequest(input: S3Event?, context: Context?): String {
        // Drill into input using the safe call operator ?. which will not continue on in the execution if it is null
        // the ?: component is an elvis operator which is used when we have a nullable reference, i.e. firstRecord,
        // we can say "if firstRecord is not null, use it, otherwise use some non-null value or throw an exception.
        // also notice the DataProcessingException is not written in Kotlin class but is in fact Java!
        // Kotlin is fully backwards compatible with Java which is awesome because it doesn't lead to headaches trying
        // to figure out where the language differs under the hood, ex, Scala wrote their own Collection library and it
        // has caused issues in the past when interweaving the two languages
        val firstRecord = input?.records?.firstOrNull()
            ?: throw DataProcessingException("ERROR: No Records can be obtained from S3 Event!")

        // Setup our connection to S3, retrieve S3 bucket/object information and copy the source object
        // to a sink bucket then get the eTag from the resulting CopyObjectResult to return
        return setupS3Client().copyObject(firstRecord.retrieveS3Information()).eTag
    }

    /**
     * Copies an object from a source S3 bucket to a sink S3 bucket.
     *
     * This is an extension function, similar to C# and Gosu, Kotlin provides the ability to extend a class with new
     * functionality without having to inherit from the class or use any type of design pattern such as Decorator.
     * This is done via special declarations called extensions. Also, extension properties are supported too!
     *
     * @param s3Information [S3Information] which holds AWS S3 Bucket/Object information
     * @return CopyObjectResult Contains the data returned by Amazon S3 from the copyObject call
     */
    private fun AmazonS3.copyObject(s3Information: S3Information): CopyObjectResult {
        return this.copyObject(
            s3Information.sourceBucket,
            s3Information.sourceKey,
            s3Information.sinkBucket,
            s3Information.sinkKey
        )
    }

    /**
     * Extension function to take an [S3EventNotification.S3EventNotificationRecord] and pull out necessary information
     * to construct a [S3Information] object.
     *
     * @return [S3Information] built from the provided [S3EventNotification.S3EventNotificationRecord]
     */
    private fun S3EventNotification.S3EventNotificationRecord.retrieveS3Information(): S3Information {
        return S3Information(this.s3.bucket.name, this.s3.`object`.key)
    }

    /**
     * Creates a [AmazonS3] object which provides access to AWS S3 web services.
     * Note that if nothing is passed in for a parameter, lets say [serviceEndpoint] then it will automatically
     * be populated with the default [LOCAL_SERVICE_ENDPOINT] constant value.
     *
     * @param serviceEndpoint         the service endpoint either with or without the protocol
     * @param signingRegion           the region to use for SigV4 signing of requests (e.g. us-west-1)
     * @param pathStyleAccessEnabled  configures the client to use path-style access for all requests.
     * @param chunkedEncodingDisabled configures the client to disable chunked encoding for all requests
     * @return [AmazonS3] provides an interface for accessing the Amazon S3 web service.
     */
    private fun setupS3Client(
        serviceEndpoint: String = LOCAL_SERVICE_ENDPOINT,
        signingRegion: String = LOCAL_SIGNING_REGION,
        pathStyleAccessEnabled: Boolean = true,
        chunkedEncodingDisabled: Boolean = true
    ): AmazonS3 {
        return AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(serviceEndpoint, signingRegion))
            // This should be set to true when running locally running a Amazon S3 instance
            .withPathStyleAccessEnabled(pathStyleAccessEnabled)
            // Configures the client to disable chunked encoding for all requests
            // set this value to true to avoid issues with local S3 bucket testing
            .withChunkedEncodingDisabled(chunkedEncodingDisabled)
            .build()
    }

    /**
     * Data class to hold S3 bucket/object information for use by the lambda function and also
     * shows how a Plain Old Java Object (POJO) is done in Kotlin. Data Classes alleviate the need
     * for getters and setters and adds other cool functionality as well like hash code, toString, etc.
     *
     * Some things that are interesting are that we can retrieve default values in the classes parameters directly
     * and assign other parameter values to other fields! Kotlin val/var are used as too which states if the object is
     * mutable (var) or immutable/final (val) and cannot be reassigned.
     *
     * In a real world situation this should be in its own class/file but for that sake of an example we place it here.
     */
    data class S3Information(
        val sourceBucket: String,   // Must always be provided, cannot be null
        val sourceKey: String,      // Must always be provided, cannot be null
        val sinkBucket: String = System.getenv(SINK_BUCKET_ENV_KEY) ?: DEFAULT_SINK_BUCKET, // Default to OS variable
        var sinkKey: String = sourceKey // Default to sourceKey if nothing is provided
    )
}