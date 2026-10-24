package com.carpool.backend.booking;

public record BookingRes(
        Long id,
        Long passengerId,
        Long tripId,
        int seats,
        BookingStatus status
) {
    public static BookingRes from(Booking booking) {
        return new BookingRes(
                booking.getId(),
                booking.getPassenger().getId(),
                booking.getTrip().getId(),
                booking.getSeats(),
                booking.getStatus()
        );
    }
}
