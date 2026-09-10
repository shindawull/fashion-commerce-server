package com.ccommit.fashionserver.service;

import com.ccommit.fashionserver.common.exception.ErrorCode;
import com.ccommit.fashionserver.common.exception.FashionServerException;
import com.ccommit.fashionserver.dto.request.cart.CartRequest;
import com.ccommit.fashionserver.dto.response.cart.CartResponse;
import com.ccommit.fashionserver.dto.response.product.ProductResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ProductService productService;

    // 정상적인 상품 추가 요청 객체 생성
    private CartRequest buildCartRequest(int productId, int quantity) {
        CartRequest request = new CartRequest();
        request.setProductId(productId);
        request.setQuantity(quantity);
        return request;
    }

    private ProductResponse buildProductResponse(int id, int price, int saleQuantity) {
        return ProductResponse.builder()
                .id(id)
                .name("남녀공용 통기성 베이직 라운드 셔츠")
                .brandName("캐럿")
                .price(price)
                .saleQuantity(saleQuantity)
                .build();
    }

    @Nested
    @DisplayName("addCart() - 장바구니 담기")
    class AddCart {

        @Test
        @DisplayName("[성공] 정상 장바구니 담기")
        void addCart_success() {
            //given
            int userId = 1;
            CartRequest request = buildCartRequest(6, 2);
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.get("cart:1", "6")).willReturn("2");
            given(productService.getDetailProduct(6)).willReturn(buildProductResponse(6, 9900, 50));
            given(hashOperations.increment("cart:1", "6", 2L)).willReturn(2L);
            // addCart 마지막에 getCartList를 호출하므로 조회용 given도 필요
            given(hashOperations.entries("cart:1")).willReturn(Map.of("6", "2"));

            //when
            List<CartResponse> result = cartService.addCart(userId, request);

            //then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getQuantity()).isEqualTo(2);
            then(hashOperations).should(times(1)).increment("cart:1", "6", 2L);
        }

        @Test
        @DisplayName("[성공] 이미 담긴 상품 추가 담기 -> 수량 합산")
        void addCart_success_accumulate() {
            int userId = 1;
            CartRequest request = buildCartRequest(6, 2);

            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(productService.getDetailProduct(6)).willReturn(buildProductResponse(6, 9900, 50));
            given(hashOperations.get("cart:1", "6")).willReturn("3"); // 이미 3개 담겨있음
            given(hashOperations.increment("cart:1", "6", 2L)).willReturn(5L);
            given(hashOperations.entries("cart:1")).willReturn(Map.of("6","5"));

            List<CartResponse> result = cartService.addCart(userId, request);

            assertThat(result.get(0).getQuantity()).isEqualTo(5);
            then(hashOperations).should(times(1)).increment("cart:1", "6", 2L);
        }

        @Test
        @DisplayName("[실패] 누적 수량이 재고 초과 -> PRODUCT_QUANTITY_NOT_ENOUGH_ERROR")
        void addCart_fail_accumulatedStockExceeded() {
            // given
            int userId = 1;
            CartRequest request = buildCartRequest(6, 30);

            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(productService.getDetailProduct(6)).willReturn(buildProductResponse(6, 9900, 50));
            given(hashOperations.get("cart:1", "6")).willReturn("30");   // 이미 30개 + 30개 = 60 > 재고 50

            // when & then
            assertThatThrownBy(() -> cartService.addCart(userId, request))
                    .isInstanceOf(FashionServerException.class)
                    .hasMessage(ErrorCode.PRODUCT_QUANTITY_NOT_ENOUGH_ERROR.getMessage());

            then(hashOperations).should(never()).increment(any(), any(), anyLong());
        }

        @Test
        @DisplayName("[실패] 주문 수량이 1 미만 -> PRODUCT_QUANTITY_NOT_ENOUGH_ERROR")
        void addCart_fail_invalidQuantity() {
            //given
            int userId = 1;
            CartRequest request = buildCartRequest(6, 0);
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(productService.getDetailProduct(6)).willReturn(buildProductResponse(6, 9900, 50));

            //when & then
            assertThatThrownBy(() -> cartService.addCart(userId, request))
                    .isInstanceOf(FashionServerException.class)
                    .hasMessage(ErrorCode.PRODUCT_QUANTITY_NOT_ENOUGH_ERROR.getMessage());
        }

        @Test
        @DisplayName("[실패] 주문 수량이 재고보다 많음 -> PRODUCT_QUANTITY_NOT_ENOUGH_ERROR")
        void addCart_fail_notEnoughStock() {
            int userId = 1;
            CartRequest request = buildCartRequest(6, 100);

            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(productService.getDetailProduct(6))
                    .willReturn(buildProductResponse(6, 9900, 50)); // 재고 50개

            //when & then
            assertThatThrownBy(() -> cartService.addCart(userId, request))
                    .isInstanceOf(FashionServerException.class)
                    .hasMessage(ErrorCode.PRODUCT_QUANTITY_NOT_ENOUGH_ERROR.getMessage());
        }
    }

    @Nested
    @DisplayName("getCartList() - 장바구니 조회")
    class GetCartList {
        @Test
        @DisplayName("[성공] 정상 장바구니 조회")
        void getCartList_success() {
            int userId = 1;
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.entries("cart:1")).willReturn(Map.of("6", "2"));
            given(productService.getDetailProduct(6)).willReturn(buildProductResponse(6, 9900, 50));

            List<CartResponse> result = cartService.getCartList(userId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getProductId()).isEqualTo(6);
            assertThat(result.get(0).getQuantity()).isEqualTo(2);
            assertThat(result.get(0).getTotalPrice()).isEqualTo(19800); // 9900 * 2
        }

        @Test
        @DisplayName("[성공] 담긴 상품 없음 - 빈 리스트 반환")
        void getCartList_success_emptyList() {
            int userId = 1;
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.entries("cart:1")).willReturn(Collections.emptyMap());

            List<CartResponse> result = cartService.getCartList(userId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateCart() - 장바구니 수정")
    class UpdateCart {
        @Test
        @DisplayName("[성공] 정상 장바구니 수량 변경")
        void updateCart_success() {
            int userId = 1;
            CartRequest request = buildCartRequest(3, 1);

            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.hasKey("cart:1", "3")).willReturn(true);
            given(productService.getDetailProduct(3)).willReturn(buildProductResponse(3, 4900, 100));
            given(hashOperations.entries("cart:1")).willReturn(Map.of("3", "1"));

            List<CartResponse> result = cartService.updateCart(userId, request);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getQuantity()).isEqualTo(1);
            then(hashOperations).should(times(1)).put("cart:1", "3", "1");
        }

        @Test
        @DisplayName("[실패] 담긴 상품 없음 - CART_PRODUCT_NOT_USING_ERROR")
        void updateCart_fail_notInCart() {
            int userId = 1;
            CartRequest request = buildCartRequest(3, 1);
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.hasKey("cart:1", "3")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> cartService.updateCart(userId, request))
                    .isInstanceOf(FashionServerException.class)
                    .hasMessage(ErrorCode.CART_PRODUCT_NOT_USING_ERROR.getMessage());
        }

        @Test
        @DisplayName("[실패] 주문 수량 1 미만 -> PRODUCT_QUANTITY_NOT_ENOUGH_ERROR")
        void updateCart_fail_invalidQuantity() {
            int userId = 1;
            CartRequest request = buildCartRequest(3, 0);

            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.hasKey("cart:1", "3")).willReturn(true);
            given(productService.getDetailProduct(3)).willReturn(buildProductResponse(3, 4900, 100));

            // when & then
            assertThatThrownBy(() -> cartService.updateCart(userId, request))
                    .isInstanceOf(FashionServerException.class)
                    .hasMessage(ErrorCode.PRODUCT_QUANTITY_NOT_ENOUGH_ERROR.getMessage());
        }

        @Test
        @DisplayName("[실패] 주문 수량이 재고보다 많음 -> PRODUCT_QUANTITY_NOT_ENOUGH_ERROR")
        void updateCart_fail_notEnoughStock() {
            int userId = 1;
            CartRequest request = buildCartRequest(3, 102);

            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.hasKey("cart:1", "3")).willReturn(true);
            given(productService.getDetailProduct(3)).willReturn(buildProductResponse(3, 4900, 100));

            // when & then
            assertThatThrownBy(() -> cartService.updateCart(userId, request))
                    .isInstanceOf(FashionServerException.class)
                    .hasMessage(ErrorCode.PRODUCT_QUANTITY_NOT_ENOUGH_ERROR.getMessage());
        }
    }

    @Nested
    @DisplayName("deleteCart() - 장바구니 특정상품 삭제")
    class DeleteCart {
        @Test
        @DisplayName("[성공] 정상 장바구니 특정 상품 삭제")
        void deleteCart_success() {
            int userId = 1;
            //given
            //Long deletedCount = redisTemplate.opsForHash().delete(cartKey, String.valueOf(productId));
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.delete("cart:1", "3")).willReturn(1L);
            given(hashOperations.entries("cart:1")).willReturn(Map.of("6", "2"));
            given(productService.getDetailProduct(6)).willReturn(buildProductResponse(6, 9900, 50));

            //when
            List<CartResponse> result = cartService.deleteCart(userId, 3);

            //then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getProductId()).isEqualTo(6);
        }

        @Test
        @DisplayName("[실패] 담긴 상품 없음 -> CART_PRODUCT_NOT_USING_ERROR")
        void deleteCart_fail_notInCart() {
            int userId = 1;

            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.delete("cart:1", "3")).willReturn(0L);

            assertThatThrownBy(() -> cartService.deleteCart(userId, 3))
                    .isInstanceOf(FashionServerException.class)
                    .hasMessage(ErrorCode.CART_PRODUCT_NOT_USING_ERROR.getMessage());
        }
    }

    @Nested
    @DisplayName("clearCart() - 장바구니 비우기")
    class ClearCart {
        @Test
        @DisplayName("[성공] 정상 장바구니 비우기")
        void clearCart_success() {
            int userId = 1;

            cartService.clearCart(userId);

            //then(hashOperations.delete("cart:1")).should();틀림
            then(redisTemplate).should(times(1)).delete("cart:1");
        }
    }
}