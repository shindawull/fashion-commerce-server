package com.ccommit.fashionserver.service;

import com.ccommit.fashionserver.common.exception.ErrorCode;
import com.ccommit.fashionserver.common.exception.FashionServerException;
import com.ccommit.fashionserver.dto.CategoryType;
import com.ccommit.fashionserver.dto.ProductDto;
import com.ccommit.fashionserver.dto.request.product.ProductInsertRequest;
import com.ccommit.fashionserver.dto.request.product.ProductSearchRequest;
import com.ccommit.fashionserver.dto.request.product.ProductUpdateRequest;
import com.ccommit.fashionserver.dto.response.product.ProductResponse;
import com.ccommit.fashionserver.mapper.ProductMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductMapper productMapper;

    /* 정상적인 상품 추가 요청 객체 생성 */
    private ProductInsertRequest buildInsertProductRequest() {
        ProductInsertRequest request = new ProductInsertRequest();
        request.setName("남녀공용 통기성 베이직 라운드 셔츠");
        request.setSaleQuantity(1);
        request.setPrice(9900);
        request.setCategoryName(CategoryType.CLOTHING.getName());
        request.setBrandName("캐럿");
        return request;
    }

    /* 정상적인 상품 수정 요청 객체 생성 */
    private ProductUpdateRequest buildUpdateProductRequest() {
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setId(1);
        request.setName("남녀공용 통기성 베이직 라운드 셔츠");
        request.setSaleQuantity(1);
        request.setPrice(10000);
        request.setCategoryName(CategoryType.CLOTHING.getName());
        request.setBrandName("캐럿");
        return request;
    }

    /* DB에서 조회되는 ProductDto 생성 */
    private ProductDto buildProductDto(int id, int saleId, CategoryType categoryType) {
        return ProductDto.builder()
                .id(id)
                .name("남녀공용 통기성 베이직 라운드 셔츠")
                .saleQuantity(1)
                .price(9900)
                .categoryId(categoryType.getNumber())
                .brandName("캐럿")
                .saleId(saleId)
                .build();
    }

    // ────────────────────────────────────────────────────────────────────────
    // 1. getProductList 테스트
    // ────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getProductList() - 상품 조회")
    class getProductList {

        @Test
        @DisplayName("[성공] 정상 상품 조회 ")
        void getProductList_success() {
            //given
            ProductSearchRequest request = new ProductSearchRequest();

            List<ProductDto> productDtoList = List.of(
                    buildProductDto(1, 1, CategoryType.CLOTHING),
                    buildProductDto(2, 1, CategoryType.CLOTHING)
            );
            given(productMapper.getProductList(anyInt(), anyString(), anyInt())).willReturn(productDtoList);

            //when
            List<ProductResponse> result = productService.getProductList(request);

            //then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("[성공] 등록된 상품 없음 -> 빈 리스트 반환")
        void getProductList_success_emptyList() {
            //given
            ProductSearchRequest request = new ProductSearchRequest();
            given(productMapper.getProductList(anyInt(), anyString(), anyInt())).willReturn(Collections.emptyList());
            //when
            List<ProductResponse> result = productService.getProductList(request);
            //then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[실패] 유효하지 않은 검색타입 -> SEARCH_TYPE_NOT_FOUND_ERROR")
        void getProductList_fail_invalidSearchType() {
            //given
            ProductSearchRequest request = new ProductSearchRequest();
            request.setSearchType("없는검색타입");

            //when & then
            assertThatThrownBy(() -> productService.getProductList(request))
                    .isInstanceOf(FashionServerException.class)
                    .hasMessage(ErrorCode.SEARCH_TYPE_NOT_FOUND_ERROR.getMessage());
        }

        @Test
        @DisplayName("[실패] 유효하지 않은 카테고리명 -> CATEGORY_TYPE_NOT_FOUND_ERROR")
        void getProductList_fail_invalidCategoryType() {
            //given
            ProductSearchRequest request = new ProductSearchRequest();
            request.setCategoryName("없는카테고리");

            //when & then
            assertThatThrownBy(() -> productService.getProductList(request))
                    .isInstanceOf(FashionServerException.class)
                    .hasMessage(ErrorCode.CATEGORY_NOT_FOUND_ERROR.getMessage());
        }
    }// end list

    // ────────────────────────────────────────────────────────────────────────
    // 2. getDetailProduct 테스트
    // ────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getDetailProduct() - 상품 상세 조회")
    class getDetailProduct {
        @Test
        @DisplayName("[성공] 정상 상품 상세 조회")
        void getDetailProduct_success() {
            //given
            int productId = 1;
            ProductDto productDto = buildProductDto(1, 1, CategoryType.CLOTHING);
            given(productMapper.getDetailProduct(productId)).willReturn(productDto);

            //when
            ProductResponse result = productService.getDetailProduct(productId);

            //then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(productId);
        }

        @Test
        @DisplayName("[실패] 유효하지 않은 상품 -> PRODUCT_NOT_FOUND_ERROR")
        void getDetailProduct_fail_invalidProduct() {
            //given
            int productId = 999;
            given(productMapper.getDetailProduct(productId)).willReturn(null);

            //when & then
            assertThatThrownBy(() -> productService.getDetailProduct(productId))
                    .isInstanceOf(FashionServerException.class)
                    .hasMessage(ErrorCode.PRODUCT_NOT_FOUND_ERROR.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // 3. insertProduct 테스트
    // ────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("insertProduct() - 상품 추가")
    class insertProduct {

        @Test
        @DisplayName("[성공] 정상 상품 추가")
        void insertProduct_success() {
            //given
            int saleId = 1;
            ProductInsertRequest request = buildInsertProductRequest();

            given(productMapper.insertProduct(any(ProductDto.class))).willReturn(1);
            // insert() 후 getDetailProduct로 결과 조회
            given(productMapper.getDetailProduct(anyInt())).willReturn(buildProductDto(1, saleId, CategoryType.CLOTHING));

            //when
            ProductResponse result = productService.insertProduct(saleId, request);

            //then
            assertThat(result).isNotNull();
            assertThat(result.getSaleId()).isEqualTo(saleId);

            then(productMapper).should(times(1)).insertProduct(any(ProductDto.class));
        }

        @Test
        @DisplayName("[실패] 유효하지 않은 카테고리명 -> CATEGORY_NOT_FOUND_ERROR")
        void insertProduct_fail_invalidCategory() {
            //given
            int saleId = 1;
            ProductInsertRequest request = buildInsertProductRequest();
            request.setCategoryName("없는카테고리");

            //when & then
            assertThatThrownBy(() -> productService.insertProduct(saleId, request))
                    .isInstanceOf(FashionServerException.class)
                    .hasMessage(ErrorCode.CATEGORY_NOT_FOUND_ERROR.getMessage());
        }

        @Test
        @DisplayName("[실패] Insert 결과 0 -> PRODUCT_INSERT_ERROR")
        void insertProduct_fail() {
            //given
            int saleId = 1;
            ProductInsertRequest request = buildInsertProductRequest();
            given(productMapper.insertProduct(any(ProductDto.class))).willReturn(0);

            //when & then
            assertThatThrownBy(() -> productService.insertProduct(saleId, request))
                    .isInstanceOf(FashionServerException.class)
                    .hasMessage(ErrorCode.PRODUCT_INSERT_ERROR.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // 4. updateProduct 테스트
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateProduct() - 상품 수정")
    class updateProduct {
        @Test
        @DisplayName("[성공] 정상 상품 수정")
        void updateProduct_success() {
            //given
            int saleId = 1;
            ProductUpdateRequest request = buildUpdateProductRequest();

            given(productMapper.getDetailProduct(anyInt())).willReturn(buildProductDto(1, saleId, CategoryType.CLOTHING));
            given(productMapper.updateProduct(any(ProductDto.class))).willReturn(1);

            //when
            ProductResponse result = productService.updateProduct(saleId, request);

            //then
            assertThat(result).isNotNull();
            assertThat(result.getSaleId()).isEqualTo(saleId);

            then(productMapper).should(times(1)).updateProduct(any(ProductDto.class));
        }

        @Test
        @DisplayName("[실패] 유효하지 않은 상품 -> PRODUCT_NOT_FOUND_ERROR")
        void updateProduct_fail_invalidProduct() {
            //given
            int saleId = 999;
            ProductUpdateRequest request = buildUpdateProductRequest();
            given(productMapper.getDetailProduct(anyInt())).willReturn(null);

            //when & then
            assertThatThrownBy(() -> productService.updateProduct(saleId, request))
                    .isInstanceOf(FashionServerException.class)
                    .hasMessage(ErrorCode.PRODUCT_NOT_FOUND_ERROR.getMessage());
        }

        @Test
        @DisplayName("[실패] update 결과 0 -> PRODUCT_UPDATE_ERROR")
        void updateProduct_fail() {
            //given
            int saleId = 1;
            ProductUpdateRequest request = buildUpdateProductRequest();
            given(productMapper.getDetailProduct(anyInt())).willReturn(buildProductDto(1, saleId, CategoryType.CLOTHING));
            given(productMapper.updateProduct(any(ProductDto.class))).willReturn(0);

            //when & then
            assertThatThrownBy(() -> productService.updateProduct(saleId, request))
                    .isInstanceOf(FashionServerException.class)
                    .hasMessage(ErrorCode.PRODUCT_UPDATE_ERROR.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // 5. deleteProduct 테스트
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteProduct() - 상품 삭제")
    class deleteProduct {
        @Test
        @DisplayName("[성공] 정상 상품 삭제")
        void deleteProduct_success() {
            // given
            int productId = 1;
            given(productMapper.getDetailProduct(productId))
                    .willReturn(buildProductDto(1, 1, CategoryType.CLOTHING));
            given(productMapper.deleteProduct(productId)).willReturn(1);

            // when
            productService.deleteProduct(productId);

            // then
            then(productMapper).should(times(1)).deleteProduct(productId);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 상품 삭제 -> PRODUCT_NOT_FOUND_ERROR")
        void deleteProduct_fail_productNotFound() {
            // given
            int productId = 999;
            given(productMapper.getDetailProduct(productId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> productService.deleteProduct(productId))
                    .isInstanceOf(FashionServerException.class)
                    .hasMessage(ErrorCode.PRODUCT_NOT_FOUND_ERROR.getMessage());
        }

        @Test
        @DisplayName("[실패] 삭제 결과 0 -> PRODUCT_DELETE_ERROR")
        void deleteProduct_fail_deleteError() {
            // given
            int productId = 1;
            given(productMapper.getDetailProduct(productId))
                    .willReturn(buildProductDto(1, 1, CategoryType.CLOTHING));
            given(productMapper.deleteProduct(productId)).willReturn(0);

            // when & then
            assertThatThrownBy(() -> productService.deleteProduct(productId))
                    .isInstanceOf(FashionServerException.class)
                    .hasMessage(ErrorCode.PRODUCT_DELETE_ERROR.getMessage());
        }

    }
}
