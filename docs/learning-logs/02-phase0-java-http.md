# Learning Log 02 — Phase 0, Assignment 2: Java refresh for banking and HTTP basics
 
| Field | Value |
|-------|-------|
| Phase | 0 — Engineering workspace and Java refresh |
| Branch | `docs/phase0-java-refresh` |
| Started | _date_ |
| Completed | _date_ |
| Time spent | _hours_ |
 
## How to write entries
 
**Concept entries:** In my own words → Why it matters for Northstar → Open questions → Sources.
 
**Experiment entries:** always write your prediction BEFORE running the code.
 
| Step | What to write |
|------|---------------|
| What I ran | The jshell snippet |
| My prediction | What you expected, written before running |
| What happened | The actual output |
| Why | Your explanation of the result |
| Northstar lesson | What this means for the project |
 
### Example experiment entry (format only)
 
**What I ran:** compared `"Hello" == new String("Hello")` and `"Hello".equals(new String("Hello"))`.
 
**My prediction:** both `true`.
 
**What happened:** `==` gave `false`, `equals` gave `true`.
 
**Why:** `==` compares references (are these the same object?); `equals` compares content. `new String(...)` always creates a new object.
 
**Northstar lesson:** never use `==` to compare value objects such as strings, money or identifiers.
 
---
 
# Part A — BigDecimal and money
 
## A1. Why `double` can't represent 0.1 exactly, and why that matters for money
 
**In my own words:** `double` stores numbers in binary (IEEE 754, 64 bits). In binary, 0.1 is an endlessly repeating fraction, the same way 1/3 is 0.333… in decimal. The computer stores the nearest binary value it can, which is very slightly wrong. Each operation can add another tiny error, so results like `0.1 + 0.2` are not exactly `0.3`, and equality checks fail.
 
**Why it matters for Northstar:** Money must be exact to the paisa. The ledger's core invariant — total debits **equal** total credits — is an exact equality check. With `double`, a balanced journal could be rejected, an unbalanced one accepted, or reconciliation could show drift that no one can explain. "Tiny" errors also multiply across millions of transactions.
 
**Sources:** `BigDecimal` Javadoc; *Effective Java*, "Avoid float and double if exact answers are required".
 
## A2. Unscaled value, scale and precision
 
**In my own words:** A `BigDecimal` is stored as an integer (the **unscaled value**) plus a **scale** that says where the decimal point goes: value = unscaled × 10^(−scale). **Precision** is the number of digits in the unscaled value.
 
Example (verified): `12.50` → unscaled value `1250`, scale `2`, precision `4`.
 
Scale can be negative: `1E+3` has unscaled value `1` and scale `−3`.
 
**Why it matters for Northstar:** INR uses scale 2 (paise). The database column `NUMERIC(19,2)` means precision 19, scale 2. Money must enforce scale 2 so every amount looks and compares the same way.
 
## A3. `equals` vs `compareTo` — and which Money should use
 
**In my own words:** `equals` compares the value **and** the scale, so `2.0` and `2.00` are *not* equal. `compareTo` compares only the numeric value, so it returns `0` for them. The Javadoc calls this ordering "inconsistent with equals". Hash-based collections use `equals`/`hashCode`; sorted collections use `compareTo` — so the same two numbers behave differently in a `HashSet` and a `TreeSet` (see experiment A-3).
 
**My decision for Money equality, and why:** Money **normalizes the scale to 2 at construction** (`500` becomes `500.00`, `500.5` becomes `500.50`). After normalization, every valid Money has the same scale, so `BigDecimal.equals` and `compareTo` agree and the trap disappears. Money's `equals` compares currency + amount. For ordering checks ("is the balance at least the amount?") use `compareTo`, never `equals`.
 
## A4. Rounding modes and "banker's rounding"
 
**In my own words:**
 
| Mode | Rule |
|------|------|
| `UP` | Away from zero |
| `DOWN` | Toward zero (truncate) |
| `CEILING` | Toward +∞ |
| `FLOOR` | Toward −∞ |
| `HALF_UP` | Nearest; ties go away from zero ("school rounding") |
| `HALF_DOWN` | Nearest; ties go toward zero |
| `HALF_EVEN` | Nearest; ties go to the even neighbor — **banker's rounding** |
| `UNNECESSARY` | Asserts no rounding is needed; throws if it is |
 
**Why a bank might prefer `HALF_EVEN`:** `HALF_UP` always pushes ties upward in magnitude, so over millions of calculations (interest, fees) it creates a systematic bias. `HALF_EVEN` rounds ties up about half the time and down half the time, so the bias cancels out statistically.
 
**Northstar note:** The rounding mode is a business/policy decision and must be documented (an ADR when interest or fees are introduced). Initial transfers do **no rounding at all** — see A5.
 
## A5. Why reject excess precision instead of silently rounding
 
**In my own words:** If a customer submits ₹100.005 and the system silently moves ₹100.01 (or ₹100.00), the money moved is **not what the customer authorized**. That creates disputes, unexplained differences across many transactions, and an exploit: an attacker can repeatedly send amounts that round in their favor (a "salami" attack). Rejecting the input makes the contract explicit: the API accepts at most 2 decimal places, full stop.
 
**Where rounding is legitimate:** amounts the system *calculates* — interest, fees, splitting a bill, currency conversion — using a documented rounding mode, with any leftover paise handled explicitly rather than lost.
 
**Useful tool (verified):** `setScale(2, RoundingMode.UNNECESSARY)` turns `500` into `500.00` and `500.5` into `500.50`, but throws `ArithmeticException: Rounding necessary` for `500.555`. That is exactly "normalize, but never silently round".
 
## A6. `long` paise vs `BigDecimal` — trade-offs
 
| | `long` (paise) | `BigDecimal` |
|---|---|---|
| Pros | Fast; exact for whole paise; simple `BIGINT` column; no scale confusion; no object allocation | Exact decimal arithmetic; explicit scale; built-in rounding modes; maps naturally to `NUMERIC`; human-readable |
| Cons | Easy to mix rupees and paise (100× bugs); every currency's minor unit must be known (INR 2, JPY 0, some currencies 3); percentages/division need hand-written rounding; overflow must be checked (`Math.addExact`), though `long` is practically large enough | Slower and more memory; `equals`/scale traps; verbose immutable API |
 
**My conclusion:** Use `BigDecimal` inside a Money value object with scale enforced at 2, matching the blueprint (`NUMERIC(19,2)`). Both approaches are used in real banking systems. Because callers only see the Money type, the internal representation could change later without touching the rest of the code.
 
## Part A experiments
 
### Experiment A-1: 0.1 + 0.2 (double vs BigDecimal from strings)
 
**What I ran:** `0.1 + 0.2`, `(0.1 + 0.2) == 0.3`, and `new BigDecimal("0.1").add(new BigDecimal("0.2"))`.
**Common prediction:** `0.3`, `true`, `0.3`.
**What happened:** `0.30000000000000004`, `false`, `0.3`.
**Why:** binary floating-point can't represent 0.1 or 0.2 exactly; the errors add up. BigDecimal from strings is exact decimal.
**Northstar lesson:** never `double` for money. An exact-equality ledger check would fail.
 
### Experiment A-2: three ways to create a BigDecimal from 0.1
 
**What I ran:** `new BigDecimal(0.1)`, `new BigDecimal("0.1")`, `BigDecimal.valueOf(0.1)`.
**Common prediction:** all three print `0.1`.
**What happened:**
- `new BigDecimal(0.1)` → `0.1000000000000000055511151231257827021181583404541015625`
- `new BigDecimal("0.1")` → `0.1`
- `BigDecimal.valueOf(0.1)` → `0.1`
**Why:** the `double` constructor faithfully converts the *already wrong* binary value. `valueOf` goes through `Double.toString`, which produces the shortest decimal string, so it looks right — but the input was still a `double`.
**Northstar lesson:** create money from **strings** (or from validated JSON decimals), never from `double`. Money should not offer any factory that accepts a `double`.
 
### Experiment A-3: 2.0 vs 2.00 — `equals`, `compareTo`, `HashSet`, `TreeSet`
 
**What I ran:** compared `new BigDecimal("2.0")` and `new BigDecimal("2.00")`, then put both into a `HashSet` and a `TreeSet`.
**Common prediction:** equal everywhere; both sets have size 1.
**What happened:** `equals` → `false`; `compareTo` → `0`; `HashSet` size `2`; `TreeSet` size `1`. Hash codes were different (`621` vs `6202`).
**Why:** `HashSet` uses `equals`/`hashCode` (scale matters); `TreeSet` uses `compareTo` (scale ignored).
**Northstar lesson:** normalize Money's scale at construction so this can never surprise us — e.g., a set of "unique amounts" or a map keyed by amount.
 
### Experiment A-4: 100 ÷ 3 with and without a rounding mode
 
**What I ran:** `new BigDecimal("100").divide(new BigDecimal("3"))`, then `divide(new BigDecimal("3"), 2, RoundingMode.HALF_EVEN)`.
**Common prediction:** `33.33…` either way.
**What happened:** without a mode → `ArithmeticException: Non-terminating decimal expansion; no exact representable decimal result.` With scale 2 and `HALF_EVEN` → `33.33`.
**Why:** BigDecimal refuses to guess. Division that doesn't terminate needs an explicit scale and rounding rule.
**Northstar lesson:** any division (interest, splitting) must state its rounding policy — and ₹100 split three ways leaves ₹0.01 that must be allocated deliberately.
 
### Experiment A-5: rounding table (scale 2)
 
Verified results:
 
| Value | HALF_UP | HALF_EVEN | DOWN | FLOOR |
|------:|---------|-----------|------|-------|
| 2.345 | 2.35 | 2.34 | 2.34 | 2.34 |
| 2.355 | 2.36 | 2.36 | 2.35 | 2.35 |
| −2.345 | −2.35 | −2.34 | −2.34 | −2.35 |
 
**What surprised me:** `HALF_EVEN` rounds 2.345 *down* (to the even 4) but 2.355 *up* (to the even 6). `DOWN` and `FLOOR` agree for positive numbers but differ for negative ones: `DOWN` moves toward zero, `FLOOR` toward −∞. Negative amounts (reversals, debits shown with a sign) need deliberate thought.
 
---
 
# Part B — Immutability, records and value objects
 
## B1. Rules for a truly immutable class
 
**In my own words:**
1. **No setters or mutating methods** — operations return a new object (`money.plus(other)` returns new Money).
2. **All fields `private final`** — assigned once in the constructor.
3. **Class is `final`** (or only private constructors) — a subclass can't add mutable state or override behavior.
4. **Defensive copies** of mutable inputs and outputs (lists, arrays, `Date`) — or use immutable types.
5. **Validate in the constructor** — an invalid instance can never exist.
6. **Don't let `this` escape** during construction.
**Why it matters:** an immutable object can't be changed after it was validated, is safe to share between threads without locks, and is safe as a `HashMap` key.
 
## B2. Entity vs value object
 
| | Entities (identity matters) | Value objects (value matters) |
|---|---|---|
| Northstar example 1 | `CustomerProfile` | `Money` |
| Northstar example 2 | `BankAccount` | `AccountNumber` |
| Northstar example 3 | `Transfer` | `IdempotencyKey` (also `DateRange` for statements) |
 
**In my own words:** An **entity** has an identity that stays the same while its attributes change: a bank account is still the same account after it goes from ACTIVE to FROZEN. Entities are compared by ID. A **value object** is defined entirely by its attributes: two `Money(INR, 500.00)` are interchangeable. Value objects are immutable and compared by value; to "change" one, you replace it.
 
## B3. What records generate, and how to validate in a record
 
**In my own words:** A record generates: a `private final` field per component, a canonical constructor, accessor methods named after the components (`amount()`, not `getAmount()`), and `equals`, `hashCode` and `toString` based on all components. Records are implicitly `final` and can't declare extra instance fields, but they can have static members, instance methods and implement interfaces.
 
**Validation:** use a **compact canonical constructor** — written without a parameter list. You validate and normalize the parameters inside it; the fields are assigned automatically afterwards.
 
**Northstar warning:** the generated `toString` prints **every** component (verified: `MoneyRec[amount=2.0]`). A record holding a password, token or full account number would leak it into logs. Override `toString` for any record with sensitive data.
 
## B4. Is a record with a `List` component immutable?
 
**In my own words:** No — it is only **shallowly** immutable. The record's field can't be reassigned, but the list it points to can still be changed by whoever passed it in, or by anyone calling the accessor (verified in B-2). Fix: in the compact constructor, replace the parameter with `List.copyOf(...)`, which makes an independent unmodifiable copy (and rejects `null` elements). The elements themselves must also be immutable for the record to be fully immutable.
 
## B5. Primitive obsession — why `transfer(String, String, BigDecimal)` is risky
 
**In my own words:** Primitive obsession means using raw `String`/`BigDecimal`/`long` for domain concepts. Risks:
- **Swapped arguments compile fine** — pass the customer ID where the account ID belongs and the compiler can't help.
- **No guarantees** — any `String` is accepted; any `BigDecimal` — negative, 3 decimals, no currency.
- **Rules scattered** — every method must re-validate, and one will forget.
Value objects fix this: `AccountId` and `CustomerId` are different types (mix-ups don't compile); if you hold a `Money`, it is already valid; behavior lives with the data (`Money.plus` checks currency).
 
**Northstar extra:** in self-service operations, `customerId` should not be a parameter from the client at all — it is derived from the authenticated principal (blueprint 5.6).
 
## B6. The `equals` / `hashCode` contract
 
**In my own words:** If `a.equals(b)` then `a.hashCode() == b.hashCode()`. `equals` must be reflexive, symmetric, transitive and consistent, and `x.equals(null)` is `false`. Unequal objects may share a hash code (collisions are allowed, just slower).
 
**What breaks in a `HashMap` if it is violated:** the map finds the bucket by `hashCode`, then checks `equals` inside it. If two equal objects have different hash codes, they land in different buckets: `get` with an equal key returns `null`, and a `HashSet` holds "duplicates". If a key's fields change after insertion, its entry is stranded in the wrong bucket and effectively lost. This is another reason keys should be immutable.
 
## Part B experiments
 
### Experiment B-1: record with a `BigDecimal` component (2.0 vs 2.00)
 
**What I ran:** `record MoneyRec(BigDecimal amount)`, compared `new MoneyRec(new BigDecimal("2.0"))` with `new MoneyRec(new BigDecimal("2.00"))`.
**Common prediction:** `true` — they're the same amount.
**What happened:** `false`.
**Why:** a record's generated `equals` calls each component's `equals`, and `BigDecimal.equals` compares scale.
**Northstar lesson:** if Money is a record, its compact constructor **must** normalize the scale. Otherwise ₹2.0 ≠ ₹2.00.
 
### Experiment B-2: record with a `List` component, original list mutated
 
**What I ran:** `record Items(List<String> items)`; created it from an `ArrayList` containing `"a"`; added `"b"` to the original list; then added `"c"` through `items()`.
**Common prediction:** the record keeps `[a]`.
**What happened:** after mutating the source → `[a, b]`; after mutating through the accessor → `[a, b, c]`.
**Why:** the record stores a reference to the same list object; `final` only stops the reference being reassigned.
**Northstar lesson:** use `List.copyOf` in compact constructors (e.g., a journal transaction's list of entries must not change after it was validated as balanced).
 
## Part B design (paper only, no code): Money value object rules
 
Based on blueprint section 4.5.
 
- **Fields:** `currency` and `amount` (`BigDecimal`). Currency is restricted to INR in the first release (ADR-002); decide whether to use `java.util.Currency` or a project enum.
- **Construction rules:**
  - Null amount or null currency → rejected.
  - Unsupported currency → rejected.
  - Scale normalized to 2: `500` → `500.00`, `500.5` → `500.50`.
  - 3+ decimal places (e.g., `500.555`) → rejected, never rounded (`setScale(2, UNNECESSARY)` behavior, wrapped in a domain exception).
  - Created from `String` or `BigDecimal` only — **no factory accepting `double`**.
- **Equality behavior:** currency + amount. Because scale is normalized, `₹500.0` and `₹500.00` are the same Money, and `equals`/`hashCode`/`compareTo` agree.
- **Supported operations:** `plus`, `minus`, `negate`; `isZero`, `isPositive`, `isNegative`; comparisons such as `isGreaterThanOrEqualTo`. All return new objects. **No multiply/divide yet** — those need a rounding policy and come with interest/fees.
- **Currency mismatch behavior:** arithmetic or comparison between different currencies throws. Never convert silently.
- **Exceptions thrown, and when** (all unchecked):
  - invalid construction (null, unsupported currency, too many decimals) → e.g. `InvalidMoneyException`;
  - different currencies in an operation → e.g. `CurrencyMismatchException`.
- **Is a positive transfer amount the same type as a balance? Why / why not:** No. A balance can legitimately be zero (and arithmetic results can be negative), but a transfer amount must be strictly positive. Keep Money general, and add a separate type (e.g., `TransferAmount` wrapping Money and enforcing `> 0`) so that "a transfer of zero" can't even be constructed. Journal entry amounts are another place where the positive rule applies, because the side (DEBIT/CREDIT) is stored separately from the amount (blueprint 4.5).
---
 
# Part C — The time API
 
## C1. The five types
 
| Type | What it represents | Northstar use (or "never use for…") |
|------|--------------------|-------------------------------------|
| `Instant` | A single point on the UTC timeline; no zone | Storing when things happened: `createdAt`, `postedAt`, audit `occurredAt` |
| `LocalDateTime` | A date and wall-clock time with **no** zone or offset — not a moment | **Never** use for when something happened; it's ambiguous |
| `OffsetDateTime` | Date-time with a fixed UTC offset (e.g., `+05:30` or `Z`) | API boundary representation (ISO 8601); works well with JDBC `timestamptz` |
| `ZonedDateTime` | Date-time with a zone ID and its rules | Computing business-day boundaries in `Asia/Kolkata` (daily limits, statement periods) |
| `LocalDate` | A calendar date with no time or zone | Statement date ranges, business dates, product effective dates |
 
## C2. Why store UTC and convert at the edges
 
**In my own words:** UTC is one unambiguous reference for every server, database and user. It has no daylight-saving gaps or repeats, and timestamps sort and compare correctly. Convert to a user's zone only for display or for business rules that depend on a local day.
 
**What goes wrong with `LocalDateTime` for a transfer timestamp:** `2026-11-17T10:30` doesn't say *where*. If the app runs in a UTC container but a developer's laptop runs in IST (verified: the sandbox defaulted to `Etc/UTC`), the same stored value means two moments 5.5 hours apart. Audit trails, ordering and cut-off times become wrong, and the true moment can't be reconstructed.
 
## C3. Daily limit "resets at midnight" — whose midnight?
 
**In my own words:** It must be midnight in the **bank's business time zone** — `Asia/Kolkata` for Northstar — not UTC. UTC midnight is 05:30 in India, so a UTC-based limit would "reset" at 5:30 am for customers.
 
**Types I would use to compute "today's transfers" for an Indian customer:**
1. Get today's `LocalDate` in `ZoneId.of("Asia/Kolkata")` (from the injected `Clock`).
2. Start = that date's `atStartOfDay(zone)` converted to an `Instant`; end = the next day's start.
3. Query transfers with `createdAt >= start AND createdAt < end` (half-open interval).
Verified: for 18 Nov 2026 in India, the window is `2026-11-17T18:30:00Z` to `2026-11-18T18:30:00Z`.
 
**Decision to record later:** "calendar day" vs "rolling 24 hours" is a business choice; the blueprint mentions rolling limits, so document which one Northstar uses.
 
## C4. Why inject a `Clock`
 
**In my own words:** Calling `Instant.now()` hard-wires the real clock into the code, so time-based rules can't be tested reliably. Injecting a `Clock` makes time a dependency like any other. Tests pass a fixed clock (deterministic results) or an offset clock ("25 hours later") to test beneficiary cooling periods, hold expiry and midnight resets — no sleeping and no flaky tests that fail near midnight. Production uses `Clock.systemUTC()`. (Blueprint 7.7.)
 
## C5. Why `Asia/Kolkata` instead of a fixed +05:30 offset
 
**In my own words:** A zone ID carries the full history and future rules of a region, maintained in the time-zone database. A fixed offset is a snapshot. Verified: Java's rules for `Asia/Kolkata` give `+05:21:10` in 1900, `+06:30` in 1943 (wartime) and `+05:30` from 1946 — so a fixed offset would be wrong for historical dates. A zone ID also states intent ("Indian business day") instead of a magic number, keeps working if a government changes the rules, and lets the code support other zones later.
 
## Part C experiments
 
### Experiment C-1: printing the four "now" values
 
**What I ran:** `Instant.now()`, `LocalDateTime.now()`, `ZonedDateTime.now(ZoneId.of("Asia/Kolkata"))`, `OffsetDateTime.now(ZoneOffset.UTC)`.
**Common prediction:** all show the same time.
**What happened** (sandbox, default zone `Etc/UTC`):
- `Instant.now()` → `2026-11-17T12:59:56.213413797Z`
- `LocalDateTime.now()` → `2026-11-17T12:59:56.232735015` (no zone info at all)
- `ZonedDateTime` Kolkata → `2026-11-17T18:29:56.233681212+05:30[Asia/Kolkata]`
- `OffsetDateTime` UTC → `2026-11-17T12:59:56.235775858Z`
**Why:** `LocalDateTime.now()` uses the machine's default zone and then throws the zone away. On your Windows machine it will show IST; in the UTC sandbox it showed UTC. Same code, different result depending on the machine.
**Northstar lesson:** never depend on the server's default zone; store `Instant`, convert explicitly.
 
### Experiment C-2: 2026-11-17T20:00:00Z in India
 
**What I ran:** `Instant.parse("2026-11-17T20:00:00Z").atZone(ZoneId.of("Asia/Kolkata"))`.
**Common prediction:** still 17 November.
**What happened:** `2026-11-18T01:30+05:30[Asia/Kolkata]` — already **18 November** in India.
**Why:** IST is 5.5 hours ahead of UTC.
**Northstar lesson:** a transfer at this instant counts toward the customer's **18 November** limit. Using the UTC date would put it in the wrong day.
 
### Experiment C-3: fixed `Clock`
 
**What I ran:** `Clock.fixed(Instant.parse("2026-11-17T10:00:00Z"), ZoneOffset.UTC)`, called `Instant.now(clock)` twice, then used `Clock.offset(clock, Duration.ofHours(25))`.
**Common prediction:** the time advances between calls.
**What happened:** both calls → `2026-11-17T10:00:00Z`; the offset clock → `2026-11-18T11:00:00Z`.
**Why:** a fixed clock always returns the same instant; an offset clock shifts it.
**Northstar lesson:** test "beneficiary added 25 hours ago is past its 24-hour cooling period" instantly and deterministically.
 
---
 
# Part D — Enums, exceptions, validation boundaries and collections
 
## D1. Checked vs unchecked for business-rule violations
 
**In my own words:** Checked exceptions must be declared or caught (compiler-enforced); unchecked exceptions (`RuntimeException` subclasses) don't. Business-rule violations — insufficient funds, invalid state transition — should be **unchecked domain exceptions** (e.g., `InsufficientFundsException` extending a base `DomainException extends RuntimeException`). The caller usually can't fix the problem locally; the exception should travel up to one central handler (`@RestControllerAdvice`) that maps it to a Problem Details response. Checked exceptions would clutter every layer's signatures and work badly with lambdas and streams.
 
**Critical Spring detail:** by default, `@Transactional` **rolls back only on unchecked exceptions** (and `Error`). A checked exception thrown from a transactional method **commits** the transaction unless rollback rules are configured. For money movement, that difference is huge.
 
## D2. Why never swallow `Exception`, and never send exception messages to clients
 
**In my own words:** `catch (Exception e) {}` hides failures: work may be half-done, nothing is logged, and debugging becomes impossible. It also catches things you never meant to handle (`NullPointerException`, programming bugs). Worse, if the exception is swallowed inside a `@Transactional` method, the transaction **commits** as if everything succeeded. Catch only specific exceptions you can actually handle; otherwise let them propagate, or wrap them with context while keeping the original cause.
 
Exception messages and stack traces must not reach clients: they reveal SQL, table and class names, library versions, file paths — and sometimes data, such as another customer's account reference in a constraint-violation message. That helps attackers and leaks private data. Return a stable error code, a generic message and a `traceId`; log details server-side without secrets (blueprint 5.8, 6.9).
 
## D3. Validation boundaries
 
| Rule | API boundary, domain/application, or both? | Why |
|------|---------------------------------------------|-----|
| Amount has at most 2 decimal places | Both | Boundary gives a clear 400; Money enforces it anyway at construction |
| Amount is positive | Both | Domain invariant that must hold for every entry point (see below) |
| Caller owns the source account | Application | Needs server-side identity and the database; the client can't be trusted |
| Sufficient available funds | Application/domain, **inside the transaction** | Depends on current state; checking earlier creates a race (time-of-check vs time-of-use) |
| Currency is INR | Both | Boundary rejects bad input early; Money enforces the supported-currency policy |
| Idempotency key is present | API boundary | It's a transport header; the application layer then uses it to claim the request |
 
**Why "amount is positive" is checked in both layers:** defense in depth. The transfer use case can be called from places that skip HTTP validation — another module, a scheduled job, a future message consumer, a test. The domain must protect its own invariants no matter who calls it; the boundary check just gives users fast, friendly errors.
 
## D4. `List.of` vs `Collections.unmodifiableList` vs `new ArrayList`
 
Verified:
 
| | Can you add/remove? | Changes if the source list changes? | Allows null? |
|---|---|---|---|
| `List.of(...)` | No — `UnsupportedOperationException` | No source; independent | No — even `contains(null)` throws `NullPointerException` |
| `Collections.unmodifiableList(...)` | No — `UnsupportedOperationException` | **Yes** — it's a read-only *view* of the source | Yes, if the source has nulls |
| `new ArrayList<>(...)` | Yes | No — independent (shallow) copy | Yes |
 
**Also:** `List.copyOf(...)` = independent, unmodifiable copy with no nulls — usually the right choice for value objects.
 
## D5. When to use `Optional` — and when not to
 
**In my own words:** Use `Optional` as a **return type** when "no result" is a normal outcome, e.g. `findAccountForCustomer(accountId, customerId)` returns `Optional<BankAccount>`. It forces the caller to handle absence, typically with `orElseThrow(...)` mapping to `RESOURCE_NOT_AVAILABLE` (404).
 
Don't use it for: fields (not serializable; awkward with JPA), method parameters (use overloads instead), or collections (return an empty list, not `Optional<List>`). Never return `null` from a method declared to return `Optional`, and avoid calling `get()` without checking.
 
## Part D experiments
 
### Experiment D-1: `Enum.valueOf` with an unknown string and with wrong case
 
**What I ran:** `KycStatus.valueOf("UNKNOWN")`, `KycStatus.valueOf("verified")`, `KycStatus.valueOf(null)`.
**Common prediction:** wrong case works; unknown returns `null`.
**What happened:**
- `"UNKNOWN"` → `IllegalArgumentException: No enum constant ...KycStatus.UNKNOWN`
- `"verified"` → `IllegalArgumentException: No enum constant ...KycStatus.verified`
- `null` → `NullPointerException: Name is null`
**Why:** `valueOf` is an exact, case-sensitive match and never returns `null`.
**Northstar lesson:** client input converted with a bare `valueOf` becomes a 500 error if unhandled. Invalid enum input must map to a 400 validation error, and the API must document its case policy (accept only `VERIFIED`, or normalize deliberately).
 
### Experiment D-2: summing a `List<BigDecimal>` with streams
 
**What I ran:** `List.of(new BigDecimal("10.50"), new BigDecimal("0.25"), new BigDecimal("100")).stream().reduce(BigDecimal.ZERO, BigDecimal::add)`.
**Common prediction:** looking for a `sum()` method.
**What happened:** `110.75`.
**Why:** there is no `sum()` for object streams; `reduce` with an identity (`ZERO`) and an operation (`add`) folds the list. The result's scale is the largest scale among the inputs.
**Northstar lesson:** this is the shape of "total debits" and "total credits" in the journal balance check — and Money would do the same via `Money::plus`.
 
### Experiment D-3: adding to `List.of(...)`
 
**What I ran:** `List.of(1, 2).add(3)`.
**Common prediction:** `[1, 2, 3]`.
**What happened:** `UnsupportedOperationException`.
**Why:** `List.of` creates an unmodifiable list.
**Northstar lesson:** good for values that must not change after validation — but the error only appears at runtime, so tests should cover it.
 
## Part D design (paper only): KYC transition table
 
Based on blueprint section 4.3. ✓ = allowed.
 
| From ↓ / To → | NOT_STARTED | SUBMITTED | UNDER_REVIEW | VERIFIED | REJECTED | NEEDS_INFORMATION |
|---|---|---|---|---|---|---|
| **NOT_STARTED** | | ✓ | | | | |
| **SUBMITTED** | | | ✓ | | | |
| **UNDER_REVIEW** | | | | ✓ | ✓ | ✓ |
| **VERIFIED** | | | | | | |
| **REJECTED** | | | | | | |
| **NEEDS_INFORMATION** | | ✓ | | | | |
 
**Terminal states (no way out) and why:** `VERIFIED` and `REJECTED`. A decision is final and audited. If a rejected customer reapplies, that should be a **new application**, not a reversal of the old decision — so the audit trail stays truthful. Self-transitions (e.g., `SUBMITTED → SUBMITTED`) are not allowed.
 
**Where should this rule live — enum, policy class or service? My reasoning:** In the **domain**: the allowed-transition table lives in the enum (or a small state-machine object in the domain package), and the `KycCase` aggregate's methods (`submit()`, `startReview()`, `verify(...)`) use it. Then every code path enforces the rule, and it can be unit-tested without Spring. The application service only orchestrates: load the case, check the actor's permission, call the domain method, save, audit. Keep three questions separate:
- "Is this transition legal?" → domain.
- "Is this actor allowed to make it?" → authorization (application/security).
- "Which reason codes are valid?" → policy.
A separate policy class becomes worthwhile only when transitions depend on outside context (configurable rules).
 
---
 
# Part E — HTTP basics
 
## E1. Safe and idempotent methods
 
| Method | Safe? | Idempotent? |
|--------|-------|-------------|
| GET | Yes | Yes |
| POST | No | No |
| PUT | No | Yes |
| PATCH | No | Not guaranteed |
| DELETE | No | Yes |
 
**Definitions:** *Safe* = read-only; no intended change on the server. *Idempotent* = sending the same request many times has the same intended effect on the server as sending it once (the response may differ — a second DELETE can return 404).
 
**Why `POST /transfers` needs an `Idempotency-Key`:** POST isn't idempotent: each request creates a new transfer. If the network times out *after* the server committed but *before* the client received the response, the client can't know whether it worked. A retry would create a second transfer and debit twice. The key lets the server recognize the replay and return the original outcome (blueprint 4.8). (There is an IETF draft standardizing this header.)
 
## E2. 401 vs 403 — and why Northstar might return 404 for another customer's account
 
**In my own words:**
- **401 Unauthorized** (badly named — it really means *unauthenticated*): no valid credentials. The client should log in.
- **403 Forbidden**: the server knows who you are, but you're not allowed (a customer calling a staff endpoint).
- **404 instead of 403 for another customer's account:** a 403 confirms the account *exists*, which lets an attacker enumerate valid account IDs. A 404 says "not available to you" without revealing anything. RFC 9110 explicitly allows a server to answer 404 to hide a resource's existence. The blueprint says to pick one policy and apply it consistently — a good ADR candidate.
## E3. 400 vs 409 vs 422
 
| Status | When to use it | Blueprint example |
|--------|----------------|-------------------|
| 400 Bad Request | Malformed or invalid input: bad JSON, wrong format, missing field | `REQUEST_VALIDATION_FAILED` |
| 409 Conflict | Request conflicts with the resource's current state | `IDEMPOTENCY_KEY_CONFLICT`, `INVALID_STATE_TRANSITION` |
| 422 Unprocessable Content | Well-formed and valid, but can't be processed for a semantic/business reason | `INSUFFICIENT_AVAILABLE_FUNDS` (blueprint allows 422 or a documented 409) |
 
## E4. Cookie attributes
 
| Attribute | What it does | Attack it helps prevent |
|-----------|--------------|-------------------------|
| `HttpOnly` | JavaScript can't read the cookie (`document.cookie`) | Session theft via XSS |
| `Secure` | Cookie is sent only over HTTPS | Interception on the network (e.g., public Wi-Fi, man-in-the-middle) |
| `SameSite` (`Strict` / `Lax` / `None`) | Controls whether the cookie is sent on cross-site requests | CSRF — reduces it, but doesn't fully replace CSRF tokens (blueprint 5.4) |
 
`Lax` (the modern browser default when unspecified) sends the cookie on top-level navigations but not on cross-site POSTs or sub-requests; `Strict` never sends it cross-site; `None` always sends it and requires `Secure`.
 
## E5. `Content-Type`, `Accept`, `Location`
 
**In my own words:**
- **`Content-Type`**: the format of the body being sent — `application/json` for requests, `application/problem+json` for Northstar errors.
- **`Accept`**: the formats the client is willing to receive (content negotiation). If the server can't satisfy it, it can respond `406 Not Acceptable`.
- **`Location`**: the URI of a newly created resource (with `201 Created`) or a redirect target.
**When `POST /transfers` would return `Location`:** when it creates a transfer: `201 Created` with `Location: /api/v1/transfers/{transferId}`. An idempotent replay returns the original outcome, including the same `Location`. Later, simulated external transfers may return `202 Accepted` with a `Location` pointing at a status resource.
 
## Part E experiments
 
> **Illustrative only** — these require internet access, which wasn't available when this reference was written. Run them yourself and replace this section with what you actually see.
 
### Experiment E-1: `curl.exe -i` against a public site
 
**Site used:** _(your choice)_
**Status line:** typically `HTTP/1.1 200 OK` or `HTTP/2 200`; a `301`/`302` means a redirect (look for `Location`).
**Headers to look for:** `Content-Type` (often `text/html; charset=UTF-8`), `Content-Length`, `Date`, `Cache-Control`, and security headers such as `Strict-Transport-Security` or `X-Content-Type-Options`.
**Cookies set (names and attributes only — never values):** many simple sites set none; sites with logins show `Set-Cookie: <name>=...; Path=/; Secure; HttpOnly; SameSite=Lax`.
 
### Experiment E-2: browser DevTools, `Set-Cookie` after login
 
**Attributes to look for** (never paste real cookie values): `Secure`, `HttpOnly`, `SameSite`, `Expires`/`Max-Age`, `Path`, `Domain`. A well-configured session cookie usually has `Secure` + `HttpOnly` + a `SameSite` value.
**What I learned:** _(write after running)_
 
---
 
## Open questions
 
- Should Money use `java.util.Currency` or a project-specific enum?
- Daily limits: calendar day in `Asia/Kolkata` or rolling 24 hours? (Needs a decision record.)
- 404 vs 403 for resources owned by someone else — which policy, documented where?
- How does JSON deserialization (Jackson) turn `"500.00"` (string) vs `500.00` (number) into `BigDecimal`, and can precision be lost? Revisit in Assignment 3 or Phase 6.
- Which rounding mode for future interest/fees — and who decides?
## Mistakes I made and what they taught me
 
_(Personal — only you can write this.)_
 
## Explain-back check
 
Tick only when you can explain it out loud without notes.
 
- [ ] Why money is never `double`
- [ ] `BigDecimal` scale, `equals` vs `compareTo`, rounding modes
- [ ] Immutability rules and value objects
- [ ] Records: what they generate, validation, the `List` trap
- [ ] `equals` / `hashCode` contract
- [ ] The five `java.time` types and UTC storage
- [ ] Why inject a `Clock`
- [ ] Checked vs unchecked exceptions; validation boundaries
- [ ] Safe vs idempotent HTTP methods
- [ ] 401 vs 403 vs 404; 400 vs 409 vs 422
- [ ] Cookie attributes