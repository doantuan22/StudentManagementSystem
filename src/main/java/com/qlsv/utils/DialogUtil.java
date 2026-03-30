/**
 * Hiển thị thông báo và xác nhận trên giao diện.
 */
package com.qlsv.utils;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

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
     * Hiển thị error với text area cho message dài.
     */
    public static void showError(Component parent, String message) {
        // Nếu message ngắn (< 150 chars và không có newline nhiều), dùng dialog thông thường
        if (message.length() < 150 && message.split("\n").length <= 3) {
            JOptionPane.showMessageDialog(parent, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Nếu message dài hoặc có nhiều dòng, dùng JTextArea với scroll
        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Dialog", Font.PLAIN, 13));
        textArea.setBackground(null);
        textArea.setBorder(null);
        
        // Tính toán kích thước phù hợp
        int lines = message.split("\n").length;
        int rows = Math.min(lines + 1, 15); // Tối đa 15 dòng
        textArea.setRows(rows);
        textArea.setColumns(50);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, Math.min(rows * 25 + 20, 400)));
        scrollPane.setBorder(null);
        
        JOptionPane.showMessageDialog(
            parent, 
            scrollPane, 
            "Lỗi", 
            JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Xử lý xác nhận.
     */
    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Xác nhận", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }
}
