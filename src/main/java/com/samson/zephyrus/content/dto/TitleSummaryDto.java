package com.samson.zephyrus.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Lightweight title representation used in browse rows and lists.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TitleSummaryDto {

    private UUID id;
    private Integer tmdbId;
    private String mediaType;
    private String title;
    private String overview;
    private String posterPath;
    private String backdropPath;
    private BigDecimal voteAverage;
    private LocalDate releaseDate;
    private String maturityRating;
    private List<String> genres;
}
