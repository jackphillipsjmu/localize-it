package com.alert.microservice.config.weather;

import com.alert.microservice.service.exception.AlertServiceException;
import com.alert.microservice.service.weather.WeatherAlertService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;

/**
 * Configuration class to establish Scheduler tasks in this service
 *
 * Annotations Used:
 *
 * <ul>
 *     <li>
 *         Configuration = Indicates that a class declares one or more Bean methods and
 *         may be processed by Spring.
 *     </li>
 *      <li>
 *         EnableScheduling = Enables Spring's scheduled task execution capability
 *     </li>
 *     <li>
 *         ConditionalOnProperty = Checks if the specified properties have a specific value a way we can disable
 *         certain things from being setup in the Application Context.
 *     </li>
 *     <li>
 *         Scheduled = An annotation that marks a method to be scheduled.
 *     </li>
 * </ul>
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = WeatherConstants.ENABLE_SCHEDULED_TASK)
public class SchedulerConfig {
    // Final variables that are injected in the constructor
    private final WeatherAlertService weatherAlertService;

    /**
     * Constructor for this Configuration class that will establish scheduled tasks for service.
     *
     * @param weatherAlertService Service class to handle weather alert related functions for this application
     */
    public SchedulerConfig(WeatherAlertService weatherAlertService) {
        this.weatherAlertService = weatherAlertService;
    }

    /**
     * Establishes scheduled task that is setup in the application properties. This will retrieve Weather Alert Data
     * at a configured time to poll data and process it.
     *
     * fixedDelayString property makes sure that there is a delay of n millisecond between the finish time of an
     * execution of a task and the start time of the next execution of the task.
     *
     * initialDelayString tells us that the task will be executed a first time after the initial delay value and it
     * will continue to be executed according to the fixed delay.
     */
    @Scheduled(fixedDelayString = WeatherConstants.FIXED_DELAY_PROPERTY, initialDelayString = WeatherConstants.INIT_DELAY_PROPERTY)
    public void processWeatherAlertData() {
        try {
            weatherAlertService.executeEndToEndProcess();
        } catch (IOException e) {
            e.printStackTrace();
            throw new AlertServiceException("Cannot Process Scheduled Weather Alerts!", e);
        }
    }
}
