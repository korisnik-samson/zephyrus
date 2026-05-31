package com.samson.zephyrus.content.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A season belonging to a TV series title.
 */
@Entity
@Table(name = "seasons")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Season {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id", nullable = false)
    @ToString.Exclude
    private Title title;

    @Column(name = "season_number", nullable = false)
    private Integer seasonNumber;

    @Column(columnDefinition = "TEXT")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "poster_path", columnDefinition = "TEXT")
    private String posterPath;

    @Column(name = "episode_count")
    private Integer episodeCount;

    @Column(name = "air_date")
    private LocalDate airDate;

    // ── Relationships ─────────────────────────────────────

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Episode> episodes = new ArrayList<>();
}
