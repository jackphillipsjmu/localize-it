package com.alert.microservice.config.weather.kafka;

import com.alert.microservice.api.WeatherAlert;
import com.alert.microservice.config.weather.WeatherConstants;
import com.alert.microservice.service.kafka.avro.GenericAvroDeserializer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class to setup Kafka Weather Alert Consumers
 *
 * Annotations Used:
 *
 * <ul>
 *     <li>
 *         Configuration = Indicates that a class declares one or more Bean methods and
 *         may be processed by Spring.
 *     </li>
 *      <li>
 *         EnableKafka = Enable Kafka listener annotated endpoints.
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
@EnableKafka
@ConditionalOnProperty(name = WeatherConstants.ENABLE_KAFKA_PROPERTY)
public class WeatherAlertConsumerConfig {

    @Value("${weather.alert.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String kafkaConsumerGroupId;

    /**
     * Establishes Kafka consumer configuration map/properties
     *
     * @return Map of String Keys to Object values representing consumer properties
     */
    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, GenericAvroDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerGroupId);

        return props;
    }

    /**
     * Kafka Consumer Factory Bean for {@link WeatherAlert}
     *
     * @return ConsumerFactory Kafka strategy to produce a {@link Consumer} instance(s).
     */
    @Bean
    public ConsumerFactory<String, WeatherAlert> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(),
                new GenericAvroDeserializer<>(WeatherAlert.class));
    }

    /**
     * Factory Bean for Kafka Consumer/Listener containers
     *
     * @return {@link KafkaListenerContainerFactory} implementation to build a ConcurrentMessageListenerContainer.
     * This should be the default for most users and a good transition paths for those that are used to building such
     * container definitions manually.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, WeatherAlert> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, WeatherAlert> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
