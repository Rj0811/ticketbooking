package com.ebay.ticketbooking.controller;

import com.ebay.ticketbooking.dto.BookingRequest;
import com.ebay.ticketbooking.dto.BookingResponse;
import com.ebay.ticketbooking.dto.FlightRequest;
import com.ebay.ticketbooking.dto.FlightResponse;
import com.ebay.ticketbooking.exception.*;
import com.ebay.ticketbooking.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for FlightBookingController using MockMvc.
 * Tests HTTP methods, status codes, and request/response serialization.
 */
@WebMvcTest(FlightBookingController.class)
class FlightBookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // --- POST /api/flights ---

    @Test
    @DisplayName("POST /api/flights - 201 Created")
    void createFlight_returns201() throws Exception {
        FlightRequest request = new FlightRequest(
                "AA101", "New York", "Los Angeles",
                LocalDateTime.of(2026, 7, 1, 8, 0), 150
        );

        FlightResponse response = new FlightResponse(
                "AA101", "New York", "Los Angeles",
                LocalDateTime.of(2026, 7, 1, 8, 0), 150, 150
        );

        when(bookingService.createFlight(any(FlightRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flightNumber").value("AA101"))
                .andExpect(jsonPath("$.totalSeats").value(150))
                .andExpect(jsonPath("$.availableSeats").value(150));
    }

    @Test
    @DisplayName("POST /api/flights - 409 Conflict for duplicate")
    void createFlight_duplicate_returns409() throws Exception {
        FlightRequest request = new FlightRequest(
                "AA101", "New York", "Los Angeles",
                LocalDateTime.of(2026, 7, 1, 8, 0), 150
        );

        when(bookingService.createFlight(any(FlightRequest.class)))
                .thenThrow(new FlightAlreadyExistsException("AA101"));

        mockMvc.perform(post("/api/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Flight already exists: AA101"));
    }

    @Test
    @DisplayName("POST /api/flights - 400 Bad Request for invalid input")
    void createFlight_invalid_returns400() throws Exception {
        // Missing required fields
        String invalidJson = "{\"totalSeats\": 0}";

        mockMvc.perform(post("/api/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    // --- POST /api/flights/{flightNumber}/bookings ---

    @Test
    @DisplayName("POST /api/flights/{flightNumber}/bookings - 201 Created")
    void bookFlight_returns201() throws Exception {
        BookingRequest request = new BookingRequest("John Doe");
        BookingResponse response = new BookingResponse(
                "uuid-123", "AA101", "John Doe", LocalDateTime.now()
        );

        when(bookingService.bookFlight(eq("AA101"), any(BookingRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/flights/AA101/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").value("uuid-123"))
                .andExpect(jsonPath("$.flightNumber").value("AA101"))
                .andExpect(jsonPath("$.passengerName").value("John Doe"));
    }

    @Test
    @DisplayName("POST /api/flights/{flightNumber}/bookings - 404 for non-existent flight")
    void bookFlight_flightNotFound_returns404() throws Exception {
        BookingRequest request = new BookingRequest("John Doe");

        when(bookingService.bookFlight(eq("UNKNOWN"), any(BookingRequest.class)))
                .thenThrow(new FlightNotFoundException("UNKNOWN"));

        mockMvc.perform(post("/api/flights/UNKNOWN/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Flight not found: UNKNOWN"));
    }

    @Test
    @DisplayName("POST /api/flights/{flightNumber}/bookings - 409 when flight is full")
    void bookFlight_flightFull_returns409() throws Exception {
        BookingRequest request = new BookingRequest("John Doe");

        when(bookingService.bookFlight(eq("AA101"), any(BookingRequest.class)))
                .thenThrow(new FlightFullException("AA101"));

        mockMvc.perform(post("/api/flights/AA101/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("No available seats on flight: AA101"));
    }

    @Test
    @DisplayName("POST /api/flights/{flightNumber}/bookings - 400 for blank passenger name")
    void bookFlight_invalidRequest_returns400() throws Exception {
        String invalidJson = "{\"passengerName\": \"\"}";

        mockMvc.perform(post("/api/flights/AA101/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    // --- DELETE /api/flights/{flightNumber}/bookings/{bookingId} ---

    @Test
    @DisplayName("DELETE /api/flights/{flightNumber}/bookings/{bookingId} - 204 No Content")
    void cancelBooking_returns204() throws Exception {
        doNothing().when(bookingService).cancelBooking("AA101", "uuid-123");

        mockMvc.perform(delete("/api/flights/AA101/bookings/uuid-123"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE - 404 for non-existent booking")
    void cancelBooking_notFound_returns404() throws Exception {
        doThrow(new BookingNotFoundException("uuid-999"))
                .when(bookingService).cancelBooking("AA101", "uuid-999");

        mockMvc.perform(delete("/api/flights/AA101/bookings/uuid-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Booking not found: uuid-999"));
    }
}
