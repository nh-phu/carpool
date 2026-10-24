package com.carpool.backend.trip;

import com.carpool.backend.Geocoding;
import com.carpool.backend.OrsRoutingService;
import com.carpool.backend.user.User;
import com.carpool.backend.user.UserRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TripService {
    private final UserRepo userRepo;
    private final TripRepo tripRepo;
    private final Geocoding geocoding;
    private final OrsRoutingService orsRoutingService;
    private final TripRouteRepo tripRouteRepo;

    public TripService(UserRepo userRepo,
                       TripRepo tripRepo,
                       Geocoding geocoding,
                       OrsRoutingService orsRoutingService,
                       TripRouteRepo tripRouteRepo
    ) {
        this.tripRepo = tripRepo;
        this.geocoding = geocoding;
        this.orsRoutingService = orsRoutingService;
        this.tripRouteRepo = tripRouteRepo;
        this.userRepo = userRepo;
    }

    public Trip create(CreateTripReq req) {
        User driver = userRepo.findById(req.driverId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Driver not found: " + req.driverId()
                ));
        var startCoord = geocoding.geocode(req.start());
        var destCoord = geocoding.geocode(req.dest());
        Trip trip = new Trip(
                driver,
                req.start().trim(),
                req.dest().trim(),
                req.departureTime(),
                req.availableSeats(),
                req.priceSeat()
        );
        trip.setStartLatitude(startCoord.latitude());
        trip.setStartLongitude(startCoord.longitude());
        trip.setDestLatitude(destCoord.latitude());
        trip.setDestLongitude(destCoord.longitude());

        Trip savedTrip = tripRepo.save(trip);

        try {
            OrsRoutingService.RouteData route = orsRoutingService.getRouteData(
                    savedTrip.getStartLongitude(),
                    savedTrip.getStartLatitude(),
                    savedTrip.getDestLongitude(),
                    savedTrip.getDestLatitude()
            );

            tripRouteRepo.saveRoute(
                    savedTrip.getId(),
                    route.geometry(),
                    route.distance(),
                    route.duration()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to save trip route", e);
        }
        return savedTrip;
    }

    public List<Trip> findAll() {
        return tripRepo.findAll().stream().sorted(Comparator.comparing(Trip::getId)).toList();
    }
    public Trip findById(Long id) {
        return tripRepo.findById(id).orElse(null);
    }
    public List<Trip> search(
            String start,
            String dest,
            LocalDateTime departureFrom,
            LocalDateTime departureUntil,
            Integer minSeats,
            Long maxPrice
            ) {
        return tripRepo.searchTrips(
            start, dest, departureFrom, departureUntil, minSeats, maxPrice);
    }
    public List<Trip> findNearbyTrips(
            String start, String dest,
            LocalDateTime departureFrom,
            LocalDateTime departureUntil,
            Integer minSeats,
            Long maxPrice
    ) {
        var startCoord = geocoding.geocode(start);
        var destCoord = geocoding.geocode(dest);
        double startLatitude = startCoord.latitude();
        double startLongitude= startCoord.longitude();
        double destLatitude = destCoord.latitude();
        double destLongitude = destCoord.longitude();
        List <Long> tripsId = tripRouteRepo.findNearbyTrips(
             startLatitude,
             startLongitude,
             destLatitude,
             destLongitude,
            5000
        );
        if (tripsId.isEmpty()) {
            return List.of();
        }
        List<Trip> matchingTrips = tripRepo.findMatchingTrips(
                tripsId,
                departureFrom, departureUntil,
                minSeats, maxPrice
        );
        Map<Long, Trip> tripsById = matchingTrips.stream()
                .collect(Collectors.toMap(Trip::getId, trip -> trip));

        return tripsId.stream()
                .map(tripsById::get)
                .filter(Objects::nonNull)
                .toList();
    }
    public java.util.Optional<TripRouteRepo.RouteDetails> getRouteDetails(
            Long tripId
    ) {
        if (tripRepo.findById(tripId).isEmpty()) {
            return java.util.Optional.empty();
        }

        return tripRouteRepo.findRouteByTripId(tripId);
    }

    public Trip update(Long id, CreateTripReq req) {
        Trip trip = findById(id);
        if (trip == null) {
            return null;
        }
        var startCoord = geocoding.geocode(req.start());
        var destCoord = geocoding.geocode(req.dest());

        trip.setStart(req.start());
        trip.setDest(req.dest());
        trip.setStartLatitude(startCoord.latitude());
        trip.setStartLongitude(startCoord.longitude());
        trip.setDestLatitude(destCoord.latitude());
        trip.setDestLongitude(destCoord.longitude());
        trip.setDepartureTime(req.departureTime());
        trip.setAvailableSeats(req.availableSeats());
        trip.setPriceSeat(req.priceSeat());

        return tripRepo.save(trip);
    }

    public boolean delete(Long id) {
        Trip trip = findById(id);
        if (trip == null) {
            return false;
        }
        tripRepo.delete(trip);
        return true;
    }
}
