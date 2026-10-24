ALTER TABLE trips
ADD COLUMN driver_id BIGINT;

ALTER TABLE trips
ADD CONSTRAINT fk_trips_driver
FOREIGN KEY (driver_id)
REFERENCES users(id);