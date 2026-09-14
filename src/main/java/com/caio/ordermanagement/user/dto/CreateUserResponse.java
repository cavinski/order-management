package com.caio.ordermanagement.user.dto;

import java.time.Instant;

public record CreateUserResponse(

    Long id,
    String name,
    String email,
    boolean active,
    Instant createdAt

) {}