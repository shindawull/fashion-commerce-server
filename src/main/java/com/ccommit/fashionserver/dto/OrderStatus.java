package com.ccommit.fashionserver.dto;


import lombok.Getter;

@Getter
public enum OrderStatus {

    RECEIVED("RECEIVED", "주문 접수"),
    ORDER_COMPLETE("ORDER_COMPLETE", "주문 완료"),
    ORDER_CANCEL("ORDER_CANCEL","주문 취소");

    private final String status;
    private final String statusName;

    OrderStatus(String status, String statusName) {
        this.status = status;
        this.statusName = statusName;
    }

}
