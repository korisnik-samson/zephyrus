package com.samson.zephyrus.subscription.model;

/**
 * Premium subscription tiers, ordered by capability.
 * {@code rank} is used for tier-gating comparisons (higher = more access).
 */
public enum SubscriptionTier {
    FREE(0, 1, "480p"),
    BASIC(1, 1, "720p"),
    STANDARD(2, 2, "1080p"),
    PREMIUM(3, 4, "4K UHD");

    private final int rank;
    private final int maxStreams;
    private final String maxQuality;

    SubscriptionTier(int rank, int maxStreams, String maxQuality) {
        this.rank = rank;
        this.maxStreams = maxStreams;
        this.maxQuality = maxQuality;
    }

    public int getRank() {
        return rank;
    }

    public int getMaxStreams() {
        return maxStreams;
    }

    public String getMaxQuality() {
        return maxQuality;
    }

    /** True if this tier meets or exceeds the required tier. */
    public boolean satisfies(SubscriptionTier required) {
        return this.rank >= required.rank;
    }
}