package com.samson.zephyrus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TMDB API configuration properties bound from application.tmdb.* in application.yml.
 */
@Data
@ConfigurationProperties(prefix = "application.tmdb")
public class TmdbProperties {

    /** TMDB API key (v3 auth) */
    private String apiKey;

    /** TMDB API base URL */
    private String baseUrl = "https://api.themoviedb.org/3";

    /** TMDB image CDN base URL */
    private String imageBaseUrl = "https://image.tmdb.org/t/p";
}
