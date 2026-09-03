package com.ccommit.fashionserver.service;

import com.ccommit.fashionserver.common.exception.ErrorCode;
import com.ccommit.fashionserver.common.exception.FashionServerException;
import com.ccommit.fashionserver.dto.*;
import com.ccommit.fashionserver.dto.response.product.ProductResponse;
import com.ccommit.fashionserver.mapper.OrderMapper;
import com.ccommit.fashionserver.mapper.PaymentMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
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
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayList<ProductInfoDto> productInfoDtoList = new ArrayList<>();
        int orderTotalPrice = 0;

        for (int i = 0; i < orderProductList.getProductDtoList().size(); i++) {
            ProductDto orderProduct = orderProductList.getProductDtoList().get(i);
            int productId = orderProduct.getId();
            int orderQuantity = orderProduct.getSaleQuantity();

            ProductResponse productDto = productService.getDetailProduct(productId);

            // 재고 차감 (원자적 UPDATE)
            int updateResult = orderMapper.decreaseSaleQuantity(orderQuantity, productId);
            if (updateResult == 0)
                throw new FashionServerException(
                        ErrorCode.PRODUCT_QUANTITY_NOT_ENOUGH_ERROR.getMessage(),
                        ErrorCode.PRODUCT_QUANTITY_NOT_ENOUGH_ERROR.getStatus());

            // 금액 계산
            int orderPrice = orderQuantity * productDto.getPrice();
            orderTotalPrice += orderPrice;

            log.debug("productId: {}, orderQuantity: {}, price: {}, orderPrice: {}, orderTotalPrice: {}",
                    productId, orderQuantity, productDto.getPrice(), orderPrice, orderTotalPrice);

            // 주문 상품 정보 담기
            ProductInfoDto productInfoDto = ProductInfoDto.builder()
                    .id(productId)
                    .saleQuantity(orderQuantity)
                    .name(productDto.getName())
                    .price(productDto.getPrice())
                    .build();
            productInfoDtoList.add(productInfoDto);
        } // for end

        String orderName = productInfoDtoList.get(0).getName() + " 외 " + (orderProductList.getProductDtoList().size() - 1) + "개";

        orderDto.setTotalPrice(orderTotalPrice);
        orderDto.setStatus(OrderStatus.ORDER_COMPLETION.getStatus());
        String json = objectMapper.writeValueAsString(productInfoDtoList);
        orderDto.setProductInfo(json);
        orderDto.setUserId(userId);

        final int LENGTH = 20; // 주문번호 길이 제한

        String orderId = RandomStringUtils.randomAlphanumeric(LENGTH);
        orderDto.setOrderId(orderId);
        int isExistOrderId = orderMapper.isExistOrderId(orderDto.getOrderId());

        if (isExistOrderId != 0)
            throw new FashionServerException(
                    ErrorCode.ORDER_DUPLICATION_ERROR.getMessage(),ErrorCode.ORDER_DUPLICATION_ERROR.getStatus());

        int insertResult = orderMapper.insertOrder(orderDto);
        if (insertResult == 0)
            throw new FashionServerException(
                    ErrorCode.ORDER_INSERT_ERROR.getMessage(),ErrorCode.ORDER_INSERT_ERROR.getStatus());

        // 카드결제 API START
        PaymentRequest paymentRequest = new PaymentRequest();
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
                    ErrorCode.ORDER_UPDATE_ERROR.getMessage(),ErrorCode.ORDER_UPDATE_ERROR.getStatus());

        return orderMapper.getUserOrder(orderDto.getOrderId(), orderDto.getUserId());
    }

    public List<OrderDto> getUserOrderList(int userId) throws ParseException {
        List<OrderDto> responseOrders = orderMapper.getUserOrderList(userId);

        if (responseOrders.isEmpty())
            throw new FashionServerException(
                    ErrorCode.ORDER_NOT_FOUND_ERROR.getMessage(),ErrorCode.USER_NOT_FOUND_ERROR.getStatus());

        for (int i = 0; i < responseOrders.size(); i++) {
            JSONParser jsonParser = new JSONParser();
            JSONArray jsonArray = (JSONArray) jsonParser.parse(responseOrders.get(i).getProductInfo());
            ArrayList<ProductInfoDto> productInfoDtoList = jsonArray;
            responseOrders.get(i).setProductInfo("");
            responseOrders.get(i).setProductInfoDtoList(productInfoDtoList);
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
                    ErrorCode.ORDER_NOT_FOUND_ERROR.getMessage(),ErrorCode.ORDER_NOT_FOUND_ERROR.getStatus());

        String orderCancelPossibleDate = orderMapper.getOrderCancelPossibleDate(OrderStatus.ORDER_COMPLETION.getStatus(), orderId);

        if (orderCancelPossibleDate == null)
            throw new FashionServerException(
                    ErrorCode.ORDER_CANCEL_IMPOSSIBLE_DATE_ERROR.getMessage(),
                    ErrorCode.ORDER_CANCEL_IMPOSSIBLE_DATE_ERROR.getStatus());

        int isOrderCancelPossible = orderMapper.isOrderCancelPossible(orderId, OrderStatus.ORDER_COMPLETION.getStatus(), orderCancelPossibleDate);

        if (isOrderCancelPossible == 0)
            throw new FashionServerException(
                    ErrorCode.ORDER_CANCEL_IMPOSSIBLE_ERROR.getMessage(),
                    ErrorCode.ORDER_CANCEL_IMPOSSIBLE_ERROR.getStatus());

        PaymentDto paymentDtoInto = paymentMapper.getPaymentInfo(orderId);
        if (paymentDtoInto == null)
            throw new FashionServerException(
                    ErrorCode.PAYMENT_NOT_FOUND_ERROR.getMessage(),ErrorCode.PAYMENT_NOT_FOUND_ERROR.getStatus());

        paymentDtoInto.setCancelReason(paymentDto.getCancelReason());

        // 토스페이먼츠 결제 취소 API : START
        // 토스페이먼츠 결제 취소 API : START
        PaymentResponse paymentResponse = paymentService.paymentCancel(paymentDtoInto);
        OrderDto orderDto = new OrderDto();
        orderDto.setStatus(OrderStatus.ORDER_CANCEL.getStatus());
        orderDto.setOrderId(orderId);
        int updateResult = orderMapper.updateOrderCancel(orderDto);
        if (updateResult == 0)
            throw new FashionServerException(ErrorCode.ORDER_UPDATE_ERROR.getMessage(),ErrorCode.ORDER_UPDATE_ERROR.getStatus());

        // TODO: 취소 시 상품재고 복원
        return orderMapper.getUserOrder(orderDto.getOrderId(), orderDto.getUserId());
    }

}
