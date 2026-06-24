package com.ebay.ticketbooking.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for the flight creation endpoint.
 * Prevents leaking internal domain model (Flight) through the API.
 */
public record FlightResponse(
        String flightNumber,
        String origin,
        String destination,
        LocalDateTime departureTime,
        int totalSeats,
        int availableSeats
) {
}
