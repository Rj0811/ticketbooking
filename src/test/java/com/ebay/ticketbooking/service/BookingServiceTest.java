package com.ebay.ticketbooking.service;

import com.ebay.ticketbooking.dto.BookingRequest;
import com.ebay.ticketbooking.dto.BookingResponse;
import com.ebay.ticketbooking.dto.FlightRequest;
import com.ebay.ticketbooking.dto.FlightResponse;
import com.ebay.ticketbooking.exception.BookingNotFoundException;
import com.ebay.ticketbooking.exception.FlightAlreadyExistsException;
import com.ebay.ticketbooking.exception.FlightFullException;
import com.ebay.ticketbooking.exception.FlightNotFoundException;
import com.ebay.ticketbooking.model.Flight;
import com.ebay.ticketbooking.repository.BookingRepository;
import com.ebay.ticketbooking.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BookingService.
 * Uses real in-memory repositories (no mocking needed since they're simple).
 */
class BookingServiceTest {

    private BookingService bookingService;
    private FlightRepository flightRepository;
    private BookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        flightRepository = new FlightRepository();
        bookingRepository = new BookingRepository();
        bookingService = new BookingService(flightRepository, bookingRepository);
    }

    // --- Flight creation tests ---

    @Test
    @DisplayName("Should create a flight successfully")
    void createFlight_success() {
        FlightRequest request = new FlightRequest(
                "AA101", "New York", "Los Angeles",
                LocalDateTime.of(2026, 7, 1, 8, 0), 150
        );

        FlightResponse response = bookingService.createFlight(request);

        assertNotNull(response);
        assertEquals("AA101", response.flightNumber());
        assertEquals("New York", response.origin());
        assertEquals("Los Angeles", response.destination());
        assertEquals(150, response.totalSeats());
        assertEquals(150, response.availableSeats());
    }

    @Test
    @DisplayName("Should reject duplicate flight number")
    void createFlight_duplicate_throwsConflict() {
        FlightRequest request = new FlightRequest(
                "AA101", "New York", "Los Angeles",
                LocalDateTime.of(2026, 7, 1, 8, 0), 150
        );
        bookingService.createFlight(request);

        assertThrows(FlightAlreadyExistsException.class, () ->
                bookingService.createFlight(request)
        );
    }

    // --- Booking tests ---

    @Test
    @DisplayName("Should book a seat successfully")
    void bookFlight_success() {
        seedFlight("AA101", 150);

        BookingResponse response = bookingService.bookFlight("AA101",
                new BookingRequest("John Doe"));

        assertNotNull(response);
        assertNotNull(response.bookingId());
        assertEquals("AA101", response.flightNumber());
        assertEquals("John Doe", response.passengerName());
        assertNotNull(response.bookedAt());
    }

    @Test
    @DisplayName("Should return 404 for non-existent flight")
    void bookFlight_flightNotFound() {
        assertThrows(FlightNotFoundException.class, () ->
                bookingService.bookFlight("UNKNOWN", new BookingRequest("John Doe"))
        );
    }

    @Test
    @DisplayName("Should reject booking when flight is full")
    void bookFlight_flightFull_throwsConflict() {
        seedFlight("AA101", 1);

        // Book the only seat
        bookingService.bookFlight("AA101", new BookingRequest("First Passenger"));

        // Try to book again - should fail
        assertThrows(FlightFullException.class, () ->
                bookingService.bookFlight("AA101", new BookingRequest("Second Passenger"))
        );
    }

    @Test
    @DisplayName("Should correctly track seat count across multiple bookings")
    void bookFlight_tracksSeatCount() {
        seedFlight("AA101", 5);

        for (int i = 1; i <= 5; i++) {
            bookingService.bookFlight("AA101", new BookingRequest("Passenger " + i));
        }

        Flight flight = flightRepository.findByFlightNumber("AA101").orElseThrow();
        assertEquals(5, flight.getBookedSeats());
        assertEquals(0, flight.getAvailableSeats());
    }

    // --- Overbooking prevention (concurrency) test ---

    @Test
    @DisplayName("Should prevent overbooking under concurrent requests")
    void bookFlight_concurrentRequests_noOverbooking() throws InterruptedException {
        int totalSeats = 10;
        int concurrentRequests = 50;
        seedFlight("AA101", totalSeats);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(concurrentRequests);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < concurrentRequests; i++) {
            final int passengerNum = i;
            executor.submit(() -> {
                try {
                    startGate.await(); // ensure all threads start together
                    bookingService.bookFlight("AA101",
                            new BookingRequest("Passenger " + passengerNum));
                    successCount.incrementAndGet();
                } catch (FlightFullException e) {
                    failureCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startGate.countDown(); // release all threads simultaneously
        doneLatch.await();
        executor.shutdown();

        assertEquals(totalSeats, successCount.get(),
                "Exactly " + totalSeats + " bookings should succeed");
        assertEquals(concurrentRequests - totalSeats, failureCount.get(),
                "Remaining requests should fail with FlightFullException");

        Flight flight = flightRepository.findByFlightNumber("AA101").orElseThrow();
        assertEquals(totalSeats, flight.getBookedSeats(),
                "Booked seats should equal total seats, no overbooking");
    }

    // --- Cancellation tests ---

    @Test
    @DisplayName("Should cancel a booking and free the seat")
    void cancelBooking_success() {
        seedFlight("AA101", 5);

        BookingResponse booking = bookingService.bookFlight("AA101",
                new BookingRequest("John Doe"));

        Flight flight = flightRepository.findByFlightNumber("AA101").orElseThrow();
        assertEquals(1, flight.getBookedSeats());

        bookingService.cancelBooking("AA101", booking.bookingId());

        assertEquals(0, flight.getBookedSeats());
        assertEquals(5, flight.getAvailableSeats());
    }

    @Test
    @DisplayName("Should return 404 when cancelling non-existent booking")
    void cancelBooking_bookingNotFound() {
        seedFlight("AA101", 5);

        assertThrows(BookingNotFoundException.class, () ->
                bookingService.cancelBooking("AA101", "non-existent-id")
        );
    }

    @Test
    @DisplayName("Should return 404 when cancelling with wrong flight number")
    void cancelBooking_wrongFlight() {
        seedFlight("AA101", 5);
        seedFlight("BA202", 5);

        BookingResponse booking = bookingService.bookFlight("AA101",
                new BookingRequest("John Doe"));

        // Try to cancel AA101's booking using BA202's flight number
        assertThrows(BookingNotFoundException.class, () ->
                bookingService.cancelBooking("BA202", booking.bookingId())
        );
    }

    @Test
    @DisplayName("Should allow rebooking after cancellation")
    void cancelBooking_thenRebook() {
        seedFlight("AA101", 1);

        // Book the only seat
        BookingResponse booking = bookingService.bookFlight("AA101",
                new BookingRequest("John Doe"));

        // Cancel
        bookingService.cancelBooking("AA101", booking.bookingId());

        // Rebook should succeed
        BookingResponse newBooking = bookingService.bookFlight("AA101",
                new BookingRequest("Jane Smith"));

        assertNotNull(newBooking);
        assertEquals("Jane Smith", newBooking.passengerName());
    }

    @Test
    @DisplayName("Should prevent double-cancel of same booking")
    void cancelBooking_doubleCancelThrows() {
        seedFlight("AA101", 5);

        BookingResponse booking = bookingService.bookFlight("AA101",
                new BookingRequest("John Doe"));

        bookingService.cancelBooking("AA101", booking.bookingId());

        // Second cancel should throw, not silently decrement seats
        assertThrows(BookingNotFoundException.class, () ->
                bookingService.cancelBooking("AA101", booking.bookingId())
        );

        // Verify seat count is correct (1 booked, 1 cancelled = 0)
        Flight flight = flightRepository.findByFlightNumber("AA101").orElseThrow();
        assertEquals(0, flight.getBookedSeats());
    }

    // --- Helper ---

    private void seedFlight(String flightNumber, int totalSeats) {
        FlightRequest request = new FlightRequest(
                flightNumber, "CityA", "CityB",
                LocalDateTime.of(2026, 7, 1, 8, 0), totalSeats
        );
        bookingService.createFlight(request);
    }
}
