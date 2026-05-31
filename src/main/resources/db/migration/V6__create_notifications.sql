-- ════════════════════════════════════════════════════════
-- V6: In-App Notifications
-- ════════════════════════════════════════════════════════

CREATE TABLE notifications (
    id         UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type       VARCHAR(30) NOT NULL,
    title      TEXT    NOT NULL,
    body       TEXT,
    action_url TEXT,
    read       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_user   ON notifications(user_id);
CREATE INDEX idx_notifications_unread ON notifications(user_id, read) WHERE NOT read;