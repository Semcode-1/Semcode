package com.napier.sem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Executes all report families using JDBC prepared statements and fixed SQL structure. */
public final class ReportRepository {
    private final Connection connection;
    public ReportRepository(Connection connection) { this.connection = connection; }

    public ReportResult run(ReportRequest request) throws SQLException {
        var parameters = new ArrayList<Object>();
        int id = request.id();
        String sql;
        if (id <= 6) {
            sql = "SELECT c.Code, c.Name, c.Continent, c.Region, c.Population, capital.Name AS Capital "
                    + "FROM country c LEFT JOIN city capital ON capital.ID = c.Capital";
            int scope = (id - 1) % 3;
            sql += scope == 1 ? where("c.Continent", request.filter(), parameters)
                    : scope == 2 ? where("c.Region", request.filter(), parameters) : "";
            sql += " ORDER BY c.Population DESC, c.Code ASC";
            if (id >= 4) sql += limit(request.limit(), parameters);
        } else if (id <= 16) {
            sql = "SELECT ci.Name, c.Name AS Country, ci.District, ci.Population "
                    + "FROM city ci JOIN country c ON c.Code = ci.CountryCode";
            int scope = (id - 7) % 5;
            sql += location(scope, request, parameters);
            sql += " ORDER BY ci.Population DESC, ci.ID ASC";
            if (id >= 12) sql += limit(request.limit(), parameters);
        } else if (id <= 22) {
            sql = "SELECT ci.Name, c.Name AS Country, ci.Population "
                    + "FROM country c JOIN city ci ON ci.ID = c.Capital";
            int scope = (id - 17) % 3;
            sql += scope == 1 ? where("c.Continent", request.filter(), parameters)
                    : scope == 2 ? where("c.Region", request.filter(), parameters) : "";
            sql += " ORDER BY ci.Population DESC, ci.ID ASC";
            if (id >= 20) sql += limit(request.limit(), parameters);
        } else if (id <= 25) {
            sql = breakdown(id);
        } else if (id <= 29) {
            sql = "SELECT SUM(c.Population) AS Population FROM country c";
            sql += switch (id) {
                case 27 -> where("c.Continent", request.filter(), parameters);
                case 28 -> where("c.Region", request.filter(), parameters);
                case 29 -> " WHERE " + country(request.filter(), parameters);
                default -> "";
            };
            // A nonexistent place is no match, not a misleading zero-population place.
            sql += " HAVING COUNT(*) > 0";
        } else if (id == 30) {
            sql = "SELECT c.Name AS Country, ci.District, SUM(ci.Population) AS Population "
                    + "FROM city ci JOIN country c ON c.Code = ci.CountryCode"
                    + where("ci.District", request.filter(), parameters)
                    + countryScope(request, parameters)
                    + " GROUP BY c.Code, c.Name, ci.District ORDER BY c.Name, c.Code";
        } else if (id == 31) {
            sql = "SELECT ci.ID, ci.Name, c.Name AS Country, ci.District, ci.Population "
                    + "FROM city ci JOIN country c ON c.Code = ci.CountryCode"
                    + where("ci.Name", request.filter(), parameters)
                    + countryScope(request, parameters) + " ORDER BY c.Name, ci.District, ci.ID";
        } else {
            // Multiply with DECIMAL, sum before rounding, and use world total as denominator.
            sql = "SELECT cl.Language, ROUND(SUM(c.Population * cl.Percentage / 100), 0) AS Speakers, "
                    + "ROUND(SUM(c.Population * cl.Percentage / 100) * 100 / "
                    + "NULLIF((SELECT SUM(Population) FROM country), 0), 2) AS WorldPercentage "
                    + "FROM countrylanguage cl JOIN country c ON c.Code = cl.CountryCode "
                    + "WHERE cl.Language IN ('Chinese','English','Hindi','Spanish','Arabic') "
                    + "GROUP BY cl.Language ORDER BY Speakers DESC, cl.Language ASC";
        }
        return query(sql, parameters);
    }

    private static String where(String column, String value, List<Object> parameters) {
        parameters.add(value);
        return " WHERE " + column + " = ?";
    }

    private static String country(String value, List<Object> parameters) {
        parameters.add(value);
        parameters.add(value);
        return "(c.Name = ? OR c.Code = ?)";
    }

    private static String countryScope(ReportRequest request, List<Object> parameters) {
        return request.country().isEmpty() ? "" : " AND " + country(request.country(), parameters);
    }

    private static String location(int scope, ReportRequest request, List<Object> parameters) {
        return switch (scope) {
            case 1 -> where("c.Continent", request.filter(), parameters);
            case 2 -> where("c.Region", request.filter(), parameters);
            case 3 -> " WHERE " + country(request.filter(), parameters);
            case 4 -> where("ci.District", request.filter(), parameters) + countryScope(request, parameters);
            default -> "";
        };
    }

    private static String limit(int value, List<Object> parameters) {
        parameters.add(value);
        return " LIMIT ?";
    }

    private static String breakdown(int id) {
        String column = id == 23 ? "c.Continent" : id == 24 ? "c.Region" : "c.Name";
        String group = id == 25 ? "c.Code, c.Name" : column;
        String total = "SUM(c.Population)";
        String urban = "SUM(COALESCE(cities.Population, 0))";
        String rural = "(" + total + " - " + urban + ")";
        // Aggregate cities first so each country's population is counted exactly once.
        // SUM(DISTINCT country.Population) would incorrectly merge equal country populations.
        return "SELECT " + column + " AS Name, " + total + " AS TotalPopulation, "
                + urban + " AS CityPopulation, ROUND(" + urban + " * 100 / NULLIF(" + total
                + ", 0), 2) AS CityPercentage, " + rural + " AS NonCityPopulation, ROUND("
                + rural + " * 100 / NULLIF(" + total + ", 0), 2) AS NonCityPercentage "
                + "FROM country c LEFT JOIN (SELECT CountryCode, SUM(Population) AS Population "
                + "FROM city GROUP BY CountryCode) cities ON cities.CountryCode = c.Code "
                + "GROUP BY " + group + " ORDER BY TotalPopulation DESC, Name ASC";
    }

    private ReportResult query(String sql, List<Object> parameters) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.size(); i++) statement.setObject(i + 1, parameters.get(i));
            try (var results = statement.executeQuery()) {
                var metadata = results.getMetaData();
                var columns = new ArrayList<String>();
                for (int i = 1; i <= metadata.getColumnCount(); i++) columns.add(metadata.getColumnLabel(i));
                var rows = new ArrayList<List<String>>();
                while (results.next()) {
                    var row = new ArrayList<String>();
                    for (int i = 1; i <= columns.size(); i++) {
                        String value = results.getString(i);
                        row.add(value == null ? "N/A" : value);
                    }
                    rows.add(List.copyOf(row));
                }
                return new ReportResult(List.copyOf(columns), List.copyOf(rows));
            }
        }
    }
}
