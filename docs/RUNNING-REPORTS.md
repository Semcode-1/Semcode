# Team guide: run the population reports

## What runs where

The Java application executes SQL through JDBC. Two Dockerfiles build the Java
application and the MySQL database. Docker Compose starts both containers, imports
`world.sql` into an empty database, waits for MySQL, then runs the selected report.
GitHub Actions repeats this setup and runs the tests. No self-hosted runner is needed.

The 32 report numbers below match the assessment. Population figures are historical
values from the supplied World dataset, not present-day estimates.

## Run a report on GitHub

1. Open **Actions → Java and Docker CI → Run workflow**.
2. Choose the branch containing this implementation (normally `develop` after merge).
3. Select a report number, or `all` to run all 32 with the example filters below.
4. Set **top_n** for Top N reports. For a filtered report, enter its location in
   **filter**. Use **country** only to narrow district/city names (e.g. `NGA`).
5. Click **Run workflow**, open the run, then open **Build JAR and Docker image**.
6. Expand **Run the application and MySQL with Compose** to see the report output.
7. Download **population-reports** from the run summary's **Artifacts** section.
   Unzip it and open `index.md` or `report-01.txt` through `report-32.txt`.
   A single-report run includes only that report's file. Downloads expire after 14 days.

Examples for the Run workflow form:

| Request | report | top_n | filter | country |
|---|---|---|---|---|
| All countries, largest first | 1 | 10 (ignored) | blank | blank |
| Top 10 countries in Africa | 5 | 10 | Africa | blank |
| All cities in Nigeria | 10 | 10 (ignored) | Nigeria | blank |
| World population | 26 | 10 (ignored) | blank | blank |
| City named Lagos in Nigeria | 31 | 10 (ignored) | Lagos | NGA |
| All 32 example reports | all | 10 | blank | blank |

GitHub's browser form depends on the default-branch workflow. While these changes
are only on a feature branch or `develop`, the new fields may not appear. Use the
local instructions or GitHub CLI below until the reviewed change reaches the default
branch through the normal GitFlow release process. Do not bypass that process.

```bash
gh workflow run build.yml --repo Semcode-1/Semcode --ref develop -f report=5 -f top_n=10 -f filter=Africa
gh workflow run build.yml --repo Semcode-1/Semcode --ref develop -f report=all -f top_n=10
```

Manual runs build, test and run reports without publishing or deployment. Pushes to
`develop` also publish the application image and verify all 32 example reports with
that image on a GitHub-hosted runner. Manual runs do not cancel each other.

## Run in IntelliJ's Terminal

Start Docker Desktop. Open the team project, then its built-in Terminal. From the
project root, use one command; Java and Maven do not need to be installed locally.

```bash
# Top 10 countries in Africa
./scripts/report.sh 5 10 Africa

# All cities in Nigeria
./scripts/report.sh 10 10 Nigeria

# World population
./scripts/report.sh 26

# All 32 reports, using the documented examples
./scripts/report.sh all
```

Quote filters containing spaces:

```bash
./scripts/report.sh 6 10 "Eastern Africa"
./scripts/report.sh 16 10 Lagos NGA
```

The arguments are **report number, N, filter, optional country**. N is validated as a
positive integer but only limits Top N reports. Country filters accept a country name
or three-letter code. After a run, view just the report output with:

```bash
docker compose logs --no-color --no-log-prefix app
```

The normal `./scripts/run.sh` defaults to report 4 (top five worldwide). The report
script supplies choices through Compose environment variables, not source edits.
The IntelliJ Java green Run button does not start the database; use Compose.

## Report catalogue

For `all`, N defaults to 10 in the report script and the workflow. The filters below
are used; district/city examples are additionally scoped to Nigeria (`NGA`). This
checks every report type, not every possible location. To inspect another location,
run its individual report with your chosen filter.

| ID | Report | Example filter |
|---|---|---|
| 1 | All countries in the world | — |
| 2 | All countries in the continent | Africa |
| 3 | All countries in the region | Eastern Africa |
| 4 | Top N countries in the world | — |
| 5 | Top N countries in the continent | Africa |
| 6 | Top N countries in the region | Eastern Africa |
| 7 | All cities in the world | — |
| 8 | All cities in the continent | Africa |
| 9 | All cities in the region | Eastern Africa |
| 10 | All cities in the country | Nigeria |
| 11 | All cities in the district | Lagos |
| 12 | Top N cities in the world | — |
| 13 | Top N cities in the continent | Africa |
| 14 | Top N cities in the region | Eastern Africa |
| 15 | Top N cities in the country | Nigeria |
| 16 | Top N cities in the district | Lagos |
| 17 | All capital cities in the world | — |
| 18 | All capital cities in the continent | Africa |
| 19 | All capital cities in the region | Eastern Africa |
| 20 | Top N capital cities in the world | — |
| 21 | Top N capital cities in the continent | Africa |
| 22 | Top N capital cities in the region | Eastern Africa |
| 23 | Population living in cities and outside cities by continent | — |
| 24 | Population living in cities and outside cities by region | — |
| 25 | Population living in cities and outside cities by country | — |
| 26 | Population of the world | — |
| 27 | Population of the continent | Africa |
| 28 | Population of the region | Eastern Africa |
| 29 | Population of the country | Nigeria |
| 30 | Population of the district | Lagos |
| 31 | Population of the city | Lagos |
| 32 | Chinese, English, Hindi, Spanish and Arabic speakers | — |

## Read results correctly

- Country, city and capital lists sort by descending population, with stable tie-breaking.
- Urban population means the cities present in this dataset; outside-city population
  is the country total minus those city totals. It is not a current census estimate.
- Zero-population places remain in breakdown reports. Their percentages are `N/A`
  because division by zero is undefined. Missing capitals also display `N/A`.
- Reports 30 and 31 can return multiple matches for a shared district/city name.
  They display country/district details (and city ID for report 31). Supply `country`
  to narrow the matches; the program does not silently choose a place.
- A valid filter with no matching rows prints `No matching records.` This is not a
  database failure. Invalid options, missing required filters and database errors fail
  the run with a nonzero exit code.
- Language speakers are estimated from country populations and language percentages.
  Each total is rounded after summation; percentages use the total world population.

## Tests and evidence

The README describes how to start an isolated test database and run `mvn clean verify`.
Tests independently compute expected report results in Java from the three base tables
and compare all 32 SQL outputs, columns and ordering. Extra cases check alternative
filters, invalid input, SQL-injection-like text, zero populations, missing capitals,
large totals, exporting all reports and authentication failures.

For your assigned report: run it with the agreed inputs, review the result, save the
workflow link and text output, and capture an actual screenshot showing its heading,
columns and results. Add that screenshot to the assessment evidence table. Do not
claim a report screenshot exists merely because automated tests pass. For long reports,
show a representative screenshot and retain the full text file and row count.

Record implementation, review, testing and presentation contributions accurately.
This shared implementation does not automatically complete sprint cards, documentation,
coverage, badges or the final submission. The final assessed version must reach `master`.

## Explain it in the meeting

“We choose a report number and inputs. Java runs its SQL against MySQL. Compose
starts the Java and database containers from their Dockerfiles. GitHub Actions builds
and tests the code, runs the reports and saves the output for download.”
