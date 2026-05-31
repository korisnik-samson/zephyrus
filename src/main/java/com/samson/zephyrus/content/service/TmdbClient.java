package com.samson.zephyrus.content.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.samson.zephyrus.config.TmdbProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Low-level HTTP client for the TMDB v3 API.
 * Returns raw {@link JsonNode} responses — mapping to domain entities
 * happens in the service layer.
 */
@Slf4j
@Component
public class TmdbClient {

    private final RestClient restClient;

    public TmdbClient(TmdbProperties tmdbProperties) {
        this.restClient = RestClient.builder()
                .baseUrl(tmdbProperties.getBaseUrl())
                .defaultHeader("Accept", "application/json")
                .defaultUriVariables(java.util.Map.of("api_key", tmdbProperties.getApiKey()))
                .build();
    }

    // ── Trending ──────────────────────────────────────────

    /**
     * GET /trending/{mediaType}/{timeWindow}
     *
     * @param mediaType  "movie" or "tv"
     * @param timeWindow "day" or "week"
     */
    public JsonNode getTrending(String mediaType, String timeWindow) {
        log.debug("TMDB → GET /trending/{}/{}", mediaType, timeWindow);
        return restClient.get()
                .uri("/trending/{mediaType}/{timeWindow}?api_key={api_key}", mediaType, timeWindow)
                .retrieve()
                .body(JsonNode.class);
    }

    // ── Popular ───────────────────────────────────────────

    /**
     * GET /{mediaType}/popular
     *
     * @param mediaType "movie" or "tv"
     */
    public JsonNode getPopular(String mediaType) {
        log.debug("TMDB → GET /{}/popular", mediaType);
        return restClient.get()
                .uri("/{mediaType}/popular?api_key={api_key}", mediaType)
                .retrieve()
                .body(JsonNode.class);
    }

    // ── Details ───────────────────────────────────────────

    public JsonNode getMovieDetails(int tmdbId) {
        log.debug("TMDB → GET /movie/{}", tmdbId);
        return restClient.get()
                .uri("/movie/{id}?api_key={api_key}&append_to_response=credits,similar", tmdbId)
                .retrieve()
                .body(JsonNode.class);
    }

    public JsonNode getTvDetails(int tmdbId) {
        log.debug("TMDB → GET /tv/{}", tmdbId);
        return restClient.get()
                .uri("/tv/{id}?api_key={api_key}&append_to_response=credits,similar", tmdbId)
                .retrieve()
                .body(JsonNode.class);
    }

    // ── Credits ───────────────────────────────────────────

    /**
     * GET /{mediaType}/{id}/credits
     */
    public JsonNode getCredits(String mediaType, int tmdbId) {
        log.debug("TMDB → GET /{}/{}/credits", mediaType, tmdbId);
        return restClient.get()
                .uri("/{mediaType}/{id}/credits?api_key={api_key}", mediaType, tmdbId)
                .retrieve()
                .body(JsonNode.class);
    }

    // ── Search ────────────────────────────────────────────

    public JsonNode searchMulti(String query, int page) {
        log.debug("TMDB → GET /search/multi?query={}&page={}", query, page);
        return restClient.get()
                .uri("/search/multi?api_key={api_key}&query={query}&page={page}", query, page)
                .retrieve()
                .body(JsonNode.class);
    }

    // ── Genres ────────────────────────────────────────────

    /**
     * GET /genre/{mediaType}/list
     *
     * @param mediaType "movie" or "tv"
     */
    public JsonNode getGenres(String mediaType) {
        log.debug("TMDB → GET /genre/{}/list", mediaType);
        return restClient.get()
                .uri("/genre/{mediaType}/list?api_key={api_key}", mediaType)
                .retrieve()
                .body(JsonNode.class);
    }

    // ── Season details ────────────────────────────────────

    /**
     * GET /tv/{id}/season/{seasonNumber}
     */
    public JsonNode getSeasonDetails(int tmdbId, int seasonNumber) {
        log.debug("TMDB → GET /tv/{}/season/{}", tmdbId, seasonNumber);
        return restClient.get()
                .uri("/tv/{id}/season/{seasonNumber}?api_key={api_key}", tmdbId, seasonNumber)
                .retrieve()
                .body(JsonNode.class);
    }

    // ── Videos (trailers) ─────────────────────────────────

    /**
     * GET /{mediaType}/{id}/videos
     *
     * @param mediaType "movie" or "tv"
     */
    public JsonNode getVideos(String mediaType, int tmdbId) {
        log.debug("TMDB → GET /{}/{}/videos", mediaType, tmdbId);
        return restClient.get()
                .uri("/{mediaType}/{id}/videos?api_key={api_key}", mediaType, tmdbId)
                .retrieve()
                .body(JsonNode.class);
    }
}
