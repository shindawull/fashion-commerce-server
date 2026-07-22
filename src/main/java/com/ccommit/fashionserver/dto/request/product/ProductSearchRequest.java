package com.ccommit.fashionserver.dto.request.product;

import com.ccommit.fashionserver.dto.CategoryType;
import com.ccommit.fashionserver.dto.SearchType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@NoArgsConstructor
public class ProductSearchRequest {
    private String categoryName;
    private String searchType;

    public String getCategoryName() {
        return categoryName == null ? CategoryType.ALL.getName() : categoryName;
    }

    public String getSearchType() {
        return searchType == null ? SearchType.NEW.getName() : searchType.toUpperCase();
    }
}
