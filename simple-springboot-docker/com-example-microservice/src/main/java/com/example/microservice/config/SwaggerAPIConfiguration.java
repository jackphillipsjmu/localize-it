package com.example.microservice.config;

import com.example.microservice.util.CommonUtil;
import com.google.common.base.Predicates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Sets up Swagger UI and can provide customized components and information.
 * Swagger definitions can help not only for documentation purposes but testing as well
 * because it can define our API(s) in a consistent format.
 *
 * <ul>
 *     <li>
 *         Configuration = Indicates that a class declares one or more Bean methods and
 *         may be processed by Spring.
 *     </li>
 *     <li>
 *         EnableSwagger2 = Indicates that Swagger support should be enabled.
 *     </li>
 * </ul>
 */
@Configuration
@EnableSwagger2
public class SwaggerAPIConfiguration {
    // Application Info Properties
    @Value("${info.app.name}")
    private String appName;

    @Value("${info.app.description}")
    private String appDescription;

    @Value("${info.app.version}")
    private String appVersion;

    @Value("${info.app.contact.name}")
    private String contactName;

    @Value("${info.app.contact.url}")
    private String contactUrl;

    @Value("${info.app.contact.email}")
    private String contactEmail;

    /**
     * Configure the Swagger documentation generation
     *
     * @return {@link Docket} a builder which is intended to be the primary interface into the Springfox framework.
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(Predicates.not(RequestHandlerSelectors.basePackage(APIConstants.SPRING_FRAMEWORK_PACKAGE)))
                .paths(PathSelectors.any())
                .build().apiInfo(apiInfo());
    }

    /**
     * General application information for this microservice.
     *
     * @return {@link ApiInfo} provides general information about the underlying API.
     */
    private ApiInfo apiInfo() {
        Contact contact = new Contact(contactName, contactUrl, contactEmail);
        return new ApiInfo(appName,
                appDescription,
                appVersion,
                APIConstants.APACHE_LICENSE_URL,
                contact,
                APIConstants.APACHE_LICENSE,
                APIConstants.APACHE_LICENSE_URL,
                CommonUtil.setOf());
    }
}
