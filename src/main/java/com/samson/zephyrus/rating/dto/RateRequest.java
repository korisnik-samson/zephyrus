package com.samson.zephyrus.rating.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RateRequest {

    @NotNull
    @Min(1)
    @Max(10)
    private Integer score;
}