package com.samson.zephyrus.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckoutResponse {
    private String checkoutUrl;
}