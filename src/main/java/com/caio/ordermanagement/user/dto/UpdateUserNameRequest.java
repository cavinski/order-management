package com.caio.ordermanagement.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserNameRequest(

    @NotBlank 
    @Size(max = 100)
    String name

) {}