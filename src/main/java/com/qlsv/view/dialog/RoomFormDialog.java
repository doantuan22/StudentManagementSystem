/**
 * Hộp thoại phòng biểu mẫu dialog.
 */
package com.qlsv.view.dialog;

import com.qlsv.view.common.AppColors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;

public class RoomFormDialog extends JDialog {

    private final JTextField roomCodeField = new JTextField();
    private final JTextField roomNameField = new JTextField();

    private RoomFormResult result;

    /**
     * Khởi tạo phòng biểu mẫu.
     */
    private RoomFormDialog(Component parent, RoomFormModel model) {
        super(resolveOwner(parent), model.title(), Dialog.ModalityType.APPLICATION_MODAL);
        initComponents(model);
    }

    /**
     * Hiển thị hộp thoại.
     */
    public static RoomFormResult showDialog(Component parent, RoomFormModel model) {
        RoomFormDialog dialog = new RoomFormDialog(parent, model);
        dialog.setVisible(true);
        return dialog.result;
    }

    /**
     * Khởi tạo các thành phần giao diện.
     */
    private void initComponents(RoomFormModel model) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(AppColors.CARD_BACKGROUND);

        JPanel headerPanel = new JPanel(new BorderLayout(0, 6));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 0, 24));

        JLabel titleLabel = new JLabel(model.title());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);


        headerPanel.add(titleLabel, BorderLayout.NORTH);


        JPanel bodyPanel = new JPanel();
        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(16, 24, 20, 24));
        bodyPanel.setLayout(new javax.swing.BoxLayout(bodyPanel, javax.swing.BoxLayout.Y_AXIS));
        bodyPanel.add(createField("Mã phòng", styleTextField(roomCodeField)));
        bodyPanel.add(javax.swing.Box.createVerticalStrut(12));
        bodyPanel.add(createField("Tên phòng", styleTextField(roomNameField)));

        JButton cancelButton = new JButton("Hủy");
        styleSecondaryButton(cancelButton);
        cancelButton.addActionListener(event -> {
            result = null;
            dispose();
        });

        JButton saveButton = new JButton("Lưu");
        stylePrimaryButton(saveButton);
        saveButton.addActionListener(event -> handleSave());

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 20, 24));
        footerPanel.add(cancelButton);
        footerPanel.add(saveButton);

        rootPanel.add(headerPanel, BorderLayout.NORTH);
        rootPanel.add(bodyPanel, BorderLayout.CENTER);
        rootPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
        roomCodeField.setText(model.roomCode());
        roomNameField.setText(model.roomName());

        getRootPane().setDefaultButton(saveButton);
        setMinimumSize(new Dimension(580, 320));
        setSize(new Dimension(620, 340));
        setLocationRelativeTo(getOwner());
        SwingUtilities.invokeLater(() -> roomCodeField.requestFocusInWindow());
    }

    /**
     * Tạo trường.
     */
    private JPanel createField(String labelText, Component inputComponent) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12.5f));
        label.setForeground(AppColors.CARD_TITLE_TEXT);
        panel.add(label, BorderLayout.NORTH);
        panel.add(inputComponent, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Áp dụng kiểu cho trường văn bản.
     */
    private JTextField styleTextField(JTextField textField) {
        textField.setFont(textField.getFont().deriveFont(Font.PLAIN, 13.5f));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.INPUT_BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        return textField;
    }

    /**
     * Áp dụng kiểu cho nút primary.
     */
    private void stylePrimaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(AppColors.BUTTON_PRIMARY);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
    }

    /**
     * Áp dụng kiểu cho nút secondary.
     */
    private void styleSecondaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(AppColors.BUTTON_NEUTRAL);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
    }

    /**
     * Xử lý lưu.
     */
    private void handleSave() {
        result = new RoomFormResult(roomCodeField.getText(), roomNameField.getText());
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
     * Xử lý model phòng biểu mẫu.
     */
    public record RoomFormModel(String title, String roomCode, String roomName) {
    }

    /**
     * Xử lý phòng kết quả biểu mẫu.
     */
    public record RoomFormResult(String roomCode, String roomName) {
    }
}
