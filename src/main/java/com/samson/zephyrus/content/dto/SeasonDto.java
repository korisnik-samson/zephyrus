package com.samson.zephyrus.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Season summary DTO (episodes are loaded separately).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeasonDto {

    private UUID id;
    private Integer seasonNumber;
    private String name;
    private String overview;
    private String posterPath;
    private Integer episodeCount;
    private LocalDate airDate;
}
