package com.ccommit.fashionserver.service;

import com.ccommit.fashionserver.config.TossPaymentConfig;
import com.ccommit.fashionserver.dto.PaymentDto;
import com.ccommit.fashionserver.dto.PaymentRequest;
import com.ccommit.fashionserver.dto.PaymentResponse;
import com.ccommit.fashionserver.dto.PaymentStatus;
import com.ccommit.fashionserver.common.exception.ErrorCode;
import com.ccommit.fashionserver.common.exception.FashionServerException;
import com.ccommit.fashionserver.mapper.PaymentMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Service
public class PaymentService {
    @Autowired
    private final PaymentMapper paymentMapper;

    @Autowired
    TossPaymentConfig tossPaymentConfig;

    public PaymentService(PaymentMapper paymentMapper) {
        this.paymentMapper = paymentMapper;
    }

    public PaymentResponse insertCardPayment(PaymentRequest paymentRequest) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add("Authorization", tossPaymentConfig.getTossPaymentApiKey());

        PaymentDto paymentDto = new PaymentDto();
        Map<String, Object> params = new HashMap<>();
        params.put("amount", paymentRequest.getAmount());
        params.put("cardExpirationMonth", paymentRequest.getCardExpirationMonth());
        params.put("cardExpirationYear", paymentRequest.getCardExpirationYear());
        params.put("cardNumber", paymentRequest.getCardNumber());
        params.put("customerIdentityNumber", paymentRequest.getCustomerIdentityNumber());
        params.put("orderId", paymentRequest.getOrderId());
        params.put("orderName", paymentRequest.getOrderName());

        HttpEntity<Map<String, Object>> requestData = new HttpEntity<>(params, httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        URI uri = URI.create(tossPaymentConfig.getTossPaymentUrl() + "/key-in");
        ResponseEntity<PaymentResponse> responseEntity;
        try {
            responseEntity = restTemplate.exchange(uri.toString(), HttpMethod.POST, requestData, PaymentResponse.class);
        } catch (HttpClientErrorException e) {
            throw new FashionServerException(ErrorCode.valueOf("HTTP_SERVER_ERROR").getMessage() + ", 토스페이먼츠 응답: " + e.getMessage(), 660);
        }

        if (responseEntity.getStatusCodeValue() != 200) {
            throw new FashionServerException(ErrorCode.valueOf("CARD_PAYMENT_SUCCESS_ERROR").getMessage(), responseEntity.getStatusCodeValue());
        } else {
            paymentDto.setPaymentKey(responseEntity.getBody().getPaymentKey());
            paymentDto.setOrderId(responseEntity.getBody().getOrderId());
            paymentDto.setStatus(PaymentStatus.PAYMENT_COMPLETE.getPaymentCode());
            paymentDto.setCardNumber(paymentRequest.getCardNumber());
            int result = paymentMapper.insertPaymentInfo(paymentDto);
            if (result == 0)
                throw new FashionServerException(ErrorCode.valueOf("CARD_PAYMENT_INSERT_ERROR").getMessage(), 651);
        }
        return responseEntity.getBody();
    }

    public PaymentResponse getPaymentHistory(String orderId) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add("Authorization", tossPaymentConfig.getTossPaymentApiKey());

        HttpEntity<String> requestData = new HttpEntity<>(httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        URI uri = URI.create(tossPaymentConfig.getTossPaymentUrl() + "/orders/" + orderId);
        ResponseEntity<PaymentResponse> responseEntity;
        try {
            responseEntity = restTemplate.exchange(uri.toString(), HttpMethod.GET, requestData, PaymentResponse.class);
        } catch (HttpClientErrorException e) {
            throw new FashionServerException(ErrorCode.valueOf("HTTP_SERVER_ERROR").getMessage() + ", 토스페이먼츠 응답: " + e.getMessage(), 660);
        }
        if (responseEntity.getStatusCodeValue() != 200)
            throw new FashionServerException(ErrorCode.valueOf("CARD_PAYMENT_SELECT_ERROR").getMessage(), 653);
        return responseEntity.getBody();
    }

    public PaymentResponse paymentCancel(PaymentDto paymentDto) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add("Authorization", tossPaymentConfig.getTossPaymentApiKey());

        Map<String, Object> params = new HashMap<>();
        params.put("cancelReason", paymentDto.getCancelReason());

        HttpEntity<Map<String, Object>> requestData = new HttpEntity<>(params, httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        URI uri = URI.create(tossPaymentConfig.getTossPaymentUrl() + "/" + paymentDto.getPaymentKey() + "/cancel");
        ResponseEntity<PaymentResponse> responseEntity;
        try {
            responseEntity = restTemplate.exchange(uri.toString(), HttpMethod.POST, requestData, PaymentResponse.class);
        } catch (HttpClientErrorException e) {
            throw new FashionServerException(ErrorCode.valueOf("HTTP_SERVER_ERROR").getMessage() + ", 토스페이먼츠 응답: " + e.getMessage(), 660);
        }
        if (responseEntity.getStatusCodeValue() == 200) {
            paymentDto.setStatus(PaymentStatus.PAYMENT_CANCEL.getPaymentCode());
            int result = paymentMapper.updatePaymentCancel(paymentDto);
            if (result == 0)
                throw new FashionServerException(ErrorCode.valueOf("CARD_PAYMENT_UPDATE_ERROR").getMessage(), 652);
        }
        return responseEntity.getBody();
    }
}
