package com.carpool.backend.trip;
import com.carpool.backend.user.User;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "trips")
public class Trip{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @Column
    private String start;
    @Column
    private Double startLatitude;
    @Column
    private Double startLongitude;

    @Column
    private String dest;
    @Column
    private Double destLatitude;
    @Column
    private Double destLongitude;

    @Column
    private LocalDateTime departureTime;
    @Column
    private int availableSeats;
    @Column
    private long priceSeat;


    protected Trip() {
    }

    public Trip(
            User driver,
            String start,
            String dest,
            LocalDateTime departureTime,
            int availableSeats,
            long priceSeat
    ) {
        this.driver = driver;
        this.start = start;
        this.dest = dest;
        this.departureTime = departureTime;
        this.availableSeats = availableSeats;
        this.priceSeat = priceSeat;
    }

    public Long getId() {
        return id;
    }
    public User getDriver() { return driver; }
    public String getStart() {
        return start;
    }
    public Double getStartLatitude() {
        return startLatitude;
    }
    public Double getStartLongitude() {
        return startLongitude;
    }
    public String getDest() {
        return dest;
    }
    public Double getDestLatitude() {
        return destLatitude;
    }
    public Double getDestLongitude() {
        return destLongitude;
    }
    public LocalDateTime getDepartureTime() {
        return departureTime;
    }
    public int getAvailableSeats() {
        return availableSeats;
    }
    public long getPriceSeat() {
        return priceSeat;
    }

    public void setDriver(User driver) { this.driver = driver; }
    public void setStart(String start) {
        this.start = start;
    }
    public void setStartLatitude(Double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public void setStartLongitude(Double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public void setDestLatitude(Double destLatitude) {
        this.destLatitude = destLatitude;
    }

    public void setDestLongitude(Double destLongitude) {
        this.destLongitude = destLongitude;
    }
    public void setDest(String dest) {
        this.dest = dest;
    }
    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }
    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }
    public void setPriceSeat(long priceSeat) {
        this.priceSeat = priceSeat;
    }
}
