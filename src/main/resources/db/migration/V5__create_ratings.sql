-- ════════════════════════════════════════════════════════
-- V5: Ratings & Reviews
-- ════════════════════════════════════════════════════════

CREATE TABLE ratings (
    id         UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID      NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
    title_id   UUID      NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    score      SMALLINT  NOT NULL CHECK (score BETWEEN 1 AND 10),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_rating UNIQUE (user_id, title_id)
);

CREATE INDEX idx_ratings_title ON ratings(title_id);
CREATE INDEX idx_ratings_user  ON ratings(user_id);

-- ── Reviews ───────────────────────────────────────────────
-- One review per user per title; can be updated in place.

CREATE TABLE reviews (
    id                UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID    NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
    title_id          UUID    NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    body              TEXT    NOT NULL,
    contains_spoilers BOOLEAN NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_review UNIQUE (user_id, title_id)
);

CREATE INDEX idx_reviews_title ON reviews(title_id);
CREATE INDEX idx_reviews_user  ON reviews(user_id);