package com.ccommit.fashionserver.dto;

import lombok.*;

import java.sql.Date;
import java.util.List;

/**
 * packageName    : com.ccommit.fashionserver.dto
 * fileName       : OrderDto
 * author         : juoiy
 * date           : 2023-09-27
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-09-27        juoiy       최초 생성
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private int id;             // 주문번호
    private String orderId; // 주문번호
    private int totalPrice;     // 총금액
    private int status;         // 주문상태
    private int paymentId;      // 결제번호
    private Date createDate;    // 등록날짜
    private Date updateDate;    // 수정날짜
    private int shippingStatus; // 배송상태
    private String productInfo; // 상품정보
    private int userId;         // 구매자번호
    private List<ProductInfoDto> productInfoDtoList; // 상품정보

}


