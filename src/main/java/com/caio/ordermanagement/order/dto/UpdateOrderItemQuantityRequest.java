package com.caio.ordermanagement.order.dto;

import jakarta.validation.constraints.Positive;

public record UpdateOrderItemQuantityRequest(

    @Positive 
    int quantity
) {}