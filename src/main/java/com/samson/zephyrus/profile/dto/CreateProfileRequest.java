package com.samson.zephyrus.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateProfileRequest {

    @NotBlank
    @Size(min = 1, max = 50)
    private String name;

    private String avatarUrl;

    private boolean kidsMode = false;

    @Pattern(regexp = "^\\d{4}$", message = "PIN must be exactly 4 digits")
    private String pin;
}