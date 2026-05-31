package com.samson.zephyrus.rating;

import com.samson.zephyrus.auth.model.User;
import com.samson.zephyrus.content.model.Title;
import com.samson.zephyrus.content.repository.TitleRepository;
import com.samson.zephyrus.rating.dto.RateRequest;
import com.samson.zephyrus.rating.dto.RatingResponse;
import com.samson.zephyrus.rating.dto.ReviewRequest;
import com.samson.zephyrus.rating.dto.ReviewResponse;
import com.samson.zephyrus.rating.model.Rating;
import com.samson.zephyrus.rating.model.Review;
import com.samson.zephyrus.rating.repository.RatingRepository;
import com.samson.zephyrus.rating.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final ReviewRepository reviewRepository;
    private final TitleRepository titleRepository;

    // ── Ratings ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public RatingResponse getMyRating(UUID userId, UUID titleId) {
        Integer myScore = ratingRepository.findByUserIdAndTitleId(userId, titleId)
                .map(r -> (int) r.getScore())
                .orElse(null);

        Double avg = ratingRepository.findAverageScoreByTitleId(titleId);
        long total = ratingRepository.countByTitleId(titleId);

        return RatingResponse.builder()
                .titleId(titleId)
                .myScore(myScore)
                .averageScore(avg != null ? Math.round(avg * 10.0) / 10.0 : null)
                .totalRatings(total)
                .build();
    }

    @Transactional
    public RatingResponse rateTitle(User user, UUID titleId, RateRequest request) {
        Title title = requireTitle(titleId);

        Rating rating = ratingRepository.findByUserIdAndTitleId(user.getId(), titleId)
                .orElseGet(() -> Rating.builder().user(user).title(title).build());

        rating.setScore((short) request.getScore().intValue());
        ratingRepository.save(rating);
        log.debug("User {} rated title {} → {}", user.getId(), titleId, request.getScore());

        return getMyRating(user.getId(), titleId);
    }

    @Transactional
    public void deleteRating(UUID userId, UUID titleId) {
        ratingRepository.deleteByUserIdAndTitleId(userId, titleId);
    }

    // ── Reviews ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviews(UUID titleId, int page, int size) {
        return reviewRepository
                .findByTitleId(titleId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::toReviewResponse);
    }

    @Transactional
    public ReviewResponse writeReview(User user, UUID titleId, ReviewRequest request) {
        Title title = requireTitle(titleId);

        Review review = reviewRepository.findByUserIdAndTitleId(user.getId(), titleId)
                .orElseGet(() -> Review.builder().user(user).title(title).build());

        review.setBody(request.getBody().trim());
        review.setContainsSpoilers(request.isContainsSpoilers());

        return toReviewResponse(reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(UUID userId, UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found: " + reviewId));
        if (!review.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Review does not belong to current user");
        }
        reviewRepository.delete(review);
    }

    // ── Private helpers ───────────────────────────────────

    private Title requireTitle(UUID titleId) {
        return titleRepository.findById(titleId)
                .orElseThrow(() -> new EntityNotFoundException("Title not found: " + titleId));
    }

    private ReviewResponse toReviewResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .userId(r.getUser().getId())
                .displayName(r.getUser().getDisplayName())
                .titleId(r.getTitle().getId())
                .body(r.getBody())
                .containsSpoilers(r.isContainsSpoilers())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toString() : null)
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                .build();
    }
}