# Running the population reports

Java executes the SQL query, MySQL holds the supplied World dataset, and Docker
Compose runs their two containers. The root Dockerfile builds Java; the database
Dockerfile is in `world-db`. GitHub Actions runs the same setup.

## Reports available now

| Selector | Report | Example N |
|---|---|---|
| `world` | Top N countries worldwide, largest population first | 5 |
| `africa` | Top N countries in Africa, largest population first | 10 |

Both print Code, Name, Continent, Region, Population and Capital. The population
figures are historical values from the World dataset, not today's estimates.
The Africa example covers one continent; it does not complete the assessment's
general user-selected continent requirement. The selector does not yet provide
all 32 reports. SQL files alone do not make a report runnable through Java.

## Run on GitHub (no IntelliJ or local Docker needed)

1. Open the repository's **Actions** tab.
2. Select **Java and Docker CI**, then **Run workflow**.
3. Choose the branch containing the report implementation (normally `develop`
   after its pull request is merged).
4. Choose `world` or `africa` and enter a positive whole number for `top_n`.
5. Click **Run workflow** and open the new run.
6. Open **Build JAR and Docker image → Run the application and MySQL with Compose**.
   Look for the report heading, result rows and `app-1 exited with code 0`.
7. On the run's summary page, download **population-report** under **Artifacts**.
   Unzip it to read `population-report.txt`. Artifacts are retained for 14 days.

For a screenshot, capture the heading, all required columns and result rows.
Keep the workflow URL with the evidence. A green run proves the selected report
executed; it does not mean all assessment reports are implemented.

The manual run builds and tests but does not publish or deploy an image. Publishing
and deployment verification run on pushes to `develop`. Manual report runs do not
cancel another teammate's manual run.

GitHub's manual-run UI requires the workflow on the default branch. If the button
or report inputs are missing while this change is only on a feature/develop branch,
use the local instructions below or the CLI dispatch below. Promote the reviewed
workflow through the team's normal GitFlow process; do not edit `master` directly.

Optional GitHub CLI (requires repository access):

```bash
gh workflow run build.yml --repo Semcode-1/Semcode --ref develop -f report=africa -f top_n=10
```

## Run from IntelliJ's Terminal

Start Docker Desktop and open the team repository in IntelliJ. In its Terminal,
run one command from the project root:

```bash
REPORT=africa TOP_N=10 ./scripts/run.sh
```

For the worldwide report:

```bash
REPORT=world TOP_N=5 ./scripts/run.sh
```

The script builds the images, waits for MySQL, then runs Java. The report finishes
and Compose stops both containers. “Aborting on container exit” is expected when
the app exits with code 0. A nonzero exit code needs investigation.

To show just the last application output after the run:

```bash
docker compose logs --no-color --no-log-prefix app
```

You do not need to change source code to switch between available reports.
The ordinary IntelliJ Java Run button does not start MySQL or supply its settings;
use Compose for the team demonstration.

## Adding your assigned reports

1. Update your local `develop` and create your own feature branch.
2. Write a Java query method in the appropriate repository class. Follow
   `CountryRepository.mostPopulousInAfrica` as a worked example. Use prepared
   statement parameters for user filters and N; never concatenate user input into SQL.
3. Add suitable data objects and print the exact columns required by the assessment.
   Country, city, capital, population and language reports have different columns.
4. Add a named entry to `Report.java` and handle it in `Report.print`. Country
   reports can reuse `printCountries`; add separate formatting for other report types.
   Add the same selector ID to `workflow_dispatch.inputs.report.options` in
   `.github/workflows/build.yml`.
5. For reports that need continent, region, country, district or city inputs,
   extend the argument validation in `App`, Compose's `command`, and the workflow
   inputs together. These filters are not implemented by the current two-choice
   selector. Keep existing commands working and use clearly named inputs.
6. Test expected values, population ordering, filters, required columns, invalid
   N and empty results as applicable. Keep existing worldwide and Africa tests passing.
   Follow the README's isolated database instructions and run `mvn clean verify`.
7. Run the report through Compose, save evidence, update this guide and open a PR
   into `develop`. Have a teammate review it and check CI before merging.

Review the current sprint assignments before choosing work. Building this shared
runner, implementing a report, reviewing code and testing output are separate
contributions. Record them accurately; leave unfinished report cards open.

## Explaining it at the meeting

“We choose a report and its inputs. Java uses JDBC to run that report's SQL against
the World database. Compose builds and starts Java and MySQL using two Dockerfiles.
The output appears in the application logs. GitHub Actions repeats the process,
runs our tests and saves the selected report for download.”
