# ADR-000: Baseline technology versions

| Field | Value |
|-------|-------|
| Status | Accepted (provisional — exact versions pending verification) |
| Date | 2026-11-17 |
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

**Option 2: use the newest stable version of each component**, provisionally.

Exact versions will be recorded in the table below when the project is generated (Assignment 3) and verified for compatibility. If verification shows a problem (for example, the newest Java release is not LTS or is not supported by the chosen Spring Boot line), this ADR will be revised rather than silently worked around.

PostgreSQL runs in Docker Compose for local development; the project does not depend on a locally installed PostgreSQL server.

## Version record (fill in at project generation)

| Component | Version | Source checked | Compatible with the rest? |
|-----------|---------|----------------|---------------------------|
| JDK (vendor + version) | _TBD_ | | |
| Spring Boot | _TBD_ | | |
| Spring Security | Managed by Spring Boot BOM | — | — |
| Maven (via Wrapper) | _TBD_ | | |
| PostgreSQL (Docker image tag) | _TBD_ | | |
| Flyway | Managed by Spring Boot BOM | — | _TBD_ |
| Testcontainers | _TBD_ | | |

## Consequences

**Positive**

- The project is built on the current generation of the stack.
- Longest available support window before the first planned upgrade.

**Negative / risks**

- Blueprint examples written for Spring Boot 3.x may not apply unchanged. Mitigation: official documentation is the primary source; check the version every tutorial targets before following it.
- The newest Java release may not be an LTS release, which would mean a short support window. Mitigation: check during verification; revise this ADR if needed.
- A brand-new PostgreSQL major version may not yet be supported by Flyway, the JDBC driver or Testcontainers. Mitigation: verify before fixing the Docker image tag.

## Review triggers

- Project generation in Assignment 3 (fill in the version record and verify compatibility).
- Any component in the table approaching end of support.
- Any compatibility problem discovered during development.