package com.ebay.ticketbooking.model;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a flight with a fixed seat capacity.
 * Uses AtomicInteger for bookedSeats to support thread-safe reads,
 * though actual booking logic uses synchronized blocks for atomicity.
 */
public class Flight {

    private final String flightNumber;
    private final String origin;
    private final String destination;
    private final LocalDateTime departureTime;
    private final int totalSeats;
    private final AtomicInteger bookedSeats;

    public Flight(String flightNumber, String origin, String destination,
                  LocalDateTime departureTime, int totalSeats) {
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.totalSeats = totalSeats;
        this.bookedSeats = new AtomicInteger(0);
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public int getBookedSeats() {
        return bookedSeats.get();
    }

    public int getAvailableSeats() {
        return totalSeats - bookedSeats.get();
    }

    public void incrementBookedSeats() {
        bookedSeats.incrementAndGet();
    }

    public void decrementBookedSeats() {
        bookedSeats.decrementAndGet();
    }
}
