package com.alert.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javax.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * Main entry point for application.
 *
 * Annotations Used:
 *
 * <ul>
 *     <li>
 *         SpringBootApplication = Convenience annotation that indicates a Configuration class that declares one or
 *         more Bean methods and triggers auto-configuration and component scanning.
 *     </li>
 *      <li>
 *         PostConstruct = Used on a method that needs to be executed after dependency injection is done to perform any
 *         initialization. This method MUST be invoked before the class is put into service.
 *     </li>
 * </ul>
 */
@SpringBootApplication
public class Application {
    // Class level constants
    private static final String UTC = "UTC";

    /**
     * Sets the default timezone to UTC explicitly.
     */
    @PostConstruct
    void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(UTC));
    }

    /**
     * Main method that kicks off Spring Boot.
     *
     * @param args String array of arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}