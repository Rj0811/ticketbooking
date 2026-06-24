# Flight Ticket Booking API

A REST API for booking flight tickets. Built with **Spring Boot 3.4** and **Java 17**, using in-memory storage.

## How to Run

**Prerequisites:** Java 17+

```bash
# Clone
git clone https://github.com/Rj0811/ticketbooking.git
cd ticketbooking

# Run
./mvnw.cmd spring-boot:run        # Windows
./mvnw spring-boot:run             # macOS/Linux

# Run tests
./mvnw.cmd test
```

The API starts on `http://localhost:8080`.

> On Windows, if `JAVA_HOME` is not set: `$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"`

## Example Requests

### Create a flight

```bash
curl -X POST http://localhost:8080/api/flights \
  -H "Content-Type: application/json" \
  -d '{"flightNumber":"AA101","origin":"New York","destination":"Los Angeles","departureTime":"2026-07-01T08:00:00","totalSeats":150}'
```

→ `201 Created`

### Book a seat

```bash
curl -X POST http://localhost:8080/api/flights/AA101/bookings \
  -H "Content-Type: application/json" \
  -d '{"passengerName":"John Doe"}'
```

→ `201 Created`
```json
{
  "bookingId": "550e8400-e29b-41d4-a716-446655440000",
  "flightNumber": "AA101",
  "passengerName": "John Doe",
  "bookedAt": "2026-06-24T10:30:00"
}
```

### Cancel a booking

```bash
curl -X DELETE http://localhost:8080/api/flights/AA101/bookings/{bookingId}
```

→ `204 No Content`

### Error examples

Booking a full flight → `409 Conflict`  
Unknown flight number → `404 Not Found`  
Missing passenger name → `400 Bad Request`

> **Note:** Three sample flights (AA101, BA202, EK303) are pre-loaded on startup.

## What I Would Improve With More Time

- **Persistent storage** — Replace in-memory maps with a database (e.g. H2/PostgreSQL) so data survives restarts
- **Idempotency** — Add an idempotency key header on `POST /bookings` to prevent duplicate bookings on client retries
- **Input sanitization** — Enforce flight number format (regex), passenger name length limits, and XSS protection beyond Bean Validation
- **Horizontal scalability** — The current `synchronized(flight)` approach only works in a single JVM; would need optimistic locking or DB-level constraints for multiple instances
- **Observability** — Structured logging with correlation IDs, request/response logging, health check and metrics endpoints
- **Authorization on cancel** — Currently anyone with a booking ID can cancel; would add a booking token or auth layer
- **API polish** — Pagination for listing flights, HATEOAS links, OpenAPI/Swagger documentation
