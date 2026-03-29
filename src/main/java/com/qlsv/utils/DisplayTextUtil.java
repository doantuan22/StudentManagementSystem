package com.qlsv.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class DisplayTextUtil {

    private static final String NOT_UPDATED = "Chưa cập nhật";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private DisplayTextUtil() {
    }

    /**
     * Trả về chuỗi mặc định "Chưa cập nhật" nếu giá trị truyền vào là null hoặc rỗng.
     */
    public static String defaultText(String value) {
        return value == null || value.isBlank() ? NOT_UPDATED : value.trim();
    }

    public static String defaultText(Object value) {
        return value == null ? NOT_UPDATED : defaultText(String.valueOf(value));
    }

    /**
     * Định dạng ngày tháng sang chuỗi dd/MM/yyyy.
     */
    public static String formatDate(LocalDate date) {
        return date == null ? NOT_UPDATED : date.format(DATE_FORMATTER);
    }

    /**
     * Định dạng ngày giờ sang chuỗi dd/MM/yyyy HH:mm.
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? NOT_UPDATED : dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * Chuyển đổi mã giới tính sang tiếng Việt để hiển thị.
     */
    public static String formatGender(String gender) {
        if (gender == null || gender.isBlank()) {
            return NOT_UPDATED;
        }
        return switch (gender.trim().toLowerCase(Locale.ROOT)) {
            case "nam" -> "Nam";
            case "nu", "nữ" -> "Nữ";
            case "khac", "khác" -> "Khác";
            default -> gender;
        };
    }

    /**
     * Chuyển đổi mã trạng thái hệ thống sang ngôn ngữ tự nhiên tiếng Việt.
     */
    public static String formatStatus(String status) {
        if (status == null || status.isBlank()) {
            return NOT_UPDATED;
        }
        return switch (status.trim().toUpperCase(Locale.ROOT)) {
            case "ACTIVE" -> "Đang hoạt động";
            case "INACTIVE" -> "Ngừng hoạt động";
            case "REGISTERED" -> "Đã đăng ký";
            case "CANCELLED" -> "Đã hủy";
            case "PENDING" -> "Chờ xử lý";
            case "PASS" -> "Đạt";
            case "FAIL" -> "Chưa đạt";
            default -> status;
        };
    }

    /**
     * Định dạng hiển thị tiết học (ví dụ: "Tiết 1 - 3").
     */
    public static String formatPeriod(Integer startPeriod, Integer endPeriod) {
        if (startPeriod == null || endPeriod == null) {
            return NOT_UPDATED;
        }
        if (startPeriod.equals(endPeriod)) {
            return "Tiết " + startPeriod;
        }
        return "Tiết " + startPeriod + " - " + endPeriod;
    }

    public static String formatUserReference(Long userId) {
        return userId == null ? "Chưa liên kết" : String.valueOf(userId);
    }

    /**
     * Nối các chuỗi văn bản duy nhất lại với nhau bằng dấu phẩy.
     */
    public static String joinUniqueTexts(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return NOT_UPDATED;
        }
        Set<String> normalizedValues = values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return normalizedValues.isEmpty() ? NOT_UPDATED : String.join(", ", normalizedValues);
    }
}
