package com.carpool.backend.trip;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TripRepo extends JpaRepository<Trip, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Trip t WHERE t.id = :id")
    Optional<Trip> findByIdForUpdate(@Param("id") Long id);
    @Query("""
    SELECT t FROM Trip t
    WHERE (:start IS NULL OR LOWER(t.start) = LOWER(:start))
      AND (:dest IS NULL OR LOWER(t.dest) = LOWER(:dest))
      AND (CAST(:departureFrom AS timestamp) IS NULL OR t.departureTime >= :departureFrom)
      AND (CAST(:departureUntil AS timestamp) IS NULL OR t.departureTime < :departureUntil)
      AND (:minSeats IS NULL OR t.availableSeats >= :minSeats)
      AND (:maxPrice IS NULL OR t.priceSeat <= :maxPrice)
    ORDER BY t.departureTime ASC
    """)
    List<Trip> searchTrips(
            @Param("start") String start,
            @Param("dest") String dest,
            @Param("departureFrom") LocalDateTime departureFrom,
            @Param("departureUntil") LocalDateTime departureUntil,
            @Param("minSeats") Integer minSeats,
            @Param("maxPrice") Long maxPrice
    );

    @Query("""
    SELECT t FROM Trip t
    WHERE t.id IN :ids
      AND (CAST(:departureFrom AS timestamp) IS NULL
           OR t.departureTime >= :departureFrom)
      AND (CAST(:departureUntil AS timestamp) IS NULL
           OR t.departureTime < :departureUntil)
      AND (:minSeats IS NULL OR t.availableSeats >= :minSeats)
      AND (:maxPrice IS NULL OR t.priceSeat <= :maxPrice)
    """)
    List<Trip> findMatchingTrips(
            @Param("ids") List<Long> ids,
            @Param("departureFrom") LocalDateTime departureFrom,
            @Param("departureUntil") LocalDateTime departureUntil,
            @Param("minSeats") Integer minSeats,
            @Param("maxPrice") Long maxPrice
    );
}
