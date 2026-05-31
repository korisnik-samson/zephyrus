package com.samson.zephyrus.recommendation;

import com.samson.zephyrus.content.dto.TitleSummaryDto;
import com.samson.zephyrus.content.model.Genre;
import com.samson.zephyrus.content.model.Title;
import com.samson.zephyrus.content.repository.TitleRepository;
import com.samson.zephyrus.playback.model.WatchProgress;
import com.samson.zephyrus.playback.repository.WatchProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final int TOP_GENRES = 3;
    private static final int FETCH_PER_GENRE = 20;

    private final WatchProgressRepository watchProgressRepository;
    private final TitleRepository titleRepository;

    /**
     * Genre-affinity recommendations: identifies the user's top genres from
     * watch history, then surfaces popular unwatched titles from those genres.
     * Falls back to overall popularity for cold-start users with no history.
     */
    @Transactional(readOnly = true)
    public List<TitleSummaryDto> getRecommendations(UUID userId, int limit) {
        List<WatchProgress> history = watchProgressRepository.findByUserIdOrderByLastWatchedAtDesc(userId);

        if (history.isEmpty()) {
            log.debug("Cold-start recommendations for user={}", userId);
            return titleRepository.findTopByPopularity(PageRequest.of(0, limit))
                    .getContent().stream().map(this::toSummary).collect(Collectors.toList());
        }

        Set<UUID> watchedIds = history.stream()
                .map(wp -> wp.getTitle().getId())
                .collect(Collectors.toSet());

        // Tally genre frequency across watch history
        Map<UUID, Long> genreFreq = new LinkedHashMap<>();
        for (WatchProgress wp : history) {
            for (Genre g : wp.getTitle().getGenres()) {
                genreFreq.merge(g.getId(), 1L, Long::sum);
            }
        }

        if (genreFreq.isEmpty()) {
            return titleRepository.findTopByPopularity(PageRequest.of(0, limit))
                    .getContent().stream().map(this::toSummary).collect(Collectors.toList());
        }

        List<UUID> topGenreIds = genreFreq.entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .limit(TOP_GENRES)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        Set<UUID> seen = new HashSet<>(watchedIds);
        List<TitleSummaryDto> results = new ArrayList<>();

        for (UUID genreId : topGenreIds) {
            if (results.size() >= limit) break;
            for (Title t : titleRepository.findByGenreId(genreId, PageRequest.of(0, FETCH_PER_GENRE)).getContent()) {
                if (!seen.contains(t.getId()) && results.size() < limit) {
                    results.add(toSummary(t));
                    seen.add(t.getId());
                }
            }
        }

        log.debug("Recommendations for user={}: {} titles from {} top genres", userId, results.size(), topGenreIds.size());
        return results;
    }

    private TitleSummaryDto toSummary(Title title) {
        List<String> genreNames = title.getGenres().stream()
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
                .genres(genreNames)
                .build();
    }
}