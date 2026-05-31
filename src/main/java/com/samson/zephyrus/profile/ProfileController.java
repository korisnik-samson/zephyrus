package com.samson.zephyrus.profile;

import com.samson.zephyrus.auth.model.User;
import com.samson.zephyrus.common.ApiResponse;
import com.samson.zephyrus.content.dto.TitleSummaryDto;
import com.samson.zephyrus.profile.dto.CreateProfileRequest;
import com.samson.zephyrus.profile.dto.ProfileResponse;
import com.samson.zephyrus.profile.dto.UpdateProfileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@Tag(name = "Profiles", description = "Per-account viewer profiles and My List")
public class ProfileController {

    private final ProfileService profileService;

    // ── Profile CRUD ──────────────────────────────────────

    @GetMapping
    @Operation(summary = "List profiles", description = "Returns all profiles for the current account")
    public ResponseEntity<ApiResponse<List<ProfileResponse>>> list(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(profileService.getProfiles(user.getId())));
    }

    @PostMapping
    @Operation(summary = "Create profile", description = "Creates a new profile (max 5 per account)")
    public ResponseEntity<ApiResponse<ProfileResponse>> create(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid CreateProfileRequest request) {
        ProfileResponse profile = profileService.createProfile(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(profile));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> get(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(profileService.getProfile(user.getId(), id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> update(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @RequestBody @Valid UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(profileService.updateProfile(user.getId(), id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete profile")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id) {
        profileService.deleteProfile(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Profile deleted"));
    }

    // ── My List ───────────────────────────────────────────

    @GetMapping("/{id}/my-list")
    @Operation(summary = "Get My List", description = "Returns all titles bookmarked by this profile")
    public ResponseEntity<ApiResponse<List<TitleSummaryDto>>> getMyList(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(profileService.getMyList(user.getId(), id)));
    }

    @PostMapping("/{id}/my-list/{titleId}")
    @Operation(summary = "Add to My List")
    public ResponseEntity<ApiResponse<Void>> addToMyList(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @PathVariable UUID titleId) {
        profileService.addToMyList(user.getId(), id, titleId);
        return ResponseEntity.ok(ApiResponse.success("Added to My List"));
    }

    @DeleteMapping("/{id}/my-list/{titleId}")
    @Operation(summary = "Remove from My List")
    public ResponseEntity<ApiResponse<Void>> removeFromMyList(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @PathVariable UUID titleId) {
        profileService.removeFromMyList(user.getId(), id, titleId);
        return ResponseEntity.ok(ApiResponse.success("Removed from My List"));
    }

    @GetMapping("/{id}/my-list/{titleId}/status")
    @Operation(summary = "Check My List status", description = "Returns whether the title is in this profile's list")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkMyList(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @PathVariable UUID titleId) {
        boolean inList = profileService.isInMyList(user.getId(), id, titleId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("inList", inList)));
    }
}