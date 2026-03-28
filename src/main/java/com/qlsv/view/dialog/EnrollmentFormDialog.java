package com.qlsv.view.dialog;

import com.qlsv.model.CourseSection;
import com.qlsv.model.Student;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.FilterOption;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

public class EnrollmentFormDialog extends JDialog {

    private static final FilterOption<String>[] STATUS_OPTIONS = new FilterOption[]{
            new FilterOption<>("Đã đăng ký", "REGISTERED"),
            new FilterOption<>("Đã hủy", "CANCELLED")
    };

    private final JTextField studentSearchField = new JTextField();
    private final DefaultListModel<Student> suggestionModel = new DefaultListModel<>();
    private final JList<Student> suggestionList = new JList<>(suggestionModel);
    private final JLabel selectedStudentLabel = new JLabel();
    private final JLabel validationLabel = new JLabel(" ");
    private final JComboBox<CourseSection> courseSectionComboBox = new JComboBox<>();
    private final JComboBox<FilterOption<String>> statusComboBox = new JComboBox<>(new DefaultComboBoxModel<>(STATUS_OPTIONS));

    private final List<Student> allStudents;
    private Student selectedStudent;
    private boolean applyingSelection;

    private EnrollmentFormResult result;

    private EnrollmentFormDialog(Component parent, EnrollmentFormModel model) {
        super(resolveOwner(parent), model.title(), Dialog.ModalityType.APPLICATION_MODAL);
        this.allStudents = model.students();
        this.selectedStudent = model.selectedStudent();
        initComponents(model);
    }

    public static EnrollmentFormResult showDialog(Component parent, EnrollmentFormModel model) {
        EnrollmentFormDialog dialog = new EnrollmentFormDialog(parent, model);
        dialog.setVisible(true);
        return dialog.result;
    }

    private void initComponents(EnrollmentFormModel model) {
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
        bodyPanel.add(createStudentSection());
        bodyPanel.add(Box.createVerticalStrut(24));
        bodyPanel.add(createEnrollmentSection());

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
        bindModel(model);
        registerStudentPickerBehavior();

        getRootPane().setDefaultButton(saveButton);
        setMinimumSize(new Dimension(760, 620));
        setSize(new Dimension(820, 680));
        setLocationRelativeTo(getOwner());
    }

    private void bindModel(EnrollmentFormModel model) {
        courseSectionComboBox.removeAllItems();
        for (CourseSection courseSection : model.courseSections()) {
            courseSectionComboBox.addItem(courseSection);
        }
        if (model.selectedCourseSection() != null) {
            courseSectionComboBox.setSelectedItem(model.selectedCourseSection());
        }

        selectStatus(model.status());
        applyingSelection = true;
        studentSearchField.setText(selectedStudent == null ? "" : formatStudentDisplay(selectedStudent));
        applyingSelection = false;
        refreshSuggestions();
        updateSelectedStudentLabel();
        SwingUtilities.invokeLater(() -> studentSearchField.requestFocusInWindow());
    }

    private JPanel createStudentSection() {
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setVisibleRowCount(6);
        suggestionList.setFixedCellHeight(28);
        suggestionList.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JScrollPane suggestionScrollPane = new JScrollPane(suggestionList);
        suggestionScrollPane.setBorder(BorderFactory.createLineBorder(AppColors.INPUT_BORDER));
        suggestionScrollPane.setPreferredSize(new Dimension(320, 150));

        selectedStudentLabel.setFont(selectedStudentLabel.getFont().deriveFont(Font.BOLD, 12.5f));
        selectedStudentLabel.setForeground(AppColors.CARD_TITLE_TEXT);

        validationLabel.setFont(validationLabel.getFont().deriveFont(Font.PLAIN, 12f));
        validationLabel.setForeground(new Color(185, 28, 28));

        JPanel pickerPanel = new JPanel(new BorderLayout(0, 8));
        pickerPanel.setOpaque(false);
        pickerPanel.add(styleTextField(studentSearchField), BorderLayout.NORTH);
        pickerPanel.add(suggestionScrollPane, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new BorderLayout(0, 4));
        statusPanel.setOpaque(false);
        statusPanel.add(selectedStudentLabel, BorderLayout.NORTH);
        statusPanel.add(validationLabel, BorderLayout.CENTER);
        pickerPanel.add(statusPanel, BorderLayout.SOUTH);

        JPanel contentPanel = createFieldGridPanel();
        contentPanel.add(createField("Tìm sinh viên", pickerPanel), fieldConstraints(0, 0, 2));
        return createSection("Sinh viên", "Tìm nhanh theo mã hoặc họ tên rồi chọn từ danh sách gợi ý.", contentPanel);
    }

    private JPanel createEnrollmentSection() {
        JPanel contentPanel = createFieldGridPanel();
        contentPanel.add(createField("Học phần", styleComboBox(courseSectionComboBox)), fieldConstraints(0, 0, 2));
        contentPanel.add(createField("Trạng thái", styleComboBox(statusComboBox)), fieldConstraints(0, 1, 2));
        return createSection("Thông tin đăng ký","", contentPanel);
    }

    private void registerStudentPickerBehavior() {
        studentSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                handleSearchChange();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                handleSearchChange();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                handleSearchChange();
            }

            private void handleSearchChange() {
                if (!applyingSelection) {
                    selectedStudent = null;
                    validationLabel.setText(" ");
                }
                refreshSuggestions();
                updateSelectedStudentLabel();
            }
        });

        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    commitSelectedStudent();
                }
            }
        });

        suggestionList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    event.consume();
                    commitSelectedStudent();
                }
            }
        });
    }

    private void refreshSuggestions() {
        suggestionModel.clear();
        List<Student> matchedStudents = filterStudentsByKeyword(studentSearchField.getText());
        for (Student student : matchedStudents) {
            suggestionModel.addElement(student);
        }
        if (selectedStudent != null) {
            suggestionList.setSelectedValue(selectedStudent, true);
        } else if (!suggestionModel.isEmpty()) {
            suggestionList.setSelectedIndex(0);
        }
    }

    private void commitSelectedStudent() {
        Student student = suggestionList.getSelectedValue();
        if (student == null) {
            return;
        }
        selectedStudent = student;
        applyingSelection = true;
        studentSearchField.setText(formatStudentDisplay(student));
        applyingSelection = false;
        validationLabel.setText(" ");
        refreshSuggestions();
        updateSelectedStudentLabel();
    }

    private void updateSelectedStudentLabel() {
        selectedStudentLabel.setText(selectedStudent == null
                ? "Chưa chọn sinh viên"
                : "Đã chọn: " + formatStudentDisplay(selectedStudent));
    }

    private void handleSave() {
        if (selectedStudent == null) {
            validationLabel.setText("Hãy chọn một sinh viên từ danh sách gợi ý.");
            return;
        }

        FilterOption<String> selectedStatus = (FilterOption<String>) statusComboBox.getSelectedItem();
        result = new EnrollmentFormResult(
                selectedStudent,
                (CourseSection) courseSectionComboBox.getSelectedItem(),
                selectedStatus == null ? "REGISTERED" : selectedStatus.value()
        );
        dispose();
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
        comboBox.setPreferredSize(new Dimension(comboBox.getPreferredSize().width, 40));
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

    private List<Student> filterStudentsByKeyword(String keyword) {
        String normalizedKeyword = normalizeSearchText(keyword);
        if (normalizedKeyword.isBlank()) {
            return allStudents.stream().limit(8).toList();
        }

        return allStudents.stream()
                .filter(student -> normalizeSearchText(student.getStudentCode()).contains(normalizedKeyword)
                        || normalizeSearchText(student.getFullName()).contains(normalizedKeyword))
                .limit(8)
                .toList();
    }

    private String formatStudentDisplay(Student student) {
        if (student == null) {
            return "";
        }
        return student.getStudentCode() + " - " + student.getFullName();
    }

    private String normalizeSearchText(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }

    private static Window resolveOwner(Component parent) {
        if (parent == null) {
            return null;
        }
        return SwingUtilities.getWindowAncestor(parent);
    }

    public record EnrollmentFormModel(
            String title,
            List<Student> students,
            Student selectedStudent,
            List<CourseSection> courseSections,
            CourseSection selectedCourseSection,
            String status
    ) {
    }

    public record EnrollmentFormResult(Student student, CourseSection courseSection, String status) {
    }
}
