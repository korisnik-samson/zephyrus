package com.samson.zephyrus.subscription;

import com.samson.zephyrus.auth.model.User;
import com.samson.zephyrus.subscription.config.StripeProperties;
import com.samson.zephyrus.subscription.dto.SubscriptionResponse;
import com.samson.zephyrus.subscription.model.Subscription;
import com.samson.zephyrus.subscription.model.SubscriptionStatus;
import com.samson.zephyrus.subscription.model.SubscriptionTier;
import com.samson.zephyrus.subscription.repository.SubscriptionRepository;
import com.stripe.Stripe;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final StripeProperties stripeProperties;

    @PostConstruct
    void init() {
        if (stripeProperties.isEnabled()) {
            Stripe.apiKey = stripeProperties.getApiKey();
            log.info("Stripe billing enabled");
        } else {
            log.info("Stripe billing disabled — subscriptions default to FREE tier");
        }
    }

    // ── Reads ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public SubscriptionTier getEffectiveTier(UUID userId) {
        return subscriptionRepository.findByUserId(userId)
                .map(Subscription::effectiveTier)
                .orElse(SubscriptionTier.FREE);
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getMySubscription(UUID userId) {
        return subscriptionRepository.findByUserId(userId)
                .map(this::toResponse)
                .orElseGet(this::freeResponse);
    }

    // ── Checkout ──────────────────────────────────────────

    @Transactional
    public String createCheckoutSession(User user, SubscriptionTier tier) {
        if (!stripeProperties.isEnabled()) {
            throw new IllegalStateException("Billing is not enabled on this server");
        }
        String priceId = stripeProperties.getPriceIds().get(tier.name());
        if (priceId == null || priceId.isBlank()) {
            throw new IllegalStateException("No Stripe price configured for tier " + tier);
        }

        try {
            String customerId = resolveOrCreateCustomer(user);

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setCustomer(customerId)
                    .setSuccessUrl(stripeProperties.getSuccessUrl())
                    .setCancelUrl(stripeProperties.getCancelUrl())
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setPrice(priceId)
                            .setQuantity(1L)
                            .build())
                    .putMetadata("userId", user.getId().toString())
                    .putMetadata("tier", tier.name())
                    .build();

            Session session = Session.create(params);
            log.info("Created Stripe checkout session for user={} tier={}", user.getId(), tier);
            return session.getUrl();
        } catch (Exception e) {
            log.error("Failed to create checkout session: {}", e.getMessage());
            throw new IllegalStateException("Could not start checkout: " + e.getMessage());
        }
    }

    // ── Webhook processing ────────────────────────────────

    /**
     * Applies a subscription state change parsed from a Stripe webhook event.
     * Called by {@link StripeWebhookController} after signature verification.
     */
    @Transactional
    public void applySubscriptionUpdate(String stripeCustomerId,
                                        String stripeSubscriptionId,
                                        SubscriptionTier tier,
                                        SubscriptionStatus status,
                                        Long currentPeriodEndEpoch,
                                        boolean cancelAtPeriodEnd) {
        Subscription sub = subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
                .or(() -> subscriptionRepository.findByStripeCustomerId(stripeCustomerId))
                .orElse(null);

        if (sub == null) {
            log.warn("No local subscription matched stripeCustomerId={} stripeSubscriptionId={}",
                    stripeCustomerId, stripeSubscriptionId);
            return;
        }

        sub.setStripeSubscriptionId(stripeSubscriptionId);
        if (tier != null) sub.setTier(tier);
        sub.setStatus(status);
        sub.setCancelAtPeriodEnd(cancelAtPeriodEnd);
        if (currentPeriodEndEpoch != null) {
            sub.setCurrentPeriodEnd(LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(currentPeriodEndEpoch), ZoneOffset.UTC));
        }
        subscriptionRepository.save(sub);
        log.info("Applied subscription update user={} tier={} status={}", sub.getUser().getId(), tier, status);
    }

    @Transactional
    public void markCanceled(String stripeSubscriptionId) {
        subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId).ifPresent(sub -> {
            sub.setStatus(SubscriptionStatus.CANCELED);
            sub.setTier(SubscriptionTier.FREE);
            subscriptionRepository.save(sub);
            log.info("Subscription canceled for user={}", sub.getUser().getId());
        });
    }

    // ── Private helpers ───────────────────────────────────

    private String resolveOrCreateCustomer(User user) throws Exception {
        Subscription sub = subscriptionRepository.findByUserId(user.getId())
                .orElseGet(() -> Subscription.builder()
                        .user(user)
                        .tier(SubscriptionTier.FREE)
                        .status(SubscriptionStatus.INCOMPLETE)
                        .build());

        if (sub.getStripeCustomerId() == null) {
            Customer customer = Customer.create(CustomerCreateParams.builder()
                    .setEmail(user.getEmail())
                    .setName(user.getDisplayName())
                    .putMetadata("userId", user.getId().toString())
                    .build());
            sub.setStripeCustomerId(customer.getId());
        }
        subscriptionRepository.save(sub);
        return sub.getStripeCustomerId();
    }

    private SubscriptionResponse toResponse(Subscription sub) {
        SubscriptionTier tier = sub.effectiveTier();
        return SubscriptionResponse.builder()
                .tier(sub.getTier().name())
                .status(sub.getStatus().name())
                .currentPeriodEnd(sub.getCurrentPeriodEnd() != null ? sub.getCurrentPeriodEnd().toString() : null)
                .cancelAtPeriodEnd(sub.isCancelAtPeriodEnd())
                .maxStreams(tier.getMaxStreams())
                .maxQuality(tier.getMaxQuality())
                .build();
    }

    private SubscriptionResponse freeResponse() {
        return SubscriptionResponse.builder()
                .tier(SubscriptionTier.FREE.name())
                .status(SubscriptionStatus.ACTIVE.name())
                .cancelAtPeriodEnd(false)
                .maxStreams(SubscriptionTier.FREE.getMaxStreams())
                .maxQuality(SubscriptionTier.FREE.getMaxQuality())
                .build();
    }
}