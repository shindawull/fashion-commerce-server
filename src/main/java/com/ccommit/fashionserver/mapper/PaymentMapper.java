package com.ccommit.fashionserver.mapper;

import com.ccommit.fashionserver.dto.PaymentDto;
import org.apache.ibatis.annotations.Mapper;

/**
 * packageName    : com.ccommit.fashionserver.mapper
 * fileName       : PaymentMapper
 * author         : juoiy
 * date           : 2023-10-14
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-14        juoiy       최초 생성
 */
@Mapper
public interface PaymentMapper {

    int insertPaymentInfo(PaymentDto paymentDto);

    int updatePaymentCancel(PaymentDto paymentDto);

    PaymentDto getPaymentInfo(String orderId);
}
