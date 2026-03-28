package com.qlsv.utils;

import com.qlsv.exception.ValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateUtil {

    private static final DateTimeFormatter INPUT_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private DateUtil() {
    }

    public static LocalDate parseRequiredDate(String value, String fieldName) {
        String normalizedValue = ValidationUtil.requireNotBlank(
                value,
                fieldName + " khong duoc de trong."
        );
        return parseDate(normalizedValue, fieldName);
    }

    public static LocalDate parseOptionalDate(String value, String fieldName) {
        String normalizedValue = value == null ? "" : value.trim();
        if (normalizedValue.isBlank()) {
            return null;
        }
        return parseDate(normalizedValue, fieldName);
    }

    public static String formatForInput(LocalDate date) {
        return date == null ? "" : date.format(INPUT_DATE_FORMATTER);
    }

    private static LocalDate parseDate(String value, String fieldName) {
        try {
            return LocalDate.parse(value, INPUT_DATE_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new ValidationException(fieldName + " phai dung dinh dang yyyy-MM-dd.");
        }
    }
}
