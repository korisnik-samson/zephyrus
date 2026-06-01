package com.samson.zephyrus.subscription;

import com.samson.zephyrus.subscription.model.SubscriptionTier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Gates a controller method (or all methods of a controller) behind a minimum
 * subscription tier. Enforced by {@link TierVerificationAspect}.
 *
 * <pre>{@code
 * @RequiresTier(SubscriptionTier.PREMIUM)
 * @PostMapping("/watch-party")
 * public ... createParty(...) { ... }
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresTier {
    SubscriptionTier value();
}