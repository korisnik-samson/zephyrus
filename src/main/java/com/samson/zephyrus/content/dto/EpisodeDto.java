package com.samson.zephyrus.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Episode DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeDto {

    private UUID id;
    private Integer episodeNumber;
    private String name;
    private String overview;
    private String stillPath;
    private Integer runtime;
    private LocalDate airDate;
}
