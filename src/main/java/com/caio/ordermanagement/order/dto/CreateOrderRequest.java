package com.caio.ordermanagement.order.dto;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequest(

    @NotNull 
    Long userId,

    @NotEmpty 
    List<ItemRequest> items
 
) {

    public record ItemRequest(

        @NotNull 
        Long productId,

        @Positive 
        int quantity
        
    ) {}
}