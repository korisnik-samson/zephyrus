package com.samson.zephyrus.admin.analytics;

import com.samson.zephyrus.admin.analytics.dto.DashboardStatsDto;
import com.samson.zephyrus.admin.analytics.dto.TopContentDto;
import com.samson.zephyrus.admin.analytics.dto.UserGrowthDto;
import com.samson.zephyrus.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin — Analytics", description = "Platform usage statistics for the admin dashboard")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard overview", description = "Returns key stats: DAU/MAU, new users, total content, total watch time")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> dashboard() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getDashboardStats()));
    }

    @GetMapping("/top-content")
    @Operation(summary = "Top watched content", description = "Returns the most-watched titles by play count, with total watch minutes")
    public ResponseEntity<ApiResponse<List<TopContentDto>>> topContent(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getTopContent(limit)));
    }

    @GetMapping("/user-growth")
    @Operation(summary = "User registration growth", description = "Daily new user registrations for the last N days (max 365)")
    public ResponseEntity<ApiResponse<List<UserGrowthDto>>> userGrowth(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getUserGrowth(days)));
    }
}