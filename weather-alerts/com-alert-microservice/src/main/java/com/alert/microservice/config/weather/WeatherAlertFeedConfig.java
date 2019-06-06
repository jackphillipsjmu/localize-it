package com.alert.microservice.config.weather;

import com.alert.microservice.service.weather.WeatherAlertFeedProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * Configuration class to establish Weather Alert Feed related configurations
 *
 * Annotations Used:
 *
 * <ul>
 *     <li>
 *         Configuration = Indicates that a class declares one or more Bean methods and
 *         may be processed by Spring.
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
public class WeatherAlertFeedConfig {

    /**
     * Creates a {@link WeatherAlertFeedProcessor} that is available to the underlying service for use
     * processing alert data/.
     *
     * @param feedUrl String URL to Atom feed
     * @param capFieldSet Set of String values to dictate what we should extract from the feed records
     * @return WeatherAlertFeedProcessor handles transforming Weather Alert Atom feed data into a objects that the
     * application can more easily interface with.
     */
    @Bean
    public WeatherAlertFeedProcessor weatherAlertFeedProcessor(@Value("${weather.alert.feed.url}") String feedUrl,
                                                               @Value("${weather.alert.cap.fields}") Set<String> capFieldSet) {
        return new WeatherAlertFeedProcessor(feedUrl, capFieldSet);
    }
}
