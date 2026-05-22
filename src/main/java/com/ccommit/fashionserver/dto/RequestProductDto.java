package com.ccommit.fashionserver.dto;

import lombok.*;

import java.util.List;

/**
 * packageName    : com.ccommit.fashionserver.dto
 * fileName       : RequestProductDto
 * author         : juoiy
 * date           : 2023-11-08
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-11-08        juoiy       최초 생성
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestProductDto {
    private List<ProductDto> productDtoList;
}
