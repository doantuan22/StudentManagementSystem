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

public class ClassRoomFormDialog extends JDialog {

    private final JTextField classCodeField = new JTextField();
    private final JTextField classNameField = new JTextField();
    private final JTextField academicYearField = new JTextField();
    private final JComboBox<Faculty> facultyComboBox = new JComboBox<>();

    private ClassRoomFormResult result;

    private ClassRoomFormDialog(Component parent, ClassRoomFormModel model) {
        super(resolveOwner(parent), model.title(), Dialog.ModalityType.APPLICATION_MODAL);
        initComponents(model);
    }

    /**
     * Hiển thị hộp thoại nhập liệu lớp học và trả về kết quả sau khi người dùng xác nhận.
     */
    public static ClassRoomFormResult showDialog(Component parent, ClassRoomFormModel model) {
        ClassRoomFormDialog dialog = new ClassRoomFormDialog(parent, model);
        dialog.setVisible(true);
        return dialog.result;
    }

    /**
     * Khởi tạo các thành phần giao diện của form nhập liệu lớp học.
     */
    private void initComponents(ClassRoomFormModel model) {
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
        bodyPanel.add(createField("Mã lớp", styleTextField(classCodeField)));
        bodyPanel.add(Box.createVerticalStrut(12));
        bodyPanel.add(createField("Tên lớp", styleTextField(classNameField)));
        bodyPanel.add(Box.createVerticalStrut(12));
        bodyPanel.add(createField("Niên khóa", styleTextField(academicYearField)));
        bodyPanel.add(Box.createVerticalStrut(12));
        bodyPanel.add(createField("Khoa", styleComboBox(facultyComboBox)));

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
        classCodeField.setText(model.classCode());
        classNameField.setText(model.className());
        academicYearField.setText(model.academicYear());
        facultyComboBox.removeAllItems();
        for (Faculty faculty : model.faculties()) {
            facultyComboBox.addItem(faculty);
        }
        if (model.selectedFaculty() != null) {
            facultyComboBox.setSelectedItem(model.selectedFaculty());
        }

        getRootPane().setDefaultButton(saveButton);
        setMinimumSize(new Dimension(620, 420));
        setSize(new Dimension(660, 460));
        setLocationRelativeTo(getOwner());
        SwingUtilities.invokeLater(() -> classCodeField.requestFocusInWindow());
    }

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

    /**
     * Thu thập dữ liệu từ các ô nhập liệu vào đối tượng kết quả và đóng hộp thoại.
     */
    private void handleSave() {
        result = new ClassRoomFormResult(
                classCodeField.getText(),
                classNameField.getText(),
                academicYearField.getText(),
                (Faculty) facultyComboBox.getSelectedItem()
        );
        dispose();
    }

    private static Window resolveOwner(Component parent) {
        if (parent == null) {
            return null;
        }
        return SwingUtilities.getWindowAncestor(parent);
    }

    public record ClassRoomFormModel(
            String title,
            String classCode,
            String className,
            String academicYear,
            List<Faculty> faculties,
            Faculty selectedFaculty
    ) {
    }

    public record ClassRoomFormResult(
            String classCode,
            String className,
            String academicYear,
            Faculty faculty
    ) {
    }
}
