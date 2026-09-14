package com.ccommit.fashionserver.controller;

import com.ccommit.fashionserver.aop.LoginCheck;
import com.ccommit.fashionserver.common.CommonResponse;
import com.ccommit.fashionserver.common.exception.ErrorCode;
import com.ccommit.fashionserver.common.exception.FashionServerException;
import com.ccommit.fashionserver.dto.OrderDto;
import com.ccommit.fashionserver.dto.PaymentDto;
import com.ccommit.fashionserver.dto.RequestProductDto;
import com.ccommit.fashionserver.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping("")
    @LoginCheck(types = LoginCheck.UserType.USER)
    public ResponseEntity<CommonResponse<OrderDto>> insertOrder(Integer userId, @RequestBody RequestProductDto orderProductList) throws JsonProcessingException {
        if (orderProductList == null || orderProductList.getProductDtoList() == null
                || orderProductList.getProductDtoList().isEmpty())
            throw new FashionServerException(
                    ErrorCode.PRODUCT_NOT_FOUND_ERROR.getMessage(), ErrorCode.PRODUCT_NOT_FOUND_ERROR.getStatus());

        OrderDto orderDto = orderService.insertOrder(userId, orderProductList);
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, "SUCCESS", "상품 주문에 성공하였습니다.", orderDto));
    }

    @GetMapping("/list")
    @LoginCheck(types = LoginCheck.UserType.USER)
    public ResponseEntity<CommonResponse<List<OrderDto>>> getUserOrderList(Integer userId) {
        List<OrderDto> orderDtoList = orderService.getUserOrderList(userId);
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, "SUCCESS", "주문 목록 조회에 성공하였습니다.", orderDtoList));
    }

    @PatchMapping("/{orderId}/cancel")
    @LoginCheck(types = LoginCheck.UserType.USER)
    public ResponseEntity<CommonResponse<OrderDto>> orderCancel(Integer userId, @PathVariable("orderId") String orderId, @RequestBody PaymentDto paymentDto) {
        OrderDto orderDto = orderService.orderCancel(userId, orderId, paymentDto);
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, "SUCCESS", "주문번호 " + orderId + " 이 정상적으로 취소되었습니다.", orderDto));
    }


}
