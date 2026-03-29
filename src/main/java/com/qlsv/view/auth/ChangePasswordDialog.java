/**
 * Hộp thoại đổi mật khẩu xác thực.
 */
package com.qlsv.view.auth;

import com.qlsv.view.common.AppColors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Window;

public class ChangePasswordDialog extends JDialog {

    private final boolean requireCurrentPassword;
    private final JPasswordField currentPasswordField = new JPasswordField(22);
    private final JPasswordField newPasswordField = new JPasswordField(22);
    private final JPasswordField confirmPasswordField = new JPasswordField(22);

    private PasswordChangeRequest result;

    /**
     * Khởi tạo đổi mật khẩu.
     */
    private ChangePasswordDialog(Component parent, String title, boolean requireCurrentPassword) {
        super(resolveOwner(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
        this.requireCurrentPassword = requireCurrentPassword;
        initComponents();
    }

    /**
     * Hiển thị hộp thoại cho phép người dùng tự đổi mật khẩu của chính mình (yêu cầu mật khẩu cũ).
     */
    public static PasswordChangeRequest showSelfChangeDialog(Component parent, String title) {
        ChangePasswordDialog dialog = new ChangePasswordDialog(parent, title, true);
        dialog.setVisible(true);
        return dialog.result;
    }

    /**
     * Hiển thị hộp thoại cho phép quản trị viên đặt lại mật khẩu của người dùng khác (không yêu cầu mật khẩu cũ).
     */
    public static PasswordChangeRequest showAdminResetDialog(Component parent, String title) {
        ChangePasswordDialog dialog = new ChangePasswordDialog(parent, title, false);
        dialog.setVisible(true);
        return dialog.result;
    }

    /**
     * Khởi tạo và sắp xếp các thành phần của hộp thoại đổi mật khẩu.
     */
    private void initComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 16));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        contentPanel.setBackground(AppColors.CARD_BACKGROUND);

        JLabel noteLabel = new JLabel(requireCurrentPassword
                ? "Vui lòng nhập đúng mật khẩu hiện tại trước khi đổi."
                : "Admin đặt lại mật khẩu cho tài khoản được chọn.");
        noteLabel.setForeground(AppColors.CARD_MUTED_TEXT);

        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        if (requireCurrentPassword) {
            formPanel.add(createPasswordField("Mật khẩu cũ", currentPasswordField));
            formPanel.add(Box.createVerticalStrut(12));
        }
        formPanel.add(createPasswordField("Mật khẩu mới", newPasswordField));
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(createPasswordField("Nhập lại mật khẩu mới", confirmPasswordField));

        JButton confirmButton = new JButton("Xác nhận");
        styleButton(confirmButton, AppColors.BUTTON_SUCCESS);
        confirmButton.addActionListener(event -> handleConfirm());

        JButton cancelButton = new JButton("Hủy");
        styleButton(cancelButton, AppColors.BUTTON_NEUTRAL);
        cancelButton.addActionListener(event -> {
            result = null;
            dispose();
        });

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footerPanel.setOpaque(false);
        footerPanel.add(cancelButton);
        footerPanel.add(confirmButton);

        contentPanel.add(noteLabel, BorderLayout.NORTH);
        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(footerPanel, BorderLayout.SOUTH);
        setContentPane(contentPanel);

        pack();
        setResizable(false);
        setLocationRelativeTo(getOwner());
        SwingUtilities.invokeLater(() -> {
            if (requireCurrentPassword) {
                currentPasswordField.requestFocusInWindow();
            } else {
                newPasswordField.requestFocusInWindow();
            }
        });
    }

    /**
     * Tạo trường mật khẩu.
     */
    private JPanel createPasswordField(String label, JPasswordField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.INPUT_BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel(label);
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Áp dụng kiểu cho nút.
     */
    private void styleButton(JButton button, java.awt.Color background) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(background);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
    }

    /**
     * Thu thập dữ liệu từ các ô nhập liệu và đóng hộp thoại để trả về kết quả.
     */
    private void handleConfirm() {
        result = new PasswordChangeRequest(
                /**
                 * Xử lý string.
                 */
                requireCurrentPassword ? new String(currentPasswordField.getPassword()) : "",
                new String(newPasswordField.getPassword()),
                new String(confirmPasswordField.getPassword())
        );
        dispose();
    }

    /**
     * Xác định owner.
     */
    private static Window resolveOwner(Component parent) {
        if (parent == null) {
            return null;
        }
        return SwingUtilities.getWindowAncestor(parent);
    }

    /**
     * Xử lý yêu cầu đổi mật khẩu.
     */
    public record PasswordChangeRequest(String currentPassword, String newPassword, String confirmPassword) {
    }
}
