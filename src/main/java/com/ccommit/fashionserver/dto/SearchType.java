package com.ccommit.fashionserver.dto;

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
}
