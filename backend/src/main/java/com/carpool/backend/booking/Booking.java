package com.carpool.backend.booking;

import com.carpool.backend.trip.Trip;
import com.carpool.backend.user.User;
import jakarta.persistence.*;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passengerId", nullable = false)
    private User passenger;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tripId", nullable = false)
    private Trip trip;
    @Column(nullable = false)
    private int seats;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.CONFIRMED;

    protected Booking() {}

    public Booking(User passenger, Trip trip, int seats) {
        this.passenger = passenger;
        this.trip = trip;
        this.seats = seats;
    }

    public Long getId() {
        return id;
    }
    public User getPassenger() {
        return passenger;
    }
    public Trip getTrip() {
        return trip;
    }
    public int getSeats() {
        return seats;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }
    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
