package com.samson.zephyrus.admin.analytics.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsDto {
    private long totalUsers;
    private long activeToday;
    private long activeThisMonth;
    private long newUsersToday;
    private long newUsersThisMonth;
    private long totalTitles;
    private long totalMovies;
    private long totalSeries;
    /** Total platform watch time in hours, rounded to 1 dp */
    private double totalWatchTimeHours;
}