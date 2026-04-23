package com.ccommit.fashionserver.dto;

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
}
