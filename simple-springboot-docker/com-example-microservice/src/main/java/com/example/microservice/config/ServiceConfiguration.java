package com.example.microservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for underlying services.
 *
 * Annotations Explained:
 * <ul>
 *     <li>
 *         Configuration = Indicates that a class declares one or more Bean methods and
 *         may be processed by Spring.
 *     </li>
 *     <li>
 *         Bean = Indicates that a method produces a bean to be managed by the Spring container.
 *     </li>
 * </ul>
 */
@Configuration
public class ServiceConfiguration {
    /**
     * Default {@link RestTemplate} to be used for HTTP communication. You can customize this with
     * different request interceptors when needed like when you would like to append Authorization
     * parameters to each request.
     *
     * @return RestTemplate Spring's central class for synchronous client-side HTTP access.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
