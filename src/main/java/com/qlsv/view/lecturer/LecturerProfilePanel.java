/**
 * Màn hình giảng viên cho hồ sơ.
 */
package com.qlsv.view.lecturer;

import com.qlsv.controller.LecturerController;
import com.qlsv.controller.UserController;
import com.qlsv.model.Lecturer;
import com.qlsv.utils.DialogUtil;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.utils.ValidationUtil;
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
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class LecturerProfilePanel extends BasePanel {

    private static final Color HERO_BACKGROUND = new Color(235, 243, 255);
    private static final Color IDENTITY_CARD_BACKGROUND = new Color(255, 255, 255);
    private static final Color WORK_CARD_BACKGROUND = new Color(244, 248, 255);
    private static final Color CONTACT_CARD_BACKGROUND = new Color(240, 252, 246);
    private static final int CARD_GAP = 16;
    private static final int SECTION_GAP = 12;
    private static final int INPUT_HEIGHT = 38;

    private final LecturerController lecturerController = new LecturerController();
    private final UserController userController = new UserController();
    private final JPanel contentPanel = new JPanel();

    private JTextField emailField;
    private JTextField phoneField;
    private JTextField addressField;
    private boolean isEditing;
    private Lecturer currentLecturer;

    /**
     * Khởi tạo hồ sơ giảng viên.
     */
    public LecturerProfilePanel() {
        setBackground(AppColors.CONTENT_BACKGROUND);
        initComponents();
        reloadData();
    }

    /**
     * Khởi tạo các thành phần giao diện cơ bản cho bảng thông tin giảng viên.
     */
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

    /**
     * Làm mới dữ liệu đang hiển thị.
     */
    @Override
    public void reloadData() {
        try {
            currentLecturer = lecturerController.getCurrentLecturer();
            renderProfile();
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    /**
     * Dựng lại giao diện hồ sơ giảng viên dựa trên dữ liệu và trạng thái hiện tại.
     */
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

    /**
     * Xây dựng thẻ biểu ngữ (Hero Card) chứa thông tin tóm tắt và nút chức năng.
     */
    private JPanel buildHeroCard() {
        JPanel card = createCard(HERO_BACKGROUND);
        card.add(buildHeroHeader(), BorderLayout.NORTH);
        card.add(buildSummaryPanel(), BorderLayout.CENTER);
        return card;
    }

    /**
     * Tạo hero đầu trang.
     */
    private JPanel buildHeroHeader() {
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel eyebrowLabel = new JLabel("HỒ SƠ GIẢNG VIÊN");
        eyebrowLabel.setFont(eyebrowLabel.getFont().deriveFont(Font.BOLD, 12f));
        eyebrowLabel.setForeground(new Color(37, 99, 235));

        JLabel titleLabel = new JLabel("Thông tin cá nhân");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 26f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        titlePanel.add(eyebrowLabel);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(8));

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

    /**
     * Tạo panel tóm tắt.
     */
    private JPanel buildSummaryPanel() {
        JPanel summaryPanel = new JPanel(new GridBagLayout());
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(CARD_GAP, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        addSummaryBadge(summaryPanel, gbc, 0, 0, createBadge("Mã GV", currentLecturer.getLecturerCode()));
        addSummaryBadge(summaryPanel, gbc, 1, 0, createBadge("Khoa", currentLecturer.getFaculty() == null
                ? "Chưa cập nhật"
                : currentLecturer.getFaculty().getFacultyName()));
        addSummaryBadge(summaryPanel, gbc, 0, 1, createBadge("Email", DisplayTextUtil.defaultText(currentLecturer.getEmail())));
        addSummaryBadge(summaryPanel, gbc, 1, 1, createBadge("Điện thoại", DisplayTextUtil.defaultText(currentLecturer.getPhone())));
        return summaryPanel;
    }

    /**
     * Xây dựng thanh công cụ chứa các nút chức năng (Đổi MK, Cập nhật, Lưu, Hủy).
     */
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
        } else {
            JButton saveButton = new JButton("Lưu thay đổi");
            styleFilledButton(saveButton, AppColors.BUTTON_SUCCESS);
            saveButton.addActionListener(event -> handleSave());

            JButton cancelButton = new JButton("Hủy");
            styleFilledButton(cancelButton, AppColors.BUTTON_NEUTRAL);
            cancelButton.addActionListener(event -> {
                isEditing = false;
                renderProfile();
            });

            actionPanel.add(saveButton);
            actionPanel.add(cancelButton);
        }
        return actionPanel;
    }

    /**
     * Sắp xếp các thẻ thông tin chi tiết (Định danh, Công tác, Liên hệ) vào lưới.
     */
    private JPanel buildSectionGrid() {
        JPanel sectionsPanel = new JPanel(new GridBagLayout());
        sectionsPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, CARD_GAP, CARD_GAP);

        gbc.gridx = 0;
        sectionsPanel.add(buildIdentityCard(), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, CARD_GAP, 0);
        sectionsPanel.add(buildWorkCard(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        sectionsPanel.add(buildContactCard(), gbc);
        return sectionsPanel;
    }

    /**
     * Tạo card identity.
     */
    private JPanel buildIdentityCard() {
        JPanel card = createSectionCard(
                "Thông tin định danh",
                "Dữ liệu cơ bản của giảng viên",
                IDENTITY_CARD_BACKGROUND
        );

        JPanel fieldsPanel = createFieldsPanel();
        fieldsPanel.add(createReadOnlyField("Mã giảng viên", currentLecturer.getLecturerCode()));
        fieldsPanel.add(Box.createVerticalStrut(SECTION_GAP));
        fieldsPanel.add(createReadOnlyField("Họ và tên", currentLecturer.getFullName()));
        fieldsPanel.add(Box.createVerticalStrut(SECTION_GAP));
        fieldsPanel.add(createReadOnlyField("Ngày sinh", DisplayTextUtil.formatDate(currentLecturer.getDateOfBirth())));

        card.add(fieldsPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Tạo card work.
     */
    private JPanel buildWorkCard() {
        JPanel card = createSectionCard(
                "Thông tin công tác",
                "Khoa và dữ liệu phục vụ giảng dạy",
                WORK_CARD_BACKGROUND
        );

        JPanel fieldsPanel = createFieldsPanel();
        fieldsPanel.add(createReadOnlyField("Khoa phụ trách", currentLecturer.getFaculty() == null
                ? "Chưa cập nhật"
                : currentLecturer.getFaculty().getFacultyName()));
        fieldsPanel.add(Box.createVerticalStrut(SECTION_GAP));
        fieldsPanel.add(createReadOnlyField("Email công tác", DisplayTextUtil.defaultText(currentLecturer.getEmail())));
        fieldsPanel.add(Box.createVerticalStrut(SECTION_GAP));
        fieldsPanel.add(createReadOnlyField("Số điện thoại", DisplayTextUtil.defaultText(currentLecturer.getPhone())));

        card.add(fieldsPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Xây dựng thẻ chứa thông tin liên hệ (Email, SĐT, Địa chỉ) với chế độ chỉnh sửa.
     */
    private JPanel buildContactCard() {
        JPanel card = createSectionCard(
                "Thông tin liên hệ",
                "Liên hệ và địa chỉ hiện tại",
                CONTACT_CARD_BACKGROUND
        );

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        if (isEditing) {
            emailField = createTextField(currentLecturer.getEmail());
            phoneField = createTextField(currentLecturer.getPhone());
            addressField = createTextField(currentLecturer.getAddress());

            content.add(createInputField("Email liên hệ", emailField));
            content.add(Box.createVerticalStrut(SECTION_GAP));
            content.add(createInputField("Số điện thoại", phoneField));
            content.add(Box.createVerticalStrut(SECTION_GAP));
            content.add(createInputField("Địa chỉ", addressField));
        } else {
            content.add(createReadOnlyField("Email liên hệ", DisplayTextUtil.defaultText(currentLecturer.getEmail())));
            content.add(Box.createVerticalStrut(SECTION_GAP));
            content.add(createReadOnlyField("Số điện thoại", DisplayTextUtil.defaultText(currentLecturer.getPhone())));
            content.add(Box.createVerticalStrut(SECTION_GAP));
            content.add(createReadOnlyField("Địa chỉ", DisplayTextUtil.defaultText(currentLecturer.getAddress())));
        }

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    /**
     * Tạo card phần.
     */
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

    /**
     * Tạo card.
     */
    private JPanel createCard(Color background) {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setOpaque(true);
        card.setBackground(background);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER, 1, true),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));
        return card;
    }

    /**
     * Tạo panel trường.
     */
    private JPanel createFieldsPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    /**
     * Tạo badge.
     */
    private JPanel createBadge(String label, String value) {
        JPanel badge = new JPanel(new BorderLayout(0, 6));
        badge.setOpaque(true);
        badge.setBackground(Color.WHITE);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER, 1, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JLabel labelComponent = new JLabel(label);
        labelComponent.setForeground(AppColors.CARD_MUTED_TEXT);
        labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD, 12f));

        JLabel valueComponent = new JLabel("<html><div>" + DisplayTextUtil.defaultText(value) + "</div></html>");
        valueComponent.setForeground(AppColors.CARD_VALUE_TEXT);
        valueComponent.setFont(valueComponent.getFont().deriveFont(Font.BOLD, 15f));

        badge.add(labelComponent, BorderLayout.NORTH);
        badge.add(valueComponent, BorderLayout.CENTER);
        return badge;
    }

    /**
     * Tạo trường read only.
     */
    private JPanel createReadOnlyField(String label, String value) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setForeground(AppColors.CARD_MUTED_TEXT);
        labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD, 12.5f));

        JLabel valueComponent = new JLabel("<html><div>" + DisplayTextUtil.defaultText(value) + "</div></html>");
        valueComponent.setForeground(AppColors.CARD_VALUE_TEXT);
        valueComponent.setFont(valueComponent.getFont().deriveFont(Font.BOLD, 14f));

        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.add(labelComponent, BorderLayout.NORTH);
        panel.add(valueComponent, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Tạo trường input.
     */
    private JPanel createInputField(String label, JTextField textField) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setForeground(AppColors.CARD_MUTED_TEXT);
        labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD, 12.5f));

        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.add(labelComponent, BorderLayout.NORTH);
        panel.add(textField, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Tạo trường văn bản.
     */
    private JTextField createTextField(String value) {
        JTextField textField = new JTextField(value == null ? "" : value);
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, INPUT_HEIGHT));
        textField.setPreferredSize(new Dimension(0, INPUT_HEIGHT));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.INPUT_BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return textField;
    }

    /**
     * Thêm tóm tắt badge.
     */
    private void addSummaryBadge(JPanel summaryPanel, GridBagConstraints template, int x, int y, JPanel badgePanel) {
        GridBagConstraints gbc = (GridBagConstraints) template.clone();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.insets = new Insets(0, 0, y == 0 ? CARD_GAP : 0, x == 0 ? CARD_GAP : 0);
        summaryPanel.add(badgePanel, gbc);
    }

    /**
     * Tạo label note.
     */
    private JLabel createNoteLabel(String text) {
        JLabel label = new JLabel("<html><div style='width:260px;line-height:1.45;'>" + text + "</div></html>");
        label.setForeground(AppColors.CARD_MUTED_TEXT);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 12.5f));
        return label;
    }

    /**
     * Áp dụng kiểu cho nút filled.
     */
    private void styleFilledButton(JButton button, Color background) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(background);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        button.setFont(button.getFont().deriveFont(Font.BOLD));
    }

    /**
     * Xử lý gửi yêu cầu lưu thông tin liên hệ của giảng viên sau khi chỉnh sửa.
     */
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

            currentLecturer = lecturerController.saveLecturer(currentLecturer);
            DialogUtil.showInfo(this, "Cập nhật thông tin thành công!");
            isEditing = false;
            reloadData();
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    /**
     * Hiển thị hộp thoại và thực hiện đổi mật khẩu cho tài khoản giảng viên.
     */
    private void openChangePasswordDialog() {
        ChangePasswordDialog.PasswordChangeRequest request = ChangePasswordDialog.showSelfChangeDialog(
                this,
                "Đổi mật khẩu giảng viên"
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
}
