-- ════════════════════════════════════════════════════════
-- V3: Playback / Watch Progress Tables
-- ════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS watch_progress (
    id               UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title_id         UUID NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    episode_id       UUID REFERENCES episodes(id) ON DELETE SET NULL,
    progress_seconds INT  NOT NULL DEFAULT 0,
    duration_seconds INT  NOT NULL DEFAULT 0,
    completed        BOOLEAN NOT NULL DEFAULT FALSE,
    last_watched_at  TIMESTAMP NOT NULL DEFAULT now(),
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now(),

    -- One progress record per user + title (+ episode)
    CONSTRAINT uq_watch_progress UNIQUE (user_id, title_id, episode_id)
);

CREATE INDEX idx_watch_progress_user ON watch_progress(user_id);
CREATE INDEX idx_watch_progress_user_last ON watch_progress(user_id, last_watched_at DESC);
