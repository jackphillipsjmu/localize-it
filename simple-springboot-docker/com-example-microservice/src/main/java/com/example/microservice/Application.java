package com.example.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javax.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * Main entry point for application.
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