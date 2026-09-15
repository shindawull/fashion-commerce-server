package com.ccommit.fashionserver.service;

import com.ccommit.fashionserver.common.exception.ErrorCode;
import com.ccommit.fashionserver.common.exception.FashionServerException;
import com.ccommit.fashionserver.config.TossPaymentConfig;
import com.ccommit.fashionserver.dto.*;
import com.ccommit.fashionserver.mapper.OrderMapper;
import com.ccommit.fashionserver.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Log4j2
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentMapper paymentMapper;
    private final TossPaymentConfig tossPaymentConfig;
    private final OrderMapper orderMapper;

    /*
     * 결제 승인
     *
     * 결제창에서 인증을 마친 결제를 최종 승인한다.
     * 인증 후 10분 안에 호출하지 않으면 결제가 만료된다.
     * */
    @Transactional
    public PaymentResponse confirmPayment(int userId, PaymentRequest request) {
        // 1. 주문 조회 - 존재하지 않는 주문번호면 예외
        OrderDto orderDto = orderMapper.getUserOrder(request.getOrderId(), userId);
        if (orderDto == null) {
            throw new FashionServerException(
                    ErrorCode.ORDER_NOT_FOUND_ERROR.getMessage(),
                    ErrorCode.ORDER_NOT_FOUND_ERROR.getStatus());
        }

        // 2. 금액 검증 - 클라이언트에서 금액을 조작해 승인하는 것을 막는다.
        if (orderDto.getTotalPrice() != request.getAmount()) {
            log.warn("[결제 금액 불일치] 주문금액: {}, 요청금액: {}",
                    orderDto.getTotalPrice(), request.getAmount());
            throw new FashionServerException(
                    ErrorCode.CARD_PAYMENT_AMOUNT_MISMATCH_ERROR.getMessage(),
                    ErrorCode.CARD_PAYMENT_AMOUNT_MISMATCH_ERROR.getStatus());
        }

        // 3. 토스페이먼츠 승인 요청
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        httpHeaders.add("Authorization", tossPaymentConfig.getAuthorizationHeader());

        Map<String, Object> params = new HashMap<>();
        params.put("paymentKey", request.getPaymentKey());
        params.put("orderId", request.getOrderId());
        params.put("amount", request.getAmount());

        HttpEntity<Map<String, Object>> requestData = new HttpEntity<>(params, httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        URI uri = URI.create(tossPaymentConfig.getTossPaymentUrl() + "/payments/confirm");

        ResponseEntity<PaymentResponse> responseEntity;
        try {
            responseEntity = restTemplate.exchange(
                    uri.toString(), HttpMethod.POST, requestData, PaymentResponse.class);
        } catch (RestClientException e) {
            throw new FashionServerException(
                    ErrorCode.HTTP_SERVER_ERROR.getMessage() + ", 토스페이먼츠 응답: " + e.getMessage(),
                    ErrorCode.HTTP_SERVER_ERROR.getStatus());
        }

        // 4. 결제 정보 저장 - orderId는 orders.id(PK)
        PaymentDto paymentDto = PaymentDto.builder()
                .orderId(orderDto.getId())
                .paymentKey(responseEntity.getBody().getPaymentKey())
                .status(PaymentStatus.PAYMENT_COMPLETE.getPaymentCode())
                .build();

        if (paymentMapper.insertPaymentInfo(paymentDto) == 0) {
            throw new FashionServerException(
                    ErrorCode.CARD_PAYMENT_INSERT_ERROR.getMessage(),
                    ErrorCode.CARD_PAYMENT_INSERT_ERROR.getStatus());
        }

        log.info("[결제 승인 완료] orderNumber: {}, payment: {}",
                request.getOrderId(), responseEntity.getBody().getPaymentKey());

        return responseEntity.getBody();
    }

    public PaymentResponse getPaymentHistory(String orderId) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add("Authorization", tossPaymentConfig.getAuthorizationHeader());

        HttpEntity<String> requestData = new HttpEntity<>(httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        URI uri = URI.create(tossPaymentConfig.getTossPaymentUrl() + "/orders/" + orderId);
        ResponseEntity<PaymentResponse> responseEntity;
        try {
            responseEntity = restTemplate.exchange(uri.toString(), HttpMethod.GET, requestData, PaymentResponse.class);
        } catch (HttpClientErrorException e) {
            throw new FashionServerException(
                    ErrorCode.HTTP_SERVER_ERROR.getMessage() + ", 토스페이먼츠 응답: " + e.getMessage(),
                    ErrorCode.HTTP_SERVER_ERROR.getStatus());
        }
        if (responseEntity.getStatusCodeValue() != 200)
            throw new FashionServerException(
                    ErrorCode.CARD_PAYMENT_SELECT_ERROR.getMessage(),
                    ErrorCode.CARD_PAYMENT_SELECT_ERROR.getStatus());
        return responseEntity.getBody();
    }

    public PaymentResponse paymentCancel(PaymentDto paymentDto) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add("Authorization", tossPaymentConfig.getAuthorizationHeader());

        Map<String, Object> params = new HashMap<>();
        params.put("cancelReason", paymentDto.getCancelReason());

        HttpEntity<Map<String, Object>> requestData = new HttpEntity<>(params, httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        URI uri = URI.create(tossPaymentConfig.getTossPaymentUrl()
                + "/payments/" + paymentDto.getPaymentKey() + "/cancel");
        ResponseEntity<PaymentResponse> responseEntity;
        try {
            responseEntity = restTemplate.exchange(uri.toString(), HttpMethod.POST, requestData, PaymentResponse.class);
        } catch (HttpClientErrorException e) {
            throw new FashionServerException(
                    ErrorCode.HTTP_SERVER_ERROR.getMessage() + ", 토스페이먼츠 응답: " + e.getMessage(),
                    ErrorCode.HTTP_SERVER_ERROR.getStatus());
        }
        if (responseEntity.getStatusCodeValue() == 200) {
            paymentDto.setStatus(PaymentStatus.PAYMENT_CANCEL.getPaymentCode());
            int result = paymentMapper.updatePaymentCancel(paymentDto);
            if (result == 0)
                throw new FashionServerException(
                        ErrorCode.CARD_PAYMENT_UPDATE_ERROR.getMessage(),
                        ErrorCode.CARD_PAYMENT_UPDATE_ERROR.getStatus());
        }
        return responseEntity.getBody();
    }
}
