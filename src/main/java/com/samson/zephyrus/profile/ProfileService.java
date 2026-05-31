package com.samson.zephyrus.profile;

import com.samson.zephyrus.auth.model.User;
import com.samson.zephyrus.content.dto.TitleSummaryDto;
import com.samson.zephyrus.content.model.Genre;
import com.samson.zephyrus.content.model.Title;
import com.samson.zephyrus.content.repository.TitleRepository;
import com.samson.zephyrus.profile.dto.CreateProfileRequest;
import com.samson.zephyrus.profile.dto.ProfileResponse;
import com.samson.zephyrus.profile.dto.UpdateProfileRequest;
import com.samson.zephyrus.profile.model.MyListEntry;
import com.samson.zephyrus.profile.model.Profile;
import com.samson.zephyrus.profile.repository.MyListRepository;
import com.samson.zephyrus.profile.repository.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final int MAX_PROFILES = 5;

    private final ProfileRepository profileRepository;
    private final MyListRepository myListRepository;
    private final TitleRepository titleRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Profiles ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProfileResponse> getProfiles(UUID userId) {
        return profileRepository.findByUserIdOrderBySortOrder(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ProfileResponse createProfile(User user, CreateProfileRequest request) {
        if (profileRepository.countByUserId(user.getId()) >= MAX_PROFILES) {
            throw new IllegalStateException("Maximum of " + MAX_PROFILES + " profiles per account");
        }
        if (profileRepository.existsByUserIdAndName(user.getId(), request.getName().trim())) {
            throw new IllegalArgumentException("A profile with that name already exists");
        }

        int sortOrder = profileRepository.countByUserId(user.getId());

        Profile profile = Profile.builder()
                .user(user)
                .name(request.getName().trim())
                .avatarUrl(request.getAvatarUrl())
                .kidsMode(request.isKidsMode())
                .pinHash(request.getPin() != null ? passwordEncoder.encode(request.getPin()) : null)
                .sortOrder(sortOrder)
                .build();

        log.info("Profile created for user={} name={}", user.getId(), profile.getName());
        return toResponse(profileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId, UUID profileId) {
        return toResponse(requireOwned(userId, profileId));
    }

    @Transactional
    public ProfileResponse updateProfile(UUID userId, UUID profileId, UpdateProfileRequest request) {
        Profile profile = requireOwned(userId, profileId);

        if (request.getName() != null) {
            String trimmed = request.getName().trim();
            if (!trimmed.equals(profile.getName())
                    && profileRepository.existsByUserIdAndName(userId, trimmed)) {
                throw new IllegalArgumentException("A profile with that name already exists");
            }
            profile.setName(trimmed);
        }
        if (request.getAvatarUrl() != null) profile.setAvatarUrl(request.getAvatarUrl());
        if (request.getKidsMode() != null) profile.setKidsMode(request.getKidsMode());
        if (request.getPin() != null) {
            profile.setPinHash(request.getPin().isBlank()
                    ? null
                    : passwordEncoder.encode(request.getPin()));
        }

        return toResponse(profileRepository.save(profile));
    }

    @Transactional
    public void deleteProfile(UUID userId, UUID profileId) {
        profileRepository.delete(requireOwned(userId, profileId));
        log.info("Profile deleted profileId={}", profileId);
    }

    // ── My List ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TitleSummaryDto> getMyList(UUID userId, UUID profileId) {
        requireOwned(userId, profileId);
        return myListRepository.findByProfileIdOrderByAddedAtDesc(profileId)
                .stream()
                .map(e -> toTitleSummary(e.getTitle()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void addToMyList(UUID userId, UUID profileId, UUID titleId) {
        requireOwned(userId, profileId);
        if (myListRepository.existsByProfileIdAndTitleId(profileId, titleId)) return;

        Title title = titleRepository.findById(titleId)
                .orElseThrow(() -> new EntityNotFoundException("Title not found: " + titleId));

        myListRepository.save(MyListEntry.builder()
                .profile(profileRepository.getReferenceById(profileId))
                .title(title)
                .build());
    }

    @Transactional
    public void removeFromMyList(UUID userId, UUID profileId, UUID titleId) {
        requireOwned(userId, profileId);
        myListRepository.deleteByProfileIdAndTitleId(profileId, titleId);
    }

    @Transactional(readOnly = true)
    public boolean isInMyList(UUID userId, UUID profileId, UUID titleId) {
        requireOwned(userId, profileId);
        return myListRepository.existsByProfileIdAndTitleId(profileId, titleId);
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

    private ProfileResponse toResponse(Profile p) {
        return ProfileResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .avatarUrl(p.getAvatarUrl())
                .kidsMode(p.isKidsMode())
                .hasPin(p.getPinHash() != null)
                .sortOrder(p.getSortOrder())
                .createdAt(p.getCreatedAt() != null ? p.getCreatedAt().toString() : null)
                .build();
    }

    private TitleSummaryDto toTitleSummary(Title title) {
        List<String> genreNames = title.getGenres().stream()
                .map(Genre::getName).collect(Collectors.toList());
        return TitleSummaryDto.builder()
                .id(title.getId())
                .tmdbId(title.getTmdbId())
                .mediaType(title.getMediaType().name())
                .title(title.getTitle())
                .overview(title.getOverview())
                .posterPath(title.getPosterPath())
                .backdropPath(title.getBackdropPath())
                .voteAverage(title.getVoteAverage())
                .releaseDate(title.getReleaseDate())
                .maturityRating(title.getMaturityRating())
                .genres(genreNames)
                .build();
    }
}