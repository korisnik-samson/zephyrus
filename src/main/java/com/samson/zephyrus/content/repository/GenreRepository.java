package com.samson.zephyrus.content.repository;

import com.samson.zephyrus.content.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GenreRepository extends JpaRepository<Genre, UUID> {

    Optional<Genre> findByTmdbId(Integer tmdbId);

    Optional<Genre> findByName(String name);
}
