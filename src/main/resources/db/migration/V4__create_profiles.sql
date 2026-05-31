-- ════════════════════════════════════════════════════════
-- V4: User Profiles (Phase 2)
-- Each account can have up to 5 named profiles (Netflix-style).
-- ════════════════════════════════════════════════════════

CREATE TABLE profiles (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name         VARCHAR(50)  NOT NULL,
    avatar_url   TEXT,
    kids_mode    BOOLEAN      NOT NULL DEFAULT FALSE,
    pin_hash     TEXT,
    sort_order   INT          NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_profile_name_per_user UNIQUE (user_id, name)
);

CREATE INDEX idx_profiles_user_id ON profiles(user_id);

-- ── My List ──────────────────────────────────────────────
-- Stores the titles a profile has bookmarked.

CREATE TABLE my_list (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    title_id   UUID NOT NULL REFERENCES titles(id)   ON DELETE CASCADE,
    added_at   TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_my_list_entry UNIQUE (profile_id, title_id)
);

CREATE INDEX idx_my_list_profile_id ON my_list(profile_id);