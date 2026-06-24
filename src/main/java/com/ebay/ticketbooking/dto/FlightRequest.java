package com.ebay.ticketbooking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Request DTO for creating/seeding a new flight.
 */
public record FlightRequest(

        @NotBlank(message = "Flight number is required")
        String flightNumber,

        @NotBlank(message = "Origin is required")
        String origin,

        @NotBlank(message = "Destination is required")
        String destination,

        @NotNull(message = "Departure time is required")
        LocalDateTime departureTime,

        @Min(value = 1, message = "Total seats must be at least 1")
        int totalSeats
) {
}
