package com.samson.zephyrus.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Redis-backed search analytics.
 *
 * <ul>
 *   <li>Trending: global sorted set {@code search:trending} — score = cumulative search count</li>
 *   <li>Recent: per-user list {@code search:recent:{userId}} — capped at {@value MAX_RECENT}</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchAnalyticsService {

    private static final String TRENDING_KEY = "search:trending";
    private static final String RECENT_PREFIX = "search:recent:";
    private static final int MAX_RECENT = 20;
    private static final int MIN_QUERY_LENGTH = 2;

    private final StringRedisTemplate stringRedisTemplate;

    public void recordSearch(UUID userId, String query) {
        if (query == null || query.length() < MIN_QUERY_LENGTH) return;
        String normalized = query.toLowerCase().trim();

        try {
            // Global trending (sorted set: ZINCRBY)
            stringRedisTemplate.opsForZSet().incrementScore(TRENDING_KEY, normalized, 1.0);

            // Per-user recent searches (deduplicated LIFO list)
            if (userId != null) {
                String recentKey = RECENT_PREFIX + userId;
                stringRedisTemplate.opsForList().remove(recentKey, 0, normalized);
                stringRedisTemplate.opsForList().leftPush(recentKey, normalized);
                stringRedisTemplate.opsForList().trim(recentKey, 0, MAX_RECENT - 1);
            }
        } catch (Exception e) {
            log.warn("Failed to record search analytics: {}", e.getMessage());
        }
    }

    public List<String> getTrendingSearches(int limit) {
        try {
            Set<String> trending = stringRedisTemplate.opsForZSet()
                    .reverseRange(TRENDING_KEY, 0, limit - 1);
            return trending != null ? List.copyOf(trending) : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to retrieve trending searches: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<String> getRecentSearches(UUID userId) {
        try {
            List<String> recent = stringRedisTemplate.opsForList()
                    .range(RECENT_PREFIX + userId, 0, MAX_RECENT - 1);
            return recent != null ? recent : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to retrieve recent searches for user={}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    public void clearRecentSearches(UUID userId) {
        try {
            stringRedisTemplate.delete(RECENT_PREFIX + userId);
        } catch (Exception e) {
            log.warn("Failed to clear recent searches for user={}: {}", userId, e.getMessage());
        }
    }
}