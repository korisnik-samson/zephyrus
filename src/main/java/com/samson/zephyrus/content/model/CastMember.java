package com.samson.zephyrus.content.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * A cast member (actor / character) associated with a title.
 */
@Entity
@Table(name = "cast_members")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CastMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "tmdb_person_id")
    private Integer tmdbPersonId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String name;

    @Column(name = "character_name", columnDefinition = "TEXT")
    private String characterName;

    @Column(name = "profile_path", columnDefinition = "TEXT")
    private String profilePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id", nullable = false)
    @ToString.Exclude
    private Title title;

    @Column(name = "display_order")
    private Integer displayOrder;
}
