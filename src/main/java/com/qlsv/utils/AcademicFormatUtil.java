package com.qlsv.utils;

import com.qlsv.exception.ValidationException;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AcademicFormatUtil {

    private static final Pattern ACADEMIC_YEAR_PATTERN =
            Pattern.compile("^(\\d{4})(?:\\s*-\\s*|\\s+)(\\d{4})$");
    private static final List<String> FIXED_SEMESTERS = List.of("HK1", "HK2", "HK3");

    private AcademicFormatUtil() {
    }

    /**
     * Chuẩn hóa niên khóa về định dạng "yyyy - yyyy" và ném lỗi nếu không hợp lệ.
     */
    public static String normalizeAcademicYear(String value, String fieldName) {
        String normalizedValue = tryNormalizeAcademicYear(value);
        if (normalizedValue == null) {
            throw new ValidationException(fieldName + " phải có định dạng yyyy - yyyy và năm sau phải lớn hơn năm trước.");
        }
        return normalizedValue;
    }

    /**
     * Thử chuẩn hóa chuỗi niên khóa, trả về null nếu định dạng không đúng hoặc năm kết thúc không lớn hơn năm bắt đầu.
     */
    public static String tryNormalizeAcademicYear(String value) {
        if (value == null) {
            return null;
        }

        String compactValue = value.trim()
                .replace('\u2013', '-')
                .replace('\u2014', '-')
                .replaceAll("\\s+", " ");
        if (compactValue.isBlank()) {
            return null;
        }

        Matcher matcher = ACADEMIC_YEAR_PATTERN.matcher(compactValue);
        if (!matcher.matches()) {
            return null;
        }

        int startYear = Integer.parseInt(matcher.group(1));
        int endYear = Integer.parseInt(matcher.group(2));
        if (endYear <= startYear) {
            return null;
        }

        return String.format("%04d - %04d", startYear, endYear);
    }

    /**
     * Định dạng lại chuỗi niên khóa để hiển thị, giữ nguyên nếu không chuẩn hóa được.
     */
    public static String formatAcademicYear(String value) {
        String normalizedValue = tryNormalizeAcademicYear(value);
        if (normalizedValue != null) {
            return normalizedValue;
        }
        return value == null ? "" : value.trim();
    }

    /**
     * So sánh hai chuỗi niên khóa dựa trên giá trị đã được chuẩn hóa.
     */
    public static boolean academicYearsEqual(String left, String right) {
        String normalizedLeft = normalizeAcademicYearForCompare(left);
        String normalizedRight = normalizeAcademicYearForCompare(right);
        return !normalizedLeft.isBlank() && normalizedLeft.equalsIgnoreCase(normalizedRight);
    }

    /**
     * Chuẩn hóa tên học kỳ (HK1, HK2, HK3) và ném lỗi nếu giá trị không nằm trong danh sách cho phép.
     */
    public static String normalizeSemester(String value, String fieldName) {
        String normalizedValue = tryNormalizeSemester(value);
        if (normalizedValue == null) {
            throw new ValidationException(fieldName + " chỉ được phép chọn HK1, HK2 hoặc HK3.");
        }
        return normalizedValue;
    }

    /**
     * Thử chuẩn hóa tên học kỳ về dạng viết hoa không khoảng trắng.
     */
    public static String tryNormalizeSemester(String value) {
        if (value == null) {
            return null;
        }

        String compactValue = value.trim()
                .replaceAll("\\s+", "")
                .toUpperCase(Locale.ROOT);
        if (FIXED_SEMESTERS.contains(compactValue)) {
            return compactValue;
        }
        return null;
    }

    /**
     * Định dạng tên học kỳ để hiển thị đồng nhất trên giao diện.
     */
    public static String formatSemester(String value) {
        String normalizedValue = tryNormalizeSemester(value);
        if (normalizedValue != null) {
            return normalizedValue;
        }
        return value == null ? "" : value.trim();
    }

    public static List<String> getFixedSemesters() {
        return FIXED_SEMESTERS;
    }

    private static String normalizeAcademicYearForCompare(String value) {
        String normalizedValue = tryNormalizeAcademicYear(value);
        if (normalizedValue != null) {
            return normalizedValue;
        }
        return value == null ? "" : value.trim();
    }
}
