# simple-java-maven-app

[![CI](https://github.com/ZarifRamzan/simple-java-maven-app/actions/workflows/ci.yml/badge.svg)](https://github.com/ZarifRamzan/simple-java-maven-app/actions/workflows/ci.yml)

A minimal Maven-built Java application that prints `Hello World!`, used as the sample
project for the Jenkins tutorial
[Build a Java app with Maven](https://jenkins.io/doc/tutorials/build-a-java-app-with-maven/).

This fork of [jenkins-docs/simple-java-maven-app](https://github.com/jenkins-docs/simple-java-maven-app)
keeps the tutorial example intact and adds a local quality gate (code formatting and
static analysis) plus a GitHub Actions pipeline that validates, tests and scans the
project on every push and pull request.

## Requirements

| Tool  | Version    | Enforced by                                          |
| ----- | ---------- | ---------------------------------------------------- |
| JDK   | 21 or newer | `maven-enforcer-plugin` (`requireJavaVersion`)       |
| Maven | 3.9.9 or newer | `maven-enforcer-plugin` (`requireMavenVersion`)   |

The build fails fast if either requirement is not met.

## Quick start

```bash
# Build the executable jar (tests skipped)
mvn -B clean package

# Run it
java -jar target/my-app-1.0-SNAPSHOT.jar
# -> Hello World!

# Run the unit tests (JUnit XML written to target/surefire-reports/)
mvn -B test

# Full build: compile, test, then the Spotless format check
mvn -B clean verify
```

## Code quality gates

Two Maven plugins keep the source consistent. Both are bound to the build lifecycle,
so they run automatically — no separate step needed in CI or Jenkins.

| Plugin                     | Version | Bound to   | What it checks                                                        | Fix / run manually        |
| -------------------------- | ------- | ---------- | --------------------------------------------------------------------- | ------------------------- |
| Spotless (google-java-format) | 2.46.1  | `verify`   | Formatting, unused imports, trailing whitespace, missing final newline | `mvn spotless:apply`      |
| Checkstyle (`google_checks.xml`) | 3.6.0   | `validate` | Style, naming, Javadoc and bug patterns; test sources included; fails on warnings | `mvn checkstyle:check`    |

Because Checkstyle runs in `validate`, even `mvn test` fails on a style violation.
Spotless only runs from `verify` onwards, so use `mvn verify` (or `install`) to check
formatting locally. `mvn spotless:apply` reformats the sources for you.

## Continuous integration (GitHub Actions)

`.github/workflows/ci.yml` runs three sequential jobs on `ubuntu-latest`:

| # | Job          | Depends on | Steps                                                                 | Artifacts                                  |
| - | ------------ | ---------- | --------------------------------------------------------------------- | ------------------------------------------ |
| 1 | **Validate** — format & lint | — | `spotless:check`, `checkstyle:check` (JDK 21, Temurin, Maven cache) | —                                          |
| 2 | **Test** — unit tests & jar  | Validate | `mvn -B clean verify`                                     | `surefire-reports-jdk21`, `my-app-jar` (7 days) |
| 3 | **Security** — CodeQL + scan | Test | CodeQL init/build/analyze for `java-kotlin` (`security-extended`); Trivy `fs` scan of vulns, secrets and misconfig, SARIF uploaded to the Security tab | `trivy-results.sarif` in the Security tab  |

Additional behaviour:

- **Triggers:** push and pull request to `master` / `main`, plus manual `workflow_dispatch`.
- **Concurrency:** a new push to the same ref cancels the run still in flight.
- **Permissions:** `contents: read` by default; the security job adds `security-events: write`
  to upload SARIF.
- **Job timeouts:** 10 / 15 / 20 minutes respectively.
- **Trivy is advisory** (`exit-code: '0'`) — findings appear in the Security tab but do not
  fail the build. Set `exit-code: '1'` in the Trivy step to make CRITICAL/HIGH findings blocking.

## Jenkins pipeline

`jenkins/Jenkinsfile` is the declarative pipeline used in the tutorial
(`agent { label '!windows' }`, unstable builds skip the remaining stages):

| Stage     | Command                                        | Notes                                            |
| --------- | ---------------------------------------------- | ------------------------------------------------ |
| `Build`   | `mvn -B -DskipTests clean package`             | Produces `target/my-app-1.0-SNAPSHOT.jar`        |
| `Test`    | `mvn test`                                     | Publishes `target/surefire-reports/*.xml` via `junit` |
| `Deliver` | `./jenkins/scripts/deliver.sh`                 | `install:install` into the local Maven repo, then runs the jar |

`jenkins/scripts/deliver.sh` echoes each command before running it and reads the project
`name` and `version` from `pom.xml` with `help:evaluate`, so it does not hardcode the jar path.

## Project structure

```
.
├── .github/
│   ├── dependabot.yml          # weekly Maven dependency updates
│   └── workflows/ci.yml        # Validate -> Test -> Security
├── jenkins/
│   ├── Jenkinsfile             # Build / Test / Deliver
│   └── scripts/deliver.sh
├── src/
│   ├── main/java/com/mycompany/app/App.java       # prints "Hello World!"
│   └── test/java/com/mycompany/app/AppTest.java   # JUnit 5 tests
├── LICENSE.txt
└── pom.xml                     # compiler, jar, enforcer, Spotless, Checkstyle
```

## Dependency and action updates

Dependabot is configured (`.github/dependabot.yml`) to check the Maven ecosystem weekly
from the repository root and open upgrade pull requests. Each upgrade PR still goes through
the full CI pipeline above.

## License

Released under the MIT License — see [LICENSE.txt](LICENSE.txt). The original project is
copyright its upstream authors in [jenkins-docs/simple-java-maven-app](https://github.com/jenkins-docs/simple-java-maven-app).
