package com.ccommit.fashionserver.dto;

import lombok.*;
import org.json.simple.JSONObject;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private String paymentKey;      //결제 키값
    private String orderId;         //주문번호
    private String orderName;       //주문 상품 이름
    private String customerName;    //구매자 명
    private JSONObject card;        //카드정보

}
