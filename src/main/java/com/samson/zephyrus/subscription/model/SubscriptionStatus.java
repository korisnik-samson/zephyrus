package com.samson.zephyrus.subscription.model;

public enum SubscriptionStatus {
    ACTIVE,
    TRIALING,
    PAST_DUE,
    CANCELED,
    INCOMPLETE;

    /** Whether the subscription currently entitles the user to paid features. */
    public boolean isEntitled() {
        return this == ACTIVE || this == TRIALING;
    }
}