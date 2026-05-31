package com.samson.zephyrus.admin.analytics;

import com.samson.zephyrus.admin.analytics.dto.DashboardStatsDto;
import com.samson.zephyrus.admin.analytics.dto.TopContentDto;
import com.samson.zephyrus.admin.analytics.dto.UserGrowthDto;
import com.samson.zephyrus.auth.repository.UserRepository;
import com.samson.zephyrus.content.model.MediaType;
import com.samson.zephyrus.content.repository.TitleRepository;
import com.samson.zephyrus.playback.repository.WatchProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsService {

    private final UserRepository userRepository;
    private final TitleRepository titleRepository;
    private final WatchProgressRepository watchProgressRepository;

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIDNIGHT);
        LocalDateTime monthStart = LocalDateTime.now().minusDays(30);

        long totalProgressSeconds = watchProgressRepository.sumTotalProgressSeconds();

        return DashboardStatsDto.builder()
                .totalUsers(userRepository.count())
                .activeToday(watchProgressRepository.countDistinctActiveUsersSince(todayStart))
                .activeThisMonth(watchProgressRepository.countDistinctActiveUsersSince(monthStart))
                .newUsersToday(userRepository.countByCreatedAtAfter(todayStart))
                .newUsersThisMonth(userRepository.countByCreatedAtAfter(monthStart))
                .totalTitles(titleRepository.count())
                .totalMovies(titleRepository.countByMediaType(MediaType.MOVIE))
                .totalSeries(titleRepository.countByMediaType(MediaType.SERIES))
                .totalWatchTimeHours(Math.round((totalProgressSeconds / 3600.0) * 10.0) / 10.0)
                .build();
    }

    @Transactional(readOnly = true)
    public List<TopContentDto> getTopContent(int limit) {
        return watchProgressRepository.findTopWatchedContent(Math.min(limit, 100))
                .stream()
                .map(row -> new TopContentDto(
                        UUID.fromString(String.valueOf(row[0])),
                        String.valueOf(row[1]),
                        row[2] != null ? String.valueOf(row[2]) : null,
                        String.valueOf(row[3]),
                        ((Number) row[4]).longValue(),
                        ((Number) row[5]).longValue() / 60
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserGrowthDto> getUserGrowth(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(Math.min(days, 365));
        return userRepository.countRegistrationsByDay(since)
                .stream()
                .map(row -> new UserGrowthDto(
                        String.valueOf(row[0]),
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
    }
}