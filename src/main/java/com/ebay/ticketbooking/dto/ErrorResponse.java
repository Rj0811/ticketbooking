package com.ebay.ticketbooking.dto;

import java.time.LocalDateTime;

/**
 * Standard error response DTO for consistent error formatting.
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        LocalDateTime timestamp
) {
    public ErrorResponse(int status, String error, String message) {
        this(status, error, message, LocalDateTime.now());
    }
}
