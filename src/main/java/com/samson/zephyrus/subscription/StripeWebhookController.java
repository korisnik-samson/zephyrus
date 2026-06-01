package com.samson.zephyrus.subscription;

import com.samson.zephyrus.subscription.config.StripeProperties;
import com.samson.zephyrus.subscription.model.SubscriptionStatus;
import com.samson.zephyrus.subscription.model.SubscriptionTier;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Receives Stripe webhook events. This endpoint is public (no JWT) but is
 * protected by Stripe signature verification using the webhook signing secret.
 */
@Slf4j
@RestController
@RequestMapping("/api/webhooks/stripe")
@RequiredArgsConstructor
@Tag(name = "Stripe Webhook", description = "Stripe subscription lifecycle events")
public class StripeWebhookController {

    private final StripeProperties stripeProperties;
    private final SubscriptionService subscriptionService;

    @PostMapping
    @Operation(summary = "Stripe webhook receiver")
    public ResponseEntity<String> handle(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {

        if (!stripeProperties.isEnabled()) {
            return ResponseEntity.ok("ignored (billing disabled)");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, stripeProperties.getWebhookSecret());
        } catch (Exception e) {
            log.warn("Stripe webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("invalid signature");
        }

        StripeObject obj = event.getDataObjectDeserializer().getObject().orElse(null);
        log.debug("Stripe webhook received: {}", event.getType());

        try {
            switch (event.getType()) {
                case "checkout.session.completed" ->
                        handleCheckoutCompleted(obj);
                case "customer.subscription.updated", "customer.subscription.created" ->
                        handleSubscriptionUpdated(obj);
                case "customer.subscription.deleted" ->
                        handleSubscriptionDeleted(obj);
                default ->
                        log.debug("Unhandled Stripe event type: {}", event.getType());
            }
        } catch (Exception e) {
            log.error("Error processing Stripe event {}: {}", event.getType(), e.getMessage());
            // Return 200 so Stripe doesn't endlessly retry on our processing bugs.
        }

        return ResponseEntity.ok("ok");
    }

    // ── Event handlers ────────────────────────────────────

    private void handleCheckoutCompleted(StripeObject obj) {
        if (obj instanceof com.stripe.model.checkout.Session session) {
            String customerId = session.getCustomer();
            String subscriptionId = session.getSubscription();
            String tierName = session.getMetadata() != null ? session.getMetadata().get("tier") : null;
            SubscriptionTier tier = parseTier(tierName);

            subscriptionService.applySubscriptionUpdate(
                    customerId, subscriptionId, tier, SubscriptionStatus.ACTIVE, null, false);
        }
    }

    private void handleSubscriptionUpdated(StripeObject obj) {
        if (obj instanceof com.stripe.model.Subscription sub) {
            subscriptionService.applySubscriptionUpdate(
                    sub.getCustomer(),
                    sub.getId(),
                    null, // tier unchanged unless metadata present
                    mapStatus(sub.getStatus()),
                    sub.getCurrentPeriodEnd(),
                    Boolean.TRUE.equals(sub.getCancelAtPeriodEnd()));
        }
    }

    private void handleSubscriptionDeleted(StripeObject obj) {
        if (obj instanceof com.stripe.model.Subscription sub) {
            subscriptionService.markCanceled(sub.getId());
        }
    }

    // ── Mapping ───────────────────────────────────────────

    private SubscriptionTier parseTier(String name) {
        if (name == null) return null;
        try {
            return SubscriptionTier.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private SubscriptionStatus mapStatus(String stripeStatus) {
        if (stripeStatus == null) return SubscriptionStatus.INCOMPLETE;
        return switch (stripeStatus) {
            case "active" -> SubscriptionStatus.ACTIVE;
            case "trialing" -> SubscriptionStatus.TRIALING;
            case "past_due", "unpaid" -> SubscriptionStatus.PAST_DUE;
            case "canceled" -> SubscriptionStatus.CANCELED;
            default -> SubscriptionStatus.INCOMPLETE;
        };
    }
}