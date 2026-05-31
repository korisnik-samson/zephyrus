package com.samson.zephyrus.notification;

import com.samson.zephyrus.auth.model.User;
import com.samson.zephyrus.notification.dto.NotificationResponse;
import com.samson.zephyrus.notification.model.Notification;
import com.samson.zephyrus.notification.model.NotificationType;
import com.samson.zephyrus.notification.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // ── Read ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(UUID userId, int page, int size) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    // ── Mutations ─────────────────────────────────────────

    @Transactional
    public void markRead(UUID userId, UUID notificationId) {
        Notification n = requireOwned(userId, notificationId);
        n.setRead(true);
        notificationRepository.save(n);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        notificationRepository.markAllReadByUserId(userId);
    }

    @Transactional
    public void delete(UUID userId, UUID notificationId) {
        notificationRepository.delete(requireOwned(userId, notificationId));
    }

    /**
     * Internal method called by other services to deliver a notification.
     */
    @Transactional
    public void send(User user, NotificationType type, String title, String body, String actionUrl) {
        notificationRepository.save(Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .body(body)
                .actionUrl(actionUrl)
                .build());
        log.debug("Notification queued for user={} type={}", user.getId(), type);
    }

    // ── Private helpers ───────────────────────────────────

    private Notification requireOwned(UUID userId, UUID notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found: " + notificationId));
        if (!n.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Notification does not belong to current user");
        }
        return n;
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType().name())
                .title(n.getTitle())
                .body(n.getBody())
                .actionUrl(n.getActionUrl())
                .read(n.isRead())
                .createdAt(n.getCreatedAt() != null ? n.getCreatedAt().toString() : null)
                .build();
    }
}