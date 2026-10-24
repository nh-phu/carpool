package com.carpool.backend.booking;

import com.carpool.backend.exception.BookingException;
import com.carpool.backend.trip.Trip;
import com.carpool.backend.trip.TripRepo;
import com.carpool.backend.user.User;
import com.carpool.backend.user.UserRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepo bookingRepo;
    private final UserRepo userRepo;
    private final TripRepo tripRepo;

    public BookingService(
            BookingRepo bookingRepo,
            UserRepo userRepo,
            TripRepo tripRepo) {
        this.bookingRepo = bookingRepo;
        this.userRepo = userRepo;
        this.tripRepo = tripRepo;
    }

    @Transactional
    public Booking create(CreateBookingReq req) {
        User passenger = userRepo.findById(req.passengerId())
                .orElseThrow(() -> new BookingException(
                        "Passenger not found",
                       HttpStatus.NOT_FOUND
                ));

        Trip trip = tripRepo.findByIdForUpdate(req.tripId())
                .orElseThrow(() -> new BookingException(
                        "Trip not found",
                        HttpStatus.NOT_FOUND
                ));
        if(!trip.getDepartureTime().isAfter(LocalDateTime.now())) {
            throw new BookingException(
                    "Cannot book",
                    HttpStatus.CONFLICT
            );
        }

        if (req.seats() > trip.getAvailableSeats()) {
            throw new BookingException(
                    "Not enough seats available",
                    HttpStatus.CONFLICT);
        }
        if (req.seats() <= 0) {
            throw new BookingException(
                    "Seat must be > 0",
                    HttpStatus.BAD_REQUEST
            );
        }
        trip.setAvailableSeats(trip.getAvailableSeats() - req.seats());
        tripRepo.save(trip);

        Booking booking = new Booking(passenger, trip, req.seats());
        return bookingRepo.save(booking);
    }
    @Transactional(readOnly = true)
    public Booking findById(Long id) {
        return bookingRepo.findById(id).orElse(null);
    }
    @Transactional(readOnly = true)
    public List<Booking> findByPassengerId(Long passengerId) {
        if (!userRepo.existsById(passengerId)) {
            throw new BookingException(
                    "Passenger not found",
                    HttpStatus.NOT_FOUND
            );
        }

        return bookingRepo.findByPassengerId(passengerId);
    }
    @Transactional(readOnly = true)
    public List<Booking> findByTripId(Long tripId) {
        if (!tripRepo.existsById(tripId)) {
            throw new BookingException(
                    "Passenger not found",
                    HttpStatus.NOT_FOUND
            );
        }

        return bookingRepo.findByTripId(tripId);
    }

    @Transactional
    public boolean cancel(Long id) {
        Booking booking = bookingRepo.findById(id).orElse(null);
        if (booking == null || booking.getStatus() == BookingStatus.CANCELLED) {
            return false;
        }
        Trip trip = tripRepo.findByIdForUpdate(booking.getTrip().getId())
                .orElseThrow(() -> new BookingException(
                        "Trip not found",
                        HttpStatus.NOT_FOUND
                ));

        trip.setAvailableSeats(trip.getAvailableSeats() + booking.getSeats());
        tripRepo.save(trip);

        booking.setStatus(BookingStatus.CANCELLED);
        return true;
    }
}
