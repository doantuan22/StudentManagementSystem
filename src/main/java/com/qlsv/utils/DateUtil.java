/**
 * Hỗ trợ chuyển đổi và định dạng ngày tháng.
 */
package com.qlsv.utils;

import com.qlsv.exception.ValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateUtil {

    private static final DateTimeFormatter INPUT_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Khởi tạo ngày.
     */
    private DateUtil() {
    }

    /**
     * Chuyển đổi chuỗi văn bản sang LocalDate, ném lỗi nếu chuỗi trống hoặc sai định dạng.
     */
    public static LocalDate parseRequiredDate(String value, String fieldName) {
        String normalizedValue = ValidationUtil.requireNotBlank(
                value,
                fieldName + " khong duoc de trong."
        );
        return parseDate(normalizedValue, fieldName);
    }

    /**
     * Chuyển đổi chuỗi sang LocalDate, trả về null nếu chuỗi trống hoặc không hợp lệ.
     */
    public static LocalDate parseOptionalDate(String value, String fieldName) {
        String normalizedValue = value == null ? "" : value.trim();
        if (normalizedValue.isBlank()) {
            return null;
        }
        return parseDate(normalizedValue, fieldName);
    }

    /**
     * Định dạng đối tượng LocalDate thành chuỗi yyyy-MM-dd để hiển thị trên form.
     */
    public static String formatForInput(LocalDate date) {
        return date == null ? "" : date.format(INPUT_DATE_FORMATTER);
    }

    /**
     * Phân tích ngày.
     */
    private static LocalDate parseDate(String value, String fieldName) {
        try {
            return LocalDate.parse(value, INPUT_DATE_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new ValidationException(fieldName + " phai dung dinh dang yyyy-MM-dd.");
        }
    }
}
