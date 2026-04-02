package com.ccommit.fashionserver.dto;

public enum UserType {
    USER("USER"),
    SELLER("SELLER"),
    ADMIN("ADMIN");

    private final String name;

    UserType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
