
package com.carpool.backend.trip;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.carpool.backend.Geocoding;
import com.carpool.backend.OrsRoutingService;
import com.carpool.backend.user.User;
import com.carpool.backend.user.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TripServiceTest {

    private TripService tripService;
    private TripRepo tripRepo;
    private Geocoding geocoding;
    private OrsRoutingService orsRoutingService;
    private TripRouteRepo tripRouteRepo;
    private UserRepo userRepo;
    @BeforeEach
    void setUp() {
        tripRepo = mock(TripRepo.class);
        geocoding = mock(Geocoding.class);
        orsRoutingService = mock(OrsRoutingService.class);
        tripRouteRepo = mock(TripRouteRepo.class);
        userRepo = mock(UserRepo.class);
        tripService = new TripService(userRepo, tripRepo, geocoding, orsRoutingService, tripRouteRepo);
    }

    @Test
    void createTrip() throws Exception {
        CreateTripReq request = new CreateTripReq(
                1L,
                "Hanoi",
                "Hai Phong",
                LocalDateTime.now().plusDays(1),
                3,
                150000
        );

        when(tripRepo.save(any(Trip.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(geocoding.geocode("Hanoi"))
                .thenReturn(new Geocoding.Coordinates(21.0285, 105.8542));

        when(geocoding.geocode("Hai Phong"))
                .thenReturn(new Geocoding.Coordinates(20.8449, 106.6881));
        OrsRoutingService.RouteData route =
                new OrsRoutingService.RouteData(
                        "{\"type\":\"LineString\",\"coordinates\":[[105.8,21.0],[106.6,20.8]]}",
                        108944.9,
                        4647.8
                );

        when(orsRoutingService.getRouteData(
                anyDouble(),
                anyDouble(),
                anyDouble(),
                anyDouble()
        )).thenReturn(route);
        User driver = new User("Test Driver", "0123456789");

        when(userRepo.findById(1L))
                .thenReturn(Optional.of(driver));
        Trip createdTrip = tripService.create(request);

        assertNotNull(createdTrip);
        assertEquals("Hanoi", createdTrip.getStart());
        assertEquals("Hai Phong", createdTrip.getDest());
        assertEquals(3, createdTrip.getAvailableSeats());
        assertEquals(
                150000,
                createdTrip.getPriceSeat()
        );

        verify(tripRepo).save(any(Trip.class));
    }

    @Test
    void findTripById() {
        User driver = new User("Test Driver", "0123456789");

        Trip trip = new Trip(
                driver,
                "Hanoi",
                "Hai Phong",
                LocalDateTime.now().plusDays(1),
                3,
                150000
        );

        when(tripRepo.findById(1L))
                .thenReturn(Optional.of(trip));
        when(geocoding.geocode("Hanoi"))
                .thenReturn(new Geocoding.Coordinates(21.0285, 105.8542));

        when(geocoding.geocode("Hai Phong"))
                .thenReturn(new Geocoding.Coordinates(20.8449, 106.6881));

        Trip foundTrip = tripService.findById(1L);

        assertNotNull(foundTrip);
        assertEquals("Hanoi", foundTrip.getStart());
        assertEquals("Hai Phong", foundTrip.getDest());
    }

    @Test
    void findTripByIdWhenNotFound() {
        when(tripRepo.findById(999L))
                .thenReturn(Optional.empty());
        when(geocoding.geocode("Hanoi"))
                .thenReturn(new Geocoding.Coordinates(21.0285, 105.8542));

        when(geocoding.geocode("Hai Phong"))
                .thenReturn(new Geocoding.Coordinates(20.8449, 106.6881));

        Trip foundTrip = tripService.findById(999L);

        assertNull(foundTrip);
    }

    @Test
    void searchTripsByStart() {
        User driver = new User("Test Driver", "0123456789");
        Trip hanoiTrip = new Trip(
                driver,
                "Hanoi",
                "Hai Phong",
                LocalDateTime.now().plusDays(1),
                3,
                150000
        );

        Trip danangTrip = new Trip(
                driver,
                "Da Nang",
                "Hue",
                LocalDateTime.now().plusDays(2),
                2,
                100000
        );

        when(tripRepo.searchTrips("Hanoi", null, null, null, 0 , null))
                .thenReturn(List.of(hanoiTrip, danangTrip));
        when(geocoding.geocode("Hanoi"))
                .thenReturn(new Geocoding.Coordinates(21.0285, 105.8542));

        when(geocoding.geocode("Hai Phong"))
                .thenReturn(new Geocoding.Coordinates(20.8449, 106.6881));

        List<Trip> results = tripService.search("Hanoi", null, null, null, 0, null);

        assertEquals("Hanoi", results.get(0).getStart());
    }

    @Test
    void searchTripsByDestination() {
        User driver = new User("Test Driver", "0123456789");
        Trip hanoiTrip = new Trip(
                driver,
                "Hanoi",
                "Hai Phong",
                LocalDateTime.now().plusDays(1),
                3,
                150000
        );

        Trip danangTrip = new Trip(
                driver,
                "Da Nang",
                "Hue",
                LocalDateTime.now().plusDays(2),
                2,
                100000
        );

        when(tripRepo.searchTrips(null, "Hai Phong", null, null, 0, null))
                .thenReturn(List.of(hanoiTrip, danangTrip));
        when(geocoding.geocode("Hanoi"))
                .thenReturn(new Geocoding.Coordinates(21.0285, 105.8542));

        when(geocoding.geocode("Hai Phong"))
                .thenReturn(new Geocoding.Coordinates(20.8449, 106.6881));

        List<Trip> results = tripService.search(null, "Hai Phong", null, null, 0, null);

        assertEquals("Hai Phong", results.get(0).getDest());
        assertTrue(results.get(0).getAvailableSeats() > 0);
    }
    @Test
    void searchShouldFilterByDepartureDate() {
        LocalDateTime from = LocalDateTime.of(2026, 10, 30, 0, 0);
        LocalDateTime until = LocalDateTime.of(2026, 10, 31, 0, 0);

        when(tripRepo.searchTrips(
                "Hanoi", "Hai Phong", from, until, 0, null
        )).thenReturn(List.of());
        when(geocoding.geocode("Hanoi"))
                .thenReturn(new Geocoding.Coordinates(21.0285, 105.8542));

        when(geocoding.geocode("Hai Phong"))
                .thenReturn(new Geocoding.Coordinates(20.8449, 106.6881));

        List<Trip> result = tripService.search(
                "Hanoi", "Hai Phong", from, until, 0 , null
        );

        assertTrue(result.isEmpty());

        verify(tripRepo).searchTrips(
                "Hanoi", "Hai Phong", from, until, 0, null
        );
    }
}
