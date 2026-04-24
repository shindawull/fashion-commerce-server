package com.ccommit.fashionserver.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.sql.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private int id;                 // 상품 번호
    private String name;            // 상품 명
    private int saleQuantity;       // 판매 수량
    private int price;              // 가격
    private int categoryId;         // 카테고리 번호
    private int likeCount;          // 좋아요 수
    private String brandName;       // 브랜드 명
    private int saleId;             // 판매자 아이디
    private Date createDate;        // 등록일
    private Date updateDate;        // 수정일
    private String categoryName;
    private String searchType;
}
