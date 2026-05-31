package com.samson.zephyrus.rating.repository;

import com.samson.zephyrus.rating.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {

    Optional<Rating> findByUserIdAndTitleId(UUID userId, UUID titleId);

    long countByTitleId(UUID titleId);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.title.id = :titleId")
    Double findAverageScoreByTitleId(@Param("titleId") UUID titleId);

    void deleteByUserIdAndTitleId(UUID userId, UUID titleId);
}