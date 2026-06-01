package com.samson.zephyrus.subscription.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CheckoutRequest {

    @NotNull
    @Pattern(regexp = "BASIC|STANDARD|PREMIUM", message = "tier must be BASIC, STANDARD, or PREMIUM")
    private String tier;
}