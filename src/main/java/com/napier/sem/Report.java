package com.napier.sem;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Runnable reports. Register each teammate's report here after its query is tested. */
public enum Report {
    WORLD("world", "Top N countries worldwide"),
    AFRICA("africa", "Top N countries in Africa");

    private final String id;
    private final String description;

    Report(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public static Report fromId(String id) {
        for (Report report : values()) {
            if (report.id.equals(id)) {
                return report;
            }
        }
        throw new IllegalArgumentException("Unknown report '" + id + "'. Use --help to list available reports.");
    }

    public static void printHelp(PrintStream out) {
        out.println("Usage: java -jar semcode.jar [positive N] [report]");
        out.println("Defaults: N=5, report=world. Example: java -jar semcode.jar 10 africa");
        for (Report report : values()) {
            out.println(report.id + " - " + report.description);
        }
        out.println("Only the reports listed above are implemented in this selector.");
    }

    public void print(Connection connection, int limit, PrintStream out) throws SQLException {
        var repository = new CountryRepository(connection);
        switch (this) {
            case WORLD -> printCountries(repository.mostPopulous(limit),
                    "Top " + limit + " countries by population:", out);
            case AFRICA -> printCountries(repository.mostPopulousInAfrica(limit),
                    "Top " + limit + " countries in Africa by population:", out);
        }
    }

    private static void printCountries(List<Country> countries, String title, PrintStream out)
            throws SQLException {
        if (countries.isEmpty()) {
            throw new SQLException("The country report returned no rows");
        }
        out.println(title);
        out.println("Code | Name | Continent | Region | Population | Capital");
        for (Country country : countries) {
            out.printf("%s | %s | %s | %s | %d | %s%n", country.code(), country.name(),
                    country.continent(), country.region(), country.population(),
                    country.capital() == null ? "N/A" : country.capital());
        }
    }
}
