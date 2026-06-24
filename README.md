# Flight Ticket Booking API

A lightweight REST API for flight ticket booking, built with **Spring Boot 3.3** and **Java 17**.

## Features

- **Book flights** — Reserve a seat on a known flight
- **Cancel bookings** — Free up a seat by cancelling a booking
- **Create flights** — Seed new flights via API
- **Overbooking prevention** — Thread-safe seat allocation ensures no flight is overbooked
- **In-memory storage** — No database required, uses `ConcurrentHashMap`

## Tech Stack

- Java 17
- Spring Boot 3.3.0
- Maven (wrapper included)
- JUnit 5 + MockMvc for testing

## Getting Started

### Prerequisites

- Java 17+

### Run the application

```bash
./mvnw spring-boot:run
```

The API will start on `http://localhost:8080`.

### Run tests

```bash
./mvnw test
```

## API Endpoints

### Create a Flight

```bash
POST /api/flights
Content-Type: application/json

{
  "flightNumber": "AA101",
  "origin": "New York",
  "destination": "Los Angeles",
  "departureTime": "2026-07-01T08:00:00",
  "totalSeats": 150
}
```

**Response:** `201 Created`

### Book a Seat

```bash
POST /api/flights/{flightNumber}/bookings
Content-Type: application/json

{
  "passengerName": "John Doe"
}
```

**Response:** `201 Created`
```json
{
  "bookingId": "550e8400-e29b-41d4-a716-446655440000",
  "flightNumber": "AA101",
  "passengerName": "John Doe",
  "bookedAt": "2026-06-24T10:30:00"
}
```

### Cancel a Booking

```bash
DELETE /api/flights/{flightNumber}/bookings/{bookingId}
```

**Response:** `204 No Content`

## Pre-loaded Sample Flights

The app starts with 3 sample flights:

| Flight | Route | Seats |
|--------|-------|-------|
| AA101 | New York → Los Angeles | 150 |
| BA202 | London → Paris | 200 |
| EK303 | Dubai → Singapore | 100 |

## Error Responses

All errors return a consistent JSON format:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Flight not found: XX999",
  "timestamp": "2026-06-24T10:30:00"
}
```

| Status | Condition |
|--------|-----------|
| `400` | Invalid request body (validation errors) |
| `404` | Flight or booking not found |
| `409` | Flight already exists / No seats available |

## Project Structure

```
src/main/java/com/ebay/ticketbooking/
├── TicketBookingApplication.java     # Application entry point
├── controller/
│   └── FlightBookingController.java  # REST endpoints
├── service/
│   └── BookingService.java           # Business logic
├── repository/
│   ├── FlightRepository.java         # In-memory flight store
│   └── BookingRepository.java        # In-memory booking store
├── model/
│   ├── Flight.java                   # Flight domain model
│   └── Booking.java                  # Booking domain model
├── dto/
│   ├── FlightRequest.java            # Flight creation request
│   ├── BookingRequest.java           # Booking request
│   ├── BookingResponse.java          # Booking confirmation
│   └── ErrorResponse.java           # Error response format
├── exception/
│   ├── FlightNotFoundException.java
│   ├── FlightFullException.java
│   ├── BookingNotFoundException.java
│   ├── FlightAlreadyExistsException.java
│   └── GlobalExceptionHandler.java   # Maps exceptions → HTTP status
└── config/
    └── DataSeeder.java               # Seeds sample flights on startup
```
