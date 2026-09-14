package com.ccommit.fashionserver.service;

import com.ccommit.fashionserver.common.exception.ErrorCode;
import com.ccommit.fashionserver.common.exception.FashionServerException;
import com.ccommit.fashionserver.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

/* 주문번호 생성기
 *
 * 주문번호 생성 규칙과 중복 처리를 담당한다.
 * 생성 방식이 바뀌어도(랜덤->날짜기반 등) OrderService는 영향 받지 않는다.
 * */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNumberGenerator {

    private final OrderMapper orderMapper;

    /* 주문번호 길이 제한*/
    private static final int ORDER_ID_LENGTH = 20;

    /* 중복 발생 시 재시도 횟수 */
    private static final int MAX_RETRY_COUNT = 5;

    /* 중복 시 최대 MAX_RETRY_COUNT 만큼 재시도 */
    public String generator() {
        for (int i = 0; i < MAX_RETRY_COUNT; i++) {
            String orderId = RandomStringUtils.randomAlphanumeric(ORDER_ID_LENGTH);
            if (orderMapper.isExistOrderId(orderId) == 0) {
                return orderId;
            }
            log.info("[주문번호 중복] 재시도 {}회차, orderId: {}", i+1, orderId);
        }
        throw new FashionServerException(
                ErrorCode.ORDER_DUPLICATION_ERROR.getMessage(),
                ErrorCode.ORDER_DUPLICATION_ERROR.getStatus());
    }
}
