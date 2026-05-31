package com.samson.zephyrus.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Cast member DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CastMemberDto {

    private UUID id;
    private Integer tmdbPersonId;
    private String name;
    private String characterName;
    private String profilePath;
    private Integer displayOrder;
}
