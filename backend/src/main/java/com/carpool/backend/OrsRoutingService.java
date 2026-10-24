package com.carpool.backend;

import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class OrsRoutingService {
    private final OrsConfig orsConfig;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OrsRoutingService(
            OrsConfig orsConfig,
            ObjectMapper objectMapper
    ) {
        this.orsConfig = orsConfig;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public JsonNode getDrivingRoute(
            double startLongitude,
            double startLatitude,
            double destLongitude,
            double destLatitude
    ) throws Exception {

        String body = objectMapper.writeValueAsString(
                java.util.Map.of(
                        "coordinates",
                    new double[][]{
                            {startLongitude, startLatitude},
                            {destLongitude, destLatitude}
                    }
                )
        );

        String url = orsConfig.getBaseUrl() + "/v2/directions/driving-car/geojson";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", orsConfig.getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            System.err.println("ORS status: " + response.statusCode());
            System.err.println("ORS response: " + response.body());
            System.err.println("Request body: " + body);
            System.err.println("Coordinates sent: [" + startLongitude + ", " + startLatitude + "] -> [" + destLongitude + ", " + destLatitude + "]");

            throw new RuntimeException("ORS request failed");
        }

        return objectMapper.readTree(response.body());
    }

    public record RouteData(
            String geometry,
            double distance,
            double duration
    ) {}
    public RouteData getRouteData(
            double startLongitude,
            double startLatitude,
            double destLongitude,
            double destLatitude
    ) throws Exception {
        JsonNode response = getDrivingRoute(
                startLongitude,
                startLatitude,
                destLongitude,
                destLatitude
        );

        JsonNode feature = response.path("features").path(0);
        JsonNode geometry = feature.path("geometry");
        JsonNode summary = feature.path("properties").path("summary");

        if (geometry.isMissingNode()
                || !geometry.path("type").asText().equals("LineString")
                || !geometry.has("coordinates")
                || summary.isMissingNode()) {
            throw new IllegalStateException("Invalid route response from ORS");
        }

        return new RouteData(
                geometry.toString(),
                summary.path("distance").asDouble(),
                summary.path("duration").asDouble()
        );
    }
}
