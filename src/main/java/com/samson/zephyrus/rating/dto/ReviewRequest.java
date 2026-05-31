package com.samson.zephyrus.rating.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewRequest {

    @NotBlank
    @Size(min = 10, max = 5000)
    private String body;

    private boolean containsSpoilers = false;
}