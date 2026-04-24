package com.ccommit.fashionserver.dto.request.product;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductSearchRequest {
    private String categoryName;    // null 이면 전체 조회
    private String searchType;      // null 이면 NEW로 기본값
}
