package com.ebay.ticketbooking.exception;

/**
 * Thrown when a flight with the given number does not exist.
 */
public class FlightNotFoundException extends RuntimeException {

    public FlightNotFoundException(String flightNumber) {
        super("Flight not found: " + flightNumber);
    }
}
