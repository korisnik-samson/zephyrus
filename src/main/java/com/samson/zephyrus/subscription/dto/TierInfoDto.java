package com.samson.zephyrus.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TierInfoDto {
    private String name;
    private int rank;
    private int maxStreams;
    private String maxQuality;
}