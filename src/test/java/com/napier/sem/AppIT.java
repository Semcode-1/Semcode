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
}