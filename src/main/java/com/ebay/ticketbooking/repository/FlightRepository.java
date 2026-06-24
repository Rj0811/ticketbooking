package com.ebay.ticketbooking.repository;

import com.ebay.ticketbooking.model.Flight;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory repository for Flight entities.
 * Uses ConcurrentHashMap for thread-safe storage.
 */
@Repository
public class FlightRepository {

    private final ConcurrentHashMap<String, Flight> flights = new ConcurrentHashMap<>();

    public Optional<Flight> findByFlightNumber(String flightNumber) {
        return Optional.ofNullable(flights.get(flightNumber));
    }

    public boolean exists(String flightNumber) {
        return flights.containsKey(flightNumber);
    }

    public Flight save(Flight flight) {
        flights.put(flight.getFlightNumber(), flight);
        return flight;
    }
}
