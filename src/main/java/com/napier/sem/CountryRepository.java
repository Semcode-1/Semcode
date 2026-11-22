package com.napier.sem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Executes country reports against the World database. */
public class CountryRepository {
    private final Connection connection;

    public CountryRepository(Connection connection) {
        this.connection = connection;
    }

    public List<Country> mostPopulous(int limit) throws SQLException {
        if (limit < 1) {
            throw new IllegalArgumentException("The number of countries must be positive");
        }
        // LEFT JOIN keeps countries that do not have a capital in the dataset.
        String sql = """
                SELECT c.Code, c.Name, c.Continent, c.Region, c.Population,
                       capital.Name AS CapitalName
                FROM country c
                LEFT JOIN city capital ON capital.ID = c.Capital
                ORDER BY c.Population DESC, c.Code ASC
                LIMIT ?
                """;
        List<Country> countries = new ArrayList<>();
        try (var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (var results = statement.executeQuery()) {
                while (results.next()) {
                    countries.add(new Country(results.getString("Code"), results.getString("Name"),
                            results.getString("Continent"), results.getString("Region"),
                            results.getLong("Population"), results.getString("CapitalName")));
                }
            }
        }
        return countries;
    }
}
