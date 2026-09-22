package com.caio.ordermanagement.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateProductRequest(

    @NotBlank 
    @Size(max = 100)
    String name,

    @NotBlank 
    String description,

    @NotNull 
    @DecimalMin(value = "0.00") 
    BigDecimal price

) {}