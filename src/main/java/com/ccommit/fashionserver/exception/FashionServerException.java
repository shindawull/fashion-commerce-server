package com.ccommit.fashionserver.exception;

public class FashionServerException extends RuntimeException {
    public Integer status;
    public FashionServerException(String message) {
        super(message);
    }

    public FashionServerException(String message, Integer status) {
        super(message);
        this.status = status;
    }
}
