package com.caio.ordermanagement.product.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProductDescriptionRequest(

    @NotBlank
    String description

) {}