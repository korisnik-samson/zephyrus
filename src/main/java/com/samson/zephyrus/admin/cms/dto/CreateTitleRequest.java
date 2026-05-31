package com.samson.zephyrus.admin.cms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateTitleRequest {

    @NotBlank
    private String title;

    @NotNull
    @Pattern(regexp = "MOVIE|SERIES", message = "mediaType must be MOVIE or SERIES")
    private String mediaType;

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
    private boolean published = true;
    private List<UUID> genreIds;
}