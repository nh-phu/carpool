CREATE TABLE trips (
    id BIGSERIAL PRIMARY KEY,
    start VARCHAR(255) NOT NULL,
    start_latitude DOUBLE PRECISION NOT NULL,
    start_longitude DOUBLE PRECISION NOT NULL,
    dest VARCHAR(255) NOT NULL,
    dest_latitude DOUBLE PRECISION NOT NULL,
    dest_longitude DOUBLE PRECISION NOT NULL,
    departure_time TIMESTAMP WITH TIME ZONE NOT NULL,
    available_seats INTEGER NOT NULL,
    price_seat BIGINT NOT NULL
);