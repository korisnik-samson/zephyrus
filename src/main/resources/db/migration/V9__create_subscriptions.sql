-- ════════════════════════════════════════════════════════
-- V9: Subscriptions (Stripe-backed premium tiers)
-- ════════════════════════════════════════════════════════

CREATE TABLE subscriptions (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                UUID         NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    stripe_customer_id     VARCHAR(255),
    stripe_subscription_id VARCHAR(255) UNIQUE,
    tier                   VARCHAR(20)  NOT NULL DEFAULT 'FREE'
                               CHECK (tier IN ('FREE', 'BASIC', 'STANDARD', 'PREMIUM')),
    status                 VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                               CHECK (status IN ('ACTIVE', 'TRIALING', 'PAST_DUE', 'CANCELED', 'INCOMPLETE')),
    current_period_end     TIMESTAMP,
    cancel_at_period_end   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at             TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subscriptions_user            ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_stripe_customer ON subscriptions(stripe_customer_id);
CREATE INDEX idx_subscriptions_stripe_sub      ON subscriptions(stripe_subscription_id);