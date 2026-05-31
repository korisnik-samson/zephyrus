package com.samson.zephyrus.content;

import com.samson.zephyrus.common.ApiResponse;
import com.samson.zephyrus.content.dto.*;
import com.samson.zephyrus.content.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for browsing, searching, and viewing content.
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Content", description = "Browse, search, and view streaming content")
public class ContentController {

    private final ContentService contentService;

    // ── Featured / Hero billboard

    @GetMapping("/content/featured")
    @Operation(summary = "Get featured titles", description = "Returns 5-8 trending titles for the hero billboard")
    public ResponseEntity<ApiResponse<FeaturedDto>> getFeatured() {
        log.debug("GET /api/content/featured");
        FeaturedDto featured = contentService.getFeatured();

        return ResponseEntity.ok(ApiResponse.success(featured));
    }

    // ── Browse rows

    @GetMapping("/content/rows")
    @Operation(summary = "Get browse rows", description = "Returns pre-assembled content rows for the home page")
    public ResponseEntity<ApiResponse<List<ContentRowDto>>> getBrowseRows() {
        log.debug("GET /api/content/rows");
        List<ContentRowDto> rows = contentService.getBrowseRows();

        return ResponseEntity.ok(ApiResponse.success(rows));
    }

    // ── Browse with filters

    @GetMapping("/content")
    @Operation(summary = "Browse content", description = "Browse titles with optional genre filter, sort, and pagination")
    public ResponseEntity<ApiResponse<SearchResultDto>> browse(
            @Parameter(description = "Genre ID to filter by") @RequestParam(required = false) UUID genre,
            @Parameter(description = "Sort field: popularity, rating, newest") @RequestParam(defaultValue = "popularity") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("GET /api/content?genre={}&sort={}&page={}&size={}", genre, sort, page, size);

        SearchResultDto result;

        if (genre != null) result = contentService.browseByGenre(genre, sort, page, size);
        else result = contentService.searchTitles("", page, size);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ── Title detail

    @GetMapping("/content/{id}")
    @Operation(summary = "Get title details", description = "Returns full title details including cast, genres, seasons, and similar titles")
    public ResponseEntity<ApiResponse<TitleDetailDto>> getTitleDetail(
            @PathVariable UUID id) {

        log.debug("GET /api/content/{}", id);
        TitleDetailDto detail = contentService.getTitleDetail(id);

        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    // ── Episodes

    @GetMapping("/content/{id}/episodes")
    @Operation(summary = "Get episodes", description = "Returns episodes for a given title and season number")
    public ResponseEntity<ApiResponse<List<EpisodeDto>>> getEpisodes(
            @PathVariable UUID id,
            @Parameter(description = "Season number") @RequestParam(defaultValue = "1") int season) {

        log.debug("GET /api/content/{}/episodes?season={}", id, season);
        List<EpisodeDto> episodes = contentService.getEpisodes(id, season);

        return ResponseEntity.ok(ApiResponse.success(episodes));
    }

    // ── Similar titles

    @GetMapping("/content/{id}/similar")
    @Operation(summary = "Get similar titles", description = "Returns titles similar to the given title based on shared genres")
    public ResponseEntity<ApiResponse<List<TitleSummaryDto>>> getSimilar(
            @PathVariable UUID id) {

        log.debug("GET /api/content/{}/similar", id);
        List<TitleSummaryDto> similar = contentService.getSimilar(id);

        return ResponseEntity.ok(ApiResponse.success(similar));
    }

    // ── Genres

    @GetMapping("/content/genres")
    @Operation(summary = "Get all genres", description = "Returns a list of all available genres")
    public ResponseEntity<ApiResponse<List<GenreDto>>> getGenres() {
        log.debug("GET /api/content/genres");
        List<GenreDto> genres = contentService.getGenres();

        return ResponseEntity.ok(ApiResponse.success(genres));
    }

    // ── Search

    @GetMapping("/search")
    @Operation(summary = "Search titles", description = "Search titles by query string with pagination")
    public ResponseEntity<ApiResponse<SearchResultDto>> search(
            @Parameter(description = "Search query") @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("GET /api/search?q={}&page={}&size={}", query, page, size);
        SearchResultDto results = contentService.searchTitles(query, page, size);

        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
