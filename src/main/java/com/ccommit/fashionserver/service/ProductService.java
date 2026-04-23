package com.ccommit.fashionserver.service;

import com.ccommit.fashionserver.common.exception.ErrorCode;
import com.ccommit.fashionserver.common.exception.FashionServerException;
import com.ccommit.fashionserver.dto.CategoryType;
import com.ccommit.fashionserver.dto.ProductDto;
import com.ccommit.fashionserver.dto.SearchType;
import com.ccommit.fashionserver.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductMapper productMapper;

    public List<ProductDto> getProductList(String categoryName, String searchType) {
        int categoryId = 0;
        String searchTypeTemp = "";
        if (categoryName == null)
            categoryName = "전체";
        int categoryAllNumber = CategoryType.ALL.getNumber();

        for (CategoryType categoryType : CategoryType.values()) {
            if (categoryName.equals(categoryType.getName())) {
                categoryId = categoryType.getNumber();
                break;
            }
        }
        if (categoryId == 0) {
            throw new FashionServerException(ErrorCode.CATEGORY_NOT_FOUND_ERROR.getMessage(), ErrorCode.CATEGORY_NOT_FOUND_ERROR.getStatus());
        }
        searchType = searchType.toUpperCase();
        for (SearchType search : SearchType.values()) {
            if (searchType.equals(search.getName())) {
                searchTypeTemp = search.getName();
                break;
            }
        }
        if (searchTypeTemp.equals("") || searchTypeTemp == null)
            throw new FashionServerException(ErrorCode.SEARCH_TYPE_NOT_FOUND_ERROR.getMessage(), ErrorCode.SEARCH_TYPE_NOT_FOUND_ERROR.getStatus());
        List<ProductDto> productDtoList = productMapper.getProductList(categoryId, searchTypeTemp, categoryAllNumber);
        if (productDtoList == null)
            throw new FashionServerException(ErrorCode.PRODUCT_NOT_FOUND_ERROR.getMessage(), ErrorCode.PRODUCT_NOT_FOUND_ERROR.getStatus());
        return productDtoList;
    }

    public ProductDto getDetailProduct(int productId) {
        ProductDto productDto = productMapper.getDetailProduct(productId);
        if (productDto == null)
            throw new FashionServerException(
                    ErrorCode.PRODUCT_NOT_FOUND_ERROR.getMessage(), ErrorCode.PRODUCT_NOT_FOUND_ERROR.getStatus());
        return productDto;
    }

    public void insertProduct(Integer loginSession, ProductDto productDto) {
        CategoryType validCategoryType = Arrays.stream(CategoryType.values())
                .filter(categoryType -> categoryType.getName().equals(productDto.getCategoryName()))
                .findFirst()
                .orElseThrow(() -> {
                    throw new FashionServerException(ErrorCode.CATEGORY_NOT_FOUND_ERROR.getMessage(), ErrorCode.CATEGORY_NOT_FOUND_ERROR.getStatus());
                });

        productDto.setCategoryId(validCategoryType.getNumber());
        productDto.setSaleId(loginSession);
        int result = productMapper.insertProduct(productDto);
        if (result == 0)
            throw new FashionServerException(ErrorCode.PRODUCT_INSERT_ERROR.getMessage(), ErrorCode.PRODUCT_INSERT_ERROR.getStatus());
    }

    public ProductDto updateProduct(Integer loginSession, ProductDto productDto) {
        ProductDto resultProductDto = new ProductDto();
        productDto.setSaleId(loginSession);
        if (productMapper.getDetailProduct(productDto.getId()) == null) {
            log.debug("존재하지 않는 상품입니다.");
            throw new FashionServerException(ErrorCode.PRODUCT_NOT_FOUND_ERROR.getMessage(), ErrorCode.PRODUCT_NOT_FOUND_ERROR.getStatus());
        }
        int result = productMapper.updateProduct(productDto);
        if (result == 0)
            throw new FashionServerException(ErrorCode.PRODUCT_UPDATE_ERROR.getMessage(), ErrorCode.PRODUCT_UPDATE_ERROR.getStatus());
        else
            return resultProductDto = productMapper.getDetailProduct(productDto.getId());
    }

    public void deleteProduct(int id) {
        if (productMapper.getDetailProduct(id) == null) {
            log.debug("존재하지 않는 상품입니다.");
            throw new FashionServerException(ErrorCode.PRODUCT_NOT_FOUND_ERROR.getMessage(), ErrorCode.PRODUCT_NOT_FOUND_ERROR.getStatus());
        }
        int result = productMapper.deleteProduct(id);
        if (result == 0) {
            log.debug("상품 삭제에 실패하였습니다.");
            throw new FashionServerException(ErrorCode.PRODUCT_DELETE_ERROR.getMessage(), ErrorCode.PRODUCT_DELETE_ERROR.getStatus());
        }
    }
}
