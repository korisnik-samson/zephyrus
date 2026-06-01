package com.samson.zephyrus.watchparty;

import com.samson.zephyrus.watchparty.dto.WatchPartyMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Handles real-time STOMP messages for watch parties.
 * Clients send to {@code /app/party/{roomId}}; the processed message is
 * broadcast to all subscribers of {@code /topic/party/{roomId}}.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WatchPartyStompController {

    private final WatchPartyService watchPartyService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/party/{roomId}")
    public void onMessage(@DestinationVariable String roomId,
                          @Payload WatchPartyMessage message) {
        WatchPartyMessage broadcast = watchPartyService.process(roomId, message);
        messagingTemplate.convertAndSend("/topic/party/" + roomId, broadcast);
    }
}