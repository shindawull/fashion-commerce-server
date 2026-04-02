package com.ccommit.fashionserver.controller;

import com.ccommit.fashionserver.aop.CommonResponse;
import com.ccommit.fashionserver.aop.LoginCheck;
import com.ccommit.fashionserver.dto.ProductDto;
import com.ccommit.fashionserver.service.ProductService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/list")
    @LoginCheck(types = {LoginCheck.UserType.USER, LoginCheck.UserType.SELLER, LoginCheck.UserType.ADMIN})
    public ResponseEntity<CommonResponse<List<ProductDto>>> getProductList(Integer loginSession, String categoryName, String searchType) {
        List<ProductDto> resultProductDtoList = (List<ProductDto>) productService.getProductList(categoryName, searchType);
        CommonResponse<List<ProductDto>> response = new CommonResponse<>(HttpStatus.OK, "SUCCESS", "상품 목록 조회 성공", resultProductDtoList);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}")
    @LoginCheck(types = {LoginCheck.UserType.USER, LoginCheck.UserType.SELLER, LoginCheck.UserType.ADMIN})
    public ResponseEntity<CommonResponse<ProductDto>> getProductDetail(Integer loginSession, @PathVariable("productId") int productId) {
        ProductDto resultProductDto = productService.getDetailProduct(productId);
        CommonResponse<ProductDto> response = new CommonResponse<>(HttpStatus.OK, "SUCCESS", "상품 상세 조회 성공", resultProductDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("")
    @LoginCheck(types = LoginCheck.UserType.SELLER)
    public ResponseEntity<CommonResponse<ProductDto>> insertProduct(Integer loginSession, @Valid @RequestBody ProductDto productDto) {
        productService.insertProduct(loginSession, productDto);
        CommonResponse<ProductDto> response = new CommonResponse<>(HttpStatus.OK, "SUCCESS", "상품 등록 성공", productDto);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("")
    @LoginCheck(types = LoginCheck.UserType.SELLER)
    public ResponseEntity<CommonResponse<ProductDto>> updateProduct(Integer loginSession, @RequestBody ProductDto productDto) {
        ProductDto resultProductDto = productService.updateProduct(loginSession, productDto);
        CommonResponse<ProductDto> response = new CommonResponse<>(HttpStatus.OK, "SUCCESS", "상품 수정 성공", resultProductDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("")
    @LoginCheck(types = LoginCheck.UserType.SELLER)
    public void deleteProduct(Integer loginSession, int id) {
        productService.deleteProduct(id);
    }
}
