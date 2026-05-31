package com.samson.zephyrus.playback.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SaveProgressRequest {

    @NotNull
    private UUID titleId;

    /** Null for movies; set for series episodes */
    private UUID episodeId;

    @Min(0)
    private int progressSeconds;

    @Min(1)
    private int durationSeconds;
}
