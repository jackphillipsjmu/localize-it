package com.alert.microservice.service.aws;

import com.alert.microservice.api.AwsLambdaProperties;
import com.alert.microservice.service.exception.AlertServiceException;
import com.alert.microservice.util.CollectionUtil;
import com.alert.microservice.util.CommonUtil;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Service to perform AWS S3 related operations
 *
 * Annotations Explained:
 *
 * <ul>
 *     <li>
 *         Service = Indicates that an annotated class is a "Service".
 *     </li>
 * </ul>
 */
@Service
public class S3FileService {
    // Logger for info/debug purposes
    private static final Logger LOG = LoggerFactory.getLogger(S3FileService.class);
    // Final variables that are injected in the service constructor
    private final AmazonS3 s3Client;
    private final LambdaService lambdaService;

    /**
     * Constructor for this {@link S3FileService}
     *
     * @param s3Client      Provides an interface for accessing the Amazon S3 web service.
     * @param lambdaService Service performs AWS Lambda related operations
     */
    public S3FileService(AmazonS3 s3Client, LambdaService lambdaService) {
        this.s3Client = s3Client;
        this.lambdaService = lambdaService;
    }

    /**
     * Retrieves a file from an S3 bucket and creates an {@link InputStream} from its contents.
     *
     * @param sourceBucket name of the S3 bucket to pull input stream from
     * @param sourceKey    the key of the object to retrieve and create an input stream
     * @return {@link InputStream} constructed from contents contained in the provided S3 file
     */
    public InputStream retrieveBucketInputStream(final String sourceBucket, final String sourceKey) {
        return s3Client.getObject(new GetObjectRequest(sourceBucket, sourceKey)).getObjectContent();
    }

    /**
     * Transforms the provided collection of objects into a CSV representation with schema/header and write it to
     * an input stream.
     *
     * @param contentCollection Collection of T to convert to CSV and push to input stream
     * @param clazz             Class to help extract CSV schema/header info and parse field properties
     * @param <T>               Generic Type
     * @return InputStream represents an input stream of bytes
     * @throws JsonProcessingException
     */
    public <T> InputStream toCSVInputStream(final Collection<T> contentCollection, final Class<T> clazz) throws JsonProcessingException {
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema csvSchema = csvMapper.schemaFor(clazz);
        ObjectWriter writer = csvMapper.writerWithSchemaFor(clazz);
        StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());

        // Grab CSV Header and add to String Joiner
        final String header = String.join(",", CollectionUtil.streamOn(csvSchema.iterator())
                .map(CsvSchema.Column::getName)
                .toArray(String[]::new));
        stringJoiner.add(header);

        // Loop through entries and add to String joiner
        for (T entry : contentCollection) {
            stringJoiner.add(writer.writeValueAsString(entry));
        }

        // Create input stream from built string
        return new ByteArrayInputStream(stringJoiner.toString().getBytes());
    }

    /**
     * Uploads the provided {@link MultipartFile} to the specified S3 bucket
     *
     * @param multipartFile A representation of an uploaded file received in a multipart request.
     * @param bucketName String S3 bucket to put file into
     * @return PutObjectResult Contains the data returned by Amazon S3 from the putObject operation.
     * @throws IOException
     */
    public PutObjectResult uploadToBucket(final MultipartFile multipartFile, final String bucketName) throws IOException {
        return uploadInputStream(multipartFile.getInputStream(), bucketName, multipartFile.getOriginalFilename());
    }

    /**
     * Uploads the provided {@link InputStream} to the specified S3 bucket with it's name corresponding to the bucket
     * key value.
     *
     * @param inputStream represents an input stream of bytes
     * @param bucketName String S3 bucket to put file into
     * @param bucketKey String S3 "filename" of data to push into bucket
     * @return PutObjectResult Contains the data returned by Amazon S3 from the putObject operation.
     */
    public PutObjectResult uploadInputStream(final InputStream inputStream, final String bucketName, final String bucketKey) {
        // Create bucket if it does not exist already
        createBucket(bucketName);
        // Instantiate empty metadata for upload
        ObjectMetadata metadata = new ObjectMetadata();
        // Put data in S3
        return s3Client.putObject(bucketName, bucketKey, inputStream, metadata);
    }

    /**
     * Appends a bucket configuration to the specified bucket to listen for ObjectCreated S3 Events and act upon them.
     *
     * @param bucketName String name of bucket to append notification configuration too
     */
    public void appendWeatherAlertLambdaListener(final String bucketName) {
        appendBucketNotificationLambda(bucketName, lambdaService.getWeatherAlertLambdaProperties());
    }

    /**
     * Appends a bucket configuration to the specified bucket to listen for ObjectCreated S3 Events and act upon them.
     *
     * @param bucketName String name of bucket to append notification configuration too
     * @param lambdaProperties {@link AwsLambdaProperties} that holds AWS Lambda related information
     */
    public void appendBucketNotificationLambda(final String bucketName, final AwsLambdaProperties lambdaProperties) {
        // Sanity checks
        CommonUtil.ifEmptyThrowException(bucketName, new AlertServiceException("Cannot append Lambda Listener to a null/empty bucket"));
        CommonUtil.ifNullThrowException(lambdaProperties, new AlertServiceException("Cannot append Bucket Notification Lambda from Null Property object"));
        LOG.debug("Bucket Name {}, Properties {}", bucketName, lambdaProperties);
        s3Client.setBucketNotificationConfiguration(bucketName, lambdaService.bucketNotificationConfiguration(lambdaProperties));
    }

    /**
     * Creates the provided S3 bucket IF it does NOT exist already.
     *
     * @param bucketName String bucket to create
     * @return boolean true if the bucket was created, false otherwise
     */
    public boolean createBucket(final String bucketName) {
        // Check if the bucket name is populated, if it is NOT then throw an exception
        CommonUtil.ifEmptyThrowException(bucketName, new AlertServiceException("Cannot create S3 Bucket with null/empty name!"));
        // Set return value initially to false
        boolean bucketCreated = false;
        // If the S3 bucket does NOT exist then create it, otherwise, do nothing
        if (!s3Client.doesBucketExistV2(bucketName)) {
            s3Client.createBucket(bucketName);
            // set bucket created to true
            bucketCreated = true;
        }
        return bucketCreated;
    }

    /**
     * Deletes all known AWS S3 Buckets.
     *
     * DO NOT USE THIS OUTSIDE OF TESTING SCENARIOS!
     *
     * @param forceDelete boolean to dictate if we should force removal or not. If the bucket is NOT empty and we do
     *                    NOT force deletion an error will be thrown.
     */
    public void deleteAllBuckets(final boolean forceDelete) {
        buckets().forEach(bucket -> deleteBucket(bucket.getName(), forceDelete));
    }

    /**
     * Deletes a AWS S3 Bucket with the specified name.
     *
     * @param bucketName  String bucket name to remove
     * @param forceDelete boolean to dictate if we should force removal or not. If the bucket is NOT empty and we do
     *                    NOT force deletion an error will be thrown.
     */
    public void deleteBucket(final String bucketName, final boolean forceDelete) {
        if (forceDelete) {
            forceDeleteS3Bucket(bucketName);
        } else {
            s3Client.deleteBucket(bucketName);
        }
    }

    /**
     * Returns a List of S3 {@link Bucket} objects
     *
     * @return List of {@link Bucket} objects which represent an Amazon S3 bucket
     */
    public List<Bucket> buckets() {
        return s3Client.listBuckets();
    }

    /**
     * Returns a List of S3 String Bucket Names
     *
     * @return List of S3 Bucket names
     */
    public List<String> bucketNames() {
        return buckets().stream().map(Bucket::getName).collect(Collectors.toList());
    }

    /**
     * Force deletes bucket by removing all contents held in the bucket then removing the bucket itself.
     *
     * @param bucketName String name of bucket to force deletion of
     */
    private void forceDeleteS3Bucket(final String bucketName) {
        // Delete all objects from the bucket. This is sufficient
        // for un-versioned buckets. For versioned buckets, when you attempt to delete objects, Amazon S3 inserts
        // delete markers for all objects, but doesn't delete the object versions.
        // To delete objects from versioned buckets, delete all of the object versions before deleting
        // the bucket (see below for an example).
        ObjectListing objectListing = s3Client.listObjects(bucketName);
        while (true) {
            for (S3ObjectSummary s3ObjectSummary : objectListing.getObjectSummaries()) {
                s3Client.deleteObject(bucketName, s3ObjectSummary.getKey());
            }
            // If the bucket contains many objects, the listObjects() call
            // might not return all of the objects in the first listing. Check to
            // see whether the listing was truncated. If so, retrieve the next page of objects
            // and delete them.
            if (objectListing.isTruncated()) {
                objectListing = s3Client.listNextBatchOfObjects(objectListing);
            } else {
                break;
            }
        }
        // Delete the actual bucket
        s3Client.deleteBucket(bucketName);
    }
}
