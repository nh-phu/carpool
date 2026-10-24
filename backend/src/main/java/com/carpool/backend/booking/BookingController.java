package com.carpool.backend.booking;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingRes> create(
            @Valid @RequestBody CreateBookingReq req) {
        Booking booking = bookingService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingRes.from(booking));
    }

    @GetMapping("/passenger/{passengerId}")
    public List<BookingRes> findByPassengerId(@PathVariable Long passengerId) {
        return bookingService.findByPassengerId(passengerId)
                .stream().map(BookingRes::from).toList();
    }
    @GetMapping("/trip/{tripId}")
    public List<BookingRes> findByTripId(@PathVariable Long tripId) {
        return bookingService.findByTripId(tripId)
                .stream().map(BookingRes::from).toList();
    }
    @GetMapping("/{id}")
    public ResponseEntity<BookingRes> findById(@PathVariable Long id) {
        Booking booking = bookingService.findById(id);
        if (booking == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(BookingRes.from(booking));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        boolean delete = bookingService.cancel(id);
        if (!delete) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
