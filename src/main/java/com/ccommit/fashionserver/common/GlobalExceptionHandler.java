package com.ccommit.fashionserver.common;

import com.ccommit.fashionserver.common.exception.FashionServerException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<Object> handleNotContent(RuntimeException ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "서버 오류가 발생했습니다.";
        log.error("[RuntimeException] {}", message);
        CommonResponse commonResponse = new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, "ERR_000", message, request.getServletPath());
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

    // JSON 파싱 오류 (값 자체가 없을 때)
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.error("[HttpMessageNotReadableException] {}", ex.getMessage());
        CommonResponse commonResponse = new CommonResponse(
                HttpStatus.BAD_REQUEST, "ERR_400",
                "요청 값을 확인해주세요.", request.getServletPath());
        return new ResponseEntity<>(commonResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    // validation 오류 (, @NotNull 등)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotvalid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("[MethodArgumentNotValidException] {}", ex.getMessage());

        // 어떤 필드가 문제인지 추출
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream().map(error -> error.getField() + " : " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        CommonResponse commonResponse = new CommonResponse(
                HttpStatus.BAD_REQUEST, "ERR_400", message, request.getServletPath());
        return new ResponseEntity<>(commonResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
}
