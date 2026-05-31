package com.samson.zephyrus.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * A labelled content row for the home / browse page.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentRowDto {

    private String label;
    private String rowType;
    private List<TitleSummaryDto> titles;
}
