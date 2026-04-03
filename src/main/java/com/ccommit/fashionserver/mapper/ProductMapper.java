package com.ccommit.fashionserver.mapper;

import com.ccommit.fashionserver.dto.ProductDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductMapper {

    int insertProduct(ProductDto productDto);

    int updateProduct(ProductDto productDto);

    ProductDto getDetailProduct(int productId);

    List<ProductDto> getProductList(int categoryId, String searchType, int categoryAllNumber);

    int deleteProduct(int id);
}
