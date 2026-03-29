package com.qlsv.utils;

import com.qlsv.exception.ValidationException;

import java.util.Locale;
import java.util.regex.Pattern;

public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^(\\+84|0)[0-9]{9,10}$");

    private ValidationUtil() {
    }

    public static String requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
        return value.trim();
    }

    public static Integer requirePositive(Integer value, String message) {
        if (value == null || value <= 0) {
            throw new ValidationException(message);
        }
        return value;
    }

    public static Double defaultScore(Double value) {
        return value == null ? 0.0 : value;
    }

    public static void requireScoreRange(Double value, String fieldName) {
        if (value == null) {
            return;
        }
        if (value < 0 || value > 10) {
            throw new ValidationException(fieldName + " phải nằm trong khoảng từ 0 đến 10.");
        }
    }

    public static String requireEmail(String value, String fieldName) {
        String normalizedValue = requireNotBlank(value, fieldName + " không được để trống.");
        if (!EMAIL_PATTERN.matcher(normalizedValue).matches()) {
            throw new ValidationException(fieldName + " không đúng định dạng.");
        }
        return normalizedValue;
    }

    public static String requirePhone(String value, String fieldName) {
        String normalizedValue = requireNotBlank(value, fieldName + " không được để trống.");
        String compactValue = normalizedValue.replace(" ", "");
        if (!PHONE_PATTERN.matcher(compactValue).matches()) {
            throw new ValidationException(fieldName + " không đúng định dạng.");
        }
        return compactValue;
    }

    public static String requireWithinLength(String value, int maxLength, String fieldName) {
        String normalizedValue = requireNotBlank(value, fieldName + " không được để trống.");
        if (normalizedValue.length() > maxLength) {
            throw new ValidationException(fieldName + " không được vượt quá " + maxLength + " ký tự.");
        }
        return normalizedValue;
    }
    public static String normalizeCodePrefix(String value, String expectedPrefix, String fieldName) {
        String normalizedValue = requireWithinLength(value, 50, fieldName);
        if (expectedPrefix == null || expectedPrefix.isBlank()) {
            return normalizedValue;
        }

        String trimmedPrefix = expectedPrefix.trim();
        if (normalizedValue.length() < trimmedPrefix.length()) {
            return normalizedValue;
        }

        String actualPrefix = normalizedValue.substring(0, trimmedPrefix.length());
        if (!actualPrefix.equalsIgnoreCase(trimmedPrefix)) {
            return normalizedValue;
        }

        return trimmedPrefix.toUpperCase(Locale.ROOT) + normalizedValue.substring(trimmedPrefix.length());
    }
}
