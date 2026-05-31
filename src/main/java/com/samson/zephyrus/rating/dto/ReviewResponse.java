package com.samson.zephyrus.rating.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ReviewResponse {
    private UUID id;
    private UUID userId;
    private String displayName;
    private UUID titleId;
    private String body;
    private boolean containsSpoilers;
    private String createdAt;
    private String updatedAt;
}