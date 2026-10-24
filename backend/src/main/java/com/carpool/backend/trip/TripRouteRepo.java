package com.carpool.backend.trip;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

@Repository
public class TripRouteRepo {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    public TripRouteRepo(JdbcTemplate jdbcTemplate,
                         ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }
    public void saveRoute(
            Long tripId,
            String geoJson,
            double distance,
            double duration
    ) {
        String sql = """
            INSERT INTO trip_routes (
                trip_id,
                route_geometry,
                distance,
                duration,
                updated_at
            )
            VALUES (
                ?,
                ST_SetSRID(
                    ST_GeomFromGeoJSON(?),
                    4326
                )::geography,
                ?,
                ?,
                NOW()
            )
            ON CONFLICT (trip_id)
            DO UPDATE SET
                route_geometry = EXCLUDED.route_geometry,
                distance = EXCLUDED.distance,
                duration = EXCLUDED.duration,
                updated_at = NOW()
            """;

        jdbcTemplate.update(
                sql,
                tripId,
                geoJson,
                distance,
                duration
        );
    }
    public List<Long> findNearbyTrips(
            double startLatitude,
            double startLongitude,
            double destLatitude,
            double destLongitude,
            double radius
    ) {
        String sql = """
       SELECT t.trip_id
        FROM (
            SELECT
                trip_id,
                ST_Distance(
                    route_geometry,
                    ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography
                ) AS pickup_distance,
                ST_Distance(
                    route_geometry,
                    ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography
                ) AS dest_distance,
                ST_LineLocatePoint(
                    route_geometry::geometry,
                    ST_SetSRID(ST_MakePoint(?, ?), 4326)
                ) AS pickup_position,
                ST_LineLocatePoint(
                    route_geometry::geometry,
                    ST_SetSRID(ST_MakePoint(?, ?), 4326)
                ) AS dest_position
            FROM trip_routes
            WHERE ST_DWithin(
                    route_geometry,
                    ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography,
                    ?
                )
              AND ST_DWithin(
                    route_geometry,
                    ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography,
                    ?
                )
        ) t
        WHERE t.pickup_position < t.dest_position
        ORDER BY t.pickup_distance + t.dest_distance ASC
        """;

        return jdbcTemplate.queryForList(
                sql,
                Long.class,
                startLongitude, startLatitude,
                destLongitude, destLatitude,
                startLongitude, startLatitude,
                destLongitude, destLatitude,
                startLongitude, startLatitude, radius,
                destLongitude, destLatitude, radius
        );
    }
    public record RouteDetails(
            tools.jackson.databind.JsonNode geometry,
            double distance,
            double duration
    ) {}

    public Optional<RouteDetails> findRouteByTripId(Long tripId) {
        String sql = """
        SELECT
            ST_AsGeoJSON(route_geometry::geometry) AS geometry,
            distance,
            duration
        FROM trip_routes
        WHERE trip_id = ?
        """;

        List<RouteDetails> routes = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    return new RouteDetails(
                            objectMapper.readTree(rs.getString("geometry")),
                            rs.getDouble("distance"),
                            rs.getDouble("duration")
                    );
                },
                tripId
        );

        return routes.stream().findFirst();
    }
}
