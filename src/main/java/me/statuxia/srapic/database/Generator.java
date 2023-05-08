package me.statuxia.srapic.database;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Generator {
    private static boolean isCreated = false;
    private final String dbUser;
    private final String dbPassword;
    private final String dbHost;
    private final String dbName;
    private final String dbURL;
    private final JSONObject dbTables;
    private final StringBuilder builder = new StringBuilder();

    private Generator(JSONObject mysqlJSON) {
        dbUser = mysqlJSON.getString("user");
        dbPassword = mysqlJSON.getString("password");
        dbHost = mysqlJSON.getString("host");
        dbName = mysqlJSON.getString("database");
        dbURL = "jdbc:mysql://%1$s/%2$S".formatted(dbHost, dbName);
        dbTables = mysqlJSON.getJSONObject("tables");
    }

    public static void create(JSONObject mysqlJSON) throws JSONException, SQLException {
        if (isCreated) {
            return;
        }
        isCreated = true;

        Generator generate = new Generator(mysqlJSON);
        generate.database()
                .metrics()
                .executeUpdate();
    }

    private Generator database() throws SQLException {
        String jdbcDB = "jdbc:mysql://%1$s".formatted(dbHost);
        String sqlDB = "CREATE DATABASE IF NOT EXISTS %1$s".formatted(dbName);

        try (Connection connection = DriverManager.getConnection(jdbcDB, dbUser, dbPassword)) {
            try (PreparedStatement ps = connection.prepareStatement(sqlDB)) {
                ps.executeUpdate();
            }
        }
        return this;
    }

    private Generator metrics() {
        String createMetrics = "CREATE TABLE IF NOT EXISTS %1$s (update_time BIGINT NOT NULL , online INT NOT NULL PRIMARY KEY );";
        builder.append(createMetrics.formatted(dbTables.getString("metrics")));
        return this;
    }

    private void executeUpdate() throws SQLException {
        try (Connection connection = DriverManager.getConnection(dbURL, dbUser, dbPassword)) {
            try (PreparedStatement ps = connection.prepareStatement(builder.toString())) {
                ps.executeUpdate();
            }
        }
    }
}
