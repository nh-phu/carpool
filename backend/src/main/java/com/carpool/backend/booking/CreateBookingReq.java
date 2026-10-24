package com.carpool.backend.booking;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateBookingReq(
        @NotNull Long passengerId,
        @NotNull Long tripId,
        int seats
) {
}
