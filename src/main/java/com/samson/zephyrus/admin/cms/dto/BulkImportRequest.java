package com.samson.zephyrus.admin.cms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class BulkImportRequest {

    @NotEmpty
    @Valid
    private List<ImportItem> items;

    @Data
    public static class ImportItem {
        @NotNull
        private Integer tmdbId;

        @NotNull
        @Pattern(regexp = "movie|tv", message = "mediaType must be 'movie' or 'tv'")
        private String mediaType;
    }
}