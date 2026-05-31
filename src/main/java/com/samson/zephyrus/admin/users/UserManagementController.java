package com.samson.zephyrus.admin.users;

import com.samson.zephyrus.admin.users.dto.AdminUserDto;
import com.samson.zephyrus.admin.users.dto.BanUserRequest;
import com.samson.zephyrus.admin.users.dto.SuspendUserRequest;
import com.samson.zephyrus.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin — Users", description = "User listing, banning, suspension, and role management")
public class UserManagementController {

    private final UserManagementService userService;

    @GetMapping
    @Operation(summary = "List users", description = "Paginated user list with optional search by name or email")
    public ResponseEntity<ApiResponse<Page<AdminUserDto>>> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(userService.listUsers(q, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user detail")
    public ResponseEntity<ApiResponse<AdminUserDto>> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUser(id)));
    }

    @PostMapping("/{id}/ban")
    @Operation(summary = "Ban user", description = "Permanently bans the user and revokes all their refresh tokens")
    public ResponseEntity<ApiResponse<AdminUserDto>> ban(
            @PathVariable UUID id,
            @RequestBody(required = false) BanUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.banUser(id, request != null ? request : new BanUserRequest())));
    }

    @DeleteMapping("/{id}/ban")
    @Operation(summary = "Unban user")
    public ResponseEntity<ApiResponse<AdminUserDto>> unban(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(userService.unbanUser(id)));
    }

    @PostMapping("/{id}/suspend")
    @Operation(summary = "Suspend user", description = "Temporarily locks the account and revokes refresh tokens")
    public ResponseEntity<ApiResponse<AdminUserDto>> suspend(
            @PathVariable UUID id,
            @RequestBody @Valid SuspendUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.suspendUser(id, request)));
    }

    @DeleteMapping("/{id}/suspend")
    @Operation(summary = "Lift suspension")
    public ResponseEntity<ApiResponse<AdminUserDto>> unsuspend(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(userService.unsuspendUser(id)));
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Set user role", description = "Promote to ADMIN or demote to USER")
    public ResponseEntity<ApiResponse<AdminUserDto>> setRole(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(userService.setRole(id, body.get("role"))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Permanently deletes the account and all associated data (cascades via DB constraints)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted"));
    }
}