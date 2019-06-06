package com.alert.microservice.config.aws.lambda;

import com.alert.microservice.api.AwsLambdaProperties;
import com.alert.microservice.config.weather.WeatherConstants;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to setup AWS Lambda related configurations
 *
 * Annotations Used:
 *
 * <ul>
 *     <li>
 *         Configuration = Indicates that a class declares one or more Bean methods and
 *         may be processed by Spring.
 *     </li>
 *     <li>
 *         ConfigurationProperties = Annotation for externalized configuration. Add this to a class definition or a
 *         Bean method in a Configuration class if you want to bind and validate some external
 *         Properties (e.g. from a .properties file).
 *     </li>
 *     <li>
 *         Bean = Indicates that a method produces a bean to be managed by the Spring container.
 *     </li>
 *     <li>
 *         EnableConfigurationProperties = Enable support for {@link ConfigurationProperties} annotated beans.
 *     </li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties
public class LambdaConfig {

    /**
     * Creates a Bean to be used by underlying Lambda service logic.
     *
     * @return AwsLambdaProperties object to hold AWS Lambda related information
     */
    @Bean
    @ConfigurationProperties(prefix = WeatherConstants.LAMBDA_PREFIX)
    public AwsLambdaProperties awsLambdaProperties() {
        return new AwsLambdaProperties();
    }

    /**
     * Creates the primary interface for accessing and interacting with AWS Lambda.
     *
     * @param lambdaProperties used to setup the return object
     * @return Interface for accessing AWS Lambda.
     */
    @Bean
    public AWSLambda awsLambda(AwsLambdaProperties lambdaProperties) {
        return AWSLambdaClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(lambdaProperties.getUrl(), lambdaProperties.getRegion()))
                .build();
    }
}
