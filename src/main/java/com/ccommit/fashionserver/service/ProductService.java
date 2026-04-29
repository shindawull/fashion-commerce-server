package com.ccommit.fashionserver.service;

import com.ccommit.fashionserver.common.exception.ErrorCode;
import com.ccommit.fashionserver.common.exception.FashionServerException;
import com.ccommit.fashionserver.dto.CategoryType;
import com.ccommit.fashionserver.dto.ProductDto;
import com.ccommit.fashionserver.dto.SearchType;
import com.ccommit.fashionserver.dto.request.product.ProductInsertRequest;
import com.ccommit.fashionserver.dto.request.product.ProductSearchRequest;
import com.ccommit.fashionserver.dto.request.product.ProductUpdateRequest;
import com.ccommit.fashionserver.dto.response.product.ProductResponse;
import com.ccommit.fashionserver.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    public List<ProductResponse> getProductList(ProductSearchRequest request) {
        String categoryName = request.getCategoryName() == null ?
                CategoryType.ALL.getName() : request.getCategoryName();
        String searchType = request.getSearchType() == null ?
                SearchType.NEW.getName() : request.getSearchType().toUpperCase();

        CategoryType validCategoryType = CategoryType.from(categoryName);
        SearchType validSearchType = SearchType.from(searchType);

        log.info("[상품 조회] categoryType : {}, searchType: {}",
                validCategoryType.getName(), validSearchType.getName());

        int categoryAllNumber = CategoryType.ALL.getNumber();
        List<ProductDto> productDtoList = productMapper.getProductList(
                validCategoryType.getNumber(), validSearchType.getName(), categoryAllNumber);

        if (productDtoList == null || productDtoList.isEmpty())
            return Collections.emptyList();

        return productDtoList.stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    public ProductResponse getDetailProduct(int productId) {
        // TODO: Redis 캐싱 적용 예정
        ProductDto productDto = productMapper.getDetailProduct(productId);
        if (productDto == null)
            throw new FashionServerException(ErrorCode.PRODUCT_NOT_FOUND_ERROR.getMessage(),
                    ErrorCode.PRODUCT_NOT_FOUND_ERROR.getStatus());
        return ProductResponse.from(productDto);
    }

    @Transactional
    public ProductResponse insertProduct(Integer loginSession, ProductInsertRequest request) {
        CategoryType validCategoryType = CategoryType.from(request.getCategoryName());

        ProductDto productDto = ProductDto.builder()
                .name(request.getName())
                .saleQuantity(request.getSaleQuantity())
                .price(request.getPrice())
                .categoryId(validCategoryType.getNumber())
                .brandName(request.getBrandName())
                .saleId(loginSession)
                .build();

        int result = productMapper.insertProduct(productDto);
        if (result == 0)
            throw new FashionServerException(ErrorCode.PRODUCT_INSERT_ERROR.getMessage(), ErrorCode.PRODUCT_INSERT_ERROR.getStatus());

        log.info("[상품 등록] 완료 productId: {}", productDto.getId());
        return ProductResponse.from(productMapper.getDetailProduct(productDto.getId()));
    }

    @Transactional
    public ProductResponse updateProduct(Integer loginSession, ProductUpdateRequest request) {
        if (productMapper.getDetailProduct(request.getId()) == null) {
            throw new FashionServerException(ErrorCode.PRODUCT_NOT_FOUND_ERROR.getMessage(),
                    ErrorCode.PRODUCT_NOT_FOUND_ERROR.getStatus());
        }

        ProductDto productDto = ProductDto.builder()
                .id(request.getId())
                .name(request.getName())
                .saleQuantity(request.getSaleQuantity())
                .price(request.getPrice())
                .brandName(request.getBrandName())
                .saleId(loginSession)
                .build();

        int result = productMapper.updateProduct(productDto);
        if (result == 0)
            throw new FashionServerException(ErrorCode.PRODUCT_UPDATE_ERROR.getMessage(),
                    ErrorCode.PRODUCT_UPDATE_ERROR.getStatus());

        return ProductResponse.from(productMapper.getDetailProduct(productDto.getId()));
    }

    @Transactional
    public void deleteProduct(int id) {
        if (productMapper.getDetailProduct(id) == null) {
            throw new FashionServerException(ErrorCode.PRODUCT_NOT_FOUND_ERROR.getMessage(),
                    ErrorCode.PRODUCT_NOT_FOUND_ERROR.getStatus());
        }

        int result = productMapper.deleteProduct(id);
        if (result == 0) {
            throw new FashionServerException(ErrorCode.PRODUCT_DELETE_ERROR.getMessage(),
                    ErrorCode.PRODUCT_DELETE_ERROR.getStatus());
        }
    }
}
