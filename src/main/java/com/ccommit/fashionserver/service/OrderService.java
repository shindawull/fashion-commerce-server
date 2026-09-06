package com.ccommit.fashionserver.service;

import com.ccommit.fashionserver.common.exception.ErrorCode;
import com.ccommit.fashionserver.common.exception.FashionServerException;
import com.ccommit.fashionserver.dto.*;
import com.ccommit.fashionserver.dto.response.product.ProductResponse;
import com.ccommit.fashionserver.mapper.OrderMapper;
import com.ccommit.fashionserver.mapper.PaymentMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {
    private final OrderMapper orderMapper;
    private final ProductService productService;
    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @Transactional
    public OrderDto insertOrder(int userId, RequestProductDto orderProductList) throws JsonProcessingException {
        OrderDto orderDto = new OrderDto();
        List<OrderItemDto> orderItemDtos = new ArrayList<>();
        int orderTotalPrice = 0;

        for (int i = 0; i < orderProductList.getProductDtoList().size(); i++) {
            ProductDto orderProduct = orderProductList.getProductDtoList().get(i);
            int productId = orderProduct.getId();
            int orderQuantity = orderProduct.getSaleQuantity();

            ProductResponse productDto = productService.getDetailProduct(productId);

            // 재고 차감 (원자적 UPDATE + 캐시 무효화)
            productService.decreaseStock(productId, orderQuantity);

            // 금액 계산
            int orderPrice = orderQuantity * productDto.getPrice();
            orderTotalPrice += orderPrice;

            log.debug("productId: {}, orderQuantity: {}, price: {}, orderPrice: {}, orderTotalPrice: {}",
                    productId, orderQuantity, productDto.getPrice(), orderPrice, orderTotalPrice);

            // 주문 상품 정보 담기
            orderItemDtos.add(OrderItemDto.builder()
                    .productId(productId)
                    .productName(productDto.getName())
                    .quantity(orderQuantity)
                    .price(productDto.getPrice())
                    .build());
        } // for end

        String orderName = orderItemDtos.get(0).getProductName()
                + " 외 " + (orderItemDtos.size() - 1) + "개";

        orderDto.setTotalPrice(orderTotalPrice);
        orderDto.setStatus(OrderStatus.ORDER_COMPLETE.getStatus());
        orderDto.setShippingStatus("PREPARING"); // TODO: 책임 분리 후 수정
        orderDto.setUserId(userId);

        final int LENGTH = 20; // 주문번호 길이 제한

        String orderId = RandomStringUtils.randomAlphanumeric(LENGTH);
        orderDto.setOrderId(orderId);
        int isExistOrderId = orderMapper.isExistOrderId(orderDto.getOrderId());

        if (isExistOrderId != 0)
            throw new FashionServerException(
                    ErrorCode.ORDER_DUPLICATION_ERROR.getMessage(), ErrorCode.ORDER_DUPLICATION_ERROR.getStatus());

        int insertResult = orderMapper.insertOrder(orderDto);
        if (insertResult == 0)
            throw new FashionServerException(
                    ErrorCode.ORDER_INSERT_ERROR.getMessage(),
                    ErrorCode.ORDER_INSERT_ERROR.getStatus());

        /* forEach안에서는 쓰는 변수 값이 변하면 안되서
        useGeneratedKeys 로 채워진 PK를 따로 각 항목에 셋팅 */
        int generatedOrderId = orderDto.getId();
        orderItemDtos.forEach(item -> item.setOrderId(generatedOrderId));

        orderMapper.insertOrderItem(orderItemDtos);

        // TODO: 카드결제 API START
        /*PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(orderDto.getTotalPrice());
        paymentRequest.setCardExpirationMonth("06");
        paymentRequest.setCardExpirationYear("25");
        paymentRequest.setCardNumber("5388032333580235");
        paymentRequest.setCustomerIdentityNumber("950609");
        paymentRequest.setOrderId(orderDto.getOrderId());
        paymentRequest.setOrderName(orderName);
        paymentService.insertCardPayment(paymentRequest);

        int paymentId = paymentMapper.getPaymentInfo(orderDto.getOrderId()).getId();
        orderDto.setPaymentId(paymentId);

        if (orderMapper.updateOrderPaymentId(orderDto) == 0)
            throw new FashionServerException(
                    ErrorCode.ORDER_UPDATE_ERROR.getMessage(), ErrorCode.ORDER_UPDATE_ERROR.getStatus());*/

        return orderMapper.getUserOrder(orderDto.getOrderId(), orderDto.getUserId());
    }

    public List<OrderDto> getUserOrderList(int userId) throws ParseException {
        List<OrderDto> responseOrders = orderMapper.getUserOrderList(userId);

        if (responseOrders.isEmpty())
            throw new FashionServerException(
                    ErrorCode.ORDER_NOT_FOUND_ERROR.getMessage(), ErrorCode.ORDER_NOT_FOUND_ERROR.getStatus());

        for (OrderDto orderDto : responseOrders) {
            orderDto.setOrderItems(orderMapper.getOrderItems(orderDto.getId()));
        }

        return responseOrders;
    }

    public List<ProductResponse> getDetailProductInfo(RequestProductDto orderProductList) {
        List<ProductResponse> productDtoList = new ArrayList<>();

        for (int i = 0; i < orderProductList.getProductDtoList().size(); i++) {
            ProductResponse productDto = productService.getDetailProduct(orderProductList.getProductDtoList().get(i).getId());
            productDto.setSaleQuantity(orderProductList.getProductDtoList().get(i).getSaleQuantity());
            productDtoList.add(productDto);
        }

        return productDtoList;
    }

    public OrderDto orderCancel(int userId, String orderId, PaymentDto paymentDto) {
        if (orderMapper.getUserOrder(orderId, userId) == null)
            throw new FashionServerException(
                    ErrorCode.ORDER_NOT_FOUND_ERROR.getMessage(), ErrorCode.ORDER_NOT_FOUND_ERROR.getStatus());

        String orderCancelPossibleDate = orderMapper.getOrderCancelPossibleDate(OrderStatus.ORDER_COMPLETE.getStatus(), orderId);

        if (orderCancelPossibleDate == null)
            throw new FashionServerException(
                    ErrorCode.ORDER_CANCEL_IMPOSSIBLE_DATE_ERROR.getMessage(),
                    ErrorCode.ORDER_CANCEL_IMPOSSIBLE_DATE_ERROR.getStatus());

        int isOrderCancelPossible = orderMapper.isOrderCancelPossible(orderId, OrderStatus.ORDER_COMPLETE.getStatus(), orderCancelPossibleDate);

        if (isOrderCancelPossible == 0)
            throw new FashionServerException(
                    ErrorCode.ORDER_CANCEL_IMPOSSIBLE_ERROR.getMessage(),
                    ErrorCode.ORDER_CANCEL_IMPOSSIBLE_ERROR.getStatus());

        PaymentDto paymentDtoInto = paymentMapper.getPaymentInfo(orderId);
        if (paymentDtoInto == null)
            throw new FashionServerException(
                    ErrorCode.PAYMENT_NOT_FOUND_ERROR.getMessage(), ErrorCode.PAYMENT_NOT_FOUND_ERROR.getStatus());

        paymentDtoInto.setCancelReason(paymentDto.getCancelReason());

        // 토스페이먼츠 결제 취소 API : START
        // 토스페이먼츠 결제 취소 API : START
        PaymentResponse paymentResponse = paymentService.paymentCancel(paymentDtoInto);
        OrderDto orderDto = new OrderDto();
        orderDto.setStatus(OrderStatus.PAYMENT_CANCEL.getStatus());
        orderDto.setOrderId(orderId);
        int updateResult = orderMapper.updateOrderCancel(orderDto);
        if (updateResult == 0)
            throw new FashionServerException(ErrorCode.ORDER_UPDATE_ERROR.getMessage(), ErrorCode.ORDER_UPDATE_ERROR.getStatus());

        // TODO: 취소 시 상품재고 복원
        return orderMapper.getUserOrder(orderDto.getOrderId(), orderDto.getUserId());
    }

}
