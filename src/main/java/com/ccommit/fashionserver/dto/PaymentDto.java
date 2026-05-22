package com.ccommit.fashionserver.dto;

import lombok.*;

/**
 * packageName    : com.ccommit.fashionserver.dto
 * fileName       : PaymentDto
 * author         : juoiy
 * date           : 2023-10-14
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-14        juoiy       최초 생성
 */
/*
*  토스페이먼츠 연동
*   paymentKey 필수 (string)
     결제의 키 값입니다. 최대 길이는 200자입니다.
     결제를 식별하는 역할로, 중복되지 않는 고유한 값입니다.
    orderId 필수 (string)
     주문 ID입니다. 주문한 결제를 식별합니다. 충분히 무작위한 값을 생성해서 각 주문마다 고유한 값을 넣어주세요.
     영문 대소문자, 숫자, 특수문자 -, _로 이루어진 6자 이상 64자 이하의 문자열이어야 합니다. 결제 데이터 관리를 위해 반드시 저장해야 합니다.
    amount 필수 (integer)
     결제할 금액입니다.
* */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private int id;
    private int status;
    private String orderId;
    private String cardNumber;
    private String paymentKey;
    private String cancelReason; // 결제취소 사유

}
