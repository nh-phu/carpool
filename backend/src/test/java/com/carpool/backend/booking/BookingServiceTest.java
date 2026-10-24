
package com.carpool.backend.booking;

import com.carpool.backend.exception.BookingException;
import com.carpool.backend.trip.Trip;
import com.carpool.backend.trip.TripRepo;
import com.carpool.backend.user.User;
import com.carpool.backend.user.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepo bookingRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private TripRepo tripRepo;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void createShouldSaveBookingAndReduceAvailableSeats() {
        User passenger = new User("Alice", "+84901234567");
        User driver = new User("Test Driver", "0123456789");

        Trip trip = new Trip(
                driver,
                "Hanoi",
                "Hai Phong",
                java.time.LocalDateTime.of(2026, 10, 30, 8, 0),
                3,
                150000
        );

        when(userRepo.findById(1L))
                .thenReturn(Optional.of(passenger));
        when(tripRepo.findByIdForUpdate(2L))
                .thenReturn(Optional.of(trip));
        when(bookingRepo.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Booking result = bookingService.create(
                new CreateBookingReq(1L, 2L, 2)
        );

        assertEquals(2, result.getSeats());
        assertEquals(1, trip.getAvailableSeats());

        verify(tripRepo).save(trip);
        verify(bookingRepo).save(any(Booking.class));
    }
    @Test
    void createShouldRejectBookingWhenNotEnoughSeatsRemain() {
        User passenger = new User("Alice", "+84901234567");
        User driver = new User("Test Driver", "0123456789");

        Trip trip = new Trip(
                driver,
                "Hanoi",
                "Hai Phong",
                java.time.LocalDateTime.of(2026, 10, 30, 8, 0),
                1,
                150000
        );

        when(userRepo.findById(1L))
                .thenReturn(Optional.of(passenger));
        when(tripRepo.findByIdForUpdate(2L))
                .thenReturn(Optional.of(trip));

        assertThrows(
                BookingException.class,
                () -> bookingService.create(new CreateBookingReq(1L, 2L, 2))
        );

        verify(bookingRepo, never()).save(any(Booking.class));
        verify(tripRepo, never()).save(any(Trip.class));
    }
    @Test
    void createShouldRejectTripThatHasAlreadyDeparted() {
        User passenger = new User("Alice", "0123456789");
        User driver = new User("Test Driver", "0123456789");

        Trip trip = new Trip(
                driver,
                "Hanoi",
                "Hai Phong",
                LocalDateTime.now().minusHours(1),
                3,
                150000
        );

        when(userRepo.findById(1L)).thenReturn(Optional.of(passenger));
        when(tripRepo.findByIdForUpdate(2L)).thenReturn(Optional.of(trip));

        CreateBookingReq req = new CreateBookingReq(1L, 2L, 1);

        assertThrows(BookingException.class, () -> bookingService.create(req));

        verify(bookingRepo, never()).save(any(Booking.class));
        verify(tripRepo, never()).save(any(Trip.class));
    }
}
