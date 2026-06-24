package com.ebay.ticketbooking.exception;

/**
 * Thrown when attempting to book a flight that has no available seats.
 */
public class FlightFullException extends RuntimeException {

    public FlightFullException(String flightNumber) {
        super("No available seats on flight: " + flightNumber);
    }
}
