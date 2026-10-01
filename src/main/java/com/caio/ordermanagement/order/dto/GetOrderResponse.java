package com.caio.ordermanagement.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import com.caio.ordermanagement.order.OrderStatus;

public record GetOrderResponse(

    Long id,
    Long userId,
    OrderStatus status,
    BigDecimal total,
    Instant createdAt,
    List<ItemResponse> items

) {

    public record ItemResponse(

        Long productId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        boolean active

    ) {}
}