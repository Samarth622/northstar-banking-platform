# Learning Log 03 — Phase 0, Assignment 3: Spring Boot skeleton

## Step 1: Generating, building and running the project

| Field | Value |
|-------|-------|
| Phase | 0 — Engineering workspace and Java refresh |
| Branch | `feature/NRB-18-spring-boot-skeleton` (merged in PR #3) |
| Started | _(fill in)_ |
| Completed | 2026-11-23 |
| Time spent | _(fill in)_ |

---

## 1. Spring Initializr decisions

| Setting | What I chose | Why |
|---|---|---|
| Project | Maven | The blueprint uses the Maven Wrapper (`mvnw`) for every build. |
| Language | Java | |
| Spring Boot version | 4.1.2 | A **GA** (general availability) release. **SNAPSHOT** = nightly build that changes daily; **M** (milestone) = early preview; **RC** (release candidate) = almost final but not released. None of those are safe for a long-running project. |
| Group | `com.northstar` | Organisation identity, written as a reversed domain name. |
| Artifact | `northstar-banking-platform` | Becomes the JAR file name; matches the repository name. |
| Package name | `com.northstar.bank` | Required by the blueprint (section 3.4). Initializr suggested a longer name built from group + artifact, so I changed it. |
| Packaging | Jar | Spring Boot runs with an embedded Tomcat inside the JAR. **War** is for deploying into an external Tomcat server — the `provided` scope case from Log 01 — which this project does not do. |
| Configuration | YAML | The blueprint uses YAML configuration; nesting keeps related settings readable. |
| Java | 21 | Matches ADR-000. Initializr only offers Java versions supported by the chosen Spring Boot version — a free compatibility check. |
| Dependencies | Spring Web only | **Minimal dependencies** (blueprint ticket 2). Each dependency is added only when the step that uses it begins. Database dependencies would stop the app from starting until a database is configured (Assignment 4). |

---

## 2. What each generated file is

| File / folder | In my own words |
|---|---|
| `pom.xml` | The project's build definition: who the project is (group, artifact, version), which Java version it uses, which libraries it needs and which plugins build it. Maven reads this file for every build. |
| `mvnw` | Maven Wrapper script for Linux, macOS and Git Bash. Downloads the exact Maven version this project needs, then runs it. |
| `mvnw.cmd` | The same wrapper script for Windows (PowerShell / CMD). |
| `.mvn/wrapper/maven-wrapper.properties` | Records which Maven version the wrapper downloads. Changing Maven's version means editing this file in a reviewed commit. |
| `src/main/java/com/northstar/bank/…Application.java` | The only Java file: the program's entry point with the `main` method. |
| `src/main/resources/application.yml` / `.yaml` | Application configuration. Currently only the application name. |
| `src/main/resources/static/` | For static web files (HTML, CSS, images). Not needed for an API backend. It is empty, so Git does not track it. |
| `src/main/resources/templates/` | For server-rendered HTML templates. Not needed either; also empty and untracked. |
| `src/test/java/com/northstar/bank/…ApplicationTests.java` | One generated test that checks the application can start. |
| `.gitignore`, `.gitattributes` | Initializr's versions were **merged into mine** rather than copied over them. |
| `HELP.md` | Links to documentation. Not committed — my `.gitignore` ignores it. |

**Folder convention:** Maven expects `src/main/java` (application code), `src/main/resources` (configuration and other non-code files), `src/test/java` (test code) and `src/test/resources` (test-only configuration). Following the convention means no extra build configuration is needed.

---

## 3. Inside `pom.xml`

### 3.1 `<parent>` — the Spring Boot BOM

The project inherits from `spring-boot-starter-parent` at version 4.1.2. The parent provides:

- **dependency management (the BOM):** a tested list of library versions that work together, so my dependencies need no `<version>`;
- sensible defaults: Java version handling, UTF-8 encoding, resource filtering and plugin configuration.

Upgrading Spring Boot later means changing this one version number, and every managed library moves together.

### 3.2 Project identity

- `<groupId>com.northstar</groupId>`
- `<artifactId>northstar-banking-platform</artifactId>`
- `<version>0.0.1-SNAPSHOT</version>`

In **my own project's** version, `SNAPSHOT` simply means "still in development, not a release". It is different from choosing a Spring Boot SNAPSHOT, which would mean depending on someone else's unfinished build.

### 3.3 `<java.version>`

Value: `21`. The parent uses this property to configure the compiler. Recorded in ADR-000.

### 3.4 `<dependencies>`

| Dependency | What it brings | Scope |
|---|---|---|
| Web starter: `(fill in — exact artifactId from pom.xml)` | Spring MVC for REST controllers, an embedded Tomcat server, and JSON conversion | compile (default) |
| `spring-boot-starter-test` (or the test starter Initializr generated) | Testing tools: JUnit, AssertJ assertions, Mockito, Spring's test support and more | `test` — used only for tests, never packaged into the application JAR |

A **starter** is a single dependency that pulls in a tested group of libraries for one purpose. Neither starter has a `<version>` — the parent BOM decides it.

### 3.5 `<build><plugins>`

`spring-boot-maven-plugin` does two jobs:

- `spring-boot:run` — starts the application directly from the compiled classes;
- `repackage` (runs automatically during `package`) — turns the normal JAR into an executable **fat JAR** that contains my classes, every dependency and a small launcher.

If `pom.xml` also configures `maven-compiler-plugin`, Initializr added it deliberately (for example for annotation processing on newer JDKs). It stays as generated.

---

## 4. The main class, line by line

| Line | Meaning |
|---|---|
| `package com.northstar.bank;` | The root package. Component scanning starts here and covers every package below it, so every future module (`identity`, `account`, `ledger`…) must live under `com.northstar.bank`. |
| `@SpringBootApplication` | A shortcut for three annotations: `@SpringBootConfiguration` (this class is a configuration source), `@EnableAutoConfiguration` (let Spring Boot set things up based on what's on the classpath) and `@ComponentScan` (find annotated classes in this package and below). |
| `public static void main(String[] args)` | A normal Java entry point, exactly like any Java program. |
| `SpringApplication.run(…Application.class, args);` | Starts Spring: prepares configuration, creates the container (ApplicationContext), creates beans and starts Tomcat. `args` passes command-line arguments through to Spring. |

**Rename:** No. I kept the class names Initializr generated for the main class and its test class.

---

## 5. The generated test

`contextLoads()` is an empty test annotated with `@SpringBootTest`. It starts the whole application context. If Spring cannot create a bean or read the configuration, the context fails to start and the test fails. It is a cheap safety net that runs on every `mvnw verify`.

---

## 6. The configuration file

- It contains `spring.application.name: northstar-banking-platform`.
- My file is named: _(fill in: `application.yml` or `application.yaml`)_. Both extensions work; the blueprint uses `.yml`.
- **Only one main configuration file.** If `application.properties` and a YAML file both exist, Spring Boot loads both, and where they conflict the properties file wins — confusing and easy to miss.

---

## 7. Building: `.\mvnw.cmd clean verify`

**What happened, in order:**

1. **First run only:** the wrapper downloaded Maven into `%USERPROFILE%\.m2\wrapper\dists\`. (This answers the open question from Log 01.)
2. **`clean`** deleted any old `target/` folder.
3. **Compile** — main and test code compiled into `target/classes` and `target/test-classes`.
4. **`test`** — the Surefire plugin ran `contextLoads()`. Reports went to `target/surefire-reports/`.
5. **`package`** — built the JAR; `spring-boot-maven-plugin` repackaged it into a fat JAR.
6. **`verify`** — final checks; nothing extra configured yet.

**My output:**

- Tests line: `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`
- Result: `BUILD SUCCESS`
- Build time: _(fill in: the `Total time:` line)_

**What `target/` contains:**

| Item | Meaning |
|---|---|
| `classes/` | Compiled application code and copied resources |
| `test-classes/` | Compiled test code |
| `surefire-reports/` | Test results |
| `northstar-banking-platform-0.0.1-SNAPSHOT.jar` | The executable fat JAR — what would be shipped |
| `northstar-banking-platform-0.0.1-SNAPSHOT.jar.original` | The plain JAR from before repackaging |

`target/` is ignored by Git: it is rebuilt from source on every build.

---

## 8. Running: reading the startup log

How I ran it: `.\mvnw.cmd spring-boot:run`

| Log line (my output) | What it means |
|---|---|
| _(fill in: the `Starting … using Java …` line)_ | Which JDK actually ran the app and the process ID. **It must say Java 21, matching ADR-000.** |
| _(fill in: the `No active profile set …` line)_ | No profile was activated, so Spring uses the built-in profile named `default`. Profiles come in a later step. |
| _(fill in: the `Tomcat started on port …` line)_ | The embedded web server is listening for HTTP requests on port 8080. |
| _(fill in: the `Started … in … seconds` line)_ | Startup is complete; the application is ready to handle requests. |

Each log line may also show `[northstar-banking-platform]`; that comes from `spring.application.name`.

**These lines map to the startup sequence:** prepare environment (profile line) → create container and beans → start Tomcat (port line) → application ready ("Started" line).

---

## 9. Why the browser showed a 404 — and why that is correct

Opening `http://localhost:8080` showed a **Whitelabel Error Page** with status **404 Not Found**.

- **The server is working:** something answered the request. If the app were not running, the browser would say it could not connect at all.
- **404 means "nothing here":** there are no controllers yet, so no code handles the path `/`, and there is no `index.html`.
- **"Whitelabel" is Spring Boot's default error page,** a fallback shown when no custom error handling exists.

**Northstar lesson:** a real API should answer errors with consistent JSON (RFC 9457 Problem Details, blueprint 6.9), not an HTML page. That comes later.

**Preview:** if Spring Security were on the classpath, the browser would show a **login page** instead of a 404, because Spring Security locks every endpoint by default (Phase 1).

---

## 10. Stopping the application

| How I stopped it | What I observed |
|---|---|
| Ctrl+C in the terminal | _(fill in)_ |

Ctrl+C asks the app to shut down **gracefully**: Spring closes the container and stops Tomcat cleanly, and the log shows shutdown messages.

**Windows detail:** because `mvnw.cmd` is a batch script, Windows may ask `Terminate batch job (Y/N)?` after Ctrl+C. That prompt comes from Windows, not from Spring. Answer `Y`.

---

## 11. Making sure nothing unwanted was committed

**Check:**

```powershell
git ls-files target .idea HELP.md
```

**Expected:** no output — none of these are tracked by Git.

**My result:** _(fill in: "no output" ✓, or what appeared and how I fixed it)_

---

## 12. Git branch hygiene I learned in this step

| Command | What it does | When I used it |
|---|---|---|
| `git branch -d <branch>` | Deletes a local branch **only if** its commits are already safe elsewhere | Removing merged branches and the AI tool's empty branch |
| `git branch -D <branch>` | Force-deletes a branch even if its commits would be lost | Not needed — `-d` is the safe default |
| `git branch -f main origin/main` | Moves local `main` to exactly where GitHub's `main` is | Fixing local `main` after I committed on it by mistake |
| `git push origin --delete <branch>` | Deletes a branch on GitHub | Removing merged remote branches |
| `git fetch --prune` | Removes local references to branches deleted on GitHub | Keeping `git log --all` tidy |

---

## Mistakes I made and what they taught me

1. **I committed the skeleton while local `main` was checked out.** My local `main` moved ahead of GitHub's `main` without a pull request. Fixed with `git branch -f main origin/main`; the commit was safe on the feature branch. **Lesson:** create the branch *before* committing; `main` only moves when a pull request is merged.
2. **An AI upgrade tool created a branch (`appmod/java-upgrade-java25`) and a checkpoint commit** without a deliberate decision to change Java versions. Nothing was changed, and I deleted the branch. **Lesson:** a version change is an ADR decision, not a side effect. Check `git status` and `git branch` before starting work, and review what tools do.
3. **Two review-tooling commits were pushed directly to `main`** instead of through a pull request. **Lesson:** every change goes through a pull request, even small ones.
4. **Pull request #3 contained only the code, not the documentation,** so a follow-up documentation PR was needed. **Lesson:** a step is done when code *and* its documentation are merged — ideally in the same pull request.

## Open questions

- What exactly is inside the fat JAR? (Open it with any zip tool and look at `BOOT-INF/` and `META-INF/MANIFEST.MF`.)
- What is the practical difference between `spring-boot:run` and `java -jar target\…jar`?
- What happens if port 8080 is already in use, and how would I change the port?
- Where do Maven's downloaded libraries live? (Hint: `%USERPROFILE%\.m2\repository`.)
- Should the project move to Java 25 (the current LTS)? To be decided deliberately and recorded in ADR-000.

## Explain-back check

Tick only when you can explain it out loud without notes.

- [ ] Why GA, not SNAPSHOT / M / RC
- [ ] Why Jar packaging, not War
- [ ] What each generated file is for
- [ ] What the parent BOM does, and why starters have no version
- [ ] What a starter is
- [ ] What `@SpringBootApplication` combines, and why the main class sits in the root package
- [ ] What `SpringApplication.run` does
- [ ] What `contextLoads()` proves
- [ ] What happens during `mvnw clean verify`
- [ ] The four startup log lines
- [ ] Why the 404 means the server is working
- [ ] When to use `git branch -d`, `-f`, `--delete` and `fetch --prune`