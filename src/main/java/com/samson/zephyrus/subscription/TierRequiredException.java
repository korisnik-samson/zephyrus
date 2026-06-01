package com.samson.zephyrus.subscription;

import com.samson.zephyrus.subscription.model.SubscriptionTier;
import lombok.Getter;

/**
 * Thrown when a user attempts to access a feature gated behind a higher
 * subscription tier than they currently hold. Mapped to HTTP 402.
 */
@Getter
public class TierRequiredException extends RuntimeException {

    private final SubscriptionTier requiredTier;
    private final SubscriptionTier currentTier;

    public TierRequiredException(SubscriptionTier requiredTier, SubscriptionTier currentTier) {
        super("This feature requires the " + requiredTier + " tier (you have " + currentTier + ")");
        this.requiredTier = requiredTier;
        this.currentTier = currentTier;
    }
}