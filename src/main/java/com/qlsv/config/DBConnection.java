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
                        "Không tìm thấy MySQL JDBC Driver (com.mysql.cj.jdbc.Driver). " +
                                "Kiểm tra dependency Maven: mysql-connector-j.",
                        exception);
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
            // Log chi tiết để dễ debug khi chạy từ IDE khác.
            LOGGER.log(Level.SEVERE,
                    "Không kết nối được MySQL. Kiểm tra db.url/db.username/db.password trong application.properties.",
                    exception);
            throw new AppException("Không kết nối được MySQL. Hãy kiểm tra application.properties và các script database.", exception);
        }
    }

    public static boolean canConnect() {
        try (Connection ignored = getConnection()) {
            return true;
        } catch (AppException | SQLException exception) {
            LOGGER.log(Level.WARNING,
                    "DBConnection.canConnect() thất bại: " + exception.getMessage(),
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

            // Lưu ý: các cột này là điều kiện tối thiểu để ứng dụng hoạt động đúng với version schema hiện tại.
            String[][] requiredColumns = new String[][]{
                    {"course_sections", "room_id"},
                    {"students", "academic_year"}
            };

            List<String> missing = new ArrayList<>();
            for (String table : requiredTables) {
                if (!tableExists(databaseMetaData, table)) {
                    missing.add("Thiếu bảng: " + table);
                }
            }
            for (String[] pair : requiredColumns) {
                if (!columnExists(databaseMetaData, pair[0], pair[1])) {
                    missing.add("Thiếu cột: " + pair[0] + "." + pair[1]);
                }
            }

            if (!missing.isEmpty()) {
                LOGGER.log(Level.SEVERE,
                        "DB schema không đạt yêu cầu. " + String.join("; ", missing));
            }
            return missing.isEmpty();
        } catch (SQLException | AppException exception) {
            LOGGER.log(Level.SEVERE,
                    "Không thể kiểm tra schema/metadata của database.",
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
