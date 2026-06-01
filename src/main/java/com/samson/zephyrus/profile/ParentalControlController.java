package com.samson.zephyrus.profile;

import com.samson.zephyrus.auth.model.User;
import com.samson.zephyrus.common.ApiResponse;
import com.samson.zephyrus.profile.dto.VerifyPinRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/profiles/{id}")
@RequiredArgsConstructor
@Tag(name = "Parental Controls", description = "Profile PIN verification and kids-mode content gating")
public class ParentalControlController {

    private final ParentalControlService parentalControlService;

    @PostMapping("/verify-pin")
    @Operation(summary = "Verify profile PIN", description = "Checks a 4-digit PIN. Profiles without a PIN always return valid=true.")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> verifyPin(
            @PathVariable UUID id,
            @RequestBody @Valid VerifyPinRequest request,
            @AuthenticationPrincipal User user) {
        boolean valid = parentalControlService.verifyPin(user.getId(), id, request.getPin());
        return ResponseEntity.ok(ApiResponse.success(Map.of("valid", valid)));
    }

    @GetMapping("/content-allowed/{titleId}")
    @Operation(summary = "Check content allowance", description = "Returns whether a title is permitted under this profile's kids-mode settings")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> contentAllowed(
            @PathVariable UUID id,
            @PathVariable UUID titleId,
            @AuthenticationPrincipal User user) {
        boolean allowed = parentalControlService.isTitleAllowed(user.getId(), id, titleId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("allowed", allowed)));
    }
}