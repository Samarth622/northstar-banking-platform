# Learning Log 01 — Phase 0, Assignment 1: Environment, Maven and Git

| Field | Value |
|-------|-------|
| Phase | 0 — Engineering workspace and Java refresh |
| Started | _17 November 2026_ |
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

Maven has a sequence of build steps called a lifecycle. Each phase performs a particular task, and running a later phase automatically executes the earlier phases.

- `package` compiles the code, runs the applicable tests and creates the final JAR or WAR.
- `verify` goes further by running additional checks to make sure the packaged application meets quality requirements.
- `install` takes the successfully built artifact and places it in my local Maven repository (`.m2/repository`) so other local projects can use it as a dependency.

For example, running `mvn verify` also executes the earlier phases, including compilation, testing and packaging.

**Why it matters for Northstar:**

Northstar needs a reliable build process. I should use `verify` as my normal validation command because it checks more than simply whether the application compiles. This will help catch problems before pushing code to GitHub or merging a branch.

**What I tried:**

Commands to run from the project directory:

```bash
mvn clean package
mvn clean verify
mvn clean install
```

Observe:

- `package`: whether the JAR is generated inside `target/`.
- `verify`: whether all verification checks pass.
- `install`: whether the artifact appears in the local `.m2/repository`.

Actual results: Pending execution.

**Open questions:**

- Which additional verification checks will Northstar configure?
- When should I use `install` instead of `verify` in a multi-module project?

**Sources:**

- Apache Maven — Introduction to the Build Lifecycle: https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html

---

## 2. Dependency scopes (compile, runtime, test, provided)

**In my own words:**

Dependency scopes tell Maven when a particular library is needed. Not every library is required during compilation and application execution.

My understanding of the four important scopes:

1. **compile:** The default scope. The dependency is available during compilation, testing and runtime.

   Northstar example: `spring-boot-starter-web`, because the application needs Spring MVC classes to compile and run.

2. **runtime:** The dependency is not needed to compile my Java source code, but it is needed when the application runs.

   Northstar example: PostgreSQL JDBC driver (`org.postgresql:postgresql`), which provides the database driver when Northstar connects to PostgreSQL.

3. **test:** The dependency is required only while compiling and executing tests. It is not included as a normal application dependency.

   Northstar example: JUnit Jupiter and Mockito for unit testing services and repositories.

4. **provided:** The dependency is needed to compile and test the application, but I expect the runtime environment to supply it.

   Northstar example: Servlet API when deploying a WAR to an external servlet container that already provides the API.

   I should not automatically mark Northstar's servlet dependencies as provided if I am using Spring Boot's embedded Tomcat server.

**Why it matters for Northstar:**

Correct dependency scopes keep the application lightweight and prevent unnecessary libraries from being included in the runtime environment. They also help avoid conflicts between libraries supplied by the application and those supplied by the deployment environment.

**Open questions:**

- How can I inspect the final dependency tree?
- How do transitive dependencies behave when different scopes are combined?

**Sources:**

- Apache Maven — Introduction to the Dependency Mechanism: https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html

---

## 3. `<dependencyManagement>`, BOMs and why I don't pin Spring Security

**In my own words:**

`<dependencyManagement>` is a section in Maven's POM that controls dependency versions and their default configuration.

It does not automatically add every listed dependency to my project. I still have to declare the dependencies I actually want inside `<dependencies>`.

A BOM (Bill of Materials) is a special Maven POM that provides a centrally maintained list of compatible dependency versions.

Spring Boot provides its own BOM called `spring-boot-dependencies`. It manages versions of Spring libraries and other commonly used third-party dependencies.

For example, instead of manually specifying versions for Spring Web, Spring Data JPA and Spring Security, I can let the Spring Boot dependency management select compatible versions.

I should not independently pin Spring Security because Spring Boot already manages a tested combination of dependencies. Manually overriding its version could introduce compatibility problems.

**Why it matters for Northstar:**

Northstar will use multiple Spring technologies, including Spring Boot, Spring Security and Spring Data JPA.

Using the Spring Boot BOM keeps their versions consistent and reduces the risk of dependency conflicts. It also makes future upgrades easier because I can upgrade Spring Boot rather than manually changing every related dependency.

**Open questions:**

- How can I see which dependency version Maven actually resolves?
- What happens if two imported BOMs manage the same dependency?
- When is overriding a Spring Security version justified?

**Sources:**

- Apache Maven — Dependency Management and BOMs: https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html
- Spring Boot — Build Systems: https://docs.spring.io/spring-boot/reference/using/build-systems.html
- Spring Security — Getting Spring Security: https://docs.spring.io/spring-security/reference/getting-spring-security.html

---

## 4. Branching strategy: my choice and why

**Options I compared:**

1. **Git Flow:** Uses separate branches such as `main`, `develop`, `feature`, `release` and `hotfix`. It is useful for larger projects with formal release cycles but can introduce unnecessary complexity.
2. **GitHub Flow:** Keeps a stable `main` branch and uses short-lived feature branches. Changes are reviewed and merged through pull requests.
3. **Working directly on main:** Changes are committed directly to the main branch. This is simple but risky because unfinished work can affect the stable codebase.

**My choice:**

I will use GitHub Flow with short-lived feature branches.

**Why (2–3 sentences):**

Northstar is an evolving project, and I want to keep the main branch stable while developing individual features independently. Feature branches allow me to experiment, test changes and review them before merging. GitHub Flow is simple enough for a solo developer but also supports collaboration if more developers join later.

**Branch naming convention I will use:**

- `main` — stable application code.
- `feature/user-authentication` — new features.
- `fix/login-validation` — bug fixes.
- `docs/project-setup` — documentation changes.
- `refactor/security-config` — code restructuring.
- `chore/dependency-updates` — maintenance tasks.

Example commands:

```bash
git switch main
git pull origin main
git switch -c feature/user-authentication
```

After completing the work:

```bash
git add .
git commit -m "feat: implement user authentication"
git push -u origin feature/user-authentication
```

I will then create a pull request and merge the reviewed changes into `main`.

---

## 5. What never gets committed, and what to do if a secret leaks

**In my own words:**

Git stores the history of my project, so anything committed can remain accessible even after I delete it from the current files.

I should never commit passwords, API keys, database credentials, private tokens, private certificates or production secrets.

Files I should normally exclude include:

- `.env`
- `.env.local`
- Files containing database passwords.
- Private SSH keys.
- IDE-specific temporary files.
- Build output such as `target/`.
- Logs and temporary files.

Instead, I should commit an `.env.example` file containing placeholder values so another developer understands which environment variables are required.

For example:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=northstar
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

**Steps if a secret is committed (in order, and why that order):**

1. **Revoke or rotate the exposed secret immediately.** This is the priority because someone may already have copied it.
2. **Check for unauthorized usage.** Review access logs and activity associated with the exposed credential.
3. **Remove the secret from the current code.** Replace it with environment variables or a secure secrets-management solution.
4. **Remove it from Git history if necessary.** Use an appropriate history-rewriting tool and coordinate with collaborators because rewriting history affects existing clones.
5. **Verify the repository is clean.** Search the repository and history for the exposed value.
6. **Add prevention measures.** Update `.gitignore` and enable secret scanning or pre-commit checks.

Deleting a secret from the latest commit does not make an already exposed credential safe. Rotation must happen first.

**Sources:**

- Git documentation — gitignore: https://git-scm.com/docs/gitignore
- GitHub documentation — Removing sensitive data from a repository: https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository

---

## 6. Line endings on Windows (`.gitattributes`)

**In my own words:**

Different operating systems represent the end of a text line differently.

- Windows generally uses CRLF (`\r\n`).
- Linux and macOS generally use LF (`\n`).

If developers use different operating systems, Git may detect unnecessary changes in files simply because their line endings differ.

A `.gitattributes` file allows me to define consistent line-ending rules for the repository.

For Northstar, I want Git to store normal text files using LF so that the repository remains consistent across Windows, Linux and CI environments.

Example `.gitattributes`:

```gitattributes
* text=auto

*.java text eol=lf
*.xml text eol=lf
*.yml text eol=lf
*.yaml text eol=lf
*.properties text eol=lf
*.sh text eol=lf
*.bat text eol=crlf
*.cmd text eol=crlf
```

**What I tried:**

Run:

```bash
git ls-files --eol
```

The command displays the line endings in the Git index and working directory.

Example output:

```text
i/lf    w/lf    attr/text=auto    pom.xml
i/lf    w/crlf  attr/text=auto    README.md
```

Meaning:

- `i/lf` means the version stored in Git's index uses LF.
- `w/lf` means the working copy on my computer uses LF.
- `w/crlf` means the working copy uses CRLF.

The ideal result for most Northstar text files is consistent LF in the repository. The working-copy result depends on the `.gitattributes` rules.

Actual output: Pending execution.

**Open questions:**

- When should I use `text=auto` versus explicitly setting `eol=lf`?
- How do I safely normalize existing files after adding `.gitattributes`?

**Sources:**

- Git documentation — gitattributes: https://git-scm.com/docs/gitattributes

---

## 7. `.gitignore` in practice

**What I tried:**

The purpose of `.gitignore` is to tell Git which untracked files should not be included in version control.

For Northstar, I should ignore environment files, build output, IDE files and logs.

Example `.gitignore`:

```gitignore
# Environment variables and secrets
.env
.env.*
!.env.example

# Maven build output
target/

# IDE files
.idea/
.vscode/
*.iml

# Logs
*.log

# Operating system files
.DS_Store
Thumbs.db
```

Commands to test:

```bash
# Create a dummy environment file
echo "DB_PASSWORD=dummy" > .env

# Check which rule ignores it
git check-ignore -v .env

# Check whether the example file is ignored
git check-ignore -v .env.example
```

Expected behaviour:

- `.env` should be ignored.
- `.env.example` should not be ignored because of the negation rule `!.env.example`.

The first command should display the matching `.gitignore` file, line number and rule.

The second command should produce no matching rule (and normally exit with status 1).

**What I learned:**

`.gitignore` prevents untracked files from being added accidentally, but it does not automatically remove files that Git already tracks.

If I accidentally commit `.env`, I must remove it from tracking and rotate any exposed credentials.

---

## 8. JDK setup: `JAVA_HOME`, vendors and multiple JDKs

**In my own words:**

The JDK (Java Development Kit) is the software package required to develop and compile Java applications.

It contains tools such as:

- `java` — executes Java applications.
- `javac` — compiles Java source code.
- Other development and diagnostic tools.

Different vendors provide JDK distributions, including Eclipse Temurin, Oracle JDK and Amazon Corretto.

`JAVA_HOME` is an environment variable that points to the root directory of the selected JDK installation.

For example, on Windows:

```text
JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21
```

The PATH variable should include:

```text
%JAVA_HOME%\bin
```

This allows the terminal to find Java commands.

I can also have multiple JDK versions installed on one computer. My terminal, IDE and Maven may use different configurations, so I need to verify which JDK each one actually uses.

**My current setup:**

Fill in after checking:

- Vendor: [Check installed JDK vendor]
- Version: [Check Java version]
- Installation path: [Check JAVA_HOME]
- IDE: [IntelliJ IDEA / Eclipse / VS Code]
- IDE project SDK: [Check selected JDK]
- Maven JDK: [Check Maven runner configuration]

Commands:

```bash
java -version
javac -version
echo %JAVA_HOME%
where java
mvn -version
```

For PowerShell:

```powershell
$env:JAVA_HOME
$env:Path -split ';'
```

`mvn -version` is particularly useful because it reports the Java version and Java home Maven is actually using.

**Open questions:**

- How can I switch between multiple JDK versions without uninstalling them?
- How can I ensure my IDE and terminal use the same JDK?
- What happens if Maven runs using a different JDK from the one configured in my IDE?

---

## 9. Docker PostgreSQL vs local PostgreSQL

**Why the blueprint prefers Docker:**

Docker allows me to run PostgreSQL inside an isolated container rather than depending on a PostgreSQL installation directly on my computer.

This provides several advantages:

1. Everyone can use the same PostgreSQL version.
2. The database environment is separated from my operating system.
3. I can start and stop the database when required.
4. I can recreate the environment using Docker configuration.
5. It reduces machine-specific installation and configuration problems.

For Northstar, Docker will make local development and testing more reproducible.

**How I will avoid the port 5432 clash:**

PostgreSQL normally listens on port `5432`.

If a local PostgreSQL installation already uses that port, Docker cannot publish another container to the same host port.

I can map a different host port to the container's internal PostgreSQL port.

Example (Windows Command Prompt):

```cmd
docker run --name northstar-postgres ^
  -e POSTGRES_DB=northstar ^
  -e POSTGRES_USER=northstar ^
  -e POSTGRES_PASSWORD=change_me ^
  -p 127.0.0.1:5433:5432 ^
  -d postgres:18
```

Here:

- `5433` is the port on my Windows machine.
- `5432` is the port inside the container.
- `127.0.0.1` restricts host access to my own machine.

Northstar's database configuration would then use:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/northstar
```

I should store the actual username and password in environment variables rather than hardcoding them in committed configuration.

I should also configure a persistent Docker volume so database data survives container recreation.

---

## Mistakes I made and what they taught me

**Learning reflection:**

One important lesson from this assignment is that setting up an engineering environment should happen in a planned order.

If I start generating application code before confirming my Java version, Maven configuration, Git setup and environment variables, I may encounter avoidable problems later.

I learned that a reliable project is not just about writing Java code. It also requires reproducible builds, consistent dependency versions, secure configuration and a clean Git workflow.

I should complete the foundation first and only then start building Northstar features.

**Actual mistakes I made:**

[Add the real mistakes encountered during setup. Do not invent mistakes just to fill this section.]

## Explain-back check

Tick only after I can explain each topic out loud without referring to notes.

- [ ] Maven lifecycle
- [ ] Maven Wrapper
- [ ] Dependency scopes
- [ ] BOMs and `<dependencyManagement>`
- [ ] Branching strategy
- [ ] Secret leak response
- [ ] Line endings and `.gitattributes`
- [ ] `JAVA_HOME` and multiple JDKs

**Final reflection:**

My biggest takeaway from Phase 0, Assignment 1 is that professional software development starts with a predictable and maintainable environment. A good foundation makes the actual application easier to build, test, debug and maintain.