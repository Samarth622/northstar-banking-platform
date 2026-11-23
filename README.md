# Northstar Banking Platform

Backend platform for **Northstar Retail Bank (NRB)**, a fictional retail bank, built as a learn-before-build Java and Spring Boot capstone project.


## What this project is

Northstar simulates the core customer and operational journeys of a retail bank. The goal is not a large number of endpoints but getting the hard parts right:

- secure authentication and authorization, including server-side resource ownership checks;
- customer onboarding with a simulated KYC workflow;
- savings account lifecycle;
- a double-entry, append-only ledger as the financial source of truth;
- idempotent, concurrency-safe internal transfers;
- maker-checker staff operations and a complete audit trail;
- automated tests at every level, observability and repeatable local delivery.

The system starts as a **modular monolith**. Selected modules may be extracted into services later, but only where there is a justified reason.

## Project status

| Phase | Focus | Gate | Status |
|------:|-------|------|--------|
| 0 | Engineering workspace and Java refresh | G0 | In progress |
| 1 | Spring Security fundamentals | G1 | Not started |
| 2 | JUnit, Mockito and testing foundation | G2 | Not started |
| 3 | Customer identity and simulated onboarding | G3 | Not started |
| 4 | Product catalogue and account lifecycle | G4 | Not started |
| 5 | Double-entry ledger | G5 | Not started |
| 6 | Internal transfers and transactional correctness | G6 | Not started |
| 7 | Beneficiaries, limits and simulated external rails | G7 | Not started |
| 8 | Staff operations, maker-checker and audit | G8 | Not started |
| 9 | Events, notifications and risk controls | G9 | Not started |
| 10 | Quality, performance and security hardening | G10 | Not started |
| 11 | Delivery, observability and operations | G11 | Not started |
| 12 | Selective microservice extraction | G12 | Not started |

A phase is complete only when its exit gate passes, not when the code compiles.

## Technology

Exact versions are recorded in [ADR-000](docs/adr/ADR-000-baseline-versions.md), `pom.xml`, the Maven Wrapper and (later) `docker-compose.yml`. This README deliberately does not repeat version numbers, so it cannot drift out of date.

- Java, Spring Boot, Spring Security, Spring Data JPA / Hibernate, Bean Validation
- PostgreSQL (via Docker Compose) with Flyway migrations
- JUnit, Mockito, Spring Boot Test, Spring Security Test, Testcontainers
- Maven Wrapper, Docker Compose, OpenAPI

Redis, Kafka, Prometheus and Grafana are deliberately deferred until a concrete use case requires them.

## Prerequisites

- JDK (version per ADR-000)
- Git
- Docker Desktop (on Windows, using the WSL 2 backend)
- An API client such as Postman or Bruno

You do not need to install Maven or PostgreSQL locally: the project ships with the Maven Wrapper and runs PostgreSQL in Docker.

## Getting started

```powershell
# Clone the repository
git clone https://github.com/Samarth622/northstar-banking-platform.git
cd northstar-banking-platform

# Build and run all tests
.\mvnw.cmd clean verify

# Run the application (http://localhost:8080)
.\mvnw.cmd spring-boot:run
```

On macOS/Linux use `./mvnw` instead of `.\mvnw.cmd`. Stop the application with Ctrl+C.

There are no endpoints yet, so `http://localhost:8080` returns a 404 page; that confirms the server is running. PostgreSQL via Docker Compose and a health endpoint are added in later steps.

## Repository layout

```
northstar-banking-platform/
├── README.md
├── pom.xml             Maven build definition
├── mvnw, mvnw.cmd      Maven Wrapper scripts (Linux/macOS and Windows)
├── .mvn/wrapper/       Maven Wrapper configuration
├── review.cmd          On-demand local code review
├── .gitignore
├── .gitattributes
├── .github/            Copilot review agent, skill and local PowerShell script
├── src/
│   ├── main/
│   │   ├── java/com/northstar/bank/   Application code
│   │   └── resources/                 Configuration
│   └── test/
│       └── java/com/northstar/bank/   Tests
└── docs/
    ├── adr/            Architecture decision records
    ├── learning-log/   Concept notes, one file per assignment
    └── blueprint/      Project blueprint (design reference)
```

Database, Docker and further modules are added as each phase begins.

## Documentation

- [Architecture decision records](docs/adr)
- [Learning log](docs/learning-log)
- [Project blueprint](docs/blueprint)
- [On-demand local code review](docs/deep-code-review-setup.md)

## Development workflow

- **Trunk-based development:** `main` is always releasable.
- One short-lived branch per step, named by type and epic, e.g. `feature/e00-first-config-value` or `docs/e00-skeleton-evidence`.
- Every change reaches `main` through a pull request reviewed against the blueprint's PR checklist (section 10.6). Nothing is pushed directly to `main`.
- Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/): `feat:`, `fix:`, `test:`, `docs:`, `refactor:`, `chore:`.
- Working principle for every feature: **Learn → Explain → Design → Implement → Test → Review → Document.**

## Non-negotiable guardrails

- Synthetic data only. Never real PAN, Aadhaar, account numbers, card details, OTPs or production secrets.
- Money is never represented as `double` or `float`.
- Posted ledger entries are never changed in place; corrections use reversals.
- Every transfer is idempotent and posts balanced ledger entries atomically.
- Customer ownership is checked server-side on every protected resource.
- Flyway owns the database schema; Hibernate only validates it.
- No secrets in the repository; secrets come from environment variables.

## Disclaimer

Northstar is a learning simulator. It is not a bank, payment institution, KYC provider or regulated financial service, and it does not connect to real bank accounts, payment networks or identity systems. Banking terminology is used for educational modelling only. No production banking, RBI, PCI DSS, ISO 27001 or other compliance claim is made.

## License

To be decided.