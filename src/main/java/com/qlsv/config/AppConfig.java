package com.qlsv.config;

import com.qlsv.exception.AppException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class AppConfig {

    private static final String CONFIG_FILE = "application.properties";
    private static final Properties PROPERTIES = loadProperties();

    private AppConfig() {
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        // Đọc file cấu hình duy nhất một lần để dùng cho toàn bộ ứng dụng.
        try (InputStream inputStream = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream != null) {
                properties.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                return properties;
            }
        } catch (IOException exception) {
            throw new AppException("Không thể đọc file cấu hình ứng dụng.", exception);
        }
        // Bắt buộc phải có trên classpath (src/main/resources khi build bằng Maven).
        throw new AppException("Không tìm thấy file cấu hình trên classpath: " + CONFIG_FILE);
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
        return getProperty("app.name", "Hệ thống quản lý thông tin");
    }
}
