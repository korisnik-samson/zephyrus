package com.samson.zephyrus.admin.users.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SuspendUserRequest {
    /** Duration in hours */
    @NotNull
    @Min(1)
    private Integer hours;

    private String reason;
}