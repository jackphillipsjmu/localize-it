package com.example.microservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Class intended to show an example Spring Boot Service.
 *
 * Annotations Explained:
 * <ul>
 *     <li>
 *         Service = Indicates that an annotated class is a "Service".
 *     </li>
 *     <li>
 *         Value = Annotation at the field or method/constructor parameter level
 *         that indicates a default value expression for the affected argument.
 *     </li>
 *     <li>
 *         Autowired = Marks a constructor, field, setter method or config method
 *         as to be autowired by Spring's dependency injection facilities.
 *     </li>
 * </ul>
 */
@Service
public class ExampleService {
    // Pulled from application.properties
    @Value("${example.get.url}")
    private String exampleGetUrl;

    // Helps to perform HTTP calls, is injected from Configuration class
    private final RestTemplate restTemplate;

    /**
     * Example Service Constructor.
     *
     * Since the Spring Team recommends: "Always use constructor based dependency injection in your beans" we've done
     * that here. You can however Autowire a Bean like so,
     *
     * @Autowired
     * private FooClass foo;
     *
     * @param restTemplate Spring's central class for synchronous client-side HTTP access.
     */
    @Autowired
    public ExampleService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Example that uses Rest Template to perform a GET request.
     *
     * @return String response from GET request
     */
    public String executeGetRequest() {
        return restTemplate.getForEntity(exampleGetUrl, String.class).getBody();
    }
}
