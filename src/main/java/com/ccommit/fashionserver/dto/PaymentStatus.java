package com.ccommit.fashionserver.dto;

/**
 * packageName    : com.ccommit.fashionserver.dto
 * fileName       : OrderStatus
 * author         : juoiy
 * date           : 2023-10-04
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-04        juoiy       최초 생성
 */
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


