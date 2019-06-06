package com.alert.microservice.service.weather;

import com.alert.microservice.api.AlertProcessingResult;
import com.alert.microservice.api.S3Properties;
import com.alert.microservice.service.aws.LambdaService;
import com.alert.microservice.service.elasticsearch.ElasticsearchService;
import com.alert.microservice.service.aws.S3FileService;
import com.alert.microservice.service.exception.AlertServiceException;
import com.alert.microservice.service.kafka.KafkaService;
import com.alert.microservice.api.WeatherAlert;
import com.amazonaws.AmazonWebServiceResult;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;

/**
 * Service class to handle weather alert related functions for this application
 * <p>
 * Annotations Explained:
 *
 * <ul>
 *  <li>
 *      Service = Indicates that an annotated class is a "Service".
 *  </li>
 *  <li>
 *      Value = Annotation at the field or method/constructor parameter level
 *      that indicates a default value expression for the affected argument.
 *  </li>
 * </ul>
 */
@Service
public class WeatherAlertService {
    // Logger for info/debug purposes
    private static final Logger LOG = LoggerFactory.getLogger(WeatherAlertService.class);

    // Values are declared in the properties file of this service
    @Value("${weather.alert.elastic.index}")
    private String elasticsearchWeatherIndex;

    // Final variables that are injected in the service constructor
    private final ElasticsearchService elasticsearchService;
    private final KafkaService kafkaService;
    private final S3FileService s3FileService;
    private final LambdaService lambdaService;
    private final S3Properties s3Properties;
    private final WeatherAlertFeedProcessor weatherAlertFeedProcessor;

    /**
     * Constructor for this {@link WeatherAlertService} class.
     *
     * @param elasticsearchService      ElasticsearchService to perform ES operations
     * @param kafkaService              KafkaService to perform Kafka operations
     * @param s3FileService             S3FileService to perform AWS S3 related operations
     * @param lambdaService             LamdaService to perform AWS Lambda related operations
     * @param s3Properties              S3Properties object to hold S3 related information for source/sink processing
     * @param weatherAlertFeedProcessor handles transforming Weather Alert Atom feed data into a objects that the
     *                                  application can more easily interface with.
     */
    public WeatherAlertService(ElasticsearchService elasticsearchService,
                               KafkaService kafkaService,
                               S3FileService s3FileService,
                               LambdaService lambdaService,
                               S3Properties s3Properties,
                               WeatherAlertFeedProcessor weatherAlertFeedProcessor) {
        this.elasticsearchService = elasticsearchService;
        this.kafkaService = kafkaService;
        this.s3FileService = s3FileService;
        this.lambdaService = lambdaService;
        this.s3Properties = s3Properties;
        this.weatherAlertFeedProcessor = weatherAlertFeedProcessor;
    }

    /**
     * Searches the Weather alert Elasticsearch index using data in the provided {@link WeatherAlert}
     *
     * @param weatherAlert  WeatherAlert to extract information from
     * @param isFuzzySearch Boolean to dictate whether to use fuzzy searching on ES data or not
     * @return Collection of WeatherAlert data pulled from the results of the Elasticsearch query
     * @throws IOException
     */
    public Collection<WeatherAlert> searchWeatherAlerts(final WeatherAlert weatherAlert, final Boolean isFuzzySearch) throws IOException {
        // If Elasticsearch is not enabled then throw exception
        elasticsearchService.ifNotEnabledThrow(new AlertServiceException("Cannot Search Weather Alerts when Elasticsearch is NOT enabled"));
        return elasticsearchService.search(weatherAlert, elasticsearchWeatherIndex, isFuzzySearch, WeatherAlert.class);
    }

    /**
     * Retrieves Elasticsearch Weather Alert data and limits the result size to the provided limit.
     *
     * @param limit int the number of results to return
     * @return Collection of Weather Alert index data
     * @throws IOException   Generally occurs when data cannot be processed correctly when being passed around
     */
    public Collection<WeatherAlert> retrieveElasticsearchData(final int limit) throws IOException {
        // If Elasticsearch is not enabled then throw exception
        elasticsearchService.ifNotEnabledThrow(new AlertServiceException("Cannot Select All Weather Alerts when Elasticsearch is NOT enabled"));
        return elasticsearchService.selectAll(elasticsearchWeatherIndex, limit, WeatherAlert.class);
    }

    /**
     * Runs the full E2E process for this service which performs the following:
     * - Pull in Weather Alert Atom Feed
     * - Transform and send the to Kafka which is then picked up by a Kafka consumer that will then push the data to ES
     * - Transforms the Weather Alert data again into CSV format and pushes it to an S3 source bucket. This source
     * bucket has a AWS Lambda function tied to it to copy the contents placed their into a predefined sink bucket.
     *
     * @throws IOException   Generally occurs when data cannot be processed correctly when being passed around
     */
    public void executeEndToEndProcess() throws IOException {
        LOG.debug("Executing End-to-End Process");
        kafkaService.ifNotEnabledThrow(new AlertServiceException("Cannot execute End-to-End Process when Kafka is NOT enabled"));
        // Pull in feed data and transform to a collection of weather alert objects
        Collection<WeatherAlert> weatherAlerts = weatherAlertFeedProcessor.process();
        // Perform Kafka -> Elasticsearch portion of process
        AlertProcessingResult kafkaAlertProcessingResult = kafkaService.pushWeatherAlerts(weatherAlerts);
        LOG.debug("Kafka Processing Result = {}", kafkaAlertProcessingResult);
        // AWS S3/Lambda
        executeS3Process(weatherAlerts);
    }

    /**
     * Pulls in weather alert Atom Feed, transform and send the to Kafka which is then picked up by a Kafka
     * consumer that will then push the data to Elasticsearch.
     *
     * @return AlertProcessingResult object that contains summary data about processing
     */
    public AlertProcessingResult processAlertFeed() {
        LOG.debug("Processing Weather Alert Feed and Pushing to Kafka");
        // Ensure Kafka is enabled before continuing
        kafkaService.ifNotEnabledThrow(new AlertServiceException("Cannot Process Weather Alert Feed when Kafka is NOT enabled"));
        // Create the data feed and extract collection of weather alerts then push alerts to Kafka
        return kafkaService.pushWeatherAlerts(weatherAlertFeedProcessor.process());
    }

    /**
     * Executes the S3 portion of the data process which reads a feed of Weather Alert data into a
     * Collection of {@link WeatherAlert} objects, creates a CSV representation and pushes it to S3.
     * This S3 bucket has a Lambda tied to it as well that will copy the data from the source bucket to the sink bucket.
     *
     * @throws IOException   Generally occurs when data cannot be processed correctly when being passed around
     */
    public void executeS3Process() throws IOException {
        // Pull in feed data and transform to a collection of weather alert objects
        executeS3Process(weatherAlertFeedProcessor.process());
    }

    /**
     * Executes the S3 portion of the data process which takes the provided Collection of {@link WeatherAlert}
     * data, creates a CSV representation and pushes it to S3. This S3 bucket has a Lambda tied to it as well that
     * will copy the data from the source bucket to the sink bucket.
     *
     * @param weatherAlerts Collection of {@link WeatherAlert} data to push to S3
     * @throws IOException   Generally occurs when data cannot be processed correctly when being passed around
     */
    private void executeS3Process(Collection<WeatherAlert> weatherAlerts) throws IOException {
        // Create an input stream from the alerts
        InputStream inputStream = s3FileService.toCSVInputStream(weatherAlerts, WeatherAlert.class);
        // Create AWS Lambda Function
        AmazonWebServiceResult createFunctionResult = lambdaService.createLambdaFunction();
        LOG.debug("Lambda Create Result = {}", createFunctionResult);
        // Create AWS Source and Sink Buckets
        s3FileService.createBucket(s3Properties.getSourceBucket());
        s3FileService.createBucket(s3Properties.getSinkBucket());
        // Configure source bucket with Lambda function
        s3FileService.appendWeatherAlertLambdaListener(s3Properties.getSourceBucket());
        // Create file name for S3 object using the current millis since epoch
        final String bucketKey = "weather-alert-" + new Date().getTime() + ".csv";
        // Push Alert data to S3 Source bucket
        PutObjectResult putObjectResult = s3FileService.uploadInputStream(inputStream, s3Properties.getSourceBucket(), bucketKey);
        LOG.debug("Put Object Result = {}", putObjectResult);
    }
}
