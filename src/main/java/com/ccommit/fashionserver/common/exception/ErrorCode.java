package com.ccommit.fashionserver.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // 회원 관련 (600번대)
    USER_INSERT_DUPLICATE_ERROR(601, "이미 사용 중인 아이디입니다."),
    USER_NOT_MATCH_ERROR(602, "회원 정보가 일치하지 않습니다."),
    USER_NOT_AUTHORIZED_ERROR(603, "접근 권한이 없습니다."),
    USER_NOT_FOUND_ERROR(604, "존재하지 않는 회원입니다."),
    USER_ALREADY_WITHDRAWN_ERROR(605, "이미 탈퇴한 회원입니다."),
    USER_WITHDRAW_NOT_ALLOWED_ERROR(606, "탈퇴할 수 없는 상태입니다."),
    LOGIN_FAIL_ERROR(607, "아이디 또는 비밀번호를 확인해주세요."),

    // 상품 관련 (610번대)
    PRODUCT_INSERT_ERROR(610, "상품 등록에 실패하였습니다."),
    PRODUCT_UPDATE_ERROR(611, "상품 수정에 실패하였습니다."),
    PRODUCT_DELETE_ERROR(612, "상품 삭제에 실패하였습니다."),
    PRODUCT_NOT_FOUND_ERROR(613, "존재하지 않는 상품입니다."),

    // 카테고리/검색 관련 (620번대)
    CATEGORY_NOT_USING_ERROR(620, "존재하지 않는 카테고리입니다."),
    SEARCH_TYPE_NOT_USING_ERROR(621, "존재하지 않는 검색 타입입니다."),

    // 공통 (666)
    INPUT_NULL_ERROR(666, "입력값이 없습니다. 확인해주세요.");

    // 에러 코드의 '코드 상태'을 반환한다.
    private final int status;

    // 에러 코드의 '코드 메시지'을 반환한다.
    private final String message;

    ErrorCode(final int status, final String message) {
        this.status = status;
        this.message = message;
    }
}
