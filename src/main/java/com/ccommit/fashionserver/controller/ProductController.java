package com.ccommit.fashionserver.controller;

import com.ccommit.fashionserver.aop.LoginCheck;
import com.ccommit.fashionserver.common.CommonResponse;
import com.ccommit.fashionserver.dto.request.product.ProductInsertRequest;
import com.ccommit.fashionserver.dto.request.product.ProductSearchRequest;
import com.ccommit.fashionserver.dto.request.product.ProductUpdateRequest;
import com.ccommit.fashionserver.dto.response.product.ProductResponse;
import com.ccommit.fashionserver.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    @GetMapping("")
    public ResponseEntity<CommonResponse<List<ProductResponse>>> getProductList(@ModelAttribute ProductSearchRequest request) {
        log.info("[상품 목록 조회] categoryName: {}, searchType: {}", request.getCategoryName(), request.getSearchType());
        List<ProductResponse> result = (List<ProductResponse>) productService.getProductList(request);
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, "SUCCESS", "상품 목록 조회 성공", result));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<CommonResponse<ProductResponse>> getProductDetail(
            @PathVariable @Min(value = 1, message = "상품ID는 1 이상이어야 합니다.") int productId) {
        log.info("[상품 상세 조회] productId: {}", productId);
        ProductResponse result = productService.getDetailProduct(productId);
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, "SUCCESS", "상품 상세 조회 성공", result));
    }

    @PostMapping("")
    @LoginCheck(types = LoginCheck.UserType.SELLER)
    public ResponseEntity<CommonResponse<ProductResponse>> insertProduct(Integer loginSession, @Valid @RequestBody ProductInsertRequest request) {
        log.info("[상품 등록] 요청 loginSession: {}", loginSession);
        ProductResponse result = productService.insertProduct(loginSession, request);
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.CREATED, "SUCCESS", "상품 등록 성공", result));
    }

    @PatchMapping("")
    @LoginCheck(types = LoginCheck.UserType.SELLER)
    public ResponseEntity<CommonResponse<ProductResponse>> updateProduct(Integer loginSession, @RequestBody ProductUpdateRequest request) {
        log.info("[상품 수정] 요청 loginSession: {}, productId: {}", loginSession, request.getId());
        ProductResponse result = productService.updateProduct(loginSession, request);
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, "SUCCESS", "상품 수정 성공", result));
    }

    @DeleteMapping("/{id}")
    @LoginCheck(types = LoginCheck.UserType.SELLER)
    public ResponseEntity<CommonResponse<String>> deleteProduct(Integer loginSession, @PathVariable int id) {
        log.info("[상품 삭제] 요청 id: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, "SUCCESS", "상품 삭제 성공", null));
    }
}
