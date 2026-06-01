package com.samson.zephyrus.watchparty;

import lombok.Data;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory state for a single watch party. Not persisted — rooms live only
 * for the duration of the session and are evicted when empty.
 */
@Data
public class WatchPartyRoom {

    private final String roomId;
    private final UUID hostUserId;
    private final UUID titleId;
    private final Set<String> participants = ConcurrentHashMap.newKeySet();

    private volatile boolean playing = false;
    private volatile double positionSeconds = 0.0;
    private volatile long lastUpdatedAt = System.currentTimeMillis();

    public WatchPartyRoom(String roomId, UUID hostUserId, UUID titleId) {
        this.roomId = roomId;
        this.hostUserId = hostUserId;
        this.titleId = titleId;
    }

    public void applyPlayback(boolean playing, double positionSeconds) {
        this.playing = playing;
        this.positionSeconds = positionSeconds;
        this.lastUpdatedAt = System.currentTimeMillis();
    }
}