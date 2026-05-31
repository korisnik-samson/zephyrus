-- ═══════════════════════════════════════════════════════════
-- V2: Content & Catalogue Tables
-- ═══════════════════════════════════════════════════════════

-- ── Genres ─────────────────────────────────────────────────
CREATE TABLE genres (
    id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tmdb_id  INT          NOT NULL UNIQUE,
    name     VARCHAR(100) NOT NULL
);

-- ── Titles (Movies & Series) ──────────────────────────────
CREATE TABLE titles (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tmdb_id            INT          UNIQUE,
    media_type         VARCHAR(10)  NOT NULL CHECK (media_type IN ('MOVIE', 'SERIES')),
    title              TEXT         NOT NULL,
    overview           TEXT,
    tagline            TEXT,
    release_date       DATE,
    runtime            INT,
    poster_path        TEXT,
    backdrop_path      TEXT,
    vote_average       DECIMAL(3,1),
    popularity         DECIMAL(10,2),
    original_language  VARCHAR(10),
    maturity_rating    VARCHAR(10),
    status             VARCHAR(20),
    created_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_titles_tmdb_id ON titles(tmdb_id);
CREATE INDEX idx_titles_media_type ON titles(media_type);
CREATE INDEX idx_titles_title ON titles USING gin(to_tsvector('english', title));

-- ── Title ↔ Genre (Many-to-Many) ──────────────────────────
CREATE TABLE title_genres (
    title_id UUID NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    genre_id UUID NOT NULL REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (title_id, genre_id)
);

CREATE INDEX idx_title_genres_genre_id ON title_genres(genre_id);

-- ── Seasons ───────────────────────────────────────────────
CREATE TABLE seasons (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title_id        UUID NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    season_number   INT  NOT NULL,
    name            TEXT,
    overview        TEXT,
    poster_path     TEXT,
    episode_count   INT,
    air_date        DATE
);

CREATE INDEX idx_seasons_title_id ON seasons(title_id);

-- ── Episodes ──────────────────────────────────────────────
CREATE TABLE episodes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    season_id       UUID NOT NULL REFERENCES seasons(id) ON DELETE CASCADE,
    episode_number  INT  NOT NULL,
    name            TEXT,
    overview        TEXT,
    still_path      TEXT,
    runtime         INT,
    air_date        DATE
);

CREATE INDEX idx_episodes_season_id ON episodes(season_id);

-- ── Cast Members ──────────────────────────────────────────
CREATE TABLE cast_members (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tmdb_person_id  INT,
    name            TEXT         NOT NULL,
    character_name  TEXT,
    profile_path    TEXT,
    title_id        UUID NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    display_order   INT
);

CREATE INDEX idx_cast_members_title_id ON cast_members(title_id);

-- ── Content Rows (Home page sections) ─────────────────────
CREATE TABLE content_rows (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    label      TEXT         NOT NULL,
    row_type   VARCHAR(20)  NOT NULL CHECK (row_type IN ('TRENDING', 'NEW_RELEASES', 'TOP_RATED', 'GENRE')),
    genre_id   UUID REFERENCES genres(id) ON DELETE SET NULL,
    sort_order INT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_content_rows_sort_order ON content_rows(sort_order);
