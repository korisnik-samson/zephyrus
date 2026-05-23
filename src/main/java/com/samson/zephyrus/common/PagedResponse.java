package com.samson.zephyrus.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Paginated API response wrapper.
 * Wraps a page of results with pagination metadata.
 *
 * @param <T> the type of items in the page
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResponse<T> {

    private boolean success;
    private List<T> data;
    private String message;
    private String error;
    private int page;
    private int totalPages;
    private long totalItems;
    private int size;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Create a paginated response from a Spring Data {@link Page}.
     *
     * @param page the Spring Data page result
     * @param <T>  the type of items
     * @return a PagedResponse wrapping the page contents and metadata
     */
    public static <T> PagedResponse<T> from(Page<T> page) {
        return PagedResponse.<T>builder()
                .success(true)
                .data(page.getContent())
                .page(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .size(page.getSize())
                .build();
    }

    /**
     * Create a paginated response with a message.
     */
    public static <T> PagedResponse<T> from(Page<T> page, String message) {
        return PagedResponse.<T>builder()
                .success(true)
                .data(page.getContent())
                .message(message)
                .page(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .size(page.getSize())
                .build();
    }
}
