package com.samson.zephyrus.content.repository;

import com.samson.zephyrus.content.model.MediaType;
import com.samson.zephyrus.content.model.Title;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TitleRepository extends JpaRepository<Title, UUID> {

    Optional<Title> findByTmdbId(Integer tmdbId);

    Page<Title> findByMediaType(MediaType mediaType, Pageable pageable);

    @Query("SELECT t FROM Title t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Title> searchByTitle(@Param("query") String query, Pageable pageable);

    @Query("SELECT t FROM Title t JOIN t.genres g WHERE g.id = :genreId")
    Page<Title> findByGenreId(@Param("genreId") UUID genreId, Pageable pageable);

    @Query("SELECT t FROM Title t ORDER BY t.popularity DESC")
    Page<Title> findTopByPopularity(Pageable pageable);

    @Query("SELECT t FROM Title t ORDER BY t.voteAverage DESC")
    Page<Title> findTopByVoteAverage(Pageable pageable);

    @Query("SELECT t FROM Title t ORDER BY t.createdAt DESC")
    Page<Title> findLatest(Pageable pageable);
}
