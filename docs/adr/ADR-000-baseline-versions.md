# ADR-000: Baseline technology versions

| Field | Value |
|-------|-------|
| Status | **Accepted** |
| Date proposed | 2026-11-17 |
| Date accepted | 2026-11-23 |
| Deciders | Samarth (project owner) |
| Supersedes | Version statements in the blueprint ("Baseline technology choices" and section 11.1) |

## Context

The project blueprint (dated 13 November 2026) specifies Java 21, the Spring Boot 3.5.x line and PostgreSQL 18.x. The blueprint itself says not to copy versions from the document and to re-check official release pages when initializing the repository. It also notes that a newer Spring Boot 4.x generation exists.

This is a long-running learning project (many months). The goal is to learn the technology generation that teams are adopting now, and to avoid starting on a line that may reach end of open-source support mid-project.

## Options considered

1. **Follow the blueprint versions exactly** (Java 21, Spring Boot 3.5.x, PostgreSQL 18.x).
   - Pro: matches the blueprint's examples; large volume of existing tutorials.
   - Con: may start on an older generation with a shorter remaining support window.
2. **Use the newest stable version of every component.**
   - Pro: current generation; longest support runway; skills match what employers are adopting.
   - Con: some blueprint guidance and many online tutorials may target the older generation.
3. **Newest stable for everything except Java, where only an LTS release is used.**
   - Pro: avoids short-lived non-LTS Java releases.
   - Con: requires checking which Java releases are LTS and which the chosen Spring Boot line supports.

## Decision

**Use the newest stable Spring Boot release with Java 21 (LTS).**

Spring Boot follows option 2 (newest GA release at project generation). Java stays on 21, an LTS release supported by Spring Boot 4.1.2. Moving to Java 25 (the newest LTS) is a candidate future change — see "Review triggers".

PostgreSQL runs in Docker Compose for local development; the project does not depend on a locally installed PostgreSQL server.

## Version record

| Component | Version | Source checked | Compatible with the rest? |
|-----------|---------|----------------|---------------------------|
| JDK | 21 — vendor: _(fill in, from `java -version`)_ | `java -version`; startup log line "Starting … using Java 21" | ✅ Offered by Spring Initializr for Spring Boot 4.1.2; build and context-load test pass |
| Spring Boot | 4.1.2 | `pom.xml` (`spring-boot-starter-parent`); start.spring.io | ✅ GA release |
| Spring Security | Managed by Spring Boot BOM (not yet added) | — | Added in Phase 1 |
| Maven (via Wrapper) | _(fill in, from `.\mvnw.cmd -v`)_ | `.mvn/wrapper/maven-wrapper.properties` | ✅ Builds the project successfully |
| PostgreSQL (Docker image tag) | To be decided | — | Recorded when Docker Compose is added (Assignment 4) |
| Flyway | Managed by Spring Boot BOM (not yet added) | — | Checked against the PostgreSQL version in Assignment 4 |
| Testcontainers | To be decided | — | Recorded when added (Phase 2) |

**Compatibility evidence (2026-11-23):** `.\mvnw.cmd clean verify` → `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`, on Java 21 with Spring Boot 4.1.2.

## Consequences

**Positive**

- The project is built on the current Spring Boot generation.
- Java 21 is an LTS release with a long support window.

**Negative / risks**

- Blueprint examples written for Spring Boot 3.x may not apply unchanged (for example, starter names and some APIs differ in 4.x). Mitigation: official documentation is the primary source; check the version every tutorial targets before following it.
- Java 21 is no longer the newest LTS. Mitigation: a deliberate review of Java 25 (see below).
- A brand-new PostgreSQL major version may not yet be supported by Flyway, the JDBC driver or Testcontainers. Mitigation: verify before fixing the Docker image tag.

## Review triggers

- **Java 25 upgrade:** to be evaluated deliberately (support timeline, Spring Boot support, tooling) and recorded here before any change. An automated upgrade tool briefly created an upgrade branch on 2026-11-23; it was deleted without changes, because version changes must go through this ADR.
- Adding PostgreSQL, Flyway or Testcontainers: fill in their rows.
- Any component in the table approaching end of support.
- Any compatibility problem discovered during development.

## Revision history

| Date | Change |
|------|--------|
| 2026-11-17 | Proposed: newest stable versions, provisionally. |
| 2026-11-23 | Accepted: Spring Boot 4.1.2 with Java 21; version record filled in; Java 25 noted as a review trigger. |