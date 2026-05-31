package com.samson.zephyrus.rating.repository;

import com.samson.zephyrus.rating.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Optional<Review> findByUserIdAndTitleId(UUID userId, UUID titleId);

    Page<Review> findByTitleId(UUID titleId, Pageable pageable);
}