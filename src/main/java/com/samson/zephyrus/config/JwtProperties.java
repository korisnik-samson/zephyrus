package com.samson.zephyrus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT configuration properties bound from application.jwt.* in application.yml.
 */
@Data
@ConfigurationProperties(prefix = "application.jwt")
public class JwtProperties {

    /** Secret key for signing JWT tokens (minimum 256 bits recommended) */
    private String secret;

    /** Access token expiry in milliseconds (default: 15 minutes) */
    private long accessTokenExpiry = 900_000;

    /** Refresh token expiry in milliseconds (default: 7 days) */
    private long refreshTokenExpiry = 604_800_000;
}
