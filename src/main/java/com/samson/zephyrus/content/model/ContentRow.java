package com.samson.zephyrus.content.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * A curated row on the home / browse page (e.g. "Trending", "Top Rated", genre-based).
 */
@Entity
@Table(name = "content_rows")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ContentRow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "row_type", nullable = false, length = 20)
    private RowType rowType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id")
    private Genre genre;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
