package com.ebay.ticketbooking.dto;

import java.time.LocalDateTime;

/**
 * Response DTO returned after a successful booking.
 */
public record BookingResponse(
        String bookingId,
        String flightNumber,
        String passengerName,
        LocalDateTime bookedAt
) {
}
