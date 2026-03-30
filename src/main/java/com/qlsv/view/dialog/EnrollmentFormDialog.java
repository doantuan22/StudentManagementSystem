/**
 * Hộp thoại đăng ký biểu mẫu dialog.
 */
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

    @SuppressWarnings("unchecked")
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

    /**
     * Khởi tạo đăng ký biểu mẫu.
     */
    private EnrollmentFormDialog(Component parent, EnrollmentFormModel model) {
        super(resolveOwner(parent), model.title(), Dialog.ModalityType.APPLICATION_MODAL);
        this.allStudents = model.students();
        this.selectedStudent = model.selectedStudent();
        initComponents(model);
    }

    /**
     * Hiển thị hộp thoại và trả về kết quả đăng ký học phần sau khi người dùng xác nhận.
     */
    public static EnrollmentFormResult showDialog(Component parent, EnrollmentFormModel model) {
        EnrollmentFormDialog dialog = new EnrollmentFormDialog(parent, model);
        dialog.setVisible(true);
        return dialog.result;
    }

    /**
     * Khởi tạo các thành phần giao diện cho form đăng ký học phần của sinh viên.
     */
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

        JScrollPane scrollPane = new JScrollPane(bodyPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

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
        registerStudentPickerBehavior();

        getRootPane().setDefaultButton(saveButton);
        setMinimumSize(new Dimension(760, 620));
        setSize(new Dimension(820, 680));
        setLocationRelativeTo(getOwner());
    }

    /**
     * Điền dữ liệu ban đầu từ model vào các trường thông tin trên form.
     */
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

    /**
     * Tạo sinh viên phần.
     */
    private JPanel createStudentSection() {
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setVisibleRowCount(12);
        suggestionList.setFixedCellHeight(30);
        suggestionList.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JScrollPane suggestionScrollPane = new JScrollPane(suggestionList);
        suggestionScrollPane.setBorder(BorderFactory.createLineBorder(AppColors.INPUT_BORDER));
        suggestionScrollPane.setPreferredSize(new Dimension(320, 420));
        suggestionScrollPane.setMinimumSize(new Dimension(0, 360));
        suggestionScrollPane.getVerticalScrollBar().setUnitIncrement(16);

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

    /**
     * Tạo đăng ký phần.
     */
    private JPanel createEnrollmentSection() {
        JPanel contentPanel = createFieldGridPanel();
        contentPanel.add(createField("Học phần", styleComboBox(courseSectionComboBox)), fieldConstraints(0, 0, 2));
        contentPanel.add(createField("Trạng thái", styleComboBox(statusComboBox)), fieldConstraints(0, 1, 2));
        return createSection("Thông tin đăng ký","", contentPanel);
    }

    /**
     * Cài đặt các lắng nghe sự kiện trên ô tìm kiếm và danh sách gợi ý sinh viên.
     */
    private void registerStudentPickerBehavior() {
        studentSearchField.getDocument().addDocumentListener(new DocumentListener() {
            /**
             * Phản ứng khi nội dung vừa được chèn.
             */
            @Override
            public void insertUpdate(DocumentEvent event) {
                handleSearchChange();
            }

            /**
             * Phản ứng khi nội dung vừa bị xóa.
             */
            @Override
            public void removeUpdate(DocumentEvent event) {
                handleSearchChange();
            }

            /**
             * Phản ứng khi thuộc tính tài liệu thay đổi.
             */
            @Override
            public void changedUpdate(DocumentEvent event) {
                handleSearchChange();
            }

            /**
             * Xử lý tìm kiếm đổi.
             */
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
            /**
             * Xử lý thao tác nhấp chuột trên giao diện.
             */
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    commitSelectedStudent();
                }
            }
        });

        suggestionList.addKeyListener(new KeyAdapter() {
            /**
             * Xử lý key pressed.
             */
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    event.consume();
                    commitSelectedStudent();
                }
            }
        });
    }

    /**
     * Làm mới danh sách gợi ý sinh viên dựa trên từ khóa tìm kiếm hiện tại.
     */
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

    /**
     * Xác nhận sinh viên được chọn từ danh sách gợi ý và cập nhật lên form.
     */
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

    /**
     * Cập nhật label sinh viên đã chọn.
     */
    private void updateSelectedStudentLabel() {
        selectedStudentLabel.setText(selectedStudent == null
                ? "Chưa chọn sinh viên"
                : "Đã chọn: " + formatStudentDisplay(selectedStudent));
    }

    /**
     * Kiểm tra tính hợp lệ và thu thập dữ liệu đăng ký để đóng hộp thoại.
     */
    private void handleSave() {
        if (selectedStudent == null) {
            validationLabel.setText("Hãy chọn một sinh viên từ danh sách gợi ý.");
            return;
        }

        @SuppressWarnings("unchecked")
        FilterOption<String> selectedStatus = (FilterOption<String>) statusComboBox.getSelectedItem();
        result = new EnrollmentFormResult(
                selectedStudent,
                (CourseSection) courseSectionComboBox.getSelectedItem(),
                selectedStatus == null ? "REGISTERED" : selectedStatus.value()
        );
        dispose();
    }

    /**
     * Tạo phần.
     */
    private JPanel createSection(String title, String subtitle, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(true);
        panel.setBackground(AppColors.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

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

    /**
     * Tạo panel trường grid.
     */
    private JPanel createFieldGridPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        return panel;
    }

    /**
     * Xử lý trường constraints.
     */
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

    /**
     * Tạo trường.
     */
    private JPanel createField(String labelText, Component inputComponent) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

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
        textField.setMinimumSize(new Dimension(180, 40));
        return textField;
    }

    /**
     * Áp dụng kiểu cho chọn box.
     */
    private <T> JComboBox<T> styleComboBox(JComboBox<T> comboBox) {
        comboBox.setFont(comboBox.getFont().deriveFont(Font.PLAIN, 13.5f));
        comboBox.setBorder(BorderFactory.createLineBorder(AppColors.INPUT_BORDER));
        comboBox.setPreferredSize(new Dimension(comboBox.getPreferredSize().width, 40));
        comboBox.setMinimumSize(new Dimension(220, 40));
        return comboBox;
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
     * Xử lý select trạng thái.
     */
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

    /**
     * Lọc danh sách sinh viên theo mã hoặc họ tên (không phân biệt dấu).
     */
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

    /**
     * Định dạng sinh viên hiển thị.
     */
    private String formatStudentDisplay(Student student) {
        if (student == null) {
            return "";
        }
        return student.getStudentCode() + " - " + student.getFullName();
    }

    /**
     * Chuẩn hóa tìm kiếm văn bản.
     */
    private String normalizeSearchText(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
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
     * Xử lý model đăng ký biểu mẫu.
     */
    public record EnrollmentFormModel(
            String title,
            List<Student> students,
            Student selectedStudent,
            List<CourseSection> courseSections,
            CourseSection selectedCourseSection,
            String status
    ) {
    }

    /**
     * Xử lý đăng ký kết quả biểu mẫu.
     */
    public record EnrollmentFormResult(Student student, CourseSection courseSection, String status) {
    }
}
