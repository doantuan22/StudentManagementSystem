package com.qlsv.utils;

import javax.swing.JOptionPane;
import java.awt.Component;

public final class DialogUtil {

    private DialogUtil() {
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thong bao", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Loi", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Xac nhan", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }
}
