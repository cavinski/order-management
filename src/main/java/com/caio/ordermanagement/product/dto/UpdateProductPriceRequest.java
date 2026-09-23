package com.caio.ordermanagement.product.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

public record UpdateProductPriceRequest(

    @NotNull
    @DecimalMin(value = "0.00")
    BigDecimal price

) {}