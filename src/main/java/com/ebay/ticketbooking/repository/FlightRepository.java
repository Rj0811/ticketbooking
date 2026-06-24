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

    /**
     * Atomically inserts a flight only if no flight with that number exists.
     *
     * @return true if inserted, false if the flight number was already present
     */
    public boolean saveIfAbsent(Flight flight) {
        return flights.putIfAbsent(flight.getFlightNumber(), flight) == null;
    }

    public Flight save(Flight flight) {
        flights.put(flight.getFlightNumber(), flight);
        return flight;
    }
}
