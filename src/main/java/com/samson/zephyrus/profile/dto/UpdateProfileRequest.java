package com.samson.zephyrus.profile.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(min = 1, max = 50)
    private String name;

    private String avatarUrl;

    private Boolean kidsMode;

    /** Send a 4-digit string to set a PIN, empty string to remove it. */
    @Pattern(regexp = "^(\\d{4})?$", message = "PIN must be 4 digits or empty to remove")
    private String pin;
}