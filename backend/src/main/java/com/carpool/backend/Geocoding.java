package com.carpool.backend;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class Geocoding {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private long lastRequestNanos = 0;

    public Geocoding(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public synchronized Coordinates geocode(String address) {
        if (address == null || address.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Address cannot be empty"
            );
        }

        if (lastRequestNanos != 0) {
            long elapsed = System.nanoTime() - lastRequestNanos;
            long minimumInterval = 1_100_000_000L;

            if (elapsed < minimumInterval) {
                try {
                    long remaining = minimumInterval - elapsed;
                    long millis = remaining / 1_000_000L;
                    int nanos = (int) (remaining % 1_000_000L);
                    Thread.sleep(millis, nanos);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ResponseStatusException(
                            HttpStatus.SERVICE_UNAVAILABLE,
                            "Geocoding request interrupted"
                    );
                }
            }
        }

        lastRequestNanos = System.nanoTime();

        String encodedAddress =
                URLEncoder.encode(address.trim(), StandardCharsets.UTF_8);

        URI uri = URI.create(
                "https://nominatim.openstreetmap.org/search"
                        + "?format=jsonv2&limit=1&q=" + encodedAddress
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(15))
                .header(
                        "User-Agent",
                        "CarpoolBackend/0.1 (viettoan1708@gmail.com)"
                )
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Geocoding service returned HTTP "
                                + response.statusCode()
                );
            }

            JsonNode results = objectMapper.readTree(response.body());

            if (!results.isArray() || results.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Location not found: " + address
                );
            }

            JsonNode first = results.get(0);

            double latitude = first.path("lat").asDouble();
            double longitude = first.path("lon").asDouble();

            return new Coordinates(latitude, longitude);

        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not read geocoding response" + e.getClass().getSimpleName() + ": " + e.getMessage(),
                    e
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Geocoding request interrupted"
            );
        }
    }

    /// @param latitude
    /// @param longitude
    public record Coordinates(double latitude, double longitude) {}
}
