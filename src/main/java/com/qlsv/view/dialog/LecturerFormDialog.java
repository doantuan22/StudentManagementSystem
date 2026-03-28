package com.qlsv.view.dialog;

import com.qlsv.model.Faculty;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.FilterOption;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.List;

public class LecturerFormDialog extends JDialog {

    private static final FilterOption<String>[] STATUS_OPTIONS = new FilterOption[]{
            new FilterOption<>("Đang hoạt động", "ACTIVE"),
            new FilterOption<>("Ngừng hoạt động", "INACTIVE")
    };

    private final JTextField lecturerCodeField = new JTextField();
    private final JTextField fullNameField = new JTextField();
    private final JTextField dateOfBirthField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextArea addressArea = new JTextArea();
    private final JComboBox<Faculty> facultyComboBox = new JComboBox<>();
    private final JComboBox<FilterOption<String>> statusComboBox = new JComboBox<>(STATUS_OPTIONS);

    private LecturerFormResult result;

    private LecturerFormDialog(Component parent, LecturerFormModel model) {
        super(resolveOwner(parent), model.title(), Dialog.ModalityType.APPLICATION_MODAL);
        initComponents(model);
    }

    public static LecturerFormResult showDialog(Component parent, LecturerFormModel model) {
        LecturerFormDialog dialog = new LecturerFormDialog(parent, model);
        dialog.setVisible(true);
        return dialog.result;
    }

    private void initComponents(LecturerFormModel model) {
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
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(16, 24, 20, 24));
        bodyPanel.add(createBasicInfoSection());
        bodyPanel.add(Box.createVerticalStrut(24));
        bodyPanel.add(createWorkInfoSection());
        bodyPanel.add(Box.createVerticalStrut(24));
        bodyPanel.add(createContactInfoSection());

        JScrollPane scrollPane = new JScrollPane(bodyPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

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
        rootPanel.add(scrollPane, BorderLayout.CENTER);
        rootPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
        bindModel(model);

        getRootPane().setDefaultButton(saveButton);
        setMinimumSize(new Dimension(720, 620));
        setSize(new Dimension(760, 660));
        setLocationRelativeTo(getOwner());
    }

    private void bindModel(LecturerFormModel model) {
        lecturerCodeField.setText(model.lecturerCode());
        fullNameField.setText(model.fullName());
        dateOfBirthField.setText(model.dateOfBirth());
        emailField.setText(model.email());
        phoneField.setText(model.phone());
        addressArea.setText(model.address());

        facultyComboBox.removeAllItems();
        for (Faculty faculty : model.faculties()) {
            facultyComboBox.addItem(faculty);
        }
        if (model.selectedFaculty() != null) {
            facultyComboBox.setSelectedItem(model.selectedFaculty());
        }

        selectStatus(model.status());
        SwingUtilities.invokeLater(() -> lecturerCodeField.requestFocusInWindow());
    }

    private JPanel createBasicInfoSection() {
        JPanel contentPanel = createFieldGridPanel();
        contentPanel.add(createField("Mã giảng viên", styleTextField(lecturerCodeField)), fieldConstraints(0, 0));
        contentPanel.add(createField("Họ và tên", styleTextField(fullNameField)), fieldConstraints(1, 0));
        contentPanel.add(createField("Ngày sinh (yyyy-MM-dd)", styleTextField(dateOfBirthField)), fieldConstraints(0, 1, 2));
        return createSection("Thông tin cơ bản", "Thông tin định danh và nhân sự của giảng viên.", contentPanel);
    }

    private JPanel createWorkInfoSection() {
        JPanel contentPanel = createFieldGridPanel();
        contentPanel.add(createField("Khoa", styleComboBox(facultyComboBox)), fieldConstraints(0, 0));
        contentPanel.add(createField("Trạng thái", styleComboBox(statusComboBox)), fieldConstraints(1, 0));
        return createSection("Thông tin công tác", "Thông tin khoa phụ trách và trạng thái sử dụng tài khoản.", contentPanel);
    }

    private JPanel createContactInfoSection() {
        JPanel contentPanel = createFieldGridPanel();
        contentPanel.add(createField("Email", styleTextField(emailField)), fieldConstraints(0, 0));
        contentPanel.add(createField("Số điện thoại", styleTextField(phoneField)), fieldConstraints(1, 0));
        contentPanel.add(createField("Địa chỉ", createAddressScrollPane()), fieldConstraints(0, 1, 2));
        return createSection("Liên hệ", "Thông tin liên lạc và địa chỉ của giảng viên.", contentPanel);
    }

    private JPanel createSection(String title, String subtitle, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(true);
        panel.setBackground(AppColors.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        JPanel headingPanel = new JPanel(new BorderLayout(0, 4));
        headingPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 12.5f));
        subtitleLabel.setForeground(AppColors.CARD_MUTED_TEXT);

        headingPanel.add(titleLabel, BorderLayout.NORTH);
        headingPanel.add(subtitleLabel, BorderLayout.CENTER);

        panel.add(headingPanel, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFieldGridPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        return panel;
    }

    private GridBagConstraints fieldConstraints(int x, int y) {
        return fieldConstraints(x, y, 1);
    }

    private GridBagConstraints fieldConstraints(int x, int y, int width) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = width;
        constraints.weightx = width;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(0, 0, 12, width == 2 ? 0 : (x == 0 ? 12 : 0));
        return constraints;
    }

    private JPanel createField(String labelText, JComponent inputComponent) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12.5f));
        label.setForeground(AppColors.CARD_TITLE_TEXT);

        panel.add(label, BorderLayout.NORTH);
        panel.add(inputComponent, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane createAddressScrollPane() {
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setRows(4);
        addressArea.setFont(addressArea.getFont().deriveFont(Font.PLAIN, 13.5f));
        addressArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JScrollPane scrollPane = new JScrollPane(addressArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.INPUT_BORDER));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JTextField styleTextField(JTextField textField) {
        textField.setFont(textField.getFont().deriveFont(Font.PLAIN, 13.5f));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.INPUT_BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        return textField;
    }

    private <T> JComboBox<T> styleComboBox(JComboBox<T> comboBox) {
        comboBox.setFont(comboBox.getFont().deriveFont(Font.PLAIN, 13.5f));
        comboBox.setBorder(BorderFactory.createLineBorder(AppColors.INPUT_BORDER));
        return comboBox;
    }

    private void stylePrimaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(AppColors.BUTTON_PRIMARY);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
    }

    private void styleSecondaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(AppColors.BUTTON_NEUTRAL);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
    }

    private void selectStatus(String statusCode) {
        for (int index = 0; index < statusComboBox.getItemCount(); index++) {
            FilterOption<String> option = statusComboBox.getItemAt(index);
            if (option != null && option.value().equalsIgnoreCase(statusCode == null ? "" : statusCode)) {
                statusComboBox.setSelectedIndex(index);
                return;
            }
        }
        statusComboBox.setSelectedIndex(0);
    }

    private void handleSave() {
        FilterOption<String> selectedStatus = (FilterOption<String>) statusComboBox.getSelectedItem();
        result = new LecturerFormResult(
                lecturerCodeField.getText(),
                fullNameField.getText(),
                dateOfBirthField.getText(),
                emailField.getText(),
                phoneField.getText(),
                addressArea.getText(),
                (Faculty) facultyComboBox.getSelectedItem(),
                selectedStatus == null ? "ACTIVE" : selectedStatus.value()
        );
        dispose();
    }

    private static Window resolveOwner(Component parent) {
        if (parent == null) {
            return null;
        }
        return SwingUtilities.getWindowAncestor(parent);
    }

    public record LecturerFormModel(
            String title,
            String lecturerCode,
            String fullName,
            String dateOfBirth,
            String email,
            String phone,
            String address,
            List<Faculty> faculties,
            Faculty selectedFaculty,
            String status
    ) {
    }

    public record LecturerFormResult(
            String lecturerCode,
            String fullName,
            String dateOfBirth,
            String email,
            String phone,
            String address,
            Faculty faculty,
            String status
    ) {
    }
}
