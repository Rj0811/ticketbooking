package com.ebay.ticketbooking.repository;

import com.ebay.ticketbooking.model.Booking;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory repository for Booking entities.
 * Uses ConcurrentHashMap for thread-safe storage.
 */
@Repository
public class BookingRepository {

    private final ConcurrentHashMap<String, Booking> bookings = new ConcurrentHashMap<>();

    public Optional<Booking> findById(String bookingId) {
        return Optional.ofNullable(bookings.get(bookingId));
    }

    public Booking save(Booking booking) {
        bookings.put(booking.getBookingId(), booking);
        return booking;
    }

    /**
     * Atomically removes a booking and returns it, or returns null if
     * it was already removed (prevents double-cancel).
     */
    public Booking removeById(String bookingId) {
        return bookings.remove(bookingId);
    }
}
