package com.samson.zephyrus.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Featured / hero billboard data for the home page.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeaturedDto {

    private List<TitleSummaryDto> titles;
}
