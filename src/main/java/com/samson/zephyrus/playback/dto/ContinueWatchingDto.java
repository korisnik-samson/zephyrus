package com.samson.zephyrus.playback.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * "Continue Watching" item — progress info combined with title metadata.
 */
@Data
@Builder
public class ContinueWatchingDto {
    private UUID progressId;
    private UUID titleId;
    private UUID episodeId;
    private String title;
    private String posterPath;
    private String backdropPath;
    private String mediaType;
    private int progressSeconds;
    private int durationSeconds;
    private String lastWatchedAt;
    /** e.g. "S1 E3" for series */
    private String episodeLabel;
}
