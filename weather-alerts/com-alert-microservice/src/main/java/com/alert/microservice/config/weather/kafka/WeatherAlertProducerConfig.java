package com.alert.microservice.config.weather.kafka;

import com.alert.microservice.api.WeatherAlert;
import com.alert.microservice.config.weather.WeatherConstants;
import com.alert.microservice.service.kafka.avro.GenericAvroSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class to setup Kafka Weather Alert Producers
 *
 * Annotations Used:
 *
 * <ul>
 *     <li>
 *         Configuration = Indicates that a class declares one or more Bean methods and
 *         may be processed by Spring.
 *     </li>
 *     <li>
 *         ConditionalOnProperty = Checks if the specified properties have a specific value a way we can disable
 *         certain things from being setup in the Application Context.
 *     </li>
 *     <li>
 *         Bean = Indicates that a method produces a bean to be managed by the Spring container.
 *     </li>
 *     <li>
 *         Value = Annotation at the field or method/constructor parameter level that indicates a default value
 *         expression for the affected argument.
 *     </li>
 * </ul>
 */
@Configuration
@ConditionalOnProperty(name = WeatherConstants.ENABLE_KAFKA_PROPERTY)
public class WeatherAlertProducerConfig {

    @Value("${weather.alert.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Establishes Kafka Producer configuration map/properties
     *
     * @return Map of String Keys to Object values representing Producer properties
     */
    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GenericAvroSerializer.class);

        return properties;
    }

    /**
     * Creates a ProducerFactory with the underlying implementation being {@link DefaultKafkaProducerFactory}
     *
     * @return ProducerFactory strategy to produce a Producer instance(s).
     */
    @Bean
    public ProducerFactory<String, WeatherAlert> weatherAlertKafkaProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    /**
     * Creates a {@link KafkaTemplate} for use with Weather Alert Kafka operations.
     *
     * @return KafkaTemplate template for executing high-level Kafka operations
     */
    @Bean(name = "weatherAlertKafkaTemplate")
    public KafkaTemplate<String, WeatherAlert> kafkaWeatherAlertKafkaTemplate() {
        return new KafkaTemplate<>(weatherAlertKafkaProducerFactory());
    }
}
