# Semcode – Population Reporting System

[![Master Build](https://github.com/Semcode-1/Semcode/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/Semcode-1/Semcode/actions/workflows/build.yml?query=branch%3Amaster)

[![Develop Build](https://github.com/Semcode-1/Semcode/actions/workflows/build.yml/badge.svg?branch=develop)](https://github.com/Semcode-1/Semcode/actions/workflows/build.yml?query=branch%3Adevelop)

[![Latest Release](https://img.shields.io/github/v/release/Semcode-1/Semcode)](https://github.com/Semcode-1/Semcode/releases)

[![License](https://img.shields.io/github/license/Semcode-1/Semcode)](https://github.com/Semcode-1/Semcode/blob/master/LICENSE)
## Project Overview

Semcode is a collaborative DevOps coursework project for SET09803. The project develops a Population Reporting System using the MySQL World database. It demonstrates software development, version control, continuous integration, containerisation, documentation and release management.

## Repository Structure

- `.github/workflows/` – GitHub Actions workflows for continuous integration.
- `docs/` – Project documentation and supporting evidence.
- `src/main/java/` – Java application source code.
- `use-cases/` – Use-case documentation and diagrams.
- `world-db/` – Database resources and the MySQL `Dockerfile`.
- `Dockerfile` – Java application image configuration.
- `docker-compose.yml` – Container configuration.
- `pom.xml` – Maven project configuration.

## Branching Strategy and Gitflow

The project follows a Gitflow-inspired branching strategy:

- **master** – Main branch containing the approved project version and releases.
- **develop** – Integration branch for ongoing development.
- **feature/*** – Branches used to develop individual features before merging into develop.
- **release/*** – Branches used to prepare and verify a release before merging into master.
- **hotfix/*** – Branches intended for urgent fixes to the released version.

### Development Workflow

1. Create a feature branch from `develop`.
2. Complete and commit the feature changes.
3. Open a pull request into `develop`.
4. Review and integrate the changes.
5. Prepare a release branch when the project is ready.
6. Merge the approved release into `master`.
7. Publish the release with a version tag.

## Getting Started

Install and start Docker Desktop (or Docker Engine with the Compose plugin).
A local Java or Maven installation is **not required** for the container run.
From a fresh checkout of this implementation:

```bash
git clone --branch develop https://github.com/Semcode-1/Semcode.git
cd Semcode
./scripts/run.sh
```

The script runs this command from the repository root:

```bash
docker compose up --build --abort-on-container-exit --exit-code-from app
```

Compose builds two images using two Dockerfiles: the root `Dockerfile` builds
the Java 17 executable JAR with Maven and packages the application;
`world-db/Dockerfile` builds MySQL and copies `world.sql` into
`/docker-entrypoint-initdb.d/` for initialization on an empty database volume.
This follows the professor's [two-Dockerfile example](https://github.com/Kevin-Sim/sem-quick-cw#set-up-world-database),
using our existing `world-db` folder and `mysql` service name.
Compose waits until the application user can query the schema,
then starts the Java application. The application queries the five most populous
countries and prints code, name, continent, region, population and capital.
These are the historical figures supplied by the World dataset.

Expected output includes:

```text
Successfully connected to the World database
Top 5 countries by population:
Code | Name | Continent | Region | Population | Capital
CHN | China | Asia | Eastern Asia | 1277558000 | Peking
```

The report is a command-line application: it finishes with exit code 0 and Compose
stops MySQL. The message “Aborting on container exit” is expected after the report.
Configuration, connection or query failures return a nonzero exit code.
Change the report size with `TOP_N=10 ./scripts/run.sh`.

### Database settings and stopping

The app uses the internal Compose hostname `mysql` and port `3306`. No database
port is exposed to the host during a normal run. Compose supplies `DB_URL`,
`DB_USER` and `DB_PASSWORD`; running the IntelliJ green button without those
settings will not start the database.

The default credentials (`semuser` / `sempass`, root password `rootpass`) are for
this local coursework database. Override `DB_PASSWORD` and `MYSQL_ROOT_PASSWORD`
in your environment before first initialization if needed. No credentials are
needed in Java source. The application uses `semuser`, not root.

```bash
docker compose down
```

This removes the project's containers and network while retaining its database
volume. MySQL only applies initialization SQL, passwords and database grants on
an empty volume. If reusing a database created by the old Compose file, the
application user's World grants may be missing. To retain that data, an administrator
can grant `semuser` access to `world.*`. For an explicitly disposable coursework
copy, `docker compose down --volumes` deletes that project's database so the next
run imports the supplied dataset again. Do not use that command on data you need.

### Unit and database integration tests

For host-side development, install JDK 17 and Maven 3.9 or later.
Unit tests run without MySQL:

```bash
mvn --batch-mode --no-transfer-progress test
```

Full verification needs the database. Use an isolated Compose project and a
localhost-only test port so this does not reuse your regular database:

```bash
export COMPOSE_PROJECT_NAME=semcode-tests
export COMPOSE_FILE=docker-compose.yml:docker-compose.test.yml
export MYSQL_PORT=3307
export DB_USER=semuser
export DB_PASSWORD=sempass
export DB_URL="jdbc:mysql://127.0.0.1:${MYSQL_PORT}/world?allowPublicKeyRetrieval=true&useSSL=false&connectTimeout=10000&socketTimeout=30000"
docker compose up --build --detach --wait --wait-timeout 180 mysql
mvn --batch-mode --no-transfer-progress clean verify
java -jar target/semcode.jar 5
docker compose down --volumes
unset COMPOSE_PROJECT_NAME COMPOSE_FILE MYSQL_PORT DB_USER DB_PASSWORD DB_URL
```

Choose another `MYSQL_PORT` if 3307 is occupied. The cleanup above deletes only
the disposable `semcode-tests` database. Surefire runs unit tests; Failsafe runs
integration tests against MySQL, including the original city-data check,
country ordering and capital lookup, application output, and failed authentication.
Reports are written to `target/surefire-reports` and `target/failsafe-reports`.

## Continuous Integration – GitHub Actions

`.github/workflows/build.yml` runs on selected branch pushes and pull requests:

1. Build the database image and start a fresh MySQL database using Compose.
2. Run Maven unit and integration tests and build the executable JAR.
3. Run the JAR against MySQL.
4. Build the application image from source and run app + MySQL through Compose.
5. Save the JAR and test reports for 14 days; show logs and remove CI resources.

Both jobs use GitHub-hosted runners. A successful
push to `develop` publishes the tested image with commit-SHA and `latest` tags.
The deployment job pulls that exact SHA image, builds the database image from
`world-db/Dockerfile`, and runs both through Compose on `ubuntu-24.04`.
It verifies the report and then removes its
own temporary database; this CLI application is not a long-running web service.
Feature branches and pull requests do not publish or deploy.

This completes the initial Java-to-database execution path. The other coursework
reports and code coverage reporting remain separate work; this demonstration does
not claim that all 32 requirements are implemented.

## Releases

The project has published the following releases:

| Version | Release Date |
|---|---|
| [v1.0.0](https://github.com/Semcode-1/Semcode/releases/tag/v1.0.0) | 13 October 2026 |
| [v1.1.0](https://github.com/Semcode-1/Semcode/releases/tag/v1.1.0) | 14 October 2026 |

See the [GitHub Releases page](https://github.com/Semcode-1/Semcode/releases) for release details.

## Team Collaboration

The project uses GitHub for collaborative development and version control. Issues and pull requests support task tracking, code review and integration of team contributions.

## Documentation Maintenance

This README provides an overview of the project, its repository structure, branching strategy, build instructions, continuous integration workflow and releases. It should be updated when significant project features, workflows or release information change.

## Coursework and Functional Requirements

The system requirements are based on the 32 functional requirements specified for the coursework.

The number of requirements successfully implemented and the percentage achieved should be completed after checking the application against the requirements.

An evidence table should be maintained to record each requirement, whether it has been met, and the supporting screenshot.

| Requirement ID | Requirement Name | Met (Yes/No) | Evidence |
|---|---|---|---|
| To be completed | To be completed | To be verified | Screenshot to be added |

## Code Coverage

Code coverage reporting has not yet been configured in the project. A coverage percentage should only be added after an appropriate testing and coverage tool has been configured and its report generated.
