package com.napier.sem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String HOST =
            System.getenv().getOrDefault("DB_HOST", "localhost");

    private static final String PORT =
            System.getenv().getOrDefault("DB_PORT", "3307");

    private static final String DATABASE =
            System.getenv().getOrDefault("DB_NAME", "world");

    private static final String USER =
            System.getenv().getOrDefault("DB_USER", "semuser");

    private static final String PASSWORD =
            System.getenv().getOrDefault("DB_PASSWORD", "sempass");

    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private DatabaseConnection() {
        // Prevent creating instances of this utility class.
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
