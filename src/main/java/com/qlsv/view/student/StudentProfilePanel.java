package com.qlsv.view.student;

import com.qlsv.controller.StudentProfileScreenController;
import com.qlsv.controller.UserController;
import com.qlsv.dto.StudentProfileDto;
import com.qlsv.utils.DialogUtil;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.view.auth.ChangePasswordDialog;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BasePanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

public class StudentProfilePanel extends BasePanel {

    private static final Color HERO_BACKGROUND = new Color(232, 240, 255);
    private static final Color ACADEMIC_CARD_BACKGROUND = new Color(255, 255, 255);
    private static final Color PERSONAL_CARD_BACKGROUND = new Color(241, 248, 255);
    private static final Color CONTACT_CARD_BACKGROUND = new Color(239, 252, 245);
    private static final int CARD_GAP = 16;
    private static final int SECTION_GAP = 12;
    private static final int INPUT_HEIGHT = 38;
    private static final int TEXT_AREA_HEIGHT = 96;

    private final StudentProfileScreenController screenController = new StudentProfileScreenController();
    private final UserController userController = new UserController();
    private final JPanel contentPanel = new JPanel();

    private StudentProfileDto currentStudent;
    private boolean isEditing;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextArea addressArea;

    public StudentProfilePanel() {
        setBackground(AppColors.CONTENT_BACKGROUND);
        initComponents();
        reloadData();
    }

    private void initComponents() {
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 24, 24));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void reloadData() {
        try {
            currentStudent = screenController.loadCurrentStudent();
            isEditing = false;
            renderProfile();
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void renderProfile() {
        contentPanel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, CARD_GAP, 0);
        contentPanel.add(buildHeroCard(), gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        contentPanel.add(buildSectionGrid(), gbc);

        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        contentPanel.add(filler, gbc);

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel buildHeroCard() {
        JPanel card = createCard(HERO_BACKGROUND);
        card.add(buildHeroHeader(), BorderLayout.NORTH);
        card.add(buildSummaryPanel(), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildHeroHeader() {
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel eyebrowLabel = new JLabel("HỒ SƠ SINH VIÊN");
        eyebrowLabel.setFont(eyebrowLabel.getFont().deriveFont(Font.BOLD, 12f));
        eyebrowLabel.setForeground(new Color(37, 99, 235));

        JLabel titleLabel = new JLabel("Thông tin cá nhân");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 26f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        JLabel subtitleLabel = createNoteLabel(
                "Theo dõi thông tin cơ bản và cập nhật ngay email, số điện thoại, địa chỉ liên hệ khi cần."
        );

        titlePanel.add(eyebrowLabel);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(8));
        titlePanel.add(subtitleLabel);

        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        headerPanel.add(titlePanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.insets = new Insets(0, CARD_GAP, 0, 0);
        headerPanel.add(buildActionPanel(), gbc);

        return headerPanel;
    }

    private JPanel buildSummaryPanel() {
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, CARD_GAP, 0));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(CARD_GAP, 0, 0, 0));

        summaryPanel.add(createBadge("MSSV", currentStudent.studentCode()));
        summaryPanel.add(createBadge("Lớp", currentStudent.classRoomName()));
        summaryPanel.add(createBadge("Khoa", currentStudent.facultyName()));
        summaryPanel.add(createBadge("Trạng thái", currentStudent.statusText()));
        return summaryPanel;
    }

    private JPanel buildActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionPanel.setOpaque(false);

        if (!isEditing) {
            JButton changePasswordButton = new JButton("Đổi MK");
            styleFilledButton(changePasswordButton, AppColors.BUTTON_PRIMARY);
            changePasswordButton.addActionListener(event -> openChangePasswordDialog());

            JButton editButton = new JButton("Cập nhật thông tin");
            styleFilledButton(editButton, AppColors.BUTTON_WARNING);
            editButton.addActionListener(event -> {
                isEditing = true;
                renderProfile();
            });

            JButton reloadButton = new JButton("Tải lại");
            styleFilledButton(reloadButton, AppColors.BUTTON_NEUTRAL);
            reloadButton.addActionListener(event -> reloadData());

            actionPanel.add(changePasswordButton);
            actionPanel.add(editButton);
            actionPanel.add(reloadButton);
        }
        return actionPanel;
    }

    private JPanel buildSectionGrid() {
        JPanel sectionsPanel = new JPanel(new GridLayout(1, 3, CARD_GAP, 0));
        sectionsPanel.setOpaque(false);
        sectionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sectionsPanel.add(buildIdentityCard());
        sectionsPanel.add(buildAcademicCard());
        sectionsPanel.add(buildContactCard());
        return sectionsPanel;
    }

    private JPanel buildAcademicCard() {
        JPanel card = createSectionCard(
                "Thông tin học tập",
                "Đào tạo & chuyên ngành",
                ACADEMIC_CARD_BACKGROUND
        );

        JPanel fieldsPanel = createFieldsPanel();
        fieldsPanel.add(createReadOnlyField("Mã số sinh viên", currentStudent.studentCode()));
        fieldsPanel.add(Box.createVerticalStrut(SECTION_GAP));
        fieldsPanel.add(createReadOnlyField("Lớp học", currentStudent.classRoomName()));
        fieldsPanel.add(Box.createVerticalStrut(SECTION_GAP));
        fieldsPanel.add(createReadOnlyField("Khoa / Viện", currentStudent.facultyName()));
        fieldsPanel.add(Box.createVerticalStrut(SECTION_GAP));
        fieldsPanel.add(createReadOnlyField("Niên khóa", currentStudent.academicYear()));

        card.add(fieldsPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildIdentityCard() {
        JPanel card = createSectionCard(
                "Thông tin nhân thân",
                "Dữ liệu định danh",
                PERSONAL_CARD_BACKGROUND
        );

        JPanel fieldsPanel = createFieldsPanel();
        fieldsPanel.add(createReadOnlyField("Họ và tên", currentStudent.fullName()));
        fieldsPanel.add(Box.createVerticalStrut(SECTION_GAP));
        fieldsPanel.add(createReadOnlyField("Giới tính", currentStudent.genderText()));
        fieldsPanel.add(Box.createVerticalStrut(SECTION_GAP));
        fieldsPanel.add(createReadOnlyField("Ngày sinh", currentStudent.dateOfBirthText()));

        card.add(fieldsPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildContactCard() {
        JPanel card = createSectionCard(
                "Thông tin liên hệ",
                "Liên lạc & thường trú",
                CONTACT_CARD_BACKGROUND
        );

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (isEditing) {
            emailField = createTextField(currentStudent.email());
            phoneField = createTextField(currentStudent.phone());
            addressArea = createTextArea(currentStudent.address());

            content.add(createInputField("Email liên lạc", emailField));
            content.add(Box.createVerticalStrut(SECTION_GAP));
            content.add(createInputField("Số điện thoại", phoneField));
            content.add(Box.createVerticalStrut(SECTION_GAP));
            content.add(createInputField("Địa chỉ thường trú", createTextAreaScrollPane(addressArea)));
            content.add(Box.createVerticalStrut(SECTION_GAP));

            JPanel actionFooter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            actionFooter.setOpaque(false);

            JButton saveButton = new JButton("Lưu");
            styleFilledButton(saveButton, AppColors.BUTTON_SUCCESS);
            saveButton.addActionListener(event -> handleSave());

            JButton cancelButton = new JButton("Hủy");
            styleFilledButton(cancelButton, AppColors.BUTTON_NEUTRAL);
            cancelButton.addActionListener(event -> {
                isEditing = false;
                renderProfile();
            });

            actionFooter.add(saveButton);
            actionFooter.add(cancelButton);
            content.add(actionFooter);
        } else {
            content.add(createReadOnlyField("Email liên lạc", currentStudent.email()));
            content.add(Box.createVerticalStrut(SECTION_GAP));
            content.add(createReadOnlyField("Số điện thoại", currentStudent.phone()));
            content.add(Box.createVerticalStrut(SECTION_GAP));
            content.add(createReadOnlyField("Địa chỉ thường trú", currentStudent.address()));
        }

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createSectionCard(String title, String subtitle, Color background) {
        JPanel card = createCard(background);

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        JLabel subtitleLabel = createNoteLabel(subtitle);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(subtitleLabel);

        card.add(headerPanel, BorderLayout.NORTH);
        return card;
    }

    private JPanel createCard(Color background) {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setOpaque(true);
        card.setBackground(background);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER, 1, true),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private JPanel createFieldsPanel() {
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setOpaque(false);
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return fieldsPanel;
    }

    private JPanel createReadOnlyField(String label, String value) {
        JPanel fieldPanel = new JPanel(new BorderLayout(0, 4));
        fieldPanel.setOpaque(false);
        fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD, 12f));
        labelComponent.setForeground(AppColors.CARD_MUTED_TEXT);

        JLabel valueComponent = new JLabel(toHtml(DisplayTextUtil.defaultText(value)));
        valueComponent.setFont(valueComponent.getFont().deriveFont(Font.BOLD, 14f));
        valueComponent.setForeground(AppColors.CARD_VALUE_TEXT);
        valueComponent.setVerticalAlignment(SwingConstants.TOP);

        fieldPanel.add(labelComponent, BorderLayout.NORTH);
        fieldPanel.add(valueComponent, BorderLayout.CENTER);
        return fieldPanel;
    }

    private JPanel createInputField(String label, Component inputComponent) {
        JPanel fieldPanel = new JPanel(new BorderLayout(0, 6));
        fieldPanel.setOpaque(false);
        fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD, 12.5f));
        labelComponent.setForeground(AppColors.CARD_MUTED_TEXT);

        fieldPanel.add(labelComponent, BorderLayout.NORTH);
        fieldPanel.add(inputComponent, BorderLayout.CENTER);
        return fieldPanel;
    }

    private JPanel createBadge(String title, String value) {
        JPanel badgePanel = new JPanel(new BorderLayout(0, 4));
        badgePanel.setOpaque(true);
        badgePanel.setBackground(Color.WHITE);
        badgePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 11f));
        titleLabel.setForeground(AppColors.CARD_MUTED_TEXT);

        JLabel valueLabel = new JLabel(toHtml(DisplayTextUtil.defaultText(value)));
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 15f));
        valueLabel.setForeground(AppColors.CARD_VALUE_TEXT);
        valueLabel.setVerticalAlignment(SwingConstants.TOP);

        badgePanel.add(titleLabel, BorderLayout.NORTH);
        badgePanel.add(valueLabel, BorderLayout.CENTER);
        return badgePanel;
    }

    private JLabel createNoteLabel(String text) {
        JLabel label = new JLabel(toHtml(text));
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 13f));
        label.setForeground(AppColors.CARD_MUTED_TEXT);
        return label;
    }

    private JTextField createTextField(String value) {
        JTextField textField = new JTextField(value == null ? "" : value);
        textField.setMinimumSize(new Dimension(140, INPUT_HEIGHT));
        textField.setPreferredSize(new Dimension(220, INPUT_HEIGHT));
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, INPUT_HEIGHT));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.INPUT_BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return textField;
    }

    private JTextArea createTextArea(String value) {
        JTextArea textArea = new JTextArea(value == null ? "" : value, 4, 20);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(textArea.getFont().deriveFont(Font.PLAIN, 13f));
        textArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        return textArea;
    }

    private JScrollPane createTextAreaScrollPane(JTextArea textArea) {
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.INPUT_BORDER));
        scrollPane.setMinimumSize(new Dimension(140, TEXT_AREA_HEIGHT));
        scrollPane.setPreferredSize(new Dimension(220, TEXT_AREA_HEIGHT));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, TEXT_AREA_HEIGHT));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private void handleSave() {
        try {
            currentStudent = screenController.updateCurrentStudentContactInfo(
                    emailField.getText(),
                    phoneField.getText(),
                    addressArea.getText()
            );
            DialogUtil.showInfo(this, "Cập nhật thông tin thành công.");
            reloadData();
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void openChangePasswordDialog() {
        ChangePasswordDialog.PasswordChangeRequest request = ChangePasswordDialog.showSelfChangeDialog(
                this,
                "Đổi mật khẩu sinh viên"
        );
        if (request == null) {
            return;
        }

        try {
            userController.changeCurrentPassword(
                    request.currentPassword(),
                    request.newPassword(),
                    request.confirmPassword()
            );
            DialogUtil.showInfo(this, "Đổi mật khẩu thành công.");
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void styleFilledButton(JButton button, Color background) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(background);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        button.setAlignmentX(Component.RIGHT_ALIGNMENT);
    }

    private String toHtml(String text) {
        return "<html><div>" + text + "</div></html>";
    }
}
