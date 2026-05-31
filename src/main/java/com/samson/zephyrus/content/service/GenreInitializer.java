package com.samson.zephyrus.content.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.samson.zephyrus.content.model.Genre;
import com.samson.zephyrus.content.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * On startup, fetches the canonical genre lists from TMDB (movies + TV)
 * and upserts them into the local {@code genres} table.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GenreInitializer {

    private final TmdbCacheService tmdbCacheService;
    private final GenreRepository genreRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initGenres() {
        log.info("Initialising genres from TMDB…");
        int count = 0;
        count += upsertGenres("movie");
        count += upsertGenres("tv");
        log.info("Genre initialisation complete — {} genres upserted", count);
    }

    private int upsertGenres(String mediaType) {
        try {
            JsonNode response = tmdbCacheService.getGenres(mediaType);
            JsonNode genres = response.get("genres");
            if (genres == null || !genres.isArray()) {
                log.warn("No genres returned for media type '{}'", mediaType);
                return 0;
            }

            int count = 0;
            for (JsonNode node : genres) {
                int tmdbId = node.get("id").asInt();
                String name = node.get("name").asText();

                genreRepository.findByTmdbId(tmdbId).ifPresentOrElse(
                        existing -> {
                            if (!existing.getName().equals(name)) {
                                existing.setName(name);
                                genreRepository.save(existing);
                                log.debug("Updated genre {} → {}", tmdbId, name);
                            }
                        },
                        () -> {
                            Genre genre = Genre.builder()
                                    .tmdbId(tmdbId)
                                    .name(name)
                                    .build();
                            genreRepository.save(genre);
                            log.debug("Inserted genre {} → {}", tmdbId, name);
                        }
                );
                count++;
            }
            return count;
        } catch (Exception e) {
            log.error("Failed to fetch genres for '{}': {}", mediaType, e.getMessage());
            return 0;
        }
    }
}
