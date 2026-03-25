package com.qlsv.config;

import com.qlsv.exception.AppException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AppConfig {

    private static final String CONFIG_FILE = "application.properties";
    private static final Properties PROPERTIES = loadProperties();

    private AppConfig() {
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        // Doc file cau hinh duy nhat mot lan de dung cho toan bo ung dung.
        try (InputStream inputStream = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream != null) {
                properties.load(inputStream);
                return properties;
            }
        } catch (IOException exception) {
            throw new AppException("Không thể đọc file cấu hình ứng dụng.", exception);
        }

        // Fallback cho truong hop IDE/launcher chi build class ma chua copy resource vao classpath.
        for (Path candidatePath : new Path[]{
                Path.of("src", "main", "resources", CONFIG_FILE),
                Path.of(CONFIG_FILE)
        }) {
            if (!Files.exists(candidatePath)) {
                continue;
            }
            try (InputStream inputStream = Files.newInputStream(candidatePath)) {
                properties.load(inputStream);
                return properties;
            } catch (IOException exception) {
                throw new AppException("Không thể đọc file cấu hình tại " + candidatePath.toAbsolutePath(), exception);
            }
        }

        throw new AppException("Không tìm thấy file cấu hình " + CONFIG_FILE);
    }

    public static String getProperty(String key) {
        return PROPERTIES.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return PROPERTIES.getProperty(key, defaultValue);
    }

    public static String getRequiredProperty(String key) {
        String value = getProperty(key);
        if (value == null || value.isBlank()) {
            throw new AppException("Thiếu cấu hình bắt buộc: " + key);
        }
        return value.trim();
    }

    public static String getDbUrl() {
        return getRequiredProperty("db.url");
    }

    public static String getDbUsername() {
        return getRequiredProperty("db.username");
    }

    public static String getDbPassword() {
        return getProperty("db.password", "");
    }

    public static String getAppName() {
        return getProperty("app.name", "Hệ thống quản lý sinh viên");
    }
}
