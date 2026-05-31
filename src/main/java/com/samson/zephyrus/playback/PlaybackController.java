package com.samson.zephyrus.playback;

import com.samson.zephyrus.auth.model.User;
import com.samson.zephyrus.common.ApiResponse;
import com.samson.zephyrus.playback.dto.ContinueWatchingDto;
import com.samson.zephyrus.playback.dto.ProgressDto;
import com.samson.zephyrus.playback.dto.SaveProgressRequest;
import com.samson.zephyrus.playback.dto.StreamDto;
import com.samson.zephyrus.playback.WatchHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/playback")
@RequiredArgsConstructor
@Tag(name = "Playback", description = "Stream URLs and watch progress tracking")
public class PlaybackController {

    private final PlaybackService playbackService;
    private final WatchHistoryService watchHistoryService;

    // ── Stream URL ────────────────────────────────────────

    @GetMapping("/{id}/stream")
    @Operation(summary = "Get stream URL", description = "Returns a playable stream URL for the given title (YouTube trailer for demo)")
    public ResponseEntity<ApiResponse<StreamDto>> getStreamUrl(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {

        log.debug("GET /api/playback/{}/stream user={}", id, user.getId());
        StreamDto stream = playbackService.getStreamUrl(id);

        return ResponseEntity.ok(ApiResponse.success(stream));
    }

    // ── Progress ──────────────────────────────────────────

    @GetMapping("/{id}/progress")
    @Operation(summary = "Get watch progress", description = "Returns saved playback progress for a title or episode")
    public ResponseEntity<ApiResponse<ProgressDto>> getProgress(
            @PathVariable UUID id,
            @Parameter(description = "Episode ID (required for series)") @RequestParam(required = false) UUID episodeId,
            @AuthenticationPrincipal User user) {

        log.debug("GET /api/playback/{}/progress episodeId={} user={}", id, episodeId, user.getId());
        Optional<ProgressDto> progress = playbackService.getProgress(user.getId(), id, episodeId);

        return progress
                .map(p -> ResponseEntity.ok(ApiResponse.success(p)))
                .orElse(ResponseEntity.ok(ApiResponse.success(null)));
    }

    @PostMapping("/{id}/progress")
    @Operation(summary = "Save watch progress", description = "Upserts playback progress for a title or episode. Call every ~10 seconds from the player.")
    public ResponseEntity<ApiResponse<ProgressDto>> saveProgress(
            @PathVariable UUID id,
            @RequestBody @Valid SaveProgressRequest request,
            @AuthenticationPrincipal User user) {

        log.debug("POST /api/playback/{}/progress user={}", id, user.getId());
        ProgressDto saved = playbackService.saveProgress(user, id, request);

        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    // ── Continue Watching ─────────────────────────────────

    @GetMapping("/continue-watching")
    @Operation(summary = "Continue watching", description = "Returns in-progress titles for the current user, ordered by last watched")
    public ResponseEntity<ApiResponse<List<ContinueWatchingDto>>> getContinueWatching(
            @AuthenticationPrincipal User user) {

        log.debug("GET /api/playback/continue-watching user={}", user.getId());
        List<ContinueWatchingDto> items = playbackService.getContinueWatching(user.getId());

        return ResponseEntity.ok(ApiResponse.success(items));
    }

    // ── Watch History ─────────────────────────────────────

    @GetMapping("/history")
    @Operation(summary = "Watch history", description = "Full watch history for the current user including completed titles, newest first")
    public ResponseEntity<ApiResponse<List<ContinueWatchingDto>>> getHistory(
            @AuthenticationPrincipal User user) {

        log.debug("GET /api/playback/history user={}", user.getId());
        return ResponseEntity.ok(ApiResponse.success(watchHistoryService.getHistory(user.getId())));
    }
}