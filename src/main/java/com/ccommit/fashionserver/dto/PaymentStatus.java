package com.ccommit.fashionserver.dto;

import lombok.Getter;


@Getter
public enum PaymentStatus {
    PAYING("PAYING", "결제 중"),
    PAYMENT_COMPLETE("PAYMENT_COMPLETE", "결제 완료"),
    PAYMENT_CANCEL("PAYMENT_CANCEL", "결제 취소"),
    REFUNDING("REFUNDING", "환불 중"),
    REFUND_COMPLETE("REFUND_COMPLETE", "환불 완료");


    private final String paymentCode;
    private final String paymentStatus;

    PaymentStatus(String paymentCode, String paymentStatus) {
        this.paymentCode = paymentCode;
        this.paymentStatus = paymentStatus;
    }
}


