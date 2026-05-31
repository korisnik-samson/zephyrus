package com.samson.zephyrus.rating.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RatingResponse {
    private UUID titleId;
    /** The current user's score, null if they haven't rated yet. */
    private Integer myScore;
    private Double averageScore;
    private long totalRatings;
}