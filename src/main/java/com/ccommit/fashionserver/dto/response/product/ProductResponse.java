package com.ccommit.fashionserver.dto.response.product;

import com.ccommit.fashionserver.dto.ProductDto;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private int id;
    private String name;
    private int saleQuantity;
    private int price;
    private int categoryId;
    private int likeCount;
    private String brandName;
    private int saleId;
    private Date createDate;
    private Date updateDate;

    public static ProductResponse from(ProductDto productDto) {
        return ProductResponse.builder()
                .id(productDto.getId())
                .name(productDto.getName())
                .saleQuantity(productDto.getSaleQuantity())
                .price(productDto.getPrice())
                .categoryId(productDto.getCategoryId())
                .likeCount(productDto.getLikeCount())
                .brandName(productDto.getBrandName())
                .saleId(productDto.getSaleId())
                .createDate(productDto.getCreateDate())
                .updateDate(productDto.getUpdateDate())
                .build();
    }
}
