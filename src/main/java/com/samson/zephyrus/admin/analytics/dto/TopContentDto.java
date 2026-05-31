package com.samson.zephyrus.admin.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class TopContentDto {
    private UUID titleId;
    private String title;
    private String posterPath;
    private String mediaType;
    private long watchCount;
    private long totalWatchMinutes;
}