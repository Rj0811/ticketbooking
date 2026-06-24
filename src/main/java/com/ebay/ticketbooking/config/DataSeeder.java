package com.ebay.ticketbooking.config;

import com.ebay.ticketbooking.model.Flight;
import com.ebay.ticketbooking.repository.FlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Seeds the in-memory store with sample flights on application startup.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final FlightRepository flightRepository;

    public DataSeeder(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Override
    public void run(String... args) {
        Flight aa101 = new Flight("AA101", "New York", "Los Angeles",
                LocalDateTime.of(2026, 7, 1, 8, 0), 150);
        Flight ba202 = new Flight("BA202", "London", "Paris",
                LocalDateTime.of(2026, 7, 1, 10, 30), 200);
        Flight ek303 = new Flight("EK303", "Dubai", "Singapore",
                LocalDateTime.of(2026, 7, 2, 14, 0), 100);

        flightRepository.save(aa101);
        flightRepository.save(ba202);
        flightRepository.save(ek303);

        log.info("Seeded {} sample flights: AA101, BA202, EK303", 3);
    }
}
