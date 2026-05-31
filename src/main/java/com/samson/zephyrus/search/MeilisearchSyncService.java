package com.samson.zephyrus.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Config;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.model.SearchResult;
import com.samson.zephyrus.content.dto.SearchResultDto;
import com.samson.zephyrus.content.dto.TitleSummaryDto;
import com.samson.zephyrus.content.model.Genre;
import com.samson.zephyrus.content.model.Title;
import com.samson.zephyrus.search.config.MeilisearchProperties;
import com.samson.zephyrus.search.dto.SearchSuggestionDto;
import com.samson.zephyrus.search.dto.TitleDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Wraps the Meilisearch Java SDK.
 * Disabled gracefully when {@code application.meilisearch.enabled=false}
 * — all methods return empty results rather than throwing.
 */
@Slf4j
@Service
public class MeilisearchSyncService {

    private final MeilisearchProperties props;
    private final ObjectMapper objectMapper;
    private Client client;
    private Index index;

    public MeilisearchSyncService(MeilisearchProperties props, ObjectMapper objectMapper) {
        this.props = props;
        this.objectMapper = objectMapper;
        if (props.isEnabled()) {
            try {
                this.client = new Client(new Config(props.getUrl(), props.getApiKey()));
                this.index = client.index(props.getIndexName());
                configureIndex();
                log.info("Meilisearch client initialised — {}", props.getUrl());
            } catch (Exception e) {
                log.warn("Meilisearch unavailable at startup ({}). Falling back to PostgreSQL.", e.getMessage());
                this.client = null;
                this.index = null;
            }
        }
    }

    public boolean isAvailable() {
        return index != null;
    }

    // ── Indexing ──────────────────────────────────────────

    public void indexTitles(List<Title> titles) {
        if (!isAvailable()) return;
        try {
            List<TitleDocument> docs = titles.stream().map(this::toDocument).collect(Collectors.toList());
            String json = objectMapper.writeValueAsString(docs);
            index.addDocuments(json, "id");
            log.info("Indexed {} titles to Meilisearch", docs.size());
        } catch (Exception e) {
            log.error("Failed to index titles to Meilisearch: {}", e.getMessage());
        }
    }

    public void indexTitle(Title title) {
        if (!isAvailable()) return;
        try {
            String json = objectMapper.writeValueAsString(List.of(toDocument(title)));
            index.addDocuments(json, "id");
        } catch (Exception e) {
            log.warn("Failed to index title {} to Meilisearch: {}", title.getId(), e.getMessage());
        }
    }

    public void deleteTitle(UUID titleId) {
        if (!isAvailable()) return;
        try {
            index.deleteDocument(titleId.toString());
        } catch (Exception e) {
            log.warn("Failed to delete title {} from Meilisearch: {}", titleId, e.getMessage());
        }
    }

    // ── Search ────────────────────────────────────────────

    public SearchResultDto search(String query, int page, int size) {
        if (!isAvailable()) return emptyResult(page, size);
        try {
            SearchRequest request = new SearchRequest(query);
            request.setOffset(page * size);
            request.setLimit(size);

            SearchResult result = index.search(request);
            List<TitleSummaryDto> titles = hitsToSummaries(result.getHits());
            long total = result.getEstimatedTotalHits() != null
                    ? result.getEstimatedTotalHits() : titles.size();

            return SearchResultDto.builder()
                    .results(titles)
                    .page(page)
                    .size(size)
                    .totalElements(total)
                    .totalPages((int) Math.ceil((double) total / size))
                    .build();
        } catch (Exception e) {
            log.warn("Meilisearch search failed: {}", e.getMessage());
            return emptyResult(page, size);
        }
    }

    public List<SearchSuggestionDto> getSuggestions(String query) {
        if (!isAvailable()) return Collections.emptyList();
        try {
            SearchRequest request = new SearchRequest(query);
            request.setOffset(0);
            request.setLimit(5);

            SearchResult result = index.search(request);
            return result.getHits().stream()
                    .map(hit -> new SearchSuggestionDto(
                            UUID.fromString(String.valueOf(hit.get("id"))),
                            String.valueOf(hit.getOrDefault("title", "")),
                            String.valueOf(hit.getOrDefault("mediaType", "")),
                            hit.get("posterPath") != null ? String.valueOf(hit.get("posterPath")) : null
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Meilisearch suggestions failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // ── Private helpers ───────────────────────────────────

    private void configureIndex() {
        try {
            index.updateSearchableAttributesSettings(
                    new String[]{"title", "overview", "genres"});
            index.updateFilterableAttributesSettings(
                    new String[]{"mediaType", "maturityRating", "genres"});
            index.updateSortableAttributesSettings(
                    new String[]{"popularity", "voteAverage"});
            log.debug("Meilisearch index settings applied");
        } catch (Exception e) {
            log.warn("Could not apply Meilisearch index settings: {}", e.getMessage());
        }
    }

    private TitleDocument toDocument(Title title) {
        List<String> genres = title.getGenres().stream()
                .map(Genre::getName).collect(Collectors.toList());
        return TitleDocument.builder()
                .id(title.getId().toString())
                .tmdbId(title.getTmdbId())
                .mediaType(title.getMediaType().name())
                .title(title.getTitle())
                .overview(title.getOverview())
                .posterPath(title.getPosterPath())
                .backdropPath(title.getBackdropPath())
                .voteAverage(title.getVoteAverage() != null ? title.getVoteAverage().doubleValue() : null)
                .popularity(title.getPopularity() != null ? title.getPopularity().doubleValue() : null)
                .releaseDate(title.getReleaseDate() != null ? title.getReleaseDate().toString() : null)
                .maturityRating(title.getMaturityRating())
                .genres(genres)
                .build();
    }

    private List<TitleSummaryDto> hitsToSummaries(List<HashMap<String, Object>> hits) {
        return hits.stream().map(hit -> {
            List<String> genres = hit.get("genres") instanceof List<?>
                    ? ((List<?>) hit.get("genres")).stream().map(Object::toString).collect(Collectors.toList())
                    : Collections.emptyList();

            return TitleSummaryDto.builder()
                    .id(UUID.fromString(String.valueOf(hit.get("id"))))
                    .tmdbId(hit.get("tmdbId") != null ? ((Number) hit.get("tmdbId")).intValue() : null)
                    .mediaType(String.valueOf(hit.getOrDefault("mediaType", "")))
                    .title(String.valueOf(hit.getOrDefault("title", "")))
                    .overview(hit.get("overview") != null ? String.valueOf(hit.get("overview")) : null)
                    .posterPath(hit.get("posterPath") != null ? String.valueOf(hit.get("posterPath")) : null)
                    .backdropPath(hit.get("backdropPath") != null ? String.valueOf(hit.get("backdropPath")) : null)
                    .voteAverage(hit.get("voteAverage") != null
                            ? new BigDecimal(String.valueOf(hit.get("voteAverage"))) : null)
                    .maturityRating(hit.get("maturityRating") != null ? String.valueOf(hit.get("maturityRating")) : null)
                    .genres(genres)
                    .build();
        }).collect(Collectors.toList());
    }

    private SearchResultDto emptyResult(int page, int size) {
        return SearchResultDto.builder()
                .results(Collections.emptyList())
                .page(page).size(size).totalElements(0).totalPages(0)
                .build();
    }
}