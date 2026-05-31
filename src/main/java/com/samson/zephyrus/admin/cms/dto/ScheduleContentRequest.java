package com.samson.zephyrus.admin.cms.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleContentRequest {
    /** Null = available immediately */
    private LocalDateTime availableFrom;
    /** Null = never expires */
    private LocalDateTime availableUntil;
}