package com.caio.ordermanagement.product.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record GetProductResponse(

    Long id,
    String name,
    String description,
    BigDecimal price,
    boolean active,
    Instant createdAt,
    Instant updatedAt

) {}