package com.ccommit.fashionserver.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class PaymentRequest {
    private int amount;
    private String orderId;
    private String paymentKey;
}
