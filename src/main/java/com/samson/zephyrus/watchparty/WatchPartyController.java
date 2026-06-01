package com.samson.zephyrus.watchparty;

import com.samson.zephyrus.auth.model.User;
import com.samson.zephyrus.common.ApiResponse;
import com.samson.zephyrus.subscription.RequiresTier;
import com.samson.zephyrus.subscription.model.SubscriptionTier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST endpoints to create and inspect watch-party rooms.
 * Real-time sync happens over STOMP (see {@link WatchPartyStompController}).
 * Hosting a party is a PREMIUM-tier feature.
 */
@RestController
@RequestMapping("/api/watch-party")
@RequiredArgsConstructor
@Tag(name = "Watch Party", description = "Synchronized group viewing (PREMIUM)")
public class WatchPartyController {

    private final WatchPartyService watchPartyService;

    @PostMapping
    @RequiresTier(SubscriptionTier.PREMIUM)
    @Operation(summary = "Create a watch party", description = "Creates a room for a title and returns its join code. Requires PREMIUM.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(
            @RequestParam UUID titleId,
            @AuthenticationPrincipal User user) {
        WatchPartyRoom room = watchPartyService.createRoom(user.getId(), titleId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(Map.of(
                "roomId", room.getRoomId(),
                "titleId", room.getTitleId(),
                "wsEndpoint", "/ws",
                "topic", "/topic/party/" + room.getRoomId(),
                "publish", "/app/party/" + room.getRoomId()
        )));
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "Get room state", description = "Returns the current participants and playback state of a room")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoom(@PathVariable String roomId) {
        WatchPartyRoom room = watchPartyService.getRoom(roomId);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "roomId", room.getRoomId(),
                "titleId", room.getTitleId(),
                "hostUserId", room.getHostUserId(),
                "participants", room.getParticipants(),
                "playing", room.isPlaying(),
                "positionSeconds", room.getPositionSeconds()
        )));
    }
}