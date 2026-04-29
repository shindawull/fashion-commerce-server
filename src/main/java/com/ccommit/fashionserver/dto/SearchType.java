package com.ccommit.fashionserver.dto;

import com.ccommit.fashionserver.common.exception.ErrorCode;
import com.ccommit.fashionserver.common.exception.FashionServerException;

import java.util.Arrays;

public enum SearchType {
    NEW("NEW"),
    LOW_PRICE("LOW_PRICE"),
    HIGH_PRICE("HIGH_PRICE"),
    LIKE("LIKE");

    private final String name;

    SearchType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static SearchType from(String searchType) {
        return Arrays.stream(SearchType.values())
                .filter(searchType1 -> searchType1.getName().equals(searchType))
                .findFirst()
                .orElseThrow(() -> new FashionServerException(ErrorCode.SEARCH_TYPE_NOT_FOUND_ERROR.getMessage(),
                        ErrorCode.SEARCH_TYPE_NOT_FOUND_ERROR.getStatus()));
    }
}
