package com.napier.sem;

import java.io.PrintStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

/** Runs a country population report using database settings supplied by Compose. */
public class App {
    public static void main(String[] args) {
        int exitCode = run(args, System.getenv(), System.out, System.err);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    static int run(String[] args, Map<String, String> environment, PrintStream out, PrintStream err) {
        try {
            if (args.length > 0 && args[0].startsWith("--")) {
                return runReports(args, environment, out);
            }
            int limit = parseLimit(args);
            String url = required(environment, "DB_URL");
            String user = required(environment, "DB_USER");
            String password = required(environment, "DB_PASSWORD");
            try (var connection = DriverManager.getConnection(url, user, password)) {
                var countries = new CountryRepository(connection).mostPopulous(limit);
                if (countries.isEmpty()) {
                    throw new SQLException("The country report returned no rows");
                }
                out.println("Successfully connected to the World database");
                out.println("Top " + limit + " countries by population:");
                out.println("Code | Name | Continent | Region | Population | Capital");
                for (Country country : countries) {
                    out.printf("%s | %s | %s | %s | %d | %s%n", country.code(), country.name(),
                            country.continent(), country.region(), country.population(),
                            country.capital() == null ? "N/A" : country.capital());
                }
            }
            return 0;
        } catch (IllegalArgumentException exception) {
            err.println("Configuration error: " + exception.getMessage());
            return 1;
        } catch (java.io.IOException exception) {
            err.println("Could not save report output: " + exception.getMessage());
            return 1;
        } catch (SQLException exception) {
            // Do not print connection URLs or credentials into CI logs.
            err.println("Database report failed (SQL state " + exception.getSQLState()
                    + "). Check database readiness, credentials and the World schema.");
            return 1;
        }
    }

    private static int runReports(String[] args, Map<String, String> environment, PrintStream out)
            throws SQLException, java.io.IOException {
        ReportOptions options = ReportOptions.parse(args);
        if (options.help()) {
            out.println("Usage: --report 1..32|all --limit N --filter PLACE --country CODE --output DIRECTORY");
            out.println("Filters are required only for reports that name a location. Country scopes district/city names.");
            for (var definition : ReportCatalogue.REPORTS) {
                out.println(definition.id() + ": " + definition.title()
                        + (definition.filterType().isEmpty() ? "" : " [filter: " + definition.filterType() + "]"));
            }
            return 0;
        }
        String url = required(environment, "DB_URL");
        String user = required(environment, "DB_USER");
        String password = required(environment, "DB_PASSWORD");
        try (var connection = DriverManager.getConnection(url, user, password)) {
            out.println("Successfully connected to the World database");
            var repository = new ReportRepository(connection);
            var exporter = options.output() == null ? null : new ReportExporter(options.output());
            for (var request : options.requests()) {
                var result = repository.run(request);
                var buffer = new java.io.ByteArrayOutputStream();
                try (var report = new PrintStream(buffer, true, java.nio.charset.StandardCharsets.UTF_8)) {
                    report.println("Report " + request.id() + ": " + ReportCatalogue.get(request.id()).title());
                    report.println("N=" + request.limit() + "; filter=" + request.filter() + "; country=" + request.country());
                    result.print(report);
                    report.println("Rows: " + result.rows().size());
                }
                String text = buffer.toString(java.nio.charset.StandardCharsets.UTF_8);
                out.print(text);
                if (exporter != null) exporter.save(request, text);
            }
        }
        return 0;
    }

    static int parseLimit(String[] args) {
        if (args.length > 1) {
            throw new IllegalArgumentException("Usage: java -jar semcode.jar [positive number of countries]");
        }
        try {
            int limit = args.length == 0 ? 5 : Integer.parseInt(args[0]);
            if (limit > 0) {
                return limit;
            }
        } catch (NumberFormatException ignored) {
            // All invalid report sizes receive the same helpful message below.
        }
        throw new IllegalArgumentException("The number of countries must be a positive integer");
    }

    private static String required(Map<String, String> environment, String name) {
        String value = environment.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must be configured; run with Docker Compose");
        }
        return value;
    }
}
