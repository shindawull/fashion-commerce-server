package com.ccommit.fashionserver.dto;


import lombok.Getter;

@Getter
public enum OrderStatus {

    RECEIVED("RECEIVED", "주문 접수"),
    ORDER_COMPLETE("ORDER_COMPLETE", "주문 완료"),
    PAYING("PAYING", "결제 중"),
    PAYMENT_COMPLETE("PAYMENT_COMPLETE", "결제 완료"),
    PAYMENT_CANCEL("PAYMENT_CANCEL", "결제 취소"),
    SHIPPING("SHIPPING", "배송 중"),
    DELIVERY_COMPLETE("DELIVERY_COMPLETE", "배송 완료"),
    REFUNDING("REFUNDING", "환불 중"),
    REFUND_COMPLETE("REFUND_COMPLETE", "환불 완료");

    private final String status;
    private final String statusName;

    OrderStatus(String status, String statusName) {
        this.status = status;
        this.statusName = statusName;
    }

}
