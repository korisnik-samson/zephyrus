package com.samson.zephyrus.admin.users;

import com.samson.zephyrus.admin.users.dto.AdminUserDto;
import com.samson.zephyrus.admin.users.dto.BanUserRequest;
import com.samson.zephyrus.admin.users.dto.SuspendUserRequest;
import com.samson.zephyrus.auth.model.Role;
import com.samson.zephyrus.auth.model.User;
import com.samson.zephyrus.auth.repository.RefreshTokenRepository;
import com.samson.zephyrus.auth.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    // ── List / search ─────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<AdminUserDto> listUsers(String query, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users = (query != null && !query.isBlank())
                ? userRepository.searchUsers(query.trim(), pageable)
                : userRepository.findAll(pageable);
        return users.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public AdminUserDto getUser(UUID id) {
        return toDto(requireUser(id));
    }

    // ── Ban ───────────────────────────────────────────────

    @Transactional
    public AdminUserDto banUser(UUID id, BanUserRequest req) {
        User user = requireUser(id);
        user.setBanned(true);
        user.setBannedAt(LocalDateTime.now());
        user.setBanReason(req.getReason());
        refreshTokenRepository.revokeAllByUserId(id);
        log.info("Admin banned user={} reason={}", id, req.getReason());
        return toDto(userRepository.save(user));
    }

    @Transactional
    public AdminUserDto unbanUser(UUID id) {
        User user = requireUser(id);
        user.setBanned(false);
        user.setBannedAt(null);
        user.setBanReason(null);
        log.info("Admin unbanned user={}", id);
        return toDto(userRepository.save(user));
    }

    // ── Suspend ───────────────────────────────────────────

    @Transactional
    public AdminUserDto suspendUser(UUID id, SuspendUserRequest req) {
        User user = requireUser(id);
        user.setSuspendedUntil(LocalDateTime.now().plusHours(req.getHours()));
        refreshTokenRepository.revokeAllByUserId(id);
        log.info("Admin suspended user={} for {}h", id, req.getHours());
        return toDto(userRepository.save(user));
    }

    @Transactional
    public AdminUserDto unsuspendUser(UUID id) {
        User user = requireUser(id);
        user.setSuspendedUntil(null);
        log.info("Admin lifted suspension for user={}", id);
        return toDto(userRepository.save(user));
    }

    // ── Role ──────────────────────────────────────────────

    @Transactional
    public AdminUserDto setRole(UUID id, String roleName) {
        User user = requireUser(id);
        user.setRole(Role.valueOf(roleName.toUpperCase()));
        log.info("Admin set role={} for user={}", roleName, id);
        return toDto(userRepository.save(user));
    }

    // ── Delete ────────────────────────────────────────────

    @Transactional
    public void deleteUser(UUID id) {
        User user = requireUser(id);
        userRepository.delete(user);
        log.info("Admin deleted user={}", id);
    }

    // ── Private helpers ───────────────────────────────────

    private User requireUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }

    private AdminUserDto toDto(User u) {
        return AdminUserDto.builder()
                .id(u.getId())
                .email(u.getEmail())
                .displayName(u.getDisplayName())
                .role(u.getRole().name())
                .avatarUrl(u.getAvatarUrl())
                .enabled(u.isEnabled())
                .banned(u.isBanned())
                .bannedAt(u.getBannedAt() != null ? u.getBannedAt().toString() : null)
                .banReason(u.getBanReason())
                .suspendedUntil(u.getSuspendedUntil() != null ? u.getSuspendedUntil().toString() : null)
                .createdAt(u.getCreatedAt() != null ? u.getCreatedAt().toString() : null)
                .updatedAt(u.getUpdatedAt() != null ? u.getUpdatedAt().toString() : null)
                .build();
    }
}