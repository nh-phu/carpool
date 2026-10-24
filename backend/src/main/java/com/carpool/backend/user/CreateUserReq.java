package com.carpool.backend.user;

import jakarta.validation.constraints.NotBlank;

public record CreateUserReq(
        @NotBlank String name,
        @NotBlank String phoneNumber
) {
}
