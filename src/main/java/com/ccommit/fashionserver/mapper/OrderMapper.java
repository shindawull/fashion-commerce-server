package com.ccommit.fashionserver.mapper;

import com.ccommit.fashionserver.dto.OrderDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {
    int insertOrder(OrderDto orderDto);

    List<OrderDto> getUserOrderList(int userId);

    OrderDto getUserOrder(@Param("orderId") String orderId,
                          @Param("userId") int userId);

    int decreaseSaleQuantity(@Param("orderQuantity") int orderQuantity,
                             @Param("productId") int productId);

    int isExistOrderId(String orderId);

    String getOrderCancelPossibleDate(@Param("status") int status,
                                      @Param("orderId") String orderId);

    int isOrderCancelPossible(@Param("orderId") String orderId,
                              @Param("status") int status,
                              @Param("orderCancelPossibleDate") String orderCancelPossibleDate);

    int updateOrderCancel(OrderDto orderDto);

    int updateOrderPaymentId(OrderDto orderDto);
}
