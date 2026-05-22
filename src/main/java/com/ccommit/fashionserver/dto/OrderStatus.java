package com.ccommit.fashionserver.dto;

/**
 * packageName    : com.ccommit.fashionserver.dto
 * fileName       : OrderStatus
 * author         : juoiy
 * date           : 2023-11-13
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-11-13        juoiy       최초 생성
 */
public enum OrderStatus {

    ORDER_COMPLETION(501, "주문 완료"),
    ORDER_CANCEL(509, "주문 취소");
    // 결제 취소
    private final int status;
    private final String statusName;

    OrderStatus(int status, String statusName) {
        this.status = status;
        this.statusName = statusName;
    }

    public int getStatus() {
        return status;
    }

    public String getStatusName() {
        return statusName;
    }


}
