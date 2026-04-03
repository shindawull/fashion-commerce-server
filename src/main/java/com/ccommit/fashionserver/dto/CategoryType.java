package com.ccommit.fashionserver.dto;

public enum CategoryType {
    CLOTHING("의류", 10),
    BAG("가방", 20),
    ACCESSORY("악세서리", 30),
    SHOES("신발", 40),
    ALL("전체", 50);

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
