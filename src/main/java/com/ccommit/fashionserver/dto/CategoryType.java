package com.ccommit.fashionserver.dto;

import com.ccommit.fashionserver.common.exception.ErrorCode;
import com.ccommit.fashionserver.common.exception.FashionServerException;

import java.util.Arrays;

public enum CategoryType {
    ALL("전체", 1),
    CLOTHING("의류", 2),
    ACCESSORY("악세서리", 3),
    SHOES("신발", 4),
    BAG("가방", 5);

    private final String name;
    private final int number;

    CategoryType(String name, int number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }


    public static CategoryType from(String categoryName) {
        return Arrays.stream(CategoryType.values())
                .filter(categoryType -> categoryType.getName().equals(categoryName))
                .findFirst()
                .orElseThrow(() -> new FashionServerException(ErrorCode.CATEGORY_NOT_FOUND_ERROR.getMessage(),
                        ErrorCode.CATEGORY_NOT_FOUND_ERROR.getStatus()));
    }
}
