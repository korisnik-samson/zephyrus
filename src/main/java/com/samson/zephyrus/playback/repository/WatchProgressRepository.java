package com.samson.zephyrus.playback.repository;

import com.samson.zephyrus.playback.model.WatchProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WatchProgressRepository extends JpaRepository<WatchProgress, UUID> {

    /**
     * Find progress for a specific title (movie) by user.
     */
    Optional<WatchProgress> findByUserIdAndTitleIdAndEpisodeIdIsNull(UUID userId, UUID titleId);

    /**
     * Find progress for a specific episode by user.
     */
    Optional<WatchProgress> findByUserIdAndTitleIdAndEpisodeId(UUID userId, UUID titleId, UUID episodeId);

    /**
     * "Continue Watching" query — in-progress items ordered by last watched.
     */
    List<WatchProgress> findByUserIdAndCompletedFalseOrderByLastWatchedAtDesc(UUID userId);

    /**
     * Full watch history ordered by last watched.
     */
    List<WatchProgress> findByUserIdOrderByLastWatchedAtDesc(UUID userId);

    /**
     * Delete all progress for a user (account deletion support).
     */
    void deleteByUserId(UUID userId);
}
