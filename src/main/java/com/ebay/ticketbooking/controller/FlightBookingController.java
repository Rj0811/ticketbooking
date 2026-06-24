package com.ebay.ticketbooking.controller;

import com.ebay.ticketbooking.dto.BookingRequest;
import com.ebay.ticketbooking.dto.BookingResponse;
import com.ebay.ticketbooking.dto.FlightRequest;
import com.ebay.ticketbooking.model.Flight;
import com.ebay.ticketbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing flight booking operations.
 *
 * Endpoints:
 *   POST   /api/flights                                  - Create/seed a flight
 *   POST   /api/flights/{flightNumber}/bookings           - Book a seat
 *   DELETE  /api/flights/{flightNumber}/bookings/{bookingId} - Cancel a booking
 */
@RestController
@RequestMapping("/api/flights")
public class FlightBookingController {

    private final BookingService bookingService;

    public FlightBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Create/seed a new flight.
     *
     * @param request flight details (flight number, origin, destination, departure time, total seats)
     * @return 201 Created with the flight details
     */
    @PostMapping
    public ResponseEntity<Flight> createFlight(@Valid @RequestBody FlightRequest request) {
        Flight flight = bookingService.createFlight(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(flight);
    }

    /**
     * Book a seat on a flight.
     *
     * @param flightNumber path variable identifying the flight
     * @param request      booking details (passenger name)
     * @return 201 Created with the booking confirmation
     */
    @PostMapping("/{flightNumber}/bookings")
    public ResponseEntity<BookingResponse> bookFlight(
            @PathVariable String flightNumber,
            @Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.bookFlight(flightNumber, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Cancel an existing booking.
     *
     * @param flightNumber path variable identifying the flight
     * @param bookingId    path variable identifying the booking
     * @return 204 No Content on success
     */
    @DeleteMapping("/{flightNumber}/bookings/{bookingId}")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable String flightNumber,
            @PathVariable String bookingId) {
        bookingService.cancelBooking(flightNumber, bookingId);
        return ResponseEntity.noContent().build();
    }
}
