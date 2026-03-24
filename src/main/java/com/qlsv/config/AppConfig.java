package com.qlsv.config;

import com.qlsv.exception.AppException;

import java.io.IOException;
import java.io.InputStream;
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
            if (inputStream == null) {
                throw new AppException("Khong tim thay file cau hinh " + CONFIG_FILE);
            }
            properties.load(inputStream);
            return properties;
        } catch (IOException exception) {
            throw new AppException("Khong the doc file cau hinh ung dung.", exception);
        }
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
            throw new AppException("Thieu cau hinh bat buoc: " + key);
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
        return getProperty("app.name", "Student Management System");
    }
}
