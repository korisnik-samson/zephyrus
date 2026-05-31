package com.samson.zephyrus.playback.repository;

import com.samson.zephyrus.playback.model.WatchProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    // ── Analytics ─────────────────────────────────────────

    @Query(value = "SELECT COUNT(DISTINCT user_id) FROM watch_progress WHERE last_watched_at >= :since", nativeQuery = true)
    long countDistinctActiveUsersSince(@Param("since") LocalDateTime since);

    @Query(value = "SELECT COALESCE(SUM(progress_seconds), 0) FROM watch_progress", nativeQuery = true)
    long sumTotalProgressSeconds();

    @Query(
        value = "SELECT t.id::text, t.title, t.poster_path, t.media_type, " +
                "COUNT(wp.id) AS watch_count, COALESCE(SUM(wp.progress_seconds), 0) AS total_seconds " +
                "FROM watch_progress wp JOIN titles t ON wp.title_id = t.id " +
                "GROUP BY t.id, t.title, t.poster_path, t.media_type " +
                "ORDER BY watch_count DESC LIMIT :limit",
        nativeQuery = true
    )
    List<Object[]> findTopWatchedContent(@Param("limit") int limit);
}
