package com.samson.zephyrus.content.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * A content genre sourced from TMDB.
 */
@Entity
@Table(name = "genres")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "tmdb_id", nullable = false, unique = true)
    private Integer tmdbId;

    @Column(nullable = false, length = 100)
    private String name;
}
