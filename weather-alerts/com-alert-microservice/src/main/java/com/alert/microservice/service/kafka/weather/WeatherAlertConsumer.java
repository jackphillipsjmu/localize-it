package com.alert.microservice.service.kafka.weather;

import com.alert.microservice.api.WeatherAlert;
import com.alert.microservice.config.weather.WeatherConstants;
import com.alert.microservice.service.elasticsearch.ElasticsearchService;
import org.elasticsearch.action.index.IndexResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka Consumer that handles consuming and processing of Kafka {@link WeatherAlert} messages
 *
 * Annotations Used:
 *
 * <ul>
 *     <li>
 *         Component = Indicates that an annotated class is a "component". Such classes are considered as candidates
 *         for auto-detection when using annotation-based configuration and classpath scanning.
 *     </li>
 *     <li>
 *         ConditionalOnProperty = Checks if the specified properties have a specific value a way we can disable
 *         certain things from being setup in the Application Context.
 *     </li>
 *     <li>
 *         Value = Annotation at the field or method/constructor parameter level that indicates a default value
 *         expression for the affected argument.
 *     </li>
 *     <li>
 *         KafkaListener = Annotation that marks a method to be the target of a Kafka message listener on the
 *         specified topics.
 *     </li>
 * </ul>
 */
@Component
@ConditionalOnProperty(name = WeatherConstants.ENABLE_KAFKA_PROPERTY)
public class WeatherAlertConsumer {
    // Logger for info/debug purposes
    private static final Logger LOG = LoggerFactory.getLogger(WeatherAlertConsumer.class);

    // Value from properties to dictate if Elasticsearch is enabled or not
    @Value(WeatherConstants.ENABLE_ELASTICSEARCH_PROPERTY)
    private Boolean elasticsearchIsEnabled;

    // What Elasticsearch index we should populate
    @Value("${weather.alert.elastic.index}")
    private String elasticsearchWeatherIndex;

    // Final variables that are injected in the service constructor
    private final ElasticsearchService elasticsearchService;

    /**
     * Constructor for this {@link WeatherAlertConsumer}
     *
     * @param elasticsearchService service class that serves up ES related functionality
     */
    public WeatherAlertConsumer(ElasticsearchService elasticsearchService) {
        this.elasticsearchService = elasticsearchService;
    }

    /**
     * Listener method to take in Weather Alert messages from Kafka and process them
     *
     * @param weatherAlert WeatherAlert that is pulled in from Kafka topic
     */
    @KafkaListener(topics = WeatherConstants.TOPIC_PROPERTY)
    public void receive(WeatherAlert weatherAlert) {
        LOG.debug("Weather Alert Kafka Consumer received {}", weatherAlert);
        // Push data from Kafka to Elasticsearch, for you know, searching.
        pushToElasticsearch(weatherAlert);
    }

    /**
     * Pushes the provided {@link WeatherAlert} to the pre-configured Elasticsearch index.
     *
     * @param weatherAlert WeatherAlert to push to Elasticsearch
     */
    private void pushToElasticsearch(final WeatherAlert weatherAlert) {
        if (elasticsearchService.isEnabled()) {
            IndexResponse indexResponse = elasticsearchService.index(weatherAlert, weatherAlert.getId(), elasticsearchWeatherIndex);
            LOG.debug("Elasticsearch Index Response {}", indexResponse);
        }
    }
}
