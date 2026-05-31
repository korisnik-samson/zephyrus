package com.samson.zephyrus.search.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Meilisearch document shape for a title.
 * Field "id" is the primary key Meilisearch uses.
 */
@Data
@Builder
public class TitleDocument {
    private String id;          // UUID as string
    private Integer tmdbId;
    private String mediaType;
    private String title;
    private String overview;
    private String posterPath;
    private String backdropPath;
    private Double voteAverage;
    private Double popularity;
    private String releaseDate;
    private String maturityRating;
    private List<String> genres;
}