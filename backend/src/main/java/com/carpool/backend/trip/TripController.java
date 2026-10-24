package com.carpool.backend.trip;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {
    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping
    public List<TripRes> findAll() {
        return tripService.findAll()
                .stream()
                .map(TripRes::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripRes> findById(@PathVariable Long id) {
        Trip trip = this.tripService.findById(id);
        if (trip == null)
            return ResponseEntity.notFound().build();
        return  ResponseEntity.ok(TripRes.from(trip));
    }
    @GetMapping("/{id}/route")
    public ResponseEntity<TripRouteRepo.RouteDetails> getRoute(
            @PathVariable Long id
    ) {
        return tripService.getRouteDetails(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @GetMapping("/search")
    public List<TripRes> search(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String dest,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate date,
            @RequestParam(required = false) Integer minSeats,
            @RequestParam(required = false) Long maxPrice
            ) {
        LocalDateTime departureFrom =
                date == null ? null : date.atStartOfDay();

        LocalDateTime departureUntil =
                date == null ? null : date.plusDays(1).atStartOfDay();
        return this.tripService.search(start, dest, departureFrom, departureUntil, minSeats, maxPrice)
                .stream().map(TripRes::from).toList();
    }
    @GetMapping("/nearby")
    public List<TripRes> findNearbyTrips(
            @RequestParam String start,
            @RequestParam String dest,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer minSeats,
            @RequestParam(required = false) Long maxPrice
    ) {
        LocalDateTime departureFrom =
                date == null ? null : date.atStartOfDay();

        LocalDateTime departureUntil =
                date == null ? null : date.plusDays(1).atStartOfDay();
        return tripService.findNearbyTrips(start, dest,
                departureFrom, departureUntil,
                minSeats,
                maxPrice).stream().map(TripRes::from).toList();
    }
    @PutMapping("/{id}")
    public ResponseEntity<TripRes> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateTripReq req
    ) {
        Trip trip = tripService.update(id, req);
        if (trip == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(TripRes.from(trip));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean delete = tripService.delete(id);
        if (!delete) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }


    @PostMapping
    public ResponseEntity<TripRes> create(
            @Valid @RequestBody CreateTripReq req
    ) {
        Trip trip = tripService.create(req);

        return ResponseEntity
                .created(URI.create("/api/trips/" + trip.getId()))
                .body(TripRes.from(trip));
    }
}
