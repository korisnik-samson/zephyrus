package com.samson.zephyrus.search;

import com.samson.zephyrus.auth.model.User;
import com.samson.zephyrus.common.ApiResponse;
import com.samson.zephyrus.content.dto.SearchResultDto;
import com.samson.zephyrus.search.dto.SearchSuggestionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Full-text search powered by Meilisearch (PostgreSQL fallback), with autocomplete and analytics")
public class SearchController {

    private final SearchService searchService;
    private final SearchAnalyticsService analyticsService;

    @GetMapping
    @Operation(
        summary = "Search titles",
        description = "Full-text search across title and overview. Uses Meilisearch when enabled, " +
                      "otherwise falls back to PostgreSQL tsquery. Search terms are recorded for analytics."
    )
    public ResponseEntity<ApiResponse<SearchResultDto>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User user) {

        log.debug("GET /api/search?q={}&page={}&size={}", q, page, size);
        analyticsService.recordSearch(user != null ? user.getId() : null, q);
        return ResponseEntity.ok(ApiResponse.success(searchService.search(q, page, size)));
    }

    @GetMapping("/suggestions")
    @Operation(
        summary = "Autocomplete suggestions",
        description = "Returns up to 5 title suggestions for the given partial query"
    )
    public ResponseEntity<ApiResponse<List<SearchSuggestionDto>>> suggestions(
            @RequestParam String q) {

        log.debug("GET /api/search/suggestions?q={}", q);
        return ResponseEntity.ok(ApiResponse.success(searchService.getSuggestions(q)));
    }

    @GetMapping("/trending")
    @Operation(
        summary = "Trending searches",
        description = "Returns the most frequently searched terms globally, ordered by search count"
    )
    public ResponseEntity<ApiResponse<List<String>>> trending(
            @RequestParam(defaultValue = "10") int limit) {

        return ResponseEntity.ok(ApiResponse.success(analyticsService.getTrendingSearches(limit)));
    }

    @GetMapping("/recent")
    @Operation(
        summary = "Recent searches",
        description = "Returns the current user's last 20 unique searches, newest first"
    )
    public ResponseEntity<ApiResponse<List<String>>> recent(
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(ApiResponse.success(analyticsService.getRecentSearches(user.getId())));
    }

    @DeleteMapping("/recent")
    @Operation(summary = "Clear recent searches")
    public ResponseEntity<ApiResponse<Void>> clearRecent(
            @AuthenticationPrincipal User user) {

        analyticsService.clearRecentSearches(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Recent searches cleared"));
    }
}