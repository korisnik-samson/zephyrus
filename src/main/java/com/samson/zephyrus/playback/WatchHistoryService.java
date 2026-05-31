package com.samson.zephyrus.playback;

import com.samson.zephyrus.content.model.Episode;
import com.samson.zephyrus.content.model.Title;
import com.samson.zephyrus.playback.dto.ContinueWatchingDto;
import com.samson.zephyrus.playback.model.WatchProgress;
import com.samson.zephyrus.playback.repository.WatchProgressRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Exposes the full watch history for a user (including completed items).
 * {@link PlaybackService} handles live progress saving; this is read-only history.
 */
@Service
@RequiredArgsConstructor
public class WatchHistoryService {

    private final WatchProgressRepository watchProgressRepository;

    @Transactional(readOnly = true)
    public List<ContinueWatchingDto> getHistory(UUID userId) {
        return watchProgressRepository.findByUserIdOrderByLastWatchedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ContinueWatchingDto toDto(@NonNull WatchProgress wp) {
        Title title = wp.getTitle();
        Episode episode = wp.getEpisode();

        String episodeLabel = null;
        if (episode != null) {
            int seasonNum = episode.getSeason().getSeasonNumber();
            episodeLabel = "S" + seasonNum + " E" + episode.getEpisodeNumber();
        }

        return ContinueWatchingDto.builder()
                .progressId(wp.getId())
                .titleId(title.getId())
                .episodeId(episode != null ? episode.getId() : null)
                .title(title.getTitle())
                .posterPath(title.getPosterPath())
                .backdropPath(title.getBackdropPath())
                .mediaType(title.getMediaType().name())
                .progressSeconds(wp.getProgressSeconds())
                .durationSeconds(wp.getDurationSeconds())
                .lastWatchedAt(wp.getLastWatchedAt() != null ? wp.getLastWatchedAt().toString() : null)
                .episodeLabel(episodeLabel)
                .build();
    }
}