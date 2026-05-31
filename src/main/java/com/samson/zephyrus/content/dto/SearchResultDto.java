package com.samson.zephyrus.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated search result wrapper.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDto {

    private List<TitleSummaryDto> results;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
