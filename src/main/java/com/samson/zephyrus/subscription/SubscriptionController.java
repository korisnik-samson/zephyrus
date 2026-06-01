package com.samson.zephyrus.subscription;

import com.samson.zephyrus.auth.model.User;
import com.samson.zephyrus.common.ApiResponse;
import com.samson.zephyrus.subscription.dto.CheckoutRequest;
import com.samson.zephyrus.subscription.dto.CheckoutResponse;
import com.samson.zephyrus.subscription.dto.SubscriptionResponse;
import com.samson.zephyrus.subscription.dto.TierInfoDto;
import com.samson.zephyrus.subscription.model.SubscriptionTier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
@Tag(name = "Subscription", description = "Premium subscription tiers and Stripe billing")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/tiers")
    @Operation(summary = "List tiers", description = "Returns all available subscription tiers and their capabilities")
    public ResponseEntity<ApiResponse<List<TierInfoDto>>> tiers() {
        List<TierInfoDto> tiers = Arrays.stream(SubscriptionTier.values())
                .map(t -> new TierInfoDto(t.name(), t.getRank(), t.getMaxStreams(), t.getMaxQuality()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(tiers));
    }

    @GetMapping
    @Operation(summary = "Get my subscription", description = "Returns the current user's subscription (defaults to FREE)")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> mySubscription(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getMySubscription(user.getId())));
    }

    @PostMapping("/checkout")
    @Operation(summary = "Start checkout", description = "Creates a Stripe Checkout session and returns its URL")
    public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(
            @RequestBody @Valid CheckoutRequest request,
            @AuthenticationPrincipal User user) {
        SubscriptionTier tier = SubscriptionTier.valueOf(request.getTier());
        String url = subscriptionService.createCheckoutSession(user, tier);
        return ResponseEntity.ok(ApiResponse.success(new CheckoutResponse(url)));
    }
}