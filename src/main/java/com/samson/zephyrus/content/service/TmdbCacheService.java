package com.samson.zephyrus.content.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Caching facade around {@link TmdbClient}.
 * <p>
 * Cache names and TTLs are configured in {@link com.samson.zephyrus.content.config.CacheConfig}.
 * <ul>
 *   <li>{@code tmdb:trending}  → 6 h</li>
 *   <li>{@code tmdb:details}   → 24 h</li>
 *   <li>{@code tmdb:genres}    → 7 d</li>
 *   <li>{@code tmdb:popular}   → 6 h</li>
 *   <li>{@code tmdb:search}    → 1 h</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TmdbCacheService {

    private final TmdbClient tmdbClient;

    @Cacheable(cacheNames = "tmdb:trending", key = "#mediaType + ':' + #timeWindow")
    public JsonNode getTrending(String mediaType, String timeWindow) {
        log.info("Cache MISS — tmdb:trending:{}:{}", mediaType, timeWindow);
        return tmdbClient.getTrending(mediaType, timeWindow);
    }

    @Cacheable(cacheNames = "tmdb:popular", key = "#mediaType")
    public JsonNode getPopular(String mediaType) {
        log.info("Cache MISS — tmdb:popular:{}", mediaType);
        return tmdbClient.getPopular(mediaType);
    }

    @Cacheable(cacheNames = "tmdb:details", key = "'movie:' + #tmdbId")
    public JsonNode getMovieDetails(int tmdbId) {
        log.info("Cache MISS — tmdb:details:movie:{}", tmdbId);
        return tmdbClient.getMovieDetails(tmdbId);
    }

    @Cacheable(cacheNames = "tmdb:details", key = "'tv:' + #tmdbId")
    public JsonNode getTvDetails(int tmdbId) {
        log.info("Cache MISS — tmdb:details:tv:{}", tmdbId);
        return tmdbClient.getTvDetails(tmdbId);
    }

    @Cacheable(cacheNames = "tmdb:details", key = "#mediaType + ':credits:' + #tmdbId")
    public JsonNode getCredits(String mediaType, int tmdbId) {
        log.info("Cache MISS — tmdb:credits:{}:{}", mediaType, tmdbId);
        return tmdbClient.getCredits(mediaType, tmdbId);
    }

    @Cacheable(cacheNames = "tmdb:search", key = "#query + ':' + #page")
    public JsonNode searchMulti(String query, int page) {
        log.info("Cache MISS — tmdb:search:{}:{}", query, page);
        return tmdbClient.searchMulti(query, page);
    }

    @Cacheable(cacheNames = "tmdb:genres", key = "#mediaType")
    public JsonNode getGenres(String mediaType) {
        log.info("Cache MISS — tmdb:genres:{}", mediaType);
        return tmdbClient.getGenres(mediaType);
    }

    @Cacheable(cacheNames = "tmdb:details", key = "#mediaType + ':videos:' + #tmdbId")
    public JsonNode getVideos(String mediaType, int tmdbId) {
        log.info("Cache MISS — tmdb:videos:{}:{}", mediaType, tmdbId);
        return tmdbClient.getVideos(mediaType, tmdbId);
    }
}
