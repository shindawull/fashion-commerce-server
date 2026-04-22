package com.ccommit.fashionserver.common;

import com.ccommit.fashionserver.common.exception.FashionServerException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<Object> handleNotContent(RuntimeException ex, HttpServletRequest request) {
        log.error("[RuntimeException] {}", ex.getMessage());
        CommonResponse commonResponse = new CommonResponse(HttpStatus.OK, "ERR_000", ex.getMessage(), request.getServletPath());
        return new ResponseEntity<>(commonResponse, new HttpHeaders(), commonResponse.getHttpStatus());
    }

    @ExceptionHandler(value = FashionServerException.class)
    public ResponseEntity<Object> handleDuplicate(FashionServerException ex, HttpServletRequest request) {
        log.error("[FashionServerException] {}", ex.getMessage());
        CommonResponse commonResponse = new CommonResponse(HttpStatus.OK, ex.status.toString(), ex.getMessage(), request.getServletPath());
        return new ResponseEntity<>(commonResponse, new HttpHeaders(), commonResponse.getHttpStatus());
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleException(Exception ex, HttpServletRequest request) {
        log.error("[Exception] {}", ex.getMessage());
        CommonResponse commonResponse = new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, "ERR_999",
                ex.getMessage(), request.getServletPath());
        return new ResponseEntity<>(commonResponse, new HttpHeaders(), commonResponse.getHttpStatus());

    }
}
