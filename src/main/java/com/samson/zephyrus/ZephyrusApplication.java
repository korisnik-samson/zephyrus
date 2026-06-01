package com.samson.zephyrus;

import com.samson.zephyrus.config.CorsProperties;
import com.samson.zephyrus.config.JwtProperties;
import com.samson.zephyrus.config.TmdbProperties;
import com.samson.zephyrus.search.config.MeilisearchProperties;
import com.samson.zephyrus.subscription.config.StripeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
    JwtProperties.class,
    TmdbProperties.class,
    CorsProperties.class,
    MeilisearchProperties.class,
    StripeProperties.class
})
public class ZephyrusApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZephyrusApplication.class, args);
    }

}
