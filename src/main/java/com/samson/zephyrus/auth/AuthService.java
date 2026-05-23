package com.samson.zephyrus.auth;

import com.samson.zephyrus.auth.dto.AuthResponse;
import com.samson.zephyrus.auth.dto.LoginRequest;
import com.samson.zephyrus.auth.dto.RegisterRequest;
import com.samson.zephyrus.auth.model.RefreshToken;
import com.samson.zephyrus.auth.model.User;
import com.samson.zephyrus.auth.repository.RefreshTokenRepository;
import com.samson.zephyrus.auth.repository.UserRepository;
import com.samson.zephyrus.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Authentication business logic — registration, login, token refresh, logout.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user account.
     *
     * @throws IllegalArgumentException if the email is already taken
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName().trim())
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        return generateAuthResponse(user);
    }

    /**
     * Authenticate a user with email and password.
     *
     * @throws BadCredentialsException if credentials are invalid
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        log.info("User logged in: {}", user.getEmail());

        return generateAuthResponse(user);
    }

    /**
     * Exchange a valid refresh token for a new access token.
     *
     * @throws IllegalArgumentException if the refresh token is invalid or expired
     */
    @Transactional
    public AuthResponse refresh(String refreshTokenStr) {
        UUID tokenUuid;
        try {
            tokenUuid = UUID.fromString(refreshTokenStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid refresh token format");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenUuid)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

        if (!refreshToken.isValid()) {
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }

        // Revoke the old refresh token (rotation)
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        User user = refreshToken.getUser();
        log.debug("Token refreshed for user: {}", user.getEmail());

        return generateAuthResponse(user);
    }

    /**
     * Revoke all refresh tokens for the given user (logout).
     */
    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("User logged out, all refresh tokens revoked: {}", userId);
    }

    /**
     * Get the currently authenticated user's info.
     */
    public AuthResponse.UserDto getCurrentUser(User user) {
        return AuthResponse.UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .role(user.getRole())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }

    // ── Private helpers ───────────────────────────────────

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(user);

        return AuthResponse.of(user, accessToken, refreshToken.getToken().toString());
    }

    private RefreshToken createRefreshToken(User user) {
        long expiryMs = jwtProperties.getRefreshTokenExpiry();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiryMs / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .expiresAt(expiresAt)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }
}
