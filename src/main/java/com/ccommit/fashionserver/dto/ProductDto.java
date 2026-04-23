package com.ccommit.fashionserver.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import java.sql.Date;

@Getter
@Setter
public class ProductDto {
    private int id;                 // 상품 번호
    @NotBlank
    private String name;            // 상품 명
    @NotBlank
    private String saleQuantity;    // 판매 수량
    @NotBlank
    private String price;           // 가격
    private int categoryId;         // 카테고리 번호
    private int likeCount;          // 좋아요 수
    @NotBlank
    private String brandName;       // 브랜드 명
    private int saleId;             // 판매자 아이디
    private Date createDate;        // 등록일
    private Date updateDate;        // 수정일
    @NotBlank
    private String categoryName;
}
