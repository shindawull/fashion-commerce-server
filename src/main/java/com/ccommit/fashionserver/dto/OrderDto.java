package com.ccommit.fashionserver.dto;

import lombok.*;

import java.sql.Date;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private int id;             // 주문번호
    private String orderId;     // 주문번호
    private int totalPrice;     // 총금액
    private String status;      // 주문상태
    private Integer paymentId;  // 결제번호
    private Date createDate;    // 등록날짜
    private Date updateDate;    // 수정날짜
    private int userId;                    // 구매자번호
    private List<OrderItemDto> orderItems; // 상품정보

}


