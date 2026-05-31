package com.samson.zephyrus.admin.cms.dto;

import com.samson.zephyrus.content.dto.CastMemberDto;
import com.samson.zephyrus.content.dto.GenreDto;
import com.samson.zephyrus.content.dto.SeasonDto;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AdminTitleDto {
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
    // Admin-only fields
    private boolean published;
    private String availableFrom;
    private String availableUntil;
    private String createdAt;
    private String updatedAt;
    // Relations
    private List<GenreDto> genres;
    private List<CastMemberDto> cast;
    private List<SeasonDto> seasons;
}