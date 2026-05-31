package com.samson.zephyrus.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class SearchSuggestionDto {
    private UUID id;
    private String title;
    private String mediaType;
    private String posterPath;
}