package com.napier.sem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CapitalCityReports {

    private static final List<String> HEADERS =
            Arrays.asList("Name", "Country", "Population");

    private static final String BASE_QUERY =
            "SELECT ci.Name AS Name, " +
            "c.Name AS Country, " +
            "ci.Population AS Population " +
            "FROM country c " +
            "JOIN city ci ON c.Capital = ci.ID ";

    // Report 17: All capital cities worldwide.
    public static void allWorld() throws SQLException {
        executeReport(
                "All Capital Cities Worldwide",
                BASE_QUERY +
                "ORDER BY ci.Population DESC, ci.Name ASC"
        );
    }

    // Report 18: Capital cities by continent.
    public static void byContinent(String continent) throws SQLException {
        executeReport(
                "Capital Cities in " + continent,
                BASE_QUERY +
                "WHERE c.Continent = ? " +
                "ORDER BY ci.Population DESC, ci.Name ASC",
                continent
        );
    }

    // Report 19: Capital cities by region.
    public static void byRegion(String region) throws SQLException {
        executeReport(
                "Capital Cities in " + region,
                BASE_QUERY +
                "WHERE c.Region = ? " +
                "ORDER BY ci.Population DESC, ci.Name ASC",
                region
        );
    }

    // Report 20: Top N capital cities worldwide.
    public static void topNWorld(int limit) throws SQLException {
        validateLimit(limit);

        executeReport(
                "Top " + limit + " Capital Cities Worldwide",
                BASE_QUERY +
                "ORDER BY ci.Population DESC, ci.Name ASC LIMIT ?",
                limit
        );
    }

    // Report 21: Top N capital cities by continent.
    public static void topNByContinent(
            String continent, int limit) throws SQLException {

        validateLimit(limit);

        executeReport(
                "Top " + limit + " Capital Cities in " + continent,
                BASE_QUERY +
                "WHERE c.Continent = ? " +
                "ORDER BY ci.Population DESC, ci.Name ASC LIMIT ?",
                continent, limit
        );
    }

    // Report 22: Top N capital cities by region.
    public static void topNByRegion(
            String region, int limit) throws SQLException {

        validateLimit(limit);

        executeReport(
                "Top " + limit + " Capital Cities in " + region,
                BASE_QUERY +
                "WHERE c.Region = ? " +
                "ORDER BY ci.Population DESC, ci.Name ASC LIMIT ?",
                region, limit
        );
    }

    private static void validateLimit(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException(
                    "The number of cities must be greater than zero."
            );
        }
    }

    private static void executeReport(
            String title, String sql, Object... parameters)
            throws SQLException {

        List<List<String>> rows = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    List<String> row = new ArrayList<>();

                    row.add(results.getString("Name"));
                    row.add(results.getString("Country"));
                    row.add(String.valueOf(results.getLong("Population")));

                    rows.add(row);
                }
            }
        }

        System.out.println("\n" + title);
        System.out.println("==============================");

        if (rows.isEmpty()) {
            System.out.println("No records available.");
            return;
        }

        ConsoleTablePrinter.print(HEADERS, rows);
        System.out.println("Total records: " + rows.size());
    }
}
