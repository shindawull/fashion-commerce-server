package com.ccommit.fashionserver.service;

import com.ccommit.fashionserver.dto.OrderItemDto;
import com.ccommit.fashionserver.dto.ProductDto;
import com.ccommit.fashionserver.dto.RequestProductDto;
import com.ccommit.fashionserver.dto.response.product.ProductResponse;
import com.ccommit.fashionserver.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/*
 * 주문 상품 서비스
 * 주문 상품의 재고 차감, 금액 계산, 저장을 담당한다.
 * OrderService는 주문 전체 흐름만 조율하고 상품 단위 처리는 여기서 한다.
 * */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderItemService {
    private final OrderMapper orderMapper;
    private final ProductService productService;

    /**
     * 주문 상품 목록을 만들면서 재고를 차감한다.
     * 재고 차감은 ProductService에서 원자적 UPDATE로 처리된다.
     */
    public List<OrderItemDto> createOrderItems(RequestProductDto request) {
        List<OrderItemDto> orderItemDtos = new ArrayList<>();

        for (ProductDto orderProduct : request.getProductDtoList()) {
            int productId = orderProduct.getId();
            int orderQuantity = orderProduct.getSaleQuantity();

            ProductResponse productDto = productService.getDetailProduct(productId);

            // 재고 차감 (원자적 UPDATE + 캐시 무효화)
            productService.decreaseStock(productId, orderQuantity);

            // 주문 상품 정보 담기
            orderItemDtos.add(OrderItemDto.builder()
                    .productId(productId)
                    .productName(productDto.getName())
                    .quantity(orderQuantity)
                    .price(productDto.getPrice())
                    .build());

            log.debug("[주문 상품] productId: {}, orderQuantity: {}, price: {}",
                    productId, orderQuantity, productDto.getPrice());
        }
        return orderItemDtos;
    }

    /* 주문 총액 계산 */
    public int calculateTotalPrice(List<OrderItemDto> orderItemDtos) {
        return orderItemDtos.stream().mapToInt(item -> item.getPrice() * item.getQuantity()).sum();
    }

    /* 주문 상품 저장 - forEach안에서는 쓰는 변수 값이 변하면 안되서
       orders INSERT 후 생성된 PK를 각 항목에 셋팅한다. */
    public void saveOrderItem(int orderId, List<OrderItemDto> orderItemDtos) {
        orderItemDtos.forEach(item -> item.setOrderId(orderId));
        orderMapper.insertOrderItem(orderItemDtos);
    }

    public List<OrderItemDto> getOrderItems(int orderId) {
        return orderMapper.getOrderItems(orderId);
    }
}
