package com.alert.microservice.config.api;

/**
 * Sudo interface to define common service properties in code. The reason why this is an interface
 * is because typical classes
 */
public interface APIConstants {
    /** General Constants **/
    // Used to exclude packages from SwaggerUI
    String SPRING_FRAMEWORK_PACKAGE = "org.springframework";
    // License information
    String APACHE_LICENSE_URL = "https://www.apache.org/licenses/LICENSE-2.0";
    String APACHE_LICENSE = "Licensed under the Apache License, Version 2.0 ";
}
