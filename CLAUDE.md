# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

An MCP (Model Context Protocol) server for a credit-card customer support agent. It exposes `@McpTool`-annotated
methods (Spring AI's `spring-ai-starter-mcp-server-webmvc`) that let an LLM agent look up customers, card
programs, card summaries, and close cards. There are **no REST controllers** — despite the `webmvc` dependency,
the WebMVC stack only carries the streamable MCP transport. All agent-facing behavior lives in
`CardService` (`src/main/java/com/example/cardsdbccp/service/CardService.java`), which is intentionally the
single service class for the whole app.

## Commands

```bash
./mvnw spring-boot:run          # run the app (port 8090); auto-starts MySQL via compose.yaml (Docker Compose support)
./mvnw test                     # run all tests
./mvnw test -Dtest=CardServiceTest                       # run one test class
./mvnw test -Dtest=CardServiceTest#getCardSummaryByCustomerId_found_returnsSummary  # run one test method
./mvnw clean install            # full build (also runnable via the `mci` skill)
docker compose up -d            # start MySQL standalone (compose.yaml), seeded from db/init/*.sql on first run
```

Tests run against a real MySQL via Testcontainers (`spring-boot-testcontainers`), not H2/mocks — see
`src/test/java/com/example/cardsdbccp/TestcontainersConfiguration.java`. Docker must be running for
integration-style tests.

## Architecture

**Layers**: `model` (JPA entities) → `repository` (Spring Data JPA) → `service` (`CardService`, the only
service class, `@Transactional(readOnly = true)` at the class level) → `dto` (record-based response types
returned to the MCP tool caller).

**Entity relationships** (see `src/main/resources/requirement-ccp.txt` for the original spec this was built from):
- `Customer` 1:1 `CustomerCardSummary` (owning side is the summary, via `@JoinColumn(customer_id)`); `Customer`
  is the `mappedBy` parent and cascades all/orphan-removal, with `assignCardSummary()` as the sync helper.
- `Customer` 1:N `Transaction`, same cascade/orphan-removal/mappedBy pattern, synced via
  `addTransaction()`/`removeTransaction()`.
- `CustomerCardSummary` N:1 `CardProgram`, `FetchType.LAZY`.
- All entities use UUID primary keys (`GenerationType.UUID`, `@JdbcTypeCode(SqlTypes.CHAR)`), business-key/UUID
  `equals()`+`hashCode()` (never the raw DB identity), constructor-based validation via
  `util/AssertUtil.java`, and protected no-arg constructors for JPA.

**Schema ownership**: the DB schema and seed data are owned entirely by `db/init/*.sql` (run once by the MySQL
container against a fresh volume — see `compose.yaml`), not by Hibernate.
`spring.jpa.hibernate.ddl-auto=none` is deliberate so Hibernate never drops/recreates the seeded tables. If you
change an entity's mapping, update the matching `db/init/0N-*.sql` file too.

**MCP tools**: each public method on `CardService` annotated `@McpTool` (name + description) becomes a tool the
agent can call, with `@ToolParam` documenting each argument. Tool server identity/instructions are configured in
`src/main/resources/application.properties` under `spring.ai.mcp.server.*`.

**`closeCard`**: business rule is "closeable only if `fraudFlag` is false and `creditOutstanding` <= 0" —
otherwise it returns a message directing the customer to call support rather than throwing.

## Repo-specific notes

- Java 25, Spring Boot 4.1.1, Spring AI 2.0.0 (BOM-managed).
- `src/main/resources/requirement-ccp.txt` is the original codegen brief for this project — useful context if
  extending the domain model, but not always in sync with the current code (e.g. `CardService.searchCustomer`
  currently only supports lookup by email, though `CustomerRepository` still has last-name/mobile finder methods
  used by the requirement's original multi-criteria search).
- A `spring-boot-skill` (`.agents/skills/spring-boot-skill/`, source-linked via `skills-lock.json`) documents this
  team's Spring Boot 4.x conventions (package layout, JPA, service layer, testing, ArchUnit, Taskfile). Consult
  its `references/` docs for the conventions this codebase already follows before introducing new patterns.
