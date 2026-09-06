package com.ccommit.fashionserver.dto;

import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    private int id;
    private int orderId;        // orders.id(pk) - orders.order_id 아님
    private int productId;
    private String productName; // 주문 시점 상품명 스냅샷
    private int quantity;
    private int price;          // 주문 시점 단가 스냅샷
}