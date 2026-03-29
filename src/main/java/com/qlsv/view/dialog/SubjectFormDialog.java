/**
 * Hộp thoại môn học biểu mẫu dialog.
 */
package com.qlsv.view.dialog;

import com.qlsv.model.Faculty;
import com.qlsv.view.common.AppColors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import java.awt.Window;
import java.util.List;

public class SubjectFormDialog extends JDialog {

    private final JTextField subjectCodeField = new JTextField();
    private final JTextField subjectNameField = new JTextField();
    private final JTextField creditsField = new JTextField();
    private final JComboBox<Faculty> facultyComboBox = new JComboBox<>();
    private final JTextArea descriptionArea = new JTextArea();

    private SubjectFormResult result;

    /**
     * Khởi tạo môn học biểu mẫu.
     */
    private SubjectFormDialog(Component parent, SubjectFormModel model) {
        super(resolveOwner(parent), model.title(), Dialog.ModalityType.APPLICATION_MODAL);
        initComponents(model);
    }

    /**
     * Hiển thị hộp thoại.
     */
    public static SubjectFormResult showDialog(Component parent, SubjectFormModel model) {
        SubjectFormDialog dialog = new SubjectFormDialog(parent, model);
        dialog.setVisible(true);
        return dialog.result;
    }

    /**
     * Khởi tạo các thành phần giao diện.
     */
    private void initComponents(SubjectFormModel model) {
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
        bodyPanel.add(createField("Mã môn học", styleTextField(subjectCodeField)));
        bodyPanel.add(Box.createVerticalStrut(12));
        bodyPanel.add(createField("Tên môn học", styleTextField(subjectNameField)));
        bodyPanel.add(Box.createVerticalStrut(12));
        bodyPanel.add(createField("Số tín chỉ", styleTextField(creditsField)));
        bodyPanel.add(Box.createVerticalStrut(12));
        bodyPanel.add(createField("Khoa", styleComboBox(facultyComboBox)));
        bodyPanel.add(Box.createVerticalStrut(12));
        bodyPanel.add(createField("Mô tả", createDescriptionScrollPane()));

        JScrollPane scrollPane = new JScrollPane(bodyPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

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
        subjectCodeField.setText(model.subjectCode());
        subjectNameField.setText(model.subjectName());
        creditsField.setText(model.credits());
        descriptionArea.setText(model.description());
        facultyComboBox.removeAllItems();
        for (Faculty faculty : model.faculties()) {
            facultyComboBox.addItem(faculty);
        }
        if (model.selectedFaculty() != null) {
            facultyComboBox.setSelectedItem(model.selectedFaculty());
        }

        getRootPane().setDefaultButton(saveButton);
        setMinimumSize(new Dimension(660, 520));
        setSize(new Dimension(700, 560));
        setLocationRelativeTo(getOwner());
        SwingUtilities.invokeLater(() -> subjectCodeField.requestFocusInWindow());
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
     * Áp dụng kiểu cho chọn box.
     */
    private <T> JComboBox<T> styleComboBox(JComboBox<T> comboBox) {
        comboBox.setFont(comboBox.getFont().deriveFont(Font.PLAIN, 13.5f));
        comboBox.setBorder(BorderFactory.createLineBorder(AppColors.INPUT_BORDER));
        return comboBox;
    }

    /**
     * Tạo description scroll pane.
     */
    private JScrollPane createDescriptionScrollPane() {
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setRows(4);
        descriptionArea.setFont(descriptionArea.getFont().deriveFont(Font.PLAIN, 13.5f));
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.INPUT_BORDER));
        return scrollPane;
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
        result = new SubjectFormResult(
                subjectCodeField.getText(),
                subjectNameField.getText(),
                creditsField.getText(),
                (Faculty) facultyComboBox.getSelectedItem(),
                descriptionArea.getText()
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
     * Xử lý model môn học biểu mẫu.
     */
    public record SubjectFormModel(
            String title,
            String subjectCode,
            String subjectName,
            String credits,
            String description,
            List<Faculty> faculties,
            Faculty selectedFaculty
    ) {
    }

    /**
     * Xử lý môn học kết quả biểu mẫu.
     */
    public record SubjectFormResult(
            String subjectCode,
            String subjectName,
            String credits,
            Faculty faculty,
            String description
    ) {
    }
}
