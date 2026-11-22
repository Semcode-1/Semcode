package com.napier.sem;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class AppIT {

    @Test
    void worldDatabaseContainsCities() throws Exception {

        // Retrieve database connection settings.
        String url = System.getenv("DB_URL");
        String username = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        // Verify that database settings are configured.
        assertNotNull(url, "DB_URL must be configured");
        assertNotNull(username, "DB_USER must be configured");
        assertNotNull(password, "DB_PASSWORD must be configured");

        // Connect to MySQL and execute a SQL query.
        try (Connection connection =
                     DriverManager.getConnection(url, username, password);

             Statement statement = connection.createStatement();

             ResultSet results = statement.executeQuery(
                     "SELECT COUNT(*) FROM world.city")) {

            // Verify the SQL query returned a result.
            assertTrue(results.next());

            // Retrieve the number of cities.
            int cityCount = results.getInt(1);

            // Verify the database contains city records.
            assertTrue(cityCount > 0,
                    "World database should contain cities");
        }
    }

    @Test
    void countryReportReturnsCorrectPopulationOrderAndCapital() throws Exception {
        try (Connection connection = DriverManager.getConnection(System.getenv("DB_URL"),
                System.getenv("DB_USER"), System.getenv("DB_PASSWORD"))) {
            var repository = new CountryRepository(connection);
            var countries = repository.mostPopulous(5);
            assertEquals(java.util.List.of("CHN", "IND", "USA", "IDN", "BRA"),
                    countries.stream().map(Country::code).toList());
            assertEquals(1_277_558_000L, countries.get(0).population());
            assertEquals("Peking", countries.get(0).capital());
            assertEquals(1, repository.mostPopulous(1).size());
            // Countries without capitals must survive the LEFT JOIN.
            var allCountries = repository.mostPopulous(1000);
            assertEquals(239, allCountries.size());
            assertTrue(allCountries.stream().anyMatch(country -> country.capital() == null));
        }
    }

    @Test
    void applicationPrintsDatabaseReport() {
        var output = new java.io.ByteArrayOutputStream();
        var errors = new java.io.ByteArrayOutputStream();
        int exit = App.run(new String[]{"5"}, System.getenv(),
                new java.io.PrintStream(output), new java.io.PrintStream(errors));
        assertEquals(0, exit, errors.toString());
        assertTrue(output.toString().contains("CHN | China | Asia | Eastern Asia | 1277558000 | Peking"));
        assertEquals(8, output.toString().lines().count());
    }

    @Test
    void databaseAuthenticationFailureIsNotReportedAsSuccess() {
        var environment = new java.util.HashMap<>(System.getenv());
        environment.put("DB_PASSWORD", "deliberately-incorrect-integration-test-password");
        var output = new java.io.ByteArrayOutputStream();
        var errors = new java.io.ByteArrayOutputStream();
        assertEquals(1, App.run(new String[0], environment,
                new java.io.PrintStream(output), new java.io.PrintStream(errors)));
        assertEquals("", output.toString());
        assertTrue(errors.toString().contains("Database report failed"));
        assertFalse(errors.toString().contains(environment.get("DB_PASSWORD")));
    }
}
