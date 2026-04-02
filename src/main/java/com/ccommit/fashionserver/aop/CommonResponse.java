package com.ccommit.fashionserver.aop;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/*
*  @NoArgsConstructor : 해당 클래스에 파라미터가 없는 기본 생성자를 자동으로 생성하기 위해 사용합니다.
*  @AllArgsConstructor : 해당 클래스의 모든 필드 값을 파라미터(인자)로 받아 초기화 할 수 있습니다.
*
* */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> {
    private HttpStatus httpStatus;
    private String code;
    private String message;
    private T requestBody;
}
