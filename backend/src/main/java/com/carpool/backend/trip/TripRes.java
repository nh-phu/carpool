package com.carpool.backend.trip;

import java.time.LocalDateTime;

public record TripRes(
        Long id,
        String start,
        Double startLatitude,
        Double startLongitude,
        String dest,
        Double destLatitude,
        Double destLongitude,
        LocalDateTime departureTime,
        int availableSeats,
        long priceSeat,
        TripDriverDto driver
) {
    public static TripRes from(Trip trip) {
        var driver = trip.getDriver();

        return new TripRes(
                trip.getId(),
                trip.getStart(),
                trip.getStartLatitude(),
                trip.getStartLongitude(),
                trip.getDest(),
                trip.getDestLatitude(),
                trip.getDestLongitude(),
                trip.getDepartureTime(),
                trip.getAvailableSeats(),
                trip.getPriceSeat(),
                new TripDriverDto(
                        driver.getId(),
                        driver.getName(),
                        driver.getPhoneNumber()
                )
        );
    }
}
