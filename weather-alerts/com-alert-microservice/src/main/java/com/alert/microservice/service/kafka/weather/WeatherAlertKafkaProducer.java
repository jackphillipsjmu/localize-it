package com.alert.microservice.service.kafka.weather;

import com.alert.microservice.api.WeatherAlert;
import com.alert.microservice.config.weather.WeatherConstants;
import com.alert.microservice.util.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;

/**
 * Kafka producer to push {@link WeatherAlert} data to Kafka
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
 * </ul>
 */
@Component
@ConditionalOnProperty(name = WeatherConstants.ENABLE_KAFKA_PROPERTY)
public class WeatherAlertKafkaProducer {
    // Logger for info/debug logging
    private static final Logger LOG = LoggerFactory.getLogger(WeatherAlertKafkaProducer.class);

    // Values pulled from application properties
    @Value("${weather.alert.kafka.topic}")
    private String weatherAlertTopic;

    // Final variable that is injected in the service constructor.
    private final KafkaTemplate<String, WeatherAlert> kafkaTemplate;

    /**
     * Constructor for this {@link WeatherAlertKafkaProducer} object.
     *
     * @param kafkaTemplate KafkaTemplate injected by Spring to help perform Kafka operations
     */
    public WeatherAlertKafkaProducer(@Qualifier("weatherAlertKafkaTemplate") KafkaTemplate<String, WeatherAlert> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Pushes a Collection of {@link WeatherAlert} data to a Kafka Topic
     *
     * @param weatherAlerts Collection of {@link WeatherAlert} data to push
     */
    public void send(Collection<WeatherAlert> weatherAlerts) {
        CollectionUtil.streamOn(weatherAlerts.iterator()).forEach(this::send);
    }

    /**
     * Helper method to push a {@link WeatherAlert} to a Kafka Topic
     *
     * @param weatherAlert entity to push to Kafka topic
     */
    private void send(final WeatherAlert weatherAlert) {
        if (Objects.nonNull(weatherAlert)) {
            LOG.debug("Sending Weather Alert to Kafka {}",weatherAlert);
            kafkaTemplate.send(weatherAlertTopic, weatherAlert);
        }
    }
}
