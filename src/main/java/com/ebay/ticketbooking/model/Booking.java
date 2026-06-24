package com.ebay.ticketbooking.model;

import java.time.LocalDateTime;

/**
 * Represents a confirmed booking for a passenger on a specific flight.
 */
public class Booking {

    private final String bookingId;
    private final String flightNumber;
    private final String passengerName;
    private final LocalDateTime bookedAt;

    public Booking(String bookingId, String flightNumber, String passengerName,
                   LocalDateTime bookedAt) {
        this.bookingId = bookingId;
        this.flightNumber = flightNumber;
        this.passengerName = passengerName;
        this.bookedAt = bookedAt;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }
}
