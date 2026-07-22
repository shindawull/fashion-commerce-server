package com.ccommit.fashionserver.dto;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductInfoDto {
    private int id;                 // 상품번호
    private String name;            // 상품명
    private int saleQuantity;    // 판매수량
    private int price;           // 가격
}
