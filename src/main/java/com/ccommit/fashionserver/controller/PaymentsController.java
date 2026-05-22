package com.ccommit.fashionserver.controller;

import com.ccommit.fashionserver.common.CommonResponse;
import com.ccommit.fashionserver.aop.LoginCheck;
import com.ccommit.fashionserver.dto.PaymentDto;
import com.ccommit.fashionserver.dto.PaymentRequest;
import com.ccommit.fashionserver.dto.PaymentResponse;
import com.ccommit.fashionserver.common.exception.ErrorCode;
import com.ccommit.fashionserver.common.exception.FashionServerException;
import com.ccommit.fashionserver.service.PaymentService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * packageName    : com.ccommit.fashionserver.controller
 * fileName       : PaymentController
 * author         : juoiy
 * date           : 2023-10-14
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-14        juoiy       최초 생성
 */
@Log4j2
@RestController
@RequestMapping("/payments")
public class PaymentsController {
    @Autowired
    private final PaymentService paymentService;

    public PaymentsController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/card-payment")
    @LoginCheck(types = LoginCheck.UserType.USER)
    public ResponseEntity<CommonResponse<PaymentResponse>> insertCardPayment(Integer loginSession, @RequestBody PaymentRequest paymentRequest) {
        PaymentResponse result = paymentService.insertCardPayment(paymentRequest);
        CommonResponse<PaymentResponse> response = new CommonResponse<>(HttpStatus.OK, "SUCCESS", "카드결제가 성공하였습니다.", result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    @LoginCheck(types = LoginCheck.UserType.USER)
    public ResponseEntity<CommonResponse<PaymentResponse>> getPaymentHistory(Integer loginSession, @PathVariable("orderId") String orderId) {
        PaymentResponse result = paymentService.getPaymentHistory(orderId);
        CommonResponse<PaymentResponse> response = new CommonResponse<>(HttpStatus.OK, "SUCCESS", "카드결제 조회에 성공하였습니다.", result);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/cancel")
    @LoginCheck(types = LoginCheck.UserType.USER)
    public ResponseEntity<CommonResponse<PaymentResponse>> paymentCancel(Integer loginSession, @RequestBody PaymentDto paymentDto) {
        if (paymentDto.getPaymentKey() == null || paymentDto.getCancelReason() == null)
            throw new FashionServerException(ErrorCode.valueOf("INPUT_NULL_ERROR").getMessage(), 999);
        paymentService.paymentCancel(paymentDto);
        CommonResponse<PaymentResponse> response = new CommonResponse<>(HttpStatus.OK, "SUCCESS", "결제 취소가 되었습니다.", null);
        return ResponseEntity.ok(response);
    }

}
