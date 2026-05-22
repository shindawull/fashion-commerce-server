package com.ccommit.fashionserver.dto;

import lombok.*;

/**
 * packageName    : com.ccommit.fashionserver.dto
 * fileName       : PaymentReq
 * author         : juoiy
 * date           : 2023-10-14
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-14        juoiy       최초 생성
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    private int amount;    //결제 금액
    private String orderId; // 주문ID
    private String orderName; // 주문명 (필수, 최대 100자, ex. 생수 외 1건)
    private String customerName; //구매자 명
    private String paymentKey;

    // 카드결제에 필요한 Request Body 파라미터
    private String cardNumber; //카드번호(필수, 최대 20자)
    private String cardExpirationYear; // 카드 유효연도(필수)
    private String cardExpirationMonth; // 카드 유효 월(필수)
    private String customerIdentityNumber; // 카드 소유자 정보(필수, 생년월일 6자리'YYMMDD 혹은 사업자등로번호10자리)


}
