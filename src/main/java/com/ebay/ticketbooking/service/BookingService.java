package com.ebay.ticketbooking.service;

import com.ebay.ticketbooking.dto.BookingRequest;
import com.ebay.ticketbooking.dto.BookingResponse;
import com.ebay.ticketbooking.dto.FlightRequest;
import com.ebay.ticketbooking.dto.FlightResponse;
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
     * Uses atomic putIfAbsent to avoid the TOCTOU race that existed
     * in the original exists() + save() approach, where two concurrent
     * requests could both pass the existence check.
     *
     * @param request flight details
     * @return response DTO with the created flight's details
     * @throws FlightAlreadyExistsException if the flight number is already registered
     */
    public FlightResponse createFlight(FlightRequest request) {
        Flight flight = new Flight(
                request.flightNumber(),
                request.origin(),
                request.destination(),
                request.departureTime(),
                request.totalSeats()
        );

        if (!flightRepository.saveIfAbsent(flight)) {
            throw new FlightAlreadyExistsException(request.flightNumber());
        }

        return toFlightResponse(flight);
    }

    /**
     * Books a seat on a flight for the given passenger.
     *
     * The entire operation — availability check, seat increment, and booking
     * creation — is performed inside the synchronized block. The original code
     * only synchronized the seat increment, creating a window where the seat
     * count could be incremented but the booking not yet saved (inconsistent
     * state on failure), or where a concurrent cancel could interleave.
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

        Booking booking;
        synchronized (flight) {
            if (!flight.tryReserveSeat()) {
                throw new FlightFullException(flightNumber);
            }

            booking = new Booking(
                    UUID.randomUUID().toString(),
                    flightNumber,
                    request.passengerName(),
                    LocalDateTime.now()
            );
            bookingRepository.save(booking);
        }

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
     * Fixed from the original: the booking is now removed and the seat released
     * inside the same synchronized block, preventing a window where a seat is
     * freed but the booking still exists (which could allow double-cancel).
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

        if (!booking.getFlightNumber().equals(flightNumber)) {
            throw new BookingNotFoundException(bookingId);
        }

        synchronized (flight) {
            // Re-check that the booking still exists inside the lock to
            // prevent double-cancel: two concurrent DELETE calls could both
            // pass the findById check above, then both enter here.
            if (bookingRepository.removeById(bookingId) == null) {
                throw new BookingNotFoundException(bookingId);
            }
            flight.releaseSeat();
        }
    }

    private FlightResponse toFlightResponse(Flight flight) {
        return new FlightResponse(
                flight.getFlightNumber(),
                flight.getOrigin(),
                flight.getDestination(),
                flight.getDepartureTime(),
                flight.getTotalSeats(),
                flight.getAvailableSeats()
        );
    }
}
