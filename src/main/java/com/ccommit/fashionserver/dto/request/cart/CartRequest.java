package com.ccommit.fashionserver.dto.request.cart;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
* 장바구니 담기 / 수량 변경 요청
* - addCart    : quantity만큼 기존 수량에 합산
* - updateCart : quantity로 덮어쓰기
* */
@Getter
@Setter
@NoArgsConstructor
public class CartRequest {
    private int productId;
    private int quantity;
}
