package com.samson.zephyrus.playback.model;

import com.samson.zephyrus.auth.model.User;
import com.samson.zephyrus.content.model.Episode;
import com.samson.zephyrus.content.model.Title;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tracks a user's playback progress for a title (movie or specific episode).
 */
@Entity
@Table(name = "watch_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "title_id", "episode_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id", nullable = false)
    private Title title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id")
    private Episode episode;

    @Column(name = "progress_seconds", nullable = false)
    private int progressSeconds;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Column(nullable = false)
    @Builder.Default
    private boolean completed = false;

    @Column(name = "last_watched_at", nullable = false)
    private LocalDateTime lastWatchedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
