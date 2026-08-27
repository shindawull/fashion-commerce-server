package com.ccommit.fashionserver.controller;

import com.ccommit.fashionserver.aop.LoginCheck;
import com.ccommit.fashionserver.common.CommonResponse;
import com.ccommit.fashionserver.dto.request.cart.CartRequest;
import com.ccommit.fashionserver.dto.response.cart.CartResponse;
import com.ccommit.fashionserver.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 장바구니 API
 * <p>
 * POST   /carts            상품 담기(같은 상품이면 수량 합산)
 * GET    /carts            장바구니 조회
 * PATCH  /carts            수량 변경
 * DELETE /carts{productId} 특정 상품 삭제
 * DELETE /carts            장바구니 비우기
 * <p>
 * userId는 @LoginCheck AOP가 세션에서 주입하므로 URL에 노출하지 않는다.
 * (기존 /orders/{userId}/carts 방식은 본인확인 코드가 매번 필요했음)
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/carts")
public class CartController {
    private final CartService cartService;

    @PostMapping("")
    @LoginCheck(types = LoginCheck.UserType.USER)
    public ResponseEntity<CommonResponse<List<CartResponse>>> addCart(Integer userId, @RequestBody CartRequest request) {
        List<CartResponse> cartList = cartService.addCart(userId, request);
        return  ResponseEntity.ok(new CommonResponse<>(
                HttpStatus.OK, "SUCCESS", "장바구니에 상품을 담았습니다.", cartList
        ));
    }

    @GetMapping("")
    @LoginCheck(types = LoginCheck.UserType.USER)
    public ResponseEntity<CommonResponse<List<CartResponse>>> getCartList(Integer userId) {
        List<CartResponse> cartList = cartService.getCartList(userId);
        return ResponseEntity.ok(new CommonResponse<>(
                HttpStatus.OK, "SUCCESS", "장바구니 목록 조회에 성공하였습니다.", cartList
        ));
    }

    @PatchMapping("")
    @LoginCheck(types = LoginCheck.UserType.USER)
    public ResponseEntity<CommonResponse<List<CartResponse>>> updateCart(Integer userId, @RequestBody CartRequest request) {
        List<CartResponse> cartList = cartService.updateCart(userId, request);
        return ResponseEntity.ok(new CommonResponse<>(
                HttpStatus.OK, "SUCCESS", "장바구니 상품 수량을 변경하였습니다.", cartList
        ));
    }

    @DeleteMapping("/{productId}")
    @LoginCheck(types = LoginCheck.UserType.USER)
    public ResponseEntity<CommonResponse<List<CartResponse>>> deleteCart(Integer userId, @PathVariable("productId") int productId) {
        List<CartResponse> cartList = cartService.deleteCart(userId, productId);
        return ResponseEntity.ok(new CommonResponse<>(
                HttpStatus.OK, "SUCCESS", "장바구니에서 상품을 삭제하였습니다.", cartList
        ));
    }

    @DeleteMapping("")
    @LoginCheck(types = LoginCheck.UserType.USER)
    public ResponseEntity<CommonResponse<List<CartResponse>>> clearCart(Integer userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(new CommonResponse<>(
                HttpStatus.OK, "SUCCESS", "장바구니를 비웠습니다.", null
        ));
    }
}
