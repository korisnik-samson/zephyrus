package com.samson.zephyrus.rating;

import com.samson.zephyrus.auth.model.User;
import com.samson.zephyrus.common.ApiResponse;
import com.samson.zephyrus.rating.dto.RateRequest;
import com.samson.zephyrus.rating.dto.RatingResponse;
import com.samson.zephyrus.rating.dto.ReviewRequest;
import com.samson.zephyrus.rating.dto.ReviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Ratings & Reviews", description = "Rate titles and write reviews")
public class RatingController {

    private final RatingService ratingService;

    // ── Ratings ───────────────────────────────────────────

    @GetMapping("/content/{id}/rating")
    @Operation(summary = "Get rating", description = "Returns the current user's score plus aggregate stats for the title")
    public ResponseEntity<ApiResponse<RatingResponse>> getMyRating(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(ratingService.getMyRating(user.getId(), id)));
    }

    @PostMapping("/content/{id}/rating")
    @Operation(summary = "Rate title", description = "Submit or update a score (1–10) for a title")
    public ResponseEntity<ApiResponse<RatingResponse>> rateTitle(
            @PathVariable UUID id,
            @RequestBody @Valid RateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(ratingService.rateTitle(user, id, request)));
    }

    @DeleteMapping("/content/{id}/rating")
    @Operation(summary = "Remove rating")
    public ResponseEntity<ApiResponse<Void>> deleteRating(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        ratingService.deleteRating(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Rating removed"));
    }

    // ── Reviews ───────────────────────────────────────────

    @GetMapping("/content/{id}/reviews")
    @Operation(summary = "List reviews", description = "Returns paginated reviews for a title, newest first")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviews(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(ratingService.getReviews(id, page, size)));
    }

    @PostMapping("/content/{id}/reviews")
    @Operation(summary = "Write review", description = "Submit or update a text review for a title")
    public ResponseEntity<ApiResponse<ReviewResponse>> writeReview(
            @PathVariable UUID id,
            @RequestBody @Valid ReviewRequest request,
            @AuthenticationPrincipal User user) {
        ReviewResponse review = ratingService.writeReview(user, id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(review));
    }

    @DeleteMapping("/reviews/{reviewId}")
    @Operation(summary = "Delete review")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal User user) {
        ratingService.deleteReview(user.getId(), reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review deleted"));
    }
}