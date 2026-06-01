package com.samson.zephyrus.profile;

import com.samson.zephyrus.content.model.Title;
import com.samson.zephyrus.content.repository.TitleRepository;
import com.samson.zephyrus.profile.model.Profile;
import com.samson.zephyrus.profile.repository.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

/**
 * Parental controls: profile PIN verification and kids-mode maturity gating.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParentalControlService {

    /** Maturity ratings considered safe for kids-mode profiles. */
    private static final Set<String> KIDS_ALLOWED_RATINGS = Set.of(
            "G", "PG", "TV-Y", "TV-Y7", "TV-G", "TV-PG");

    private final ProfileRepository profileRepository;
    private final TitleRepository titleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Verifies a 4-digit PIN against a profile. Profiles without a PIN set are
     * treated as always-unlocked (returns true).
     */
    @Transactional(readOnly = true)
    public boolean verifyPin(UUID userId, UUID profileId, String pin) {
        Profile profile = requireOwned(userId, profileId);
        if (profile.getPinHash() == null) {
            return true;
        }
        boolean ok = passwordEncoder.matches(pin, profile.getPinHash());
        if (!ok) {
            log.debug("PIN verification failed for profile={}", profileId);
        }
        return ok;
    }

    /**
     * Determines whether a title may be played under the given profile.
     * Non-kids profiles allow everything; kids profiles are limited to a
     * whitelist of family-friendly maturity ratings.
     */
    @Transactional(readOnly = true)
    public boolean isTitleAllowed(UUID userId, UUID profileId, UUID titleId) {
        Profile profile = requireOwned(userId, profileId);
        if (!profile.isKidsMode()) {
            return true;
        }
        Title title = titleRepository.findById(titleId)
                .orElseThrow(() -> new EntityNotFoundException("Title not found: " + titleId));

        String rating = title.getMaturityRating();
        // Unrated content is blocked in kids mode to be safe.
        return rating != null && KIDS_ALLOWED_RATINGS.contains(rating.toUpperCase());
    }

    // ── Private helpers ───────────────────────────────────

    private Profile requireOwned(UUID userId, UUID profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found: " + profileId));
        if (!profile.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Profile does not belong to current user");
        }
        return profile;
    }
}