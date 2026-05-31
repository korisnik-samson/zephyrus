package com.samson.zephyrus.playback.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class StreamDto {
    private UUID titleId;
    private String title;
    private String mediaType;
    /** YouTube trailer URL, or null if unavailable */
    private String streamUrl;
}