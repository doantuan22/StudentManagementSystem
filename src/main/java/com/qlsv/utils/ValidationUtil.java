package com.qlsv.utils;

import com.qlsv.exception.ValidationException;

public final class ValidationUtil {

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
            throw new ValidationException(fieldName + " phai nam trong khoang 0 den 10.");
        }
    }
}
