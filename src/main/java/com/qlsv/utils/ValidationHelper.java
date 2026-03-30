/**
 * Helper class cho validation UI feedback.
 */
package com.qlsv.utils;

import com.qlsv.view.common.AppColors;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.border.Border;
import java.awt.Color;

public final class ValidationHelper {

    private static final Color ERROR_BORDER_COLOR = new Color(220, 38, 38);
    private static final Border ERROR_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ERROR_BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
    );
    private static final Border NORMAL_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColors.INPUT_BORDER),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
    );

    private ValidationHelper() {
    }

    /**
     * Đánh dấu field là lỗi.
     */
    public static void markError(JComponent component) {
        if (component instanceof JTextField) {
            component.setBorder(ERROR_BORDER);
        }
    }

    /**
     * Xóa đánh dấu lỗi.
     */
    public static void clearError(JComponent component) {
        if (component instanceof JTextField) {
            component.setBorder(NORMAL_BORDER);
        }
    }

    /**
     * Kiểm tra field có trống không.
     */
    public static boolean isEmpty(JTextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    /**
     * Validate field bắt buộc.
     */
    public static boolean validateRequired(JTextField field, String fieldName) {
        if (isEmpty(field)) {
            markError(field);
            return false;
        }
        clearError(field);
        return true;
    }

    /**
     * Validate email format.
     */
    public static boolean validateEmail(JTextField field) {
        String email = field.getText();
        if (email == null || email.trim().isEmpty()) {
            return true; // Optional field
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            markError(field);
            return false;
        }
        clearError(field);
        return true;
    }

    /**
     * Validate date format (yyyy-MM-dd).
     */
    public static boolean validateDate(JTextField field) {
        String date = field.getText();
        if (date == null || date.trim().isEmpty()) {
            return true; // Optional field
        }
        if (!date.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            markError(field);
            return false;
        }
        clearError(field);
        return true;
    }
}
