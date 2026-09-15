# cards-db-ccp
Cards MCP Server
------------------------

An MCP (Model Context Protocol) server that gives an LLM-based credit-card customer support agent tools to look
up customers, card programs, and card summaries, and to close a card. It exposes no REST API — the only
agent-facing surface is the set of `@McpTool` methods below, served over the streamable MCP transport.

## Tools

All tools live in `CardService` (`src/main/java/com/example/cardsdbccp/service/CardService.java`).

| Tool | Description |
|---|---|
| `search_customer` | Look up a customer by email and return their profile (name, email, mobile, DOB). |
| `search_card_program_by_name` | Fetch card program details (interest rate, credit limits, interest-free days, air loyalty plan) by program name. |
| `search_card_summary_by_customer_email` | Fetch a customer's card summary (program, status, fraud flag, expiry, outstanding credit) by email. |
| `close_customer_card` | Close a customer's card by email — allowed only when there's no outstanding credit and the card isn't fraud-flagged; otherwise returns a message pointing the customer to call support. |

## Features

- **MCP tool server** — Spring AI's MCP server framework (`spring-ai-starter-mcp-server-webmvc`) exposes typed,
  annotated tools an LLM agent can call directly; no hand-rolled REST layer.
- **Streamable HTTP transport** on port `8090` (`spring.ai.mcp.server.protocol=streamable`).
- **Business-rule enforcement** — card closure logic lives server-side (`fraudFlag == false` and
  `creditOutstanding <= 0`), so the agent can't close a card it shouldn't.
- **JPA domain model** — `Customer` 1:1 `CustomerCardSummary`, `Customer` 1:N `Transaction`,
  `CustomerCardSummary` N:1 `CardProgram`, all with UUID primary keys.
- **DB-owned schema** — schema and seed data live in `db/init/*.sql`, applied once against a fresh MySQL volume;
  Hibernate never manages DDL (`ddl-auto=none`).
- **Docker Compose dev support** — `./mvnw spring-boot:run` auto-starts a seeded MySQL instance via
  `compose.yaml`.
- **Testcontainers-backed tests** — tests run against a real MySQL container, not H2 or mocks.

## Tech stack

- **Java 25**
- **Spring Boot 4.1.1**
- **Spring AI 2.0.0** (`spring-ai-starter-mcp-server-webmvc`) — MCP server
- **Spring Data JPA** + **MySQL** (`mysql-connector-j`)
- **Spring Boot Docker Compose** support for local MySQL
- **Testcontainers** (MySQL) for integration tests
- **Maven** (wrapper included)

## Running it

```bash
./mvnw spring-boot:run          # runs the app on port 8090; auto-starts MySQL via compose.yaml
docker compose up -d            # or start MySQL standalone, seeded from db/init/*.sql on first run
./mvnw test                     # run all tests (requires Docker for Testcontainers)
./mvnw clean install            # full build
```

## Architecture

```
model (JPA entities) → repository (Spring Data JPA) → service (CardService) → dto (MCP response records)
```

- `model` — `Customer`, `CustomerCardSummary`, `CardProgram`, `Transaction`, `CardStatus`
- `repository` — Spring Data JPA repositories
- `service` — `CardService`, the single `@Transactional(readOnly = true)` service class holding all `@McpTool`
  methods
- `dto` — record-based response types returned to the MCP tool caller

See `CLAUDE.md` and `src/main/resources/requirement-ccp.txt` for further background on the domain model and
design decisions.
