package com.qlsv.config;

import com.qlsv.exception.AppException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DBConnection {

    private static volatile boolean driverLoaded;
    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());

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
                LOGGER.log(Level.SEVERE,
                        "Khong tim thay MySQL JDBC Driver (com.mysql.cj.jdbc.Driver). " +
                                "Kiem tra dependency Maven: mysql-connector-j.",
                        exception);
                throw new AppException("Khong tim thay MySQL JDBC Driver. Hay kiem tra dependency Maven hoac thu vien trong thu muc lib.", exception);
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
            LOGGER.log(Level.SEVERE,
                    "Khong ket noi duoc MySQL. Kiem tra db.url/db.username/db.password trong application.properties.",
                    exception);
            throw new AppException("Khong ket noi duoc MySQL. Hay kiem tra application.properties va cac script database.", exception);
        }
    }

    public static boolean canConnect() {
        try (Connection ignored = getConnection()) {
            return true;
        } catch (AppException | SQLException exception) {
            LOGGER.log(Level.WARNING,
                    "DBConnection.canConnect() that bai: " + exception.getMessage(),
                    exception);
            return false;
        }
    }

    public static boolean hasRequiredTables() {
        try (Connection connection = getConnection()) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            String[] requiredTables = new String[]{
                    "roles",
                    "users",
                    "faculties",
                    "class_rooms",
                    "rooms",
                    "students",
                    "lecturers",
                    "subjects",
                    "course_sections",
                    "enrollments",
                    "scores",
                    "schedules"
            };

            // Cac cot nay la dieu kien toi thieu de app dung voi schema da tach lich hoc ra khoi course_sections.
            String[][] requiredColumns = new String[][]{
                    {"students", "academic_year"},
                    {"schedules", "course_section_id"},
                    {"schedules", "room_id"}
            };

            List<String> missing = new ArrayList<>();
            for (String table : requiredTables) {
                if (!tableExists(databaseMetaData, table)) {
                    missing.add("Thieu bang: " + table);
                }
            }
            for (String[] pair : requiredColumns) {
                if (!columnExists(databaseMetaData, pair[0], pair[1])) {
                    missing.add("Thieu cot: " + pair[0] + "." + pair[1]);
                }
            }

            if (!missing.isEmpty()) {
                LOGGER.log(Level.SEVERE,
                        "DB schema khong dat yeu cau. " + String.join("; ", missing));
            }
            return missing.isEmpty();
        } catch (SQLException | AppException exception) {
            LOGGER.log(Level.SEVERE,
                    "Khong the kiem tra schema/metadata cua database.",
                    exception);
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
