package com.caio.ordermanagement.order.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequest(

    @NotNull 
    Long userId,

    @NotEmpty 
    List<@Valid  ItemRequest> items
 
) {

    public record ItemRequest(

        @NotNull 
        Long productId,

        @Positive 
        int quantity
        
    ) {}
}