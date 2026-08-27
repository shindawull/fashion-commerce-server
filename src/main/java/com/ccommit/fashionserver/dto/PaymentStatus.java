package com.ccommit.fashionserver.dto;

public enum PaymentStatus {
    PAYMENT_COMPLETE("결제 완료", 2000),
    PAYMENT_CANCEL("결제 취소", 9999);

    private int paymentCode;
    private String paymentStatus;

    public int getPaymentCode() {
        return paymentCode;
    }

    public void setPaymentCode(int paymentCode) {
        this.paymentCode = paymentCode;
    }

    PaymentStatus(String paymentStatus, int paymentCode) {
        this.paymentStatus = paymentStatus;
        this.paymentCode = paymentCode;
    }
}


