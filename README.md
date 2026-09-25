# cards-service-rest

A Spring Boot REST API for a credit-card customer support agent: look up customers, card programs and card
summaries, and close cards.

The main purpose of this project is to **measure how much enabling virtual threads improves throughput** for a
typical blocking, database-backed Spring MVC application.

Please read the article here: https://medium.com/@snbaravani/virtual-threads-measuring-the-spring-boot-performance-leap-d5c6e304b177

## Virtual threads in two lines

Virtual threads are lightweight threads managed by the JVM rather than the OS, so an application can run
millions of them and give every request its own thread. When a virtual thread blocks on I/O (a DB call, an
HTTP call, `Thread.sleep`), the JVM unmounts it from its carrier thread, so the carrier can serve other work.

Official docs: [JEP 444: Virtual Threads](https://openjdk.org/jeps/444) ·
[Oracle Java documentation: Virtual Threads](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html)

## Tech stack

| Area          | Technology                                                     |
|---------------|----------------------------------------------------------------|
| Language      | Java 23                                                        |
| Framework     | Spring Boot 4.1.1 (Spring MVC on embedded Tomcat)              |
| Persistence   | Spring Data JPA / Hibernate, HikariCP connection pool          |
| Database      | MySQL (via `compose.yaml`), schema + seed data in `db/init/`   |
| API docs      | springdoc-openapi 3.1.1 (Swagger UI)                           |
| Utilities     | Lombok                                                         |
| Local dev     | Spring Boot Docker Compose support, DevTools                   |
| Testing       | JUnit 5, Spring Boot Test, Testcontainers (MySQL)              |
| Build         | Maven (wrapper included)                                       |

## API

Base URL: `http://localhost:8090`

| Method | Path                                    | Description                                              |
|--------|-----------------------------------------|----------------------------------------------------------|
| GET    | `/api/customers/{email}`                | Look up a customer by email                              |
| GET    | `/api/card-programs?name={name}`        | Look up card programs by name                            |
| GET    | `/api/customers/{email}/card-summary`   | Get a customer's card summary                            |
| POST   | `/api/customers/{email}/close-card`     | Close a card (only if no outstanding balance and no fraud flag) |

Errors are returned as RFC 9457 `ProblemDetail` responses (`404` not found, `400` invalid input, `500`
unexpected).

- Swagger UI: http://localhost:8090/swagger-ui.html
- OpenAPI spec: http://localhost:8090/v3/api-docs

Sample customer from the seed data: `john.doe@example.com`.

## Running it

Prerequisites: JDK 23+, Docker.

```bash
./mvnw spring-boot:run      # starts the app on port 8090 and auto-starts MySQL via compose.yaml
docker compose up -d        # or start MySQL on its own (seeded from db/init/*.sql on first run)
./mvnw test                 # run all tests (Testcontainers needs Docker)
./mvnw clean install        # full build
```

The database schema is owned by `db/init/*.sql`, not Hibernate (`spring.jpa.hibernate.ddl-auto=none`).

## Project structure

```
controller  → REST endpoints (CardController)
service     → business logic (CardService, @Transactional)
repository  → Spring Data JPA repositories
model       → JPA entities (Customer, CustomerCardSummary, CardProgram, Transaction)
dto         → record-based response types
exception   → GlobalExceptionHandler, ResourceNotFoundException
```

## Benchmarking virtual threads

### How the test is set up

`CardService.searchCustomer` calls `Thread.sleep(200)` to simulate a slow blocking call (e.g. a downstream
service or slow query). The relevant settings in `application.properties` are:

```properties
spring.threads.virtual.enabled=true            # toggle this between runs
server.tomcat.threads.max=20                   # small platform-thread pool, to make the bottleneck visible
spring.datasource.hikari.maximum-pool-size=200 # enough DB connections that the pool isn't the limit
```

- **Virtual threads off:** Tomcat serves requests on at most 20 platform threads. Each request holds its thread
  for ~200 ms while sleeping, so throughput tops out at about 20 / 0.2 s = **~100 requests/s**, and extra
  requests queue up.
- **Virtual threads on:** each request gets its own virtual thread and `server.tomcat.threads.max` no longer
  applies. Blocked requests don't hold an OS thread, so throughput should grow with concurrency until another
  limit, such as the Hikari connection pool or CPU, is reached.

### Running the benchmark

1. Start the app with virtual threads **disabled**:

   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.arguments=--spring.threads.virtual.enabled=false
   ```

2. Run a load test against the slow endpoint, e.g. with [`hey`](https://github.com/rakyll/hey)
   (`brew install hey`):

   ```bash
   hey -n 5000 -c 500 http://localhost:8090/api/customers/john.doe@example.com
   ```

   Or with ApacheBench: `ab -n 5000 -c 500 http://localhost:8090/api/customers/john.doe@example.com`

3. Record requests/sec and latency (average, p95, p99).
4. Restart the app with virtual threads **enabled** (`--spring.threads.virtual.enabled=true`, the default in
   `application.properties`) and repeat the same load test.
5. Compare the two runs. Increase concurrency (`-c`) to see where each setup levels off.

To make the comparison fair, warm up the JVM with a short run first and discard those results.

### Results

Fill in with your own measurements:

| Mode                 | Concurrency | Requests/sec | Avg latency | p95 latency | p99 latency |
|----------------------|-------------|--------------|-------------|-------------|-------------|
| Platform threads (20)| 500         |              |             |             |             |
| Virtual threads      | 500         |              |             |             |             |
