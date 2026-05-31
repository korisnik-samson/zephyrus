package com.samson.zephyrus.admin.cms.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class UpdateTitleRequest {
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
    private Boolean published;
    private List<UUID> genreIds;
}