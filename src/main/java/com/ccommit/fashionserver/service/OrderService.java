package com.ccommit.fashionserver.service;

import com.ccommit.fashionserver.common.exception.ErrorCode;
import com.ccommit.fashionserver.common.exception.FashionServerException;
import com.ccommit.fashionserver.dto.*;
import com.ccommit.fashionserver.mapper.OrderMapper;
import com.ccommit.fashionserver.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {
    private final OrderMapper orderMapper;
    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;
    private final OrderItemService orderItemService;
    private final OrderNumberGenerator orderNumberGenerator;

    @Transactional
    public OrderDto insertOrder(int userId, RequestProductDto orderProductList) {
        // 1. 주문 상품 처리 (재고 차감 + 조립)
        List<OrderItemDto> orderItemDtos = orderItemService.createOrderItems(orderProductList);
        int orderTotalPrice = orderItemService.calculateTotalPrice(orderItemDtos);

        // 2. 주문 생성
        OrderDto orderDto = new OrderDto();
        orderDto.setOrderId(orderNumberGenerator.generator());
        orderDto.setTotalPrice(orderTotalPrice);
        orderDto.setStatus(OrderStatus.RECEIVED.getStatus()); // 결제 대기
        orderDto.setShippingStatus("PREPARING"); // TODO: 책임 분리 후 수정
        orderDto.setUserId(userId);

        /* TODO: 결제 복구 시 다시 복원
        String orderName = orderItemDtos.get(0).getProductName()
                + " 외 " + (orderItemDtos.size() - 1) + "개";*/

        int insertResult = orderMapper.insertOrder(orderDto);
        if (insertResult == 0)
            throw new FashionServerException(
                    ErrorCode.ORDER_INSERT_ERROR.getMessage(),
                    ErrorCode.ORDER_INSERT_ERROR.getStatus());

        // 3. 주문 상품 저장 (orders PK 참조)
        orderItemService.saveOrderItem(orderDto.getId(), orderItemDtos);

        /*int paymentId = paymentMapper.getPaymentInfo(orderDto.getOrderId()).getId();
        orderDto.setPaymentId(paymentId);*/
        // payment end

        /*if (orderMapper.updateOrderPaymentId(orderDto) == 0)
            throw new FashionServerException(
                    ErrorCode.ORDER_UPDATE_ERROR.getMessage(), ErrorCode.ORDER_UPDATE_ERROR.getStatus());
*/
        OrderDto result = orderMapper.getUserOrder(orderDto.getOrderId(), userId);
        result.setOrderItems(orderItemService.getOrderItems(result.getId()));

        return result;
    }

    public List<OrderDto> getUserOrderList(int userId) {
        List<OrderDto> responseOrders = orderMapper.getUserOrderList(userId);

        if (responseOrders.isEmpty())
            throw new FashionServerException(
                    ErrorCode.ORDER_NOT_FOUND_ERROR.getMessage(), ErrorCode.ORDER_NOT_FOUND_ERROR.getStatus());

        for (OrderDto orderDto : responseOrders) {
            orderDto.setOrderItems(orderItemService.getOrderItems(orderDto.getId()));
        }

        return responseOrders;
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
                    ErrorCode.CARD_PAYMENT_NOT_FOUND_ERROR.getMessage(), ErrorCode.CARD_PAYMENT_NOT_FOUND_ERROR.getStatus());

        paymentDtoInto.setCancelReason(paymentDto.getCancelReason());

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
