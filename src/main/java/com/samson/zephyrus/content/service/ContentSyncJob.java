package com.samson.zephyrus.content.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.samson.zephyrus.content.model.Genre;
import com.samson.zephyrus.content.model.MediaType;
import com.samson.zephyrus.content.model.Title;
import com.samson.zephyrus.content.repository.GenreRepository;
import com.samson.zephyrus.content.repository.TitleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Nightly job that pulls trending and popular content from TMDB and upserts
 * it into the local titles table. Runs at 03:00 every day.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContentSyncJob {

    private final TmdbClient tmdbClient;
    private final TitleRepository titleRepository;
    private final GenreRepository genreRepository;

    @Scheduled(cron = "0 0 3 * * *")
    public void syncContent() {
        log.info("Nightly content sync started");
        int total = 0;

        total += sync(() -> tmdbClient.getTrending("movie", "week"), "movie", "trending movies");
        total += sync(() -> tmdbClient.getTrending("tv", "week"),   "tv",    "trending TV");
        total += sync(() -> tmdbClient.getPopular("movie"),          "movie", "popular movies");
        total += sync(() -> tmdbClient.getPopular("tv"),             "tv",    "popular TV");

        log.info("Nightly content sync complete — {} titles upserted", total);
    }

    // ── Private helpers ───────────────────────────────────

    private int sync(TmdbSupplier supplier, String tmdbMediaType, String label) {
        try {
            JsonNode response = supplier.get();

            int count = processPage(response, tmdbMediaType);
            log.info("Synced {} — {} titles", label, count);

            return count;

        } catch (Exception e) {
            log.error("Failed to sync {}: {}", label, e.getMessage());
            return 0;
        }
    }

    @Transactional
    public int processPage(@NonNull JsonNode response, String tmdbMediaType) {
        JsonNode results = response.path("results");
        if (!results.isArray()) return 0;

        int count = 0;

        for (JsonNode node : results) {
            try {
                upsertTitle(node, tmdbMediaType);
                count++;
            } catch (Exception e) {
                log.warn("Skipped tmdbId={}: {}", node.path("id").asInt(), e.getMessage());
            }
        }
        return count;
    }

    private void upsertTitle(@NonNull JsonNode node, String tmdbMediaType) {
        int tmdbId = node.path("id").asInt();
        MediaType mediaType = "movie".equals(tmdbMediaType) ? MediaType.MOVIE : MediaType.SERIES;

        Title title = titleRepository.findByTmdbId(tmdbId).orElseGet(Title::new);

        title.setTmdbId(tmdbId);
        title.setMediaType(mediaType);
        title.setTitle(titleString(node, tmdbMediaType));
        title.setOverview(textOrNull(node, "overview"));
        title.setPosterPath(textOrNull(node, "poster_path"));
        title.setBackdropPath(textOrNull(node, "backdrop_path"));
        title.setVoteAverage(decimalOrNull(node, "vote_average"));
        title.setPopularity(decimalOrNull(node, "popularity"));
        title.setOriginalLanguage(textOrNull(node, "original_language"));
        title.setReleaseDate(parseDateOrNull(releaseDateKey(node, tmdbMediaType)));
        title.setGenres(resolveGenres(node.path("genre_ids")));

        titleRepository.save(title);
    }

    private String titleString(JsonNode node, String tmdbMediaType) {
        return "movie".equals(tmdbMediaType)
                ? node.path("title").asText(node.path("name").asText())
                : node.path("name").asText(node.path("title").asText());
    }

    private String releaseDateKey(JsonNode node, String tmdbMediaType) {
        return "movie".equals(tmdbMediaType)
                ? node.path("release_date").asText("")
                : node.path("first_air_date").asText("");
    }

    private @NonNull List<Genre> resolveGenres(@NonNull JsonNode genreIds) {
        List<Genre> genres = new ArrayList<>();
        if (genreIds.isArray()) {
            for (JsonNode gId : genreIds) {
                genreRepository.findByTmdbId(gId.asInt()).ifPresent(genres::add);
            }
        }
        return genres;
    }

    private @Nullable String textOrNull(@NonNull JsonNode node, String field) {
        JsonNode n = node.path(field);
        return n.isMissingNode() || n.isNull() ? null : n.asText();
    }

    private @Nullable BigDecimal decimalOrNull(@NonNull JsonNode node, String field) {
        JsonNode n = node.path(field);
        if (n.isMissingNode() || n.isNull()) return null;
        try {
            return new BigDecimal(n.asText());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate parseDateOrNull(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;

        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    @FunctionalInterface
    private interface TmdbSupplier {
        JsonNode get();
    }
}