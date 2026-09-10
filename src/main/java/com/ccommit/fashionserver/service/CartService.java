package com.ccommit.fashionserver.service;

import com.ccommit.fashionserver.common.exception.ErrorCode;
import com.ccommit.fashionserver.common.exception.FashionServerException;
import com.ccommit.fashionserver.dto.request.cart.CartRequest;
import com.ccommit.fashionserver.dto.response.cart.CartResponse;
import com.ccommit.fashionserver.dto.response.product.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 장바구니 서비스
 * [설계 의도]
 * - 장바구니는 DB테이블 없이 Redis를 메인 저장소로 사용한다.
 *  -> 임시 데이터이고, 빠른 읽기/쓰기가 필요하며, TTL로 자동 만료가 가능하기 때문.
 * - @Cacheable 대신 RedisTemplate 을 쓰는 이유
 *  -> @Cacheable 은 DB 조회 결과를 캐싱하는 용도(보조 저장소)라 목적이 다름.
 *  -> 장바구니는 Redis 가 원본이므로 직접 제어가 필요하다.
 *
 * [Redis 자료구조 — Hash]
 * cart:1  (userId = 1)
 * ├─ 10 → 2   (상품 10번 2개)
 * ├─ 20 → 5   (상품 20번 5개)
 * └─ 30 → 1   (상품 30번 1개)
 *
 * [상품 정보를 저장하지 않는 이유]
 * - productId와 수량만 저장하고, 조회 시 상품 정보는 ProductService에서 가져온다.
 * - 상품명/가격을 통째로 저장하면 판매자가 가격을 수정했을 때
 * 장바구니에는 옛날 가격이 남아 주문 시 정합성이 깨진다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    private final StringRedisTemplate redisTemplate;
    private final ProductService productService;
    private static final String CART_KEY_PREFIX = "cart:";
    private static final Duration CART_TTL = Duration.ofDays(7);

    private String getCartKey(Integer userId) {
        return CART_KEY_PREFIX + userId;
    }

    public List<CartResponse> addCart(Integer userId, CartRequest request) {
        ProductResponse product = productService.getDetailProduct(request.getProductId());

        String cartKey = getCartKey(userId);
        String field = String.valueOf(request.getProductId());

        Object currentQuantity = redisTemplate.opsForHash().get(cartKey, field);

        int cartQuantity = currentQuantity == null ? 0 : Integer.parseInt(String.valueOf(currentQuantity));

        // HINCRBY - 기존 수량에 더한다. 없으면 0에서 시작
        int requestedQuantity = cartQuantity + request.getQuantity();

        validateQuantity(requestedQuantity);
        validateStock(product, requestedQuantity);

        Long resultQuantity = redisTemplate.opsForHash().increment(cartKey, field, request.getQuantity());

        // 담을 때마다 만료 시간 갱신
        redisTemplate.expire(cartKey, CART_TTL);

        log.info("[장바구니 담기] userId: {}, productId: {}, 누적수량: {}",
                userId, request.getProductId(), resultQuantity);

        return getCartList(userId);
    }

    public List<CartResponse> getCartList(Integer userId) {
        // HGETALL - { productId : quantity } 전체 조회
        Map<Object, Object> cartMap = redisTemplate.opsForHash().entries(getCartKey(userId));

        if (cartMap.isEmpty()) {
            log.info("[장바구니 조회] userId: {} - 담긴 상품 없음", userId);
            return Collections.emptyList();
        }

        List<CartResponse> cartList = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : cartMap.entrySet()) {
            int productId = Integer.parseInt(String.valueOf(entry.getKey()));
            int quantity = Integer.parseInt(String.valueOf(entry.getValue()));

            //상품 정보는 항상 최신값으로 조회 (@Cacheable 적용되어 있어 DB부하 적응)
            ProductResponse product = productService.getDetailProduct(productId);
            cartList.add(CartResponse.of(product, quantity));
        }

        return cartList;
    }

    public List<CartResponse> updateCart(Integer userId, CartRequest request) {
        String cartKey = getCartKey(userId);
        String field = String.valueOf(request.getProductId());

        if (!redisTemplate.opsForHash().hasKey(cartKey, field)) {
            throw new FashionServerException(
                    ErrorCode.CART_PRODUCT_NOT_USING_ERROR.getMessage(),
                    ErrorCode.CART_PRODUCT_NOT_USING_ERROR.getStatus());
        }

        ProductResponse product = productService.getDetailProduct(request.getProductId());
        validateQuantity(request.getQuantity());
        validateStock(product, request.getQuantity());

        //HSET - 기존 값을 덮어쓴다
        redisTemplate.opsForHash().put(cartKey, field, String.valueOf(request.getQuantity()));
        redisTemplate.expire(cartKey, CART_TTL);

        log.info("[장바구니 수량 변경] userId: {}, productId: {}, 변경수량: {}",
                userId, request.getProductId(), request.getQuantity());

        return getCartList(userId);
    }

    public List<CartResponse> deleteCart(Integer userId, int productId) {
        String cartKey = getCartKey(userId);

        // HDEL - 삭제된 field 개수 반환, 0이면 애초에 없던 상품
        Long deletedCount = redisTemplate.opsForHash().delete(cartKey, String.valueOf(productId));

        if (deletedCount == 0) {
            throw new FashionServerException(
                    ErrorCode.CART_PRODUCT_NOT_USING_ERROR.getMessage(),
                    ErrorCode.CART_PRODUCT_NOT_USING_ERROR.getStatus());
        }

        log.info("[장바구니 상품 삭제] userId: {}, productId: {}", userId, productId);
        return getCartList(userId);
    }

    public void clearCart(Integer userId) {
        // DEL - key 자체를 삭제
        redisTemplate.delete(getCartKey(userId));
        log.info("[장바구니 비우기] userId: {}", userId);
    }

    private void validateQuantity(int quantity) {
        if (quantity < 1) {
            throw new FashionServerException(
                    ErrorCode.PRODUCT_QUANTITY_NOT_ENOUGH_ERROR.getMessage(),
                    ErrorCode.PRODUCT_QUANTITY_NOT_ENOUGH_ERROR.getStatus());
        }
    }

    private void validateStock(ProductResponse product, int quantity) {
        if (product.getSaleQuantity() < quantity) {
            throw new FashionServerException(
                    ErrorCode.PRODUCT_QUANTITY_NOT_ENOUGH_ERROR.getMessage(),
                    ErrorCode.PRODUCT_QUANTITY_NOT_ENOUGH_ERROR.getStatus());
        }
    }
}
