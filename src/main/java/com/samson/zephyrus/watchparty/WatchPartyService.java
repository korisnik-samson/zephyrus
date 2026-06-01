package com.samson.zephyrus.watchparty;

import com.samson.zephyrus.content.repository.TitleRepository;
import com.samson.zephyrus.watchparty.dto.WatchPartyMessage;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the lifecycle and shared playback state of watch-party rooms.
 * State is held in-memory; rooms are evicted once the last participant leaves.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WatchPartyService {

    private final TitleRepository titleRepository;
    private final Map<String, WatchPartyRoom> rooms = new ConcurrentHashMap<>();

    public WatchPartyRoom createRoom(UUID hostUserId, UUID titleId) {
        if (!titleRepository.existsById(titleId)) {
            throw new EntityNotFoundException("Title not found: " + titleId);
        }
        String roomId = UUID.randomUUID().toString().substring(0, 8);
        WatchPartyRoom room = new WatchPartyRoom(roomId, hostUserId, titleId);
        rooms.put(roomId, room);
        log.info("Watch party created room={} host={} title={}", roomId, hostUserId, titleId);
        return room;
    }

    public WatchPartyRoom getRoom(String roomId) {
        WatchPartyRoom room = rooms.get(roomId);
        if (room == null) {
            throw new EntityNotFoundException("Watch party room not found: " + roomId);
        }
        return room;
    }

    public void join(String roomId, String participant) {
        getRoom(roomId).getParticipants().add(participant);
        log.debug("{} joined room {}", participant, roomId);
    }

    public void leave(String roomId, String participant) {
        WatchPartyRoom room = rooms.get(roomId);
        if (room == null) return;
        room.getParticipants().remove(participant);
        if (room.getParticipants().isEmpty()) {
            rooms.remove(roomId);
            log.info("Watch party room {} evicted (empty)", roomId);
        }
    }

    /**
     * Applies an incoming control message to the room's shared state and returns
     * the message to broadcast (timestamped).
     */
    public WatchPartyMessage process(String roomId, WatchPartyMessage message) {
        WatchPartyRoom room = getRoom(roomId);
        message.setTimestamp(System.currentTimeMillis());

        switch (message.getType()) {
            case JOIN -> join(roomId, message.getSenderName());
            case LEAVE -> leave(roomId, message.getSenderName());
            case PLAY -> room.applyPlayback(true, message.getPositionSeconds());
            case PAUSE -> room.applyPlayback(false, message.getPositionSeconds());
            case SEEK -> room.applyPlayback(room.isPlaying(), message.getPositionSeconds());
            case CHAT, STATE -> { /* no state mutation */ }
        }

        // Reflect authoritative room state on the outgoing message
        message.setPlaying(room.isPlaying());
        if (message.getType() != WatchPartyMessage.Type.CHAT) {
            message.setPositionSeconds(room.getPositionSeconds());
        }
        return message;
    }
}