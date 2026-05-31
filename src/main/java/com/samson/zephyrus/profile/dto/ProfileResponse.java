package com.samson.zephyrus.profile.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ProfileResponse {
    private UUID id;
    private String name;
    private String avatarUrl;
    private boolean kidsMode;
    private boolean hasPin;
    private int sortOrder;
    private String createdAt;
}