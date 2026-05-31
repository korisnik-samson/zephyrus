package com.samson.zephyrus.playback.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ProgressDto {
    private UUID id;
    private UUID titleId;
    private UUID episodeId;
    private int progressSeconds;
    private int durationSeconds;
    private boolean completed;
    private String lastWatchedAt;
}
