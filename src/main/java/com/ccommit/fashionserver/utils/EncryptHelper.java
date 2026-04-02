package com.ccommit.fashionserver.utils;

public interface EncryptHelper {
    public String hashPassword(String password);

    boolean isMach(String password, String hashed);
}
