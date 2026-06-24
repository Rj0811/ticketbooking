package com.ebay.ticketbooking.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for booking a seat on a flight.
 */
public record BookingRequest(

        @NotBlank(message = "Passenger name is required")
        String passengerName
) {
}
