package com.ccommit.fashionserver.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestProductDto {
    private List<ProductDto> productDtoList;
}
