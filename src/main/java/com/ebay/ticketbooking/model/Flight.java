package com.ebay.ticketbooking.model;

import java.time.LocalDateTime;

/**
 * Represents a flight with a fixed seat capacity.
 *
 * Thread safety: all mutable state (bookedSeats) must be accessed
 * while holding the intrinsic lock on this Flight instance.
 * The field is a plain int — AtomicInteger was removed because every
 * access already requires the synchronized block for check-then-act
 * atomicity, so the atomic wrapper added no value and gave false
 * confidence that individual calls were safe without locking.
 */
public class Flight {

    private final String flightNumber;
    private final String origin;
    private final String destination;
    private final LocalDateTime departureTime;
    private final int totalSeats;
    private int bookedSeats;  // guarded by synchronized(this)

    public Flight(String flightNumber, String origin, String destination,
                  LocalDateTime departureTime, int totalSeats) {
        if (totalSeats < 1) {
            throw new IllegalArgumentException("totalSeats must be >= 1");
        }
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.totalSeats = totalSeats;
        this.bookedSeats = 0;
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
        return bookedSeats;
    }

    public int getAvailableSeats() {
        return totalSeats - bookedSeats;
    }

    /**
     * Attempts to reserve one seat. Returns true if successful,
     * false if the flight is already full.
     * Caller MUST hold synchronized(this).
     */
    public boolean tryReserveSeat() {
        if (bookedSeats >= totalSeats) {
            return false;
        }
        bookedSeats++;
        return true;
    }

    /**
     * Releases one previously reserved seat.
     * Caller MUST hold synchronized(this).
     *
     * @throws IllegalStateException if bookedSeats is already 0
     */
    public void releaseSeat() {
        if (bookedSeats <= 0) {
            throw new IllegalStateException(
                    "Cannot release seat: no booked seats on flight " + flightNumber);
        }
        bookedSeats--;
    }
}
