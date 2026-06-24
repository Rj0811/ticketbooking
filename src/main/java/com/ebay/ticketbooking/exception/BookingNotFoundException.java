package com.ebay.ticketbooking.exception;

/**
 * Thrown when a booking with the given ID does not exist.
 */
public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(String bookingId) {
        super("Booking not found: " + bookingId);
    }
}
