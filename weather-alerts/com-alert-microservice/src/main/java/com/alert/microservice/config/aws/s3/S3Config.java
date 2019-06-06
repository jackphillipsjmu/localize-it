package com.alert.microservice.config.aws.s3;

import com.alert.microservice.api.S3Properties;
import com.alert.microservice.config.weather.WeatherConstants;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to setup AWS S3 related configurations
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
 *         {@code @Bean} method in a {@code @Configuration} class if you want to bind and validate some external
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
public class S3Config {

    /**
     * Creates Bean to use in underlying service layer to easily access information about what we will be operating
     * on in S3.
     *
     * @return S3Properties object to hold S3 related information for source/sink processing
     */
    @Bean
    @ConfigurationProperties(prefix = WeatherConstants.S3_PREFIX)
    public S3Properties s3Properties() {
        return new S3Properties();
    }

    /**
     * Creates a {@link AmazonS3} object which provides access to AWS S3 web services.
     *
     * @param s3Properties {@link S3Properties}
     * @return {@link AmazonS3} provides an interface for accessing the Amazon S3 web service.
     */
    @Bean
    public AmazonS3 setupAmazonS3(S3Properties s3Properties) {
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(s3Properties.getUrl(), s3Properties.getRegion()))
                // This should be set to true when running locally running a Amazon S3 instance
                .withPathStyleAccessEnabled(s3Properties.getPathStyleAccessEnabled())
                // Configures the client to disable chunked encoding for all requests
                // set this value to true to avoid issues with local S3 bucket testing
                .withChunkedEncodingDisabled(s3Properties.getPathStyleAccessEnabled())
                .build();
    }
}
