package com.alert.microservice.config.weather;

/**
 * Interface to hold constant values for use by underlying service classes.
 * By default, these values are static and final by default in Java.
 */
public interface WeatherConstants {
    // Configuration property constant
    String ENABLE_KAFKA_PROPERTY = "weather.alert.kafka.enabled";
    String ENABLE_SCHEDULED_TASK = "weather.alert.scheduler.enabled";
    // Below constants are used in @Value annotations
    String ENABLE_KAFKA_VALUE_PROPERTY = "${weather.alert.kafka.enabled}";
    String ENABLE_ELASTICSEARCH_PROPERTY = "${weather.alert.elasticsearch.enabled}";
    String TOPIC_PROPERTY = "${weather.alert.kafka.topic}";
    // Lambda
    String LAMBDA_PREFIX = "weather.alert.lambda";
    // S3
    String S3_PREFIX = "weather.alert.s3";
    // Scheduled Processing
    String FIXED_DELAY_PROPERTY = "${fixedDelay.in.milliseconds}";
    String INIT_DELAY_PROPERTY = "${initialDelay.in.milliseconds}";
}
