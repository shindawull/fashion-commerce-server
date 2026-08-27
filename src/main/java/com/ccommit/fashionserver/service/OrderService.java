package com.ccommit.fashionserver.service;

import com.ccommit.fashionserver.dto.*;
import com.ccommit.fashionserver.common.exception.ErrorCode;
import com.ccommit.fashionserver.common.exception.FashionServerException;
import com.ccommit.fashionserver.dto.response.product.ProductResponse;
import com.ccommit.fashionserver.mapper.OrderMapper;
import com.ccommit.fashionserver.mapper.PaymentMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Log4j2
@RequiredArgsConstructor
@Service
public class OrderService {
    private final OrderMapper orderMapper;
    private final ProductService productService;
    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;
    private final StringRedisTemplate redisTemplate;

    public OrderDto insertOrder(int userId, RequestProductDto orderProductList) throws JsonProcessingException {
        OrderDto orderDto = new OrderDto();
        ObjectMapper objectMapper = new ObjectMapper();
        int orderTotalPrice = 0;
        ArrayList<ProductInfoDto> productInfoDtoList = new ArrayList<>();
        for (int i = 0; i < orderProductList.getProductDtoList().size(); i++) {
            ProductResponse productDto = productService.getDetailProduct(orderProductList.getProductDtoList().get(i).getId());
            int productQuantity = productDto.getSaleQuantity();
            int orderQuantity = orderProductList.getProductDtoList().get(i).getSaleQuantity();
            if (productQuantity < 1 || productQuantity < orderQuantity)
                throw new FashionServerException(ErrorCode.valueOf("PRODUCT_QUANTITY_NOT_ENOUGH_ERROR").getMessage(), 614);

            int resultQuantity = productQuantity - orderQuantity;
            log.debug("상품수량 - 주문수량 = 차감결과수량 : " + productQuantity + " - " + orderQuantity + " = " + resultQuantity);

            int updateResult = orderMapper.updateSaleQuantity(resultQuantity, orderProductList.getProductDtoList().get(i).getId());
            if (updateResult == 0)
                throw new FashionServerException(ErrorCode.valueOf("PRODUCT_UPDATE_ERROR").getMessage(), 611);

            int orderPrice = orderQuantity * (productDto.getPrice());
            orderTotalPrice += orderPrice;
            log.debug("productId: " + productDto.getId() + ", orderQuantity: " + orderQuantity + ",price: " + productDto.getPrice()
                    + ", orderPrice: " + orderPrice + ", orderTotalPrice: " + orderTotalPrice);
            ProductInfoDto productInfoDto = ProductInfoDto.builder()
                    .id(productDto.getId())
                    .saleQuantity(orderQuantity)
                    .name(productDto.getName())
                    .price(productDto.getPrice())
                    .build();
            productInfoDtoList.add(productInfoDto);

        }
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
            throw new FashionServerException(ErrorCode.valueOf("ORDER_ID_DUPLICATION_ERROR").getMessage(), 631);
        int insertResult = orderMapper.insertOrder(orderDto);
        if (insertResult == 0)
            throw new FashionServerException(ErrorCode.valueOf("ORDER_INSERT_ERROR").getMessage(), 630);
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
            throw new FashionServerException(ErrorCode.valueOf("ORDER_UPDATE_ERROR").getMessage(), 636);
        return orderMapper.getUserOrder(orderDto.getOrderId(), orderDto.getUserId());
    }

    public List<OrderDto> getUserOrderList(int userId) throws ParseException {
        List<OrderDto> responseOrders = orderMapper.getUserOrderList(userId);
        if (responseOrders.size() == 0)
            throw new FashionServerException(ErrorCode.valueOf("ORDER_NOT_USING_ERROR").getMessage(), 632);
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

    @Cacheable(cacheNames = "cartList", key = "#userId")
    public List<ProductResponse> addCartList(int userId, RequestProductDto orderProductList) {
        List<ProductResponse> productDtoList = getDetailProductInfo(orderProductList);
        return productDtoList;
    }

    @CachePut(cacheNames = "cartList", key = "#userId")
    public List<ProductResponse> putCartList(int userId, RequestProductDto orderProductList) {
        List<ProductResponse> productDtoList = getDetailProductInfo(orderProductList);
        return productDtoList;
    }

    public List<ProductResponse> getCartList(int userId) throws ParseException {
        String redisKey = "cartList::" + userId;
        String redisValue = redisTemplate.opsForValue().get(redisKey);
        if (redisValue == null) {
            log.debug("장바구니에 담긴 상품이 없습니다.");
            throw new FashionServerException(ErrorCode.valueOf("CART_PRODUCT_NOT_USING_ERROR").getMessage(), 640);
        }
        List<ProductResponse> productDtoList = new ArrayList<>();
        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray = (JSONArray) jsonParser.parse(redisValue);
        List<JSONObject> jsonArrayProduct = (List<JSONObject>) jsonArray.get(1);
        for (int i = 0; i < jsonArrayProduct.size(); i++) {
            ProductResponse productDto = productService.getDetailProduct(Integer.parseInt(String.valueOf(jsonArrayProduct.get(i).get("id"))));
            productDto.setSaleQuantity(Integer.parseInt(String.valueOf(jsonArrayProduct.get(i).get("saleQuantity"))));
            productDtoList.add(productDto);
        }
        return productDtoList;
    }

    @CacheEvict(cacheNames = "cartList", key = "#userId", beforeInvocation = false)
    public OrderDto cartOrder(int userId) throws ParseException, JsonProcessingException {
        List<ProductResponse> productDtoList = getCartList(userId);
        RequestProductDto requestProductDto = new RequestProductDto();
        //requestProductDto.setProductDtoList(productDtoList);
        OrderDto orderDto = insertOrder(userId, requestProductDto);
        return orderDto;
    }

    public OrderDto orderCancel(int userId, String orderId, PaymentDto paymentDto) {
        if (orderMapper.getUserOrder(orderId, userId) == null)
            throw new FashionServerException(ErrorCode.valueOf("ORDER_NOT_USING_ERROR").getMessage(), 632);
        String orderCancelPossibleDate = orderMapper.getOrderCancelPossibleDate(OrderStatus.ORDER_COMPLETION.getStatus(), orderId);
        if (orderCancelPossibleDate == null)
            throw new FashionServerException(ErrorCode.valueOf("ORDER_CANCEL_POSSIBLE_DATE_NOT_USING_ERROR").getMessage(), 633);
        int isOrderCancelPossible = orderMapper.isOrderCancelPossible(orderId, OrderStatus.ORDER_COMPLETION.getStatus(), orderCancelPossibleDate);
        if (isOrderCancelPossible == 0)
            throw new FashionServerException(ErrorCode.valueOf("ORDER_CANCEL_IMPOSSIBLE_ERROR").getMessage(), 634);

        PaymentDto paymentDtoInto = paymentMapper.getPaymentInfo(orderId);
        if (paymentDtoInto == null)
            throw new FashionServerException(ErrorCode.valueOf("PAYMENT_NOT_USING_ERROR").getMessage(), 654);
        paymentDtoInto.setCancelReason(paymentDto.getCancelReason());
        // 토스페이먼츠 결제 취소 API : START
        PaymentResponse paymentResponse = paymentService.paymentCancel(paymentDtoInto);
        OrderDto orderDto = new OrderDto();
        orderDto.setStatus(OrderStatus.ORDER_CANCEL.getStatus());
        orderDto.setOrderId(orderId);
        int updateResult = orderMapper.updateOrderCancel(orderDto);
        if (updateResult == 0)
            throw new FashionServerException(ErrorCode.valueOf("ORDER_CANCEL_UPDATE_ERROR").getMessage(), 635);
        // TODO: 취소 시 상품재고 복원
        return orderMapper.getUserOrder(orderDto.getOrderId(), orderDto.getUserId());
    }

}
