package com.ebay.ticketbooking.service;

import com.ebay.ticketbooking.dto.BookingRequest;
import com.ebay.ticketbooking.dto.BookingResponse;
import com.ebay.ticketbooking.dto.FlightRequest;
import com.ebay.ticketbooking.exception.BookingNotFoundException;
import com.ebay.ticketbooking.exception.FlightAlreadyExistsException;
import com.ebay.ticketbooking.exception.FlightFullException;
import com.ebay.ticketbooking.exception.FlightNotFoundException;
import com.ebay.ticketbooking.model.Booking;
import com.ebay.ticketbooking.model.Flight;
import com.ebay.ticketbooking.repository.BookingRepository;
import com.ebay.ticketbooking.repository.FlightRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service layer handling all booking business logic.
 * Ensures thread-safe seat allocation to prevent overbooking.
 */
@Service
public class BookingService {

    private final FlightRepository flightRepository;
    private final BookingRepository bookingRepository;

    public BookingService(FlightRepository flightRepository, BookingRepository bookingRepository) {
        this.flightRepository = flightRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Creates a new flight in the system.
     *
     * @param request flight details
     * @return the created Flight
     * @throws FlightAlreadyExistsException if the flight number is already registered
     */
    public Flight createFlight(FlightRequest request) {
        if (flightRepository.exists(request.flightNumber())) {
            throw new FlightAlreadyExistsException(request.flightNumber());
        }

        Flight flight = new Flight(
                request.flightNumber(),
                request.origin(),
                request.destination(),
                request.departureTime(),
                request.totalSeats()
        );

        return flightRepository.save(flight);
    }

    /**
     * Books a seat on a flight for the given passenger.
     * Synchronized on the flight object to prevent overbooking under concurrent requests.
     *
     * @param flightNumber the flight to book
     * @param request      booking details (passenger name)
     * @return booking confirmation
     * @throws FlightNotFoundException if the flight does not exist
     * @throws FlightFullException     if no seats are available
     */
    public BookingResponse bookFlight(String flightNumber, BookingRequest request) {
        Flight flight = flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new FlightNotFoundException(flightNumber));

        // Synchronized on the flight object to prevent race conditions
        // that could lead to overbooking
        synchronized (flight) {
            if (flight.getAvailableSeats() <= 0) {
                throw new FlightFullException(flightNumber);
            }
            flight.incrementBookedSeats();
        }

        Booking booking = new Booking(
                UUID.randomUUID().toString(),
                flightNumber,
                request.passengerName(),
                LocalDateTime.now()
        );

        bookingRepository.save(booking);

        return new BookingResponse(
                booking.getBookingId(),
                booking.getFlightNumber(),
                booking.getPassengerName(),
                booking.getBookedAt()
        );
    }

    /**
     * Cancels an existing booking and frees the seat.
     *
     * @param flightNumber the flight number
     * @param bookingId    the booking to cancel
     * @throws FlightNotFoundException  if the flight does not exist
     * @throws BookingNotFoundException if the booking does not exist or doesn't match the flight
     */
    public void cancelBooking(String flightNumber, String bookingId) {
        Flight flight = flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new FlightNotFoundException(flightNumber));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        // Verify the booking belongs to the specified flight
        if (!booking.getFlightNumber().equals(flightNumber)) {
            throw new BookingNotFoundException(bookingId);
        }

        bookingRepository.deleteById(bookingId);

        synchronized (flight) {
            flight.decrementBookedSeats();
        }
    }
}
