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
    PRODUCT_QUANTITY_NOT_ENOUGH_ERROR(614, "상품 재고가 부족합니다."),

    // 카테고리/검색 관련 (620번대)
    CATEGORY_NOT_FOUND_ERROR(620, "존재하지 않는 카테고리입니다."),
    SEARCH_TYPE_NOT_FOUND_ERROR(621, "존재하지 않는 검색 타입입니다."),
    USER_TYPE_NOT_FOUND_ERROR(622, "존재하지 않는 회원 타입입니다."),

    // 주문 관련 (630번대)
    ORDER_INSERT_ERROR(630, "주문정보 등록에 실패하였습니다."),
    ORDER_UPDATE_ERROR(631, "주문정보 수정에 실패하였습니다."),
    ORDER_CANCEL_ERROR(632, "주문정보 취소에 실패하였습니다."),
    ORDER_DUPLICATION_ERROR(634, "이미 존재하는 주문정보입니다."),
    ORDER_NOT_FOUND_ERROR(635, "존재하지않는 주문정보 입니다."),
    ORDER_CANCEL_IMPOSSIBLE_DATE_ERROR(636, "주문취소가 불가능한 날짜입니다."),
    ORDER_CANCEL_IMPOSSIBLE_ERROR(636, "주문취소가 불가능합니다."),

    // 장바구니 관련 (640번대)
    CART_PRODUCT_NOT_USING_ERROR(640, "장바구니에 담긴 상품이 없습니다."),

    // 결제 관련 (650번대)
    CARD_PAYMENT_NOT_FOUND_ERROR(650, "존재하지 않는 결제정보 입니다."),
    CARD_PAYMENT_SUCCESS_ERROR(651, "카드 결제에 실패하였습니다."),
    CARD_PAYMENT_INSERT_ERROR(652, "카드 결제 정보 등록에 실패하였습니다."),
    CARD_PAYMENT_SELECT_ERROR(653, "카드 결제 정보 조회에 실패하였습니다."),
    CARD_PAYMENT_UPDATE_ERROR(654, "카드 결제 정보 수정에 실패하였습니다."),
    CARD_PAYMENT_AMOUNT_MISMATCH_ERROR(655, "결제 금액이 주문 금액과 일치하지 않습니다."),

    // 공통 (690)
    INPUT_NULL_ERROR(690, "입력값이 없습니다. 확인해주세요."),
    HTTP_SERVER_ERROR(691, "외부 결제 서버 통신 중 오류가 발생하였습니다."),
    ;

    // 에러 코드의 '코드 상태'을 반환한다.
    private final int status;

    // 에러 코드의 '코드 메시지'을 반환한다.
    private final String message;

    ErrorCode(final int status, final String message) {
        this.status = status;
        this.message = message;
    }
}
