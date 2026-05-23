package com.samson.zephyrus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CORS configuration properties bound from application.cors.* in application.yml.
 */
@Data
@ConfigurationProperties(prefix = "application.cors")
public class CorsProperties {

    /** Comma-separated list of allowed origins for CORS */
    private String allowedOrigins = "http://localhost:3000";
}
