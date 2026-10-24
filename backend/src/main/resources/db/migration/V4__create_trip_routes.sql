CREATE TABLE IF NOT EXISTS trip_routes (
    trip_id BIGINT PRIMARY KEY
        REFERENCES trips(id) ON DELETE CASCADE,
    route_geometry geography(LineString, 4326) NOT NULL,
    distance DOUBLE PRECISION NOT NULL,
    duration DOUBLE PRECISION NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_trip_routes_route_geometry
    ON trip_routes USING GIST (route_geometry);