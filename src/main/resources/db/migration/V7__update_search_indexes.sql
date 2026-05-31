-- ════════════════════════════════════════════════════════
-- V7: Improve full-text search indexes
-- Replace the title-only GIN index with a combined
-- title + overview index so searches match both fields.
-- ════════════════════════════════════════════════════════

DROP INDEX IF EXISTS idx_titles_title;

CREATE INDEX idx_titles_fts
    ON titles
    USING gin(to_tsvector('english', title || ' ' || COALESCE(overview, '')));