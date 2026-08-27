package com.ccommit.fashionserver.mapper;

import com.ccommit.fashionserver.dto.PaymentDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentMapper {

    int insertPaymentInfo(PaymentDto paymentDto);

    int updatePaymentCancel(PaymentDto paymentDto);

    PaymentDto getPaymentInfo(String orderId);
}
