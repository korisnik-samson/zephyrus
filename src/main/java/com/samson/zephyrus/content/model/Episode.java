package com.samson.zephyrus.content.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * An episode within a season.
 */
@Entity
@Table(name = "episodes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    @ToString.Exclude
    private Season season;

    @Column(name = "episode_number", nullable = false)
    private Integer episodeNumber;

    @Column(columnDefinition = "TEXT")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "still_path", columnDefinition = "TEXT")
    private String stillPath;

    private Integer runtime;

    @Column(name = "air_date")
    private LocalDate airDate;
}
