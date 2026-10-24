package com.carpool.backend.trip;

public record RouteMatch(
        Long tripId,
        double startDistance,
        double destDistance,
        double startPosition,
        double destPosition
) {
    public double score() {
        return startDistance + destDistance;
    }
}
