package com.samson.zephyrus.recommendation;

import com.samson.zephyrus.auth.model.User;
import com.samson.zephyrus.common.ApiResponse;
import com.samson.zephyrus.content.dto.TitleSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations", description = "Personalized content recommendations based on watch history")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    @Operation(
        summary = "Get recommendations",
        description = "Returns personalized title recommendations based on the user's top watched genres. " +
                      "Falls back to overall popularity for new users with no history."
    )
    public ResponseEntity<ApiResponse<List<TitleSummaryDto>>> getRecommendations(
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal User user) {
        log.debug("GET /api/recommendations user={} limit={}", user.getId(), limit);
        List<TitleSummaryDto> recs = recommendationService.getRecommendations(user.getId(), limit);
        return ResponseEntity.ok(ApiResponse.success(recs));
    }
}