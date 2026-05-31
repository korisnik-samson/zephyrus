package com.samson.zephyrus.playback;

import com.fasterxml.jackson.databind.JsonNode;
import com.samson.zephyrus.auth.model.User;
import com.samson.zephyrus.content.model.Episode;
import com.samson.zephyrus.content.model.MediaType;
import com.samson.zephyrus.content.model.Title;
import com.samson.zephyrus.content.repository.EpisodeRepository;
import com.samson.zephyrus.content.repository.TitleRepository;
import com.samson.zephyrus.content.service.TmdbCacheService;
import com.samson.zephyrus.playback.dto.ContinueWatchingDto;
import com.samson.zephyrus.playback.dto.ProgressDto;
import com.samson.zephyrus.playback.dto.SaveProgressRequest;
import com.samson.zephyrus.playback.dto.StreamDto;
import com.samson.zephyrus.playback.model.WatchProgress;
import com.samson.zephyrus.playback.repository.WatchProgressRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaybackService {

    private static final double COMPLETION_THRESHOLD = 0.9;

    private final WatchProgressRepository watchProgressRepository;
    private final TitleRepository titleRepository;
    private final EpisodeRepository episodeRepository;
    private final TmdbCacheService tmdbCacheService;

    // ── Stream URL ────────────────────────────────────────

    @Transactional(readOnly = true)
    public StreamDto getStreamUrl(UUID titleId) {
        Title title = titleRepository.findById(titleId)
                .orElseThrow(() -> new EntityNotFoundException("Title not found: " + titleId));

        String tmdbMediaType = title.getMediaType() == MediaType.MOVIE ? "movie" : "tv";
        String streamUrl = resolveTrailerUrl(tmdbMediaType, title.getTmdbId());

        return StreamDto.builder()
                .titleId(titleId)
                .title(title.getTitle())
                .mediaType(title.getMediaType().name())
                .streamUrl(streamUrl)
                .build();
    }

    // ── Progress ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public Optional<ProgressDto> getProgress(UUID userId, UUID titleId, UUID episodeId) {
        Optional<WatchProgress> progress = episodeId != null
                ? watchProgressRepository.findByUserIdAndTitleIdAndEpisodeId(userId, titleId, episodeId)
                : watchProgressRepository.findByUserIdAndTitleIdAndEpisodeIdIsNull(userId, titleId);

        return progress.map(this::toProgressDto);
    }

    @Transactional
    public ProgressDto saveProgress(User user, UUID titleId, @NonNull SaveProgressRequest request) {
        Title title = titleRepository.findById(titleId)
                .orElseThrow(() -> new EntityNotFoundException("Title not found: " + titleId));

        Episode episode = null;
        if (request.getEpisodeId() != null) {
            episode = episodeRepository.findById(request.getEpisodeId())
                    .orElseThrow(() -> new EntityNotFoundException("Episode not found: " + request.getEpisodeId()));
        }

        final Episode resolvedEpisode = episode;

        Optional<WatchProgress> existing = resolvedEpisode != null
                ? watchProgressRepository.findByUserIdAndTitleIdAndEpisodeId(
                        user.getId(), titleId, resolvedEpisode.getId())
                : watchProgressRepository.findByUserIdAndTitleIdAndEpisodeIdIsNull(user.getId(), titleId);

        WatchProgress progress = existing.orElseGet(() -> WatchProgress.builder()
                .user(user)
                .title(title)
                .episode(resolvedEpisode)
                .build());

        boolean completed = request.getDurationSeconds() > 0
                && (double) request.getProgressSeconds() / request.getDurationSeconds() >= COMPLETION_THRESHOLD;

        progress.setProgressSeconds(request.getProgressSeconds());
        progress.setDurationSeconds(request.getDurationSeconds());
        progress.setCompleted(completed);
        progress.setLastWatchedAt(LocalDateTime.now());

        WatchProgress saved = watchProgressRepository.save(progress);
        log.debug("Progress saved for user={} title={} progress={}s", user.getId(), titleId, request.getProgressSeconds());

        return toProgressDto(saved);
    }

    // ── Continue Watching ─────────────────────────────────

    @Transactional(readOnly = true)
    public List<ContinueWatchingDto> getContinueWatching(UUID userId) {
        return watchProgressRepository
                .findByUserIdAndCompletedFalseOrderByLastWatchedAtDesc(userId)
                .stream()
                .map(this::toContinueWatchingDto)
                .collect(Collectors.toList());
    }

    // ── Private helpers ───────────────────────────────────

    private String resolveTrailerUrl(String tmdbMediaType, Integer tmdbId) {
        if (tmdbId == null) return null;

        try {
            JsonNode videos = tmdbCacheService.getVideos(tmdbMediaType, tmdbId);
            JsonNode results = videos.path("results");
            if (results.isArray()) {
                for (JsonNode video : results) {
                    String site = video.path("site").asText();
                    String type = video.path("type").asText();
                    if ("YouTube".equals(site) && ("Trailer".equals(type) || "Teaser".equals(type))) {
                        return "https://www.youtube.com/watch?v=" + video.path("key").asText();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch videos for tmdbId={}: {}", tmdbId, e.getMessage());
        }

        return null;
    }

    private ProgressDto toProgressDto(@NonNull WatchProgress wp) {
        return ProgressDto.builder()
                .id(wp.getId())
                .titleId(wp.getTitle().getId())
                .episodeId(wp.getEpisode() != null ? wp.getEpisode().getId() : null)
                .progressSeconds(wp.getProgressSeconds())
                .durationSeconds(wp.getDurationSeconds())
                .completed(wp.isCompleted())
                .lastWatchedAt(wp.getLastWatchedAt() != null ? wp.getLastWatchedAt().toString() : null)
                .build();
    }

    private ContinueWatchingDto toContinueWatchingDto(@NonNull WatchProgress wp) {
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