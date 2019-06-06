package com.alert.microservice.service.kafka;

import com.alert.microservice.api.AlertProcessingResult;
import com.alert.microservice.api.Status;
import com.alert.microservice.api.WeatherAlert;
import com.alert.microservice.config.weather.WeatherConstants;
import com.alert.microservice.service.ToggleComponent;
import com.alert.microservice.service.kafka.weather.WeatherAlertKafkaProducer;
import com.alert.microservice.util.CollectionUtil;
import org.apache.avro.reflect.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

/**
 * Service class to help Kafka related operations.
 *
 * Annotations Explained:
 *
 * <ul>
 *     <li>
 *         Service = Indicates that an annotated class is a "Service".
 *     </li>
 *     <li>
 *         Value = Annotation at the field or method/constructor parameter level
 *         that indicates a default value expression for the affected argument.
 *     </li>
 * </ul>
 */
@Service
public class KafkaService extends ToggleComponent {
    // Logger for info/debug purposes
    private static final Logger LOG = LoggerFactory.getLogger(KafkaService.class);

    // Value from properties to dictate if Kafka is enabled or not
    @Value(WeatherConstants.ENABLE_KAFKA_VALUE_PROPERTY)
    private Boolean kafkaIsEnabled;

    // Final variable that is injected in the service constructor.
    // Note that this can be null!
    private final WeatherAlertKafkaProducer kafkaProducer;

    @Override
    public boolean isEnabled() {
        return kafkaIsEnabled;
    }

    /**
     * Constructor for this {@link KafkaService} service instance
     *
     * @param kafkaProducer WeatherAlertKafkaProducer nullable Kafka Producer that helps push data to Kafka
     */
    public KafkaService(@Nullable WeatherAlertKafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    /**
     * Pushes Collection of {@link WeatherAlert} objects to Kafka topic if enabled
     *
     * @param weatherAlerts Collection of {@link WeatherAlert} objects to push to Kafka topic
     * @return AlertProcessingResult object that contains summary data about processing
     */
    public AlertProcessingResult pushWeatherAlerts(final Collection<WeatherAlert> weatherAlerts) {
        // If Kafka is enabled and we have data to push then do so
        if (kafkaIsEnabled && CollectionUtil.isNotEmpty(weatherAlerts)) {
            kafkaProducer.send(weatherAlerts);
        }
        return createProcessingResult(weatherAlerts);
    }

    /**
     * Creates a {@link AlertProcessingResult} after inspecting the provided Collection of {@link WeatherAlert} data.
     *
     * @param weatherAlerts Collection of {@link WeatherAlert} data to check
     * @return AlertProcessingResult object that contains summary data about processing
     */
    private AlertProcessingResult createProcessingResult(final Collection<WeatherAlert> weatherAlerts) {
        AlertProcessingResult alertProcessingResult = new AlertProcessingResult();
        alertProcessingResult.setId(UUID.randomUUID().toString());
        alertProcessingResult.setAlertsProcessed(CollectionUtil.size(weatherAlerts));
        alertProcessingResult.setStatus((CollectionUtil.isNotEmpty(weatherAlerts)) ? Status.SUCCESS : Status.UNPROCESSED);
        alertProcessingResult.setTimestamp(new Date());
        // Log result if in debug mode
        LOG.debug("Kafka Alert {} (Kafka Enabled = {})", alertProcessingResult, kafkaIsEnabled);

        return alertProcessingResult;
    }
}
