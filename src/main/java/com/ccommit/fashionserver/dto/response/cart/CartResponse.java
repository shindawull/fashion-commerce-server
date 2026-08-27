package com.ccommit.fashionserver.dto.response.cart;

import com.ccommit.fashionserver.dto.response.product.ProductResponse;
import lombok.Builder;
import lombok.Getter;

/**
 * 장바구니 조회 응답
 * - Redis에는 productId와 quantity만 저장되어 있고,
 * 상품명/가격 등은 조회시점에 ProductService에서 최신값을 가져와 조합한다.
 */
@Getter
@Builder
public class CartResponse {
    private int productId;
    private String name;
    private String brandName;
    private int price;      // 상품 단가 (최신값)
    private int quantity;   // 장바구니에 담은 수량
    private int totalPrice; // 단가x 수량

    public static CartResponse of(ProductResponse productResponse, int quantity) {
        return CartResponse.builder()
                .productId(productResponse.getId())
                .name(productResponse.getName())
                .brandName(productResponse.getBrandName())
                .price(productResponse.getPrice())
                .quantity(quantity)
                .totalPrice(productResponse.getPrice() * quantity)
                .build();
    }

}
