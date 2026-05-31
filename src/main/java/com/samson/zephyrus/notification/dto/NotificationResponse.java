package com.samson.zephyrus.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class NotificationResponse {
    private UUID id;
    private String type;
    private String title;
    private String body;
    private String actionUrl;
    private boolean read;
    private String createdAt;
}