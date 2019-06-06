package com.alert.microservice.service.aws;

import com.alert.microservice.api.AwsLambdaProperties;
import com.alert.microservice.api.WeatherAlert;
import com.alert.microservice.service.exception.AlertServiceException;
import com.alert.microservice.tests.AbstractMockitoTest;
import com.alert.microservice.tests.DataGenUtil;
import com.alert.microservice.util.CollectionUtil;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketNotificationConfiguration;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class S3FileServiceTest extends AbstractMockitoTest {
    private static final String BUCKET_NAME = "BUCKET";

    @Mock
    private AmazonS3 s3Client;

    @Mock
    private LambdaService lambdaService;

    @InjectMocks
    private S3FileService s3FileService;

    @Test
    public void testRetrieveBucketInputStream() throws IOException {
        final String bucketName = "bucket";
        final String bucketKey = "alerts";
        List<WeatherAlert> weatherAlerts = CollectionUtil.listOf(DataGenUtil.randomWeatherAlert(), DataGenUtil.randomWeatherAlert());
        InputStream inputStream = s3FileService.toCSVInputStream(weatherAlerts, WeatherAlert.class);
        S3Object s3Object = new S3Object();
        s3Object.setBucketName(bucketName);
        s3Object.setKey(bucketKey);
        s3Object.setObjectContent(inputStream);

        Mockito.when(s3Client.getObject(Mockito.any(GetObjectRequest.class))).thenReturn(s3Object);
        String result = inputStreamToString(s3FileService.retrieveBucketInputStream(bucketName, bucketKey));
        Assert.assertTrue(result.contains(weatherAlerts.get(0).getId()));
        Assert.assertTrue(result.contains(weatherAlerts.get(1).getId()));
    }

    @Test
    public void testToCSVInputStream() throws IOException {
        List<WeatherAlert> weatherAlerts = CollectionUtil.listOf(DataGenUtil.randomWeatherAlert(), DataGenUtil.randomWeatherAlert());
        String result = inputStreamToString(s3FileService.toCSVInputStream(weatherAlerts, WeatherAlert.class));
        Assert.assertTrue(result.contains(weatherAlerts.get(0).getId()));
        Assert.assertTrue(result.contains(weatherAlerts.get(1).getId()));
    }

    @Test(expected = AlertServiceException.class)
    public void testAppendWeatherAlertLambdaListenerNullBucketName() {
        s3FileService.appendWeatherAlertLambdaListener(null);
    }

    @Test(expected = AlertServiceException.class)
    public void testAppendWeatherAlertLambdaListenerEmptyBucketName() {
        s3FileService.appendWeatherAlertLambdaListener("");
    }

    @Test
    public void testAppendWeatherAlertLambdaListener() {
        AwsLambdaProperties lambdaProperties = new AwsLambdaProperties();
        Mockito.when(lambdaService.getWeatherAlertLambdaProperties()).thenReturn(lambdaProperties);
        s3FileService.appendWeatherAlertLambdaListener(BUCKET_NAME);
        // Verify the Lambda Service was actually called once
        Mockito.verify(lambdaService, Mockito.times(1)).bucketNotificationConfiguration(lambdaProperties);
    }

    @Test
    public void testAppendBucketNotificationLambda() {
        BucketNotificationConfiguration bucketConfig = new BucketNotificationConfiguration();
        Mockito.when(lambdaService.bucketNotificationConfiguration(Mockito.any(AwsLambdaProperties.class))).thenReturn(bucketConfig);
        s3FileService.appendBucketNotificationLambda(BUCKET_NAME, new AwsLambdaProperties());
        Mockito.verify(s3Client, Mockito.times(1)).setBucketNotificationConfiguration(BUCKET_NAME, bucketConfig);
    }

    @Test(expected = AlertServiceException.class)
    public void testCreateBucketEmptyBucketName() {
        s3FileService.createBucket("");
    }

    @Test(expected = AlertServiceException.class)
    public void testCreateBucketNullBucketName() {
        s3FileService.createBucket(null);
    }

    @Test
    public void testCreateBucket() {
        Assert.assertTrue(s3FileService.createBucket("BUCKET"));
    }

    @Test
    public void testDeleteAllBuckets() {
        s3FileService.deleteAllBuckets(false);
    }

    @Test
    public void testDeleteBucket() {
        s3FileService.deleteBucket("BUCKET", false);
    }

    @Test
    public void testBuckets() {
        List<Bucket> randomBuckets = CollectionUtil.listOf(DataGenUtil.randomBucket());
        Mockito.when(s3Client.listBuckets()).thenReturn(randomBuckets);
        Assert.assertEquals(randomBuckets.get(0).getName(), s3FileService.buckets().get(0).getName());
    }

    /**
     * Helper method to create a String from the provided {@link InputStream}
     *
     * @param inputStream abstract class is the superclass of all classes representing an input stream of bytes.
     * @return String built from each "line" of the input stream
     * @throws IOException if something bad happens processing the input stream
     */
    private String inputStreamToString(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }
}
