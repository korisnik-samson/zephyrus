package com.samson.zephyrus.admin.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserGrowthDto {
    private String date;
    private long newUsers;
}