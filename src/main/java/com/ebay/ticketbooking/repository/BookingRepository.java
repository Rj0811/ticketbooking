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

    public void deleteById(String bookingId) {
        bookings.remove(bookingId);
    }
}
