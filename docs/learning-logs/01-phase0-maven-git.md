# Learning Log 01 — Phase 0, Assignment 1: Environment, Maven and Git

| Field | Value |
|-------|-------|
| Phase | 0 — Engineering workspace and Java refresh |
| Started | _date_ |
| Completed | _date_ |
| Time spent | _hours_ |

## How to write an entry

For each concept, answer **in your own words**, without copying from docs or from Claude:

- **In my own words:** explain it as if teaching a friend.
- **Why it matters for Northstar:** connect it to this project.
- **What I tried:** commands you ran and what you actually observed.
- **Open questions:** anything you are still unsure about.
- **Sources:** the official docs you read.

If you cannot write "In my own words" without looking, you have not learned it yet.

---

## Example entry: Maven Wrapper

**In my own words:** The wrapper is a pair of small scripts (`mvnw` for Linux/macOS, `mvnw.cmd` for Windows) plus a properties file that names the exact Maven version this project uses. When anyone runs it, it downloads that Maven version if it is not already present. Nobody needs Maven installed, and everybody — me, CI and a reviewer — builds with the same version.

**Why it matters for Northstar:** Gate G0 says a clean clone must build with documented commands. Without the wrapper, different Maven versions on different machines can cause "works on my machine" failures.

**What it does NOT do:** It does not choose the JDK. Maven still uses `JAVA_HOME`.

**What I tried:** _(in Assignment 3, once the wrapper exists: run `.\mvnw.cmd -v` and record which Maven and Java versions it reports)_

**Open questions:** Where on my machine does the wrapper store the Maven version it downloads?

**Sources:** Apache Maven Wrapper documentation.

---

## 1. Maven lifecycle: `verify` vs `package` vs `install`

**In my own words:**

**Why it matters for Northstar:**

**What I tried:**

**Open questions:**

**Sources:**

---

## 2. Dependency scopes (compile, runtime, test, provided)

**In my own words** (one Northstar example per scope):

**Why it matters for Northstar:**

**Open questions:**

**Sources:**

---

## 3. `<dependencyManagement>`, BOMs and why I don't pin Spring Security

**In my own words:**

**Why it matters for Northstar:**

**Open questions:**

**Sources:**

---

## 4. Branching strategy: my choice and why

**Options I compared:**

**My choice:**

**Why (2–3 sentences):**

**Branch naming convention I will use:**

---

## 5. What never gets committed, and what to do if a secret leaks

**In my own words:**

**Steps if a secret is committed (in order, and why that order):**

**Sources:**

---

## 6. Line endings on Windows (`.gitattributes`)

**In my own words:**

**What I tried:** run `git ls-files --eol` and paste or describe the output. What do `i/lf` and `w/lf` mean?

**Open questions:**

---

## 7. `.gitignore` in practice

**What I tried:** create a dummy `.env` file and run `git check-ignore -v .env`. Which rule matched? Then check that `.env.example` is NOT ignored.

**What I learned:**

---

## 8. JDK setup: `JAVA_HOME`, vendors and multiple JDKs

**In my own words:**

**My current setup** (vendor, version, how IDE / terminal / Maven pick the JDK):

**Open questions:**

---

## 9. Docker PostgreSQL vs local PostgreSQL

**Why the blueprint prefers Docker:**

**How I will avoid the port 5432 clash:**

---

## Mistakes I made and what they taught me

_(Example: generating the Spring Boot project before finishing the foundation steps. What did that teach you about doing steps in order?)_

## Explain-back check

Tick only when you can explain it out loud without notes.

- [ ] Maven lifecycle
- [ ] Maven Wrapper
- [ ] Dependency scopes
- [ ] BOMs and `<dependencyManagement>`
- [ ] Branching strategy
- [ ] Secret leak response
- [ ] Line endings and `.gitattributes`
- [ ] `JAVA_HOME` and multiple JDKs