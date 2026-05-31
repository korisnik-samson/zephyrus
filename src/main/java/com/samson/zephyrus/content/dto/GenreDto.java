package com.samson.zephyrus.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Genre reference DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenreDto {

    private UUID id;
    private Integer tmdbId;
    private String name;
}
