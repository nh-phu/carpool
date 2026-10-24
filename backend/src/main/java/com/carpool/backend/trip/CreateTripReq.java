package com.carpool.backend.trip;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateTripReq(
        @NotNull
        Long driverId,
        @NotBlank
        String start,
        @NotBlank
        String dest,

        @NotNull @Future
        LocalDateTime departureTime,

        @Min(1)
        int availableSeats,
        @NotNull
        long priceSeat
) {
}
