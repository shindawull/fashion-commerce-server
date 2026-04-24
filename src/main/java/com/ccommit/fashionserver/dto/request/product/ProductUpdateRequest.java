package com.ccommit.fashionserver.dto.request.product;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductUpdateRequest {
    private int id;
    private String name;
    private Integer saleQuantity;
    private Integer price;
    private String categoryName;
    private String brandName;
}
