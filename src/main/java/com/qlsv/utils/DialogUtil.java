/**
 * Hiển thị thông báo và xác nhận trên giao diện.
 */
package com.qlsv.utils;

import javax.swing.JOptionPane;
import java.awt.Component;

public final class DialogUtil {

    /**
     * Khởi tạo dữ liệu.
     */
    private DialogUtil() {
    }

    /**
     * Hiển thị thông tin.
     */
    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Hiển thị error.
     */
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Xử lý xác nhận.
     */
    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Xác nhận", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }
}
