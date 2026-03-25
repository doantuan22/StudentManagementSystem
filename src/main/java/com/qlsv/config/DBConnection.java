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
                throw new AppException("Không tìm thấy MySQL JDBC Driver. Hãy kiểm tra dependency Maven hoặc thư viện trong thư mục lib.", exception);
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
            throw new AppException("Không kết nối được MySQL. Hãy kiểm tra application.properties và các script database.", exception);
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
                    && tableExists(databaseMetaData, "faculties")
                    && tableExists(databaseMetaData, "class_rooms")
                    && tableExists(databaseMetaData, "students")
                    && tableExists(databaseMetaData, "lecturers")
                    && tableExists(databaseMetaData, "subjects")
                    && tableExists(databaseMetaData, "course_sections")
                    && tableExists(databaseMetaData, "enrollments")
                    && tableExists(databaseMetaData, "scores")
                    && tableExists(databaseMetaData, "schedules")
                    && columnExists(databaseMetaData, "course_sections", "room")
                    && columnExists(databaseMetaData, "students", "academic_year");
        } catch (SQLException | AppException exception) {
            return false;
        }
    }

    private static boolean tableExists(DatabaseMetaData databaseMetaData, String tableName) throws SQLException {
        try (var resultSet = databaseMetaData.getTables(null, null, tableName, null)) {
            return resultSet.next();
        }
    }

    private static boolean columnExists(DatabaseMetaData databaseMetaData, String tableName, String columnName) throws SQLException {
        try (var resultSet = databaseMetaData.getColumns(null, null, tableName, columnName)) {
            return resultSet.next();
        }
    }
}
