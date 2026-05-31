package com.samson.zephyrus.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "application.meilisearch")
public class MeilisearchProperties {
    private boolean enabled = false;
    private String url = "http://localhost:7700";
    private String apiKey = "masterKey";
    private String indexName = "titles";
}