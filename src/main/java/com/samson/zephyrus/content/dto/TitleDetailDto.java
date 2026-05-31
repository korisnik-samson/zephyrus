package com.samson.zephyrus.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Full title details including cast, seasons, and similar titles.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TitleDetailDto {

    private UUID id;
    private Integer tmdbId;
    private String mediaType;
    private String title;
    private String overview;
    private String tagline;
    private LocalDate releaseDate;
    private Integer runtime;
    private String posterPath;
    private String backdropPath;
    private BigDecimal voteAverage;
    private BigDecimal popularity;
    private String originalLanguage;
    private String maturityRating;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<GenreDto> genres;
    private List<CastMemberDto> cast;
    private List<SeasonDto> seasons;
    private List<TitleSummaryDto> similarTitles;
}
