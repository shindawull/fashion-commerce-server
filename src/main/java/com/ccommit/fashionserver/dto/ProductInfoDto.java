package com.ccommit.fashionserver.dto;

import lombok.*;

/**
 * packageName    : com.ccommit.fashionserver.dto
 * fileName       : ProductDto
 * author         : juoiy
 * date           : 2023-08-24
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-08-24        juoiy       최초 생성
 */
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
