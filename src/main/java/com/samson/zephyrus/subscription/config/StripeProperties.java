package com.samson.zephyrus.subscription.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Stripe configuration. When {@code enabled=false} the subscription endpoints
 * operate in a degraded mode (no checkout sessions) but tier reads still work.
 */
@Data
@ConfigurationProperties(prefix = "application.stripe")
public class StripeProperties {
    private boolean enabled = false;
    private String apiKey = "";
    private String webhookSecret = "";
    private String successUrl = "http://localhost:3000/account/billing?success=true";
    private String cancelUrl = "http://localhost:3000/account/billing?canceled=true";

    /** Maps a tier name (BASIC/STANDARD/PREMIUM) to its Stripe price ID. */
    private Map<String, String> priceIds = new HashMap<>();
}