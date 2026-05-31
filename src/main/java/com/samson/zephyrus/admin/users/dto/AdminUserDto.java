package com.samson.zephyrus.admin.users.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AdminUserDto {
    private UUID id;
    private String email;
    private String displayName;
    private String role;
    private String avatarUrl;
    private boolean enabled;
    private boolean banned;
    private String bannedAt;
    private String banReason;
    private String suspendedUntil;
    private String createdAt;
    private String updatedAt;
}