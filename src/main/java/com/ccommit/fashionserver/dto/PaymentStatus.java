package com.ccommit.fashionserver.dto;

import lombok.Getter;


@Getter
public enum PaymentStatus {
    PAYMENT_COMPLETE("결제 완료", 2000),
    PAYMENT_CANCEL("결제 취소", 9999);


    private final int paymentCode;
    private final String paymentStatus;

    PaymentStatus(String paymentStatus, int paymentCode) {
        this.paymentStatus = paymentStatus;
        this.paymentCode = paymentCode;
    }
}


