package com.qlsv.view.lecturer;

import com.qlsv.controller.LecturerController;
import com.qlsv.model.Lecturer;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BasePanel;
import com.qlsv.utils.ValidationUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class LecturerProfilePanel extends BasePanel {

    private final LecturerController lecturerController = new LecturerController();
    private final JPanel container = new JPanel(new BorderLayout());
    private final JPanel profileFieldsPanel = new JPanel(new GridLayout(0, 1, 15, 15));
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField addressField;
    private boolean isEditing = false;
    private Lecturer currentLecturer;

    public LecturerProfilePanel() {
        container.setOpaque(false);
        profileFieldsPanel.setOpaque(false);
        container.add(profileFieldsPanel, BorderLayout.NORTH);

        // Them JScrollPane de noi dung khong bi che khuat khi cua so nho
        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
        reloadData();
    }

    @Override
    public void reloadData() {
        try {
            currentLecturer = lecturerController.getCurrentLecturer();
            renderProfile();
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void renderProfile() {
        profileFieldsPanel.removeAll();

        JPanel profileCard = new JPanel(new BorderLayout(0, 20));
        profileCard.setBackground(AppColors.CARD_BACKGROUND);
        profileCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)
        ));

        // Header panel voi tieu de va nut bam
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Hồ sơ giảng viên");
        titleLabel.setFont(titleLabel.getFont().deriveFont(java.awt.Font.BOLD, 20f));
        titleLabel.setForeground(AppColors.CARD_TITLE_TEXT);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        if (!isEditing) {
            JButton editButton = new JButton("Sửa thông tin");
            styleFilledButton(editButton, AppColors.BUTTON_WARNING); // Dong bo mau vang voi nut Sua cua ADMIN
            editButton.addActionListener(e -> {
                isEditing = true;
                renderProfile();
            });
            buttonPanel.add(editButton);
        } else {
            JButton saveButton = new JButton("Lưu thay đổi");
            styleFilledButton(saveButton, AppColors.BUTTON_SUCCESS); // Nut Luu mau xanh
            saveButton.addActionListener(e -> handleSave());

            JButton cancelButton = new JButton("Hủy");
            styleFilledButton(cancelButton, AppColors.BUTTON_NEUTRAL); // Nut Huy mau xam
            cancelButton.addActionListener(e -> {
                isEditing = false;
                renderProfile();
            });
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
        }
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        profileCard.add(headerPanel, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridLayout(0, 2, 20, 15));
        fieldsPanel.setOpaque(false);

        addProfileField(fieldsPanel, "Mã giảng viên", DisplayTextUtil.defaultText(currentLecturer.getLecturerCode()), false);
        addProfileField(fieldsPanel, "Họ và tên", DisplayTextUtil.defaultText(currentLecturer.getFullName()), false);

        if (isEditing) {
            emailField = new JTextField(currentLecturer.getEmail());
            phoneField = new JTextField(currentLecturer.getPhone());
            addressField = new JTextField(currentLecturer.getAddress());

            // Style cho cac o nhap lieu
            styleTextField(emailField);
            styleTextField(phoneField);
            styleTextField(addressField);

            addFieldWithComponent(fieldsPanel, "Email", emailField);
            addFieldWithComponent(fieldsPanel, "Số điện thoại", phoneField);
            addFieldWithComponent(fieldsPanel, "Địa chỉ", addressField);
        } else {
            addProfileField(fieldsPanel, "Email", DisplayTextUtil.defaultText(currentLecturer.getEmail()), false);
            addProfileField(fieldsPanel, "Số điện thoại", DisplayTextUtil.defaultText(currentLecturer.getPhone()), false);
            addProfileField(fieldsPanel, "Địa chỉ", DisplayTextUtil.defaultText(currentLecturer.getAddress()), false);
        }

        addProfileField(fieldsPanel, "Khoa", currentLecturer.getFaculty() != null ? currentLecturer.getFaculty().getFacultyName() : "Chưa cập nhật", false);

        profileCard.add(fieldsPanel, BorderLayout.CENTER);
        profileFieldsPanel.add(profileCard);

        profileFieldsPanel.revalidate();
        profileFieldsPanel.repaint();
    }

    private void addFieldWithComponent(JPanel panel, String label, JTextField component) {
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(labelComp.getFont().deriveFont(java.awt.Font.BOLD, 13f));
        labelComp.setForeground(AppColors.CARD_MUTED_TEXT);
        
        JPanel itemPanel = new JPanel(new BorderLayout(0, 5));
        itemPanel.setOpaque(false);
        itemPanel.add(labelComp, BorderLayout.NORTH);
        itemPanel.add(component, BorderLayout.CENTER);
        
        panel.add(itemPanel);
    }

    private void styleTextField(JTextField textField) {
        textField.setPreferredSize(new java.awt.Dimension(0, 36));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.INPUT_BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void styleFilledButton(JButton button, java.awt.Color background) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(background);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        button.setFont(button.getFont().deriveFont(java.awt.Font.BOLD));
    }

    private void handleSave() {
        try {
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();

            ValidationUtil.requireEmail(email, "Email");
            ValidationUtil.requirePhone(phone, "Số điện thoại");
            ValidationUtil.requireWithinLength(address, 255, "Địa chỉ");

            currentLecturer.setEmail(email);
            currentLecturer.setPhone(phone);
            currentLecturer.setAddress(address);

            lecturerController.saveLecturer(currentLecturer);
            DialogUtil.showInfo(this, "Cập nhật thông tin thành công!");
            isEditing = false;
            renderProfile();
        } catch (Exception e) {
            DialogUtil.showError(this, e.getMessage());
        }
    }

    private void addProfileField(JPanel panel, String label, String value, boolean isBold) {
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(labelComp.getFont().deriveFont(java.awt.Font.BOLD, 13f));
        labelComp.setForeground(AppColors.CARD_MUTED_TEXT);

        JLabel valueComp = new JLabel(value);
        valueComp.setForeground(AppColors.CARD_VALUE_TEXT);
        valueComp.setFont(valueComp.getFont().deriveFont(14f));
        if (isBold || !isEditing) {
            valueComp.setFont(valueComp.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        }

        JPanel itemPanel = new JPanel(new BorderLayout(0, 5));
        itemPanel.setOpaque(false);
        itemPanel.add(labelComp, BorderLayout.NORTH);
        itemPanel.add(valueComp, BorderLayout.CENTER);

        panel.add(itemPanel);
    }
}
