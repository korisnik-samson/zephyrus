package com.samson.zephyrus.subscription;

import com.samson.zephyrus.auth.model.User;
import com.samson.zephyrus.subscription.model.SubscriptionTier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Enforces {@link RequiresTier} on controller methods. Resolves the current
 * authenticated user and verifies their effective subscription tier meets the
 * requirement, otherwise throws {@link TierRequiredException} (HTTP 402).
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class TierVerificationAspect {

    private final SubscriptionService subscriptionService;

    @Before("@annotation(com.samson.zephyrus.subscription.RequiresTier) " +
            "|| @within(com.samson.zephyrus.subscription.RequiresTier)")
    public void verifyTier(org.aspectj.lang.JoinPoint joinPoint) {
        RequiresTier annotation = resolveAnnotation(joinPoint);
        if (annotation == null) return;

        SubscriptionTier required = annotation.value();
        User user = currentUser();
        if (user == null) {
            throw new AccessDeniedException("Authentication required");
        }

        SubscriptionTier current = subscriptionService.getEffectiveTier(user.getId());
        if (!current.satisfies(required)) {
            log.debug("Tier gate blocked user={} required={} current={}", user.getId(), required, current);
            throw new TierRequiredException(required, current);
        }
    }

    private RequiresTier resolveAnnotation(org.aspectj.lang.JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresTier onMethod = method.getAnnotation(RequiresTier.class);
        if (onMethod != null) return onMethod;
        return joinPoint.getTarget().getClass().getAnnotation(RequiresTier.class);
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }
}