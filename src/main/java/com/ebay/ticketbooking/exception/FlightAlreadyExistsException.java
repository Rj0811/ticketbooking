package com.ebay.ticketbooking.exception;

/**
 * Thrown when trying to create a flight that already exists.
 */
public class FlightAlreadyExistsException extends RuntimeException {

    public FlightAlreadyExistsException(String flightNumber) {
        super("Flight already exists: " + flightNumber);
    }
}
