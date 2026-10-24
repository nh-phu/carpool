
package com.carpool.backend.trip;

import com.carpool.backend.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TripController.class)
class TripControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TripService tripService;

    @Test
    void getTripsReturnsOk() throws Exception {
        when(tripService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getMissingTripReturnsNotFound() throws Exception {
        when(tripService.findById(999L)).thenReturn(null);

        mockMvc.perform(get("/api/trips/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTripReturnsCreated() throws Exception {
        when(tripService.create(any(CreateTripReq.class)))
                .thenReturn(new Trip(
                        new User("Test Driver", "0123457689"),
                        "Hanoi",
                        "Hai Phong",
                        java.time.LocalDateTime.parse("2026-10-30T08:00:00"),
                        3,
                        150000
                ));

        mockMvc.perform(post("/api/trips")
                        .contentType("application/json")
                        .content("""
                    {
                      "driverId": 1,
                      "start": "Hanoi",
                      "dest": "Hai Phong",
                      "departureTime": "2026-10-30T08:00:00",
                      "availableSeats": 3,
                      "priceSeat": 150000
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.start").value("Hanoi"))
                .andExpect(jsonPath("$.dest").value("Hai Phong"))
                .andExpect(jsonPath("$.availableSeats").value(3))
                .andExpect(jsonPath("$.priceSeat").value(150000));

    }

    @Test
    void createTripWithInvalidSeatsReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/trips")
                        .contentType("application/json")
                        .content("""
                    {
                      "driverId": 1,
                      "start": "Hanoi",
                      "dest": "Hai Phong",
                      "departureTime": "2026-10-30T08:00:00",
                      "availableSeats": 0,
                      "priceSeat": 150000
                    }
                    """))
                .andExpect(status().isBadRequest());
    }
}
