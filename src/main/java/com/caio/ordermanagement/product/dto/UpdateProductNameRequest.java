package com.caio.ordermanagement.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProductNameRequest(

    @NotBlank
    @Size(max = 100)
    String name

) {}