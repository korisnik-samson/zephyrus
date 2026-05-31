package com.samson.zephyrus.search;

import com.samson.zephyrus.content.dto.SearchResultDto;
import com.samson.zephyrus.content.dto.TitleSummaryDto;
import com.samson.zephyrus.content.model.Genre;
import com.samson.zephyrus.content.model.Title;
import com.samson.zephyrus.content.repository.TitleRepository;
import com.samson.zephyrus.search.dto.SearchSuggestionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Unified search: delegates to Meilisearch when available,
 * falls back to PostgreSQL full-text search transparently.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final MeilisearchSyncService meilisearchSyncService;
    private final TitleRepository titleRepository;

    @Transactional(readOnly = true)
    public SearchResultDto search(String query, int page, int size) {
        if (meilisearchSyncService.isAvailable()) {
            try {
                return meilisearchSyncService.search(query, page, size);
            } catch (Exception e) {
                log.warn("Meilisearch unavailable, falling back to PostgreSQL: {}", e.getMessage());
            }
        }
        return searchPostgres(query, page, size);
    }

    @Transactional(readOnly = true)
    public List<SearchSuggestionDto> getSuggestions(String query) {
        if (query == null || query.isBlank()) return Collections.emptyList();

        if (meilisearchSyncService.isAvailable()) {
            try {
                return meilisearchSyncService.getSuggestions(query);
            } catch (Exception e) {
                log.warn("Meilisearch suggestions unavailable, falling back: {}", e.getMessage());
            }
        }

        return titleRepository.searchByTitle(query.trim(), PageRequest.of(0, 5))
                .getContent().stream()
                .map(t -> new SearchSuggestionDto(t.getId(), t.getTitle(), t.getMediaType().name(), t.getPosterPath()))
                .collect(Collectors.toList());
    }

    // ── Private helpers ───────────────────────────────────

    private SearchResultDto searchPostgres(String query, int page, int size) {
        Page<Title> results = (query == null || query.isBlank())
                ? titleRepository.findTopByPopularity(PageRequest.of(page, size))
                : titleRepository.searchFullText(query.trim(), PageRequest.of(page, size));

        List<TitleSummaryDto> titles = results.getContent().stream()
                .map(this::toSummary).collect(Collectors.toList());

        return SearchResultDto.builder()
                .results(titles)
                .page(results.getNumber())
                .size(results.getSize())
                .totalElements(results.getTotalElements())
                .totalPages(results.getTotalPages())
                .build();
    }

    private TitleSummaryDto toSummary(Title title) {
        List<String> genres = title.getGenres().stream()
                .map(Genre::getName).collect(Collectors.toList());
        return TitleSummaryDto.builder()
                .id(title.getId())
                .tmdbId(title.getTmdbId())
                .mediaType(title.getMediaType().name())
                .title(title.getTitle())
                .overview(title.getOverview())
                .posterPath(title.getPosterPath())
                .backdropPath(title.getBackdropPath())
                .voteAverage(title.getVoteAverage())
                .releaseDate(title.getReleaseDate())
                .maturityRating(title.getMaturityRating())
                .genres(genres)
                .build();
    }
}