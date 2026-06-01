package com.samson.zephyrus.subscription.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionResponse {
    private String tier;
    private String status;
    private String currentPeriodEnd;
    private boolean cancelAtPeriodEnd;
    private int maxStreams;
    private String maxQuality;
}