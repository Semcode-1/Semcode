# Semcode – Population Reporting System

[![Master Build](https://github.com/Semcode-1/Semcode/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/Semcode-1/Semcode/actions/workflows/build.yml)
[![Develop Build](https://github.com/Semcode-1/Semcode/actions/workflows/build.yml/badge.svg?branch=develop)](https://github.com/Semcode-1/Semcode/actions/workflows/build.yml)
[![Test Coverage](https://github.com/Semcode-1/Semcode/raw/master/.github/badges/jacoco.svg)](https://github.com/Semcode-1/Semcode/actions/workflows/build.yml)
[![Latest Release](https://img.shields.io/github/v/release/Semcode-1/Semcode)](https://github.com/Semcode-1/Semcode/releases/latest)
[![License](https://img.shields.io/github/license/Semcode-1/Semcode)](https://github.com/Semcode-1/Semcode/blob/master/LICENSE)


## Project Overview

Semcode is a collaborative DevOps coursework project for SET09803. The project develops a Population Reporting System using the MySQL World database. It demonstrates software development, version control, continuous integration, containerisation, documentation and release management.

## Repository Structure

- `.github/workflows/` – GitHub Actions workflows for continuous integration.
- `docs/` – Project documentation and supporting evidence.
- `src/main/java/` – Java application source code.
- `use-cases/` – Use-case documentation and diagrams.
- `world-db/` – Database resources.
- `Dockerfile` – Docker image configuration.
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

## Build and Run

### Requirements

- Java Development Kit (JDK) 17
- Apache Maven 3.9 or later
- Docker

### Build the Application

Run the following command:

```bash
mvn --batch-mode --no-transfer-progress clean verify
```

### Run the Java Application

```bash
java -jar target/semcode.jar
```

### Build the Docker Image

```bash
docker build --tag semcode:local .
```

### Run the Docker Container

Run the following command:

`docker run --rm --network none semcode:local`

## Continuous Integration – GitHub Actions

The project uses GitHub Actions to automate the build and verification process.

The workflow is named **Java and Docker CI** and is located at:

`.github/workflows/build.yml`

The workflow includes:

- Checking out the repository.
- Setting up Java 17.
- Running the Maven verification build.
- Running the packaged Java application.
- Building the Docker image.
- Running the application inside a Docker container.
- Saving the build artifact for 14 days.

The workflow is configured to run on selected branch pushes and pull requests.

### Testing

The Maven verification process checks that the project builds successfully. The workflow also checks that the packaged application and Docker container can start.


Automated testing is configured using JUnit 5. The project includes a test for application startup. Maven runs the tests as part of the verification build, and GitHub Actions checks the build and test results.

## Releases
T
he project has published the following releases:

| Version | Release Date |
|---|---|
| [v1.0.0](https://github.com/Semcode-1/Semcode/releases/tag/v1.0.0) | 13 October 2026 |
| [v1.1.0](https://github.com/Semcode-1/Semcode/releases/tag/v1.1.0) | 14 October 2026 |
| [v1.2.0](https://github.com/Semcode-1/Semcode/releases/tag/v1.2.0) | 26 November 2026 |

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

Code coverage reporting is configured using JaCoCo. GitHub Actions generates the coverage report and publishes an SVG coverage badge in `.github/badges/jacoco.svg`. The coverage percentage is displayed in the Test Coverage badge.
