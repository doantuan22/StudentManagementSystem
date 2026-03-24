package com.qlsv.config;

import com.qlsv.exception.AppException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {

    private static volatile boolean driverLoaded;

    private DBConnection() {
    }

    private static void ensureDriverLoaded() {
        if (driverLoaded) {
            return;
        }

        synchronized (DBConnection.class) {
            if (driverLoaded) {
                return;
            }
            try {
                // Nap JDBC driver mot lan truoc khi tao ket noi.
                Class.forName("com.mysql.cj.jdbc.Driver");
                driverLoaded = true;
            } catch (ClassNotFoundException exception) {
                throw new AppException("Khong tim thay MySQL JDBC Driver. Hay kiem tra dependency Maven/lib.", exception);
            }
        }
    }

    public static Connection getConnection() {
        ensureDriverLoaded();
        try {
            // Lay ket noi moi cho moi thao tac DAO de de quan ly transaction.
            return DriverManager.getConnection(
                    AppConfig.getDbUrl(),
                    AppConfig.getDbUsername(),
                    AppConfig.getDbPassword()
            );
        } catch (SQLException exception) {
            throw new AppException("Khong ket noi duoc MySQL. Hay kiem tra application.properties va script database.", exception);
        }
    }

    public static boolean canConnect() {
        try (Connection ignored = getConnection()) {
            return true;
        } catch (AppException | SQLException exception) {
            return false;
        }
    }

    public static boolean hasRequiredTables() {
        try (Connection connection = getConnection()) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            return tableExists(databaseMetaData, "roles")
                    && tableExists(databaseMetaData, "users")
                    && tableExists(databaseMetaData, "course_sections")
                    && tableExists(databaseMetaData, "enrollments")
                    && tableExists(databaseMetaData, "scores")
                    && tableExists(databaseMetaData, "schedules");
        } catch (SQLException | AppException exception) {
            return false;
        }
    }

    private static boolean tableExists(DatabaseMetaData databaseMetaData, String tableName) throws SQLException {
        try (var resultSet = databaseMetaData.getTables(null, null, tableName, null)) {
            return resultSet.next();
        }
    }
}
