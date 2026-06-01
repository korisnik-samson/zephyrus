package com.samson.zephyrus.watchparty.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A real-time message exchanged between watch-party participants.
 * The same shape is used for both inbound (client → server) and
 * broadcast (server → /topic) messages.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchPartyMessage {

    public enum Type { JOIN, LEAVE, PLAY, PAUSE, SEEK, CHAT, STATE }

    private Type type;
    private String senderName;
    /** Playback head position in seconds (for PLAY/PAUSE/SEEK/STATE). */
    private double positionSeconds;
    /** Whether playback is currently playing (for STATE broadcasts). */
    private boolean playing;
    /** Free-text body for CHAT messages. */
    private String content;
    /** Server timestamp (epoch millis). */
    private long timestamp;
}