-- ════════════════════════════════════════════════════════
-- V8: Admin — content scheduling + user ban/suspend
-- ════════════════════════════════════════════════════════

-- ── Content scheduling ────────────────────────────────────
ALTER TABLE titles
    ADD COLUMN published       BOOLEAN   NOT NULL DEFAULT TRUE,
    ADD COLUMN available_from  TIMESTAMP,
    ADD COLUMN available_until TIMESTAMP;

CREATE INDEX idx_titles_published ON titles(published) WHERE NOT published;

-- ── User ban / suspend ────────────────────────────────────
ALTER TABLE users
    ADD COLUMN banned           BOOLEAN   NOT NULL DEFAULT FALSE,
    ADD COLUMN banned_at        TIMESTAMP,
    ADD COLUMN ban_reason       TEXT,
    ADD COLUMN suspended_until  TIMESTAMP;

CREATE INDEX idx_users_banned ON users(banned) WHERE banned;