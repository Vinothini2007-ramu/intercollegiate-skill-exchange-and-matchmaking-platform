package com.skillexchange.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/skill_exchange"
            + "?useSSL=false"
            + "&serverTimezone=UTC"
            + "&allowPublicKeyRetrieval=true";

    private static final String USER = "skillexchange_app";
    private static final String PASSWORD = "skill123";

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "MySQL JDBC driver not found on classpath", e);
        }

        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }
}