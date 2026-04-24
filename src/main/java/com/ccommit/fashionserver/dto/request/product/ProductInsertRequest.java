package com.ccommit.fashionserver.dto.request.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductInsertRequest {
    @NotBlank(message = "상품명을 입력해주세요.")
    private String name;

    @Min(value = 0, message = "수량은 0개 이상이어야 합니다.")
    private int saleQuantity;

    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private int price;

    @NotBlank(message = "카테고리명을 입력해주세요.")
    private String categoryName;

    @NotBlank(message = "브랜드명을 입력해주세요.")
    private String brandName;
}
