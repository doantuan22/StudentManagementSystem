/**
 * Kiểm tra và chuẩn hóa dữ liệu đầu vào.
 */
package com.qlsv.utils;

import com.qlsv.exception.ValidationException;

import java.util.Locale;
import java.util.regex.Pattern;

public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^(\\+84|0)[0-9]{9,10}$");

    /**
     * Khởi tạo kiểm tra.
     */
    private ValidationUtil() {
    }

    /**
     * Kiểm tra và đảm bảo chuỗi không được để trống, trả về chuỗi đã cắt khoảng trắng.
     */
    public static String requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
        return value.trim();
    }

    /**
     * Đảm bảo giá trị số nguyên phải là số dương.
     */
    public static Integer requirePositive(Integer value, String message) {
        if (value == null || value <= 0) {
            throw new ValidationException(message);
        }
        return value;
    }

    /**
     * Xử lý điểm mặc định.
     */
    public static Double defaultScore(Double value) {
        return value == null ? 0.0 : value;
    }

    /**
     * Kiểm tra điểm số phải nằm trong thang điểm từ 0 đến 10.
     */
    public static void requireScoreRange(Double value, String fieldName) {
        if (value == null) {
            return;
        }
        if (value < 0 || value > 10) {
            throw new ValidationException(fieldName + " phải nằm trong khoảng từ 0 đến 10.");
        }
    }

    /**
     * Kiểm tra và đảm bảo chuỗi nhập vào đúng định dạng email.
     */
    public static String requireEmail(String value, String fieldName) {
        String normalizedValue = requireNotBlank(value, fieldName + " không được để trống.");
        if (!EMAIL_PATTERN.matcher(normalizedValue).matches()) {
            throw new ValidationException(fieldName + " không đúng định dạng.");
        }
        return normalizedValue;
    }

    /**
     * Kiểm tra và đảm bảo chuỗi nhập vào đúng định dạng số điện thoại Việt Nam.
     */
    public static String requirePhone(String value, String fieldName) {
        String normalizedValue = requireNotBlank(value, fieldName + " không được để trống.");
        String compactValue = normalizedValue.replace(" ", "");
        if (!PHONE_PATTERN.matcher(compactValue).matches()) {
            throw new ValidationException(fieldName + " không đúng định dạng.");
        }
        return compactValue;
    }

    /**
     * Kiểm tra độ dài tối đa của chuỗi ký tự.
     */
    public static String requireWithinLength(String value, int maxLength, String fieldName) {
        String normalizedValue = requireNotBlank(value, fieldName + " không được để trống.");
        if (normalizedValue.length() > maxLength) {
            throw new ValidationException(fieldName + " không được vượt quá " + maxLength + " ký tự.");
        }
        return normalizedValue;
    }
    /**
     * Chuẩn hóa tiền tố của mã (ví dụ: SV, GV) về định dạng viết hoa.
     */
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
