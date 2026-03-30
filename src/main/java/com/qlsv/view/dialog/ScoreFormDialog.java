/**
 * Hộp thoại điểm biểu mẫu dialog.
 */
package com.qlsv.view.dialog;

import com.qlsv.model.Enrollment;
import com.qlsv.model.Student;
import com.qlsv.view.common.AppColors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScoreFormDialog extends JDialog {

    private final JTextField studentSearchField = new JTextField();
    private final DefaultListModel<Student> suggestionModel = new DefaultListModel<>();
    private final JList<Student> suggestionList = new JList<>(suggestionModel);
    private final JLabel selectedStudentLabel = new JLabel();
    private final JLabel validationLabel = new JLabel(" ");
    private final JComboBox<Enrollment> enrollmentComboBox = new JComboBox<>();
    private final JTextField processScoreField = new JTextField();
    private final JTextField midtermScoreField = new JTextField();
    private final JTextField finalScoreField = new JTextField();

    private final List<Enrollment> allEnrollments;
    private Student selectedStudent;
    private boolean applyingStudentSelection;

    private ScoreFormResult result;

    /**
     * Khởi tạo điểm biểu mẫu.
     */
    private ScoreFormDialog(Component parent, ScoreFormModel model) {
        super(resolveOwner(parent), model.title(), Dialog.ModalityType.APPLICATION_MODAL);
        this.allEnrollments = model.enrollments();
        this.selectedStudent = resolveStudent(model.selectedEnrollment());
        initComponents(model);
    }

    /**
     * Hiển thị hộp thoại.
     */
    public static ScoreFormResult showDialog(Component parent, ScoreFormModel model) {
        ScoreFormDialog dialog = new ScoreFormDialog(parent, model);
        dialog.setVisible(true);
        return dialog.result;
    }

    /**
     * Khởi tạo các thành phần giao diện.
     */
    private void initComponents(ScoreFormModel model) {
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
        bodyPanel.add(Box.createVerticalStrut(24));
        bodyPanel.add(createScoreSection());

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
        configureSuggestionRenderer();
        configureEnrollmentRenderer();
        bindModel(model);
        registerStudentPickerBehavior();

        getRootPane().setDefaultButton(saveButton);
        setMinimumSize(new Dimension(760, 620));
        setSize(new Dimension(820, 680));
        setLocationRelativeTo(getOwner());
    }

    /**
     * Xử lý model bind.
     */
    private void bindModel(ScoreFormModel model) {
        applyingStudentSelection = true;
        studentSearchField.setText(selectedStudent == null ? "" : formatStudentDisplay(selectedStudent));
        applyingStudentSelection = false;
        refreshSuggestions();
        refreshEnrollmentChoices(model.selectedEnrollment());
        updateSelectedStudentLabel();
        validationLabel.setText(" ");

        processScoreField.setText(model.processScore());
        midtermScoreField.setText(model.midtermScore());
        finalScoreField.setText(model.finalScore());

        SwingUtilities.invokeLater(() -> studentSearchField.requestFocusInWindow());
    }

    /**
     * Tạo phần sinh viên.
     */
    private JPanel createStudentSection() {
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setVisibleRowCount(8);
        suggestionList.setFixedCellHeight(30);
        suggestionList.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JScrollPane suggestionScrollPane = new JScrollPane(suggestionList);
        suggestionScrollPane.setBorder(BorderFactory.createLineBorder(AppColors.INPUT_BORDER));
        suggestionScrollPane.setPreferredSize(new Dimension(320, 260));
        suggestionScrollPane.setMinimumSize(new Dimension(0, 220));
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
        return createSection(
                "Sinh viên",
                "Tìm theo mã hoặc họ tên rồi chọn từ danh sách gợi ý để lọc đúng đăng ký học phần.",
                contentPanel
        );
    }

    /**
     * Tạo đăng ký phần.
     */
    private JPanel createEnrollmentSection() {
        JPanel contentPanel = createFieldGridPanel();
        contentPanel.add(createField("Đăng ký học phần", styleComboBox(enrollmentComboBox)), fieldConstraints(0, 0, 2));
        return createSection(
                "Thông tin bản ghi",
                "Sau khi chọn sinh viên, danh sách đăng ký sẽ tự thu gọn theo sinh viên đó.",
                contentPanel
        );
    }

    /**
     * Tạo điểm phần.
     */
    private JPanel createScoreSection() {
        JPanel contentPanel = createFieldGridPanel();
        contentPanel.add(createField("Điểm quá trình", styleTextField(processScoreField)), fieldConstraints(0, 0));
        contentPanel.add(createField("Điểm giữa kỳ", styleTextField(midtermScoreField)), fieldConstraints(1, 0));
        contentPanel.add(createField("Điểm cuối kỳ", styleTextField(finalScoreField)), fieldConstraints(0, 1, 2));
        return createSection("Điểm thành phần", "Các giá trị vẫn được kiểm tra bởi flow xử lý hiện tại.", contentPanel);
    }

    /**
     * Cài đặt các lắng nghe cho bộ chọn sinh viên.
     */
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
                if (!applyingStudentSelection) {
                    selectedStudent = null;
                    validationLabel.setText(" ");
                    refreshEnrollmentChoices(null);
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

    /**
     * Cài đặt renderer cho gợi ý sinh viên.
     */
    private void configureSuggestionRenderer() {
        suggestionList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Student student) {
                    setText(formatStudentDisplay(student));
                }
                return component;
            }
        });
    }

    /**
     * Cài đặt renderer cho chọn box đăng ký.
     */
    private void configureEnrollmentRenderer() {
        enrollmentComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Enrollment enrollment) {
                    setText(formatEnrollmentDisplay(enrollment));
                } else {
                    setText("");
                }
                return component;
            }
        });
    }

    /**
     * Làm mới danh sách gợi ý sinh viên.
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
     * Làm mới danh sách đăng ký theo sinh viên đang chọn.
     */
    private void refreshEnrollmentChoices(Enrollment preferredEnrollment) {
        enrollmentComboBox.removeAllItems();
        List<Enrollment> visibleEnrollments = filterEnrollmentsBySelectedStudent();
        for (Enrollment enrollment : visibleEnrollments) {
            enrollmentComboBox.addItem(enrollment);
        }

        if (preferredEnrollment != null) {
            enrollmentComboBox.setSelectedItem(preferredEnrollment);
        } else if (enrollmentComboBox.getItemCount() > 0) {
            enrollmentComboBox.setSelectedIndex(0);
        }
    }

    /**
     * Xác nhận sinh viên được chọn.
     */
    private void commitSelectedStudent() {
        Student student = suggestionList.getSelectedValue();
        if (student == null) {
            return;
        }
        selectedStudent = student;
        applyingStudentSelection = true;
        studentSearchField.setText(formatStudentDisplay(student));
        applyingStudentSelection = false;
        validationLabel.setText(" ");
        refreshSuggestions();
        refreshEnrollmentChoices(null);
        updateSelectedStudentLabel();
    }

    /**
     * Cập nhật nhãn sinh viên đã chọn.
     */
    private void updateSelectedStudentLabel() {
        selectedStudentLabel.setText(selectedStudent == null
                ? "Chưa chọn sinh viên"
                : "Đã chọn: " + formatStudentDisplay(selectedStudent));
    }

    /**
     * Kiểm tra dữ liệu và đóng hộp thoại.
     */
    private void handleSave() {
        Enrollment selectedEnrollment = (Enrollment) enrollmentComboBox.getSelectedItem();
        if (selectedEnrollment == null) {
            validationLabel.setText("Hãy chọn đúng đăng ký học phần cần lưu điểm.");
            return;
        }

        if (selectedStudent != null && !matchesSelectedStudent(selectedEnrollment)) {
            validationLabel.setText("Đăng ký học phần không khớp với sinh viên đã chọn.");
            return;
        }

        result = new ScoreFormResult(
                selectedEnrollment,
                processScoreField.getText(),
                midtermScoreField.getText(),
                finalScoreField.getText()
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
    private GridBagConstraints fieldConstraints(int x, int y) {
        return fieldConstraints(x, y, 1);
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
        textField.setMinimumSize(new Dimension(160, 40));
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
     * Lọc sinh viên theo mã hoặc họ tên.
     */
    private List<Student> filterStudentsByKeyword(String keyword) {
        String normalizedKeyword = normalizeSearchText(keyword);
        List<Student> students = collectStudentsFromEnrollments();
        if (normalizedKeyword.isBlank()) {
            return students.stream().limit(8).toList();
        }

        return students.stream()
                .filter(student -> normalizeSearchText(student.getStudentCode()).contains(normalizedKeyword)
                        || normalizeSearchText(student.getFullName()).contains(normalizedKeyword))
                .limit(8)
                .toList();
    }

    /**
     * Thu gọn danh sách đăng ký theo sinh viên hiện tại.
     */
    private List<Enrollment> filterEnrollmentsBySelectedStudent() {
        if (selectedStudent == null) {
            return allEnrollments;
        }
        return allEnrollments.stream()
                .filter(this::matchesSelectedStudent)
                .toList();
    }

    /**
     * Thu danh sách sinh viên duy nhất từ enrollments hiện có.
     */
    private List<Student> collectStudentsFromEnrollments() {
        Map<String, Student> studentsByKey = new LinkedHashMap<>();
        for (Enrollment enrollment : allEnrollments) {
            Student student = resolveStudent(enrollment);
            if (student == null) {
                continue;
            }
            String key = student.getId() != null
                    ? "ID:" + student.getId()
                    : "CODE:" + normalizeSearchText(student.getStudentCode());
            studentsByKey.putIfAbsent(key, student);
        }
        return List.copyOf(studentsByKey.values());
    }

    /**
     * Kiểm tra enrollment có khớp sinh viên đã chọn hay không.
     */
    private boolean matchesSelectedStudent(Enrollment enrollment) {
        Student enrollmentStudent = resolveStudent(enrollment);
        if (selectedStudent == null || enrollmentStudent == null) {
            return false;
        }
        if (selectedStudent.getId() != null && enrollmentStudent.getId() != null) {
            return selectedStudent.getId().equals(enrollmentStudent.getId());
        }
        return normalizeSearchText(selectedStudent.getStudentCode())
                .equals(normalizeSearchText(enrollmentStudent.getStudentCode()));
    }

    /**
     * Định dạng sinh viên hiển thị.
     */
    private String formatStudentDisplay(Student student) {
        if (student == null) {
            return "";
        }
        return defaultText(student.getStudentCode()) + " - " + defaultText(student.getFullName());
    }

    /**
     * Định dạng đăng ký hiển thị.
     */
    private String formatEnrollmentDisplay(Enrollment enrollment) {
        if (enrollment == null) {
            return "";
        }

        String sectionCode = enrollment.getCourseSection() == null
                ? "Chưa cập nhật học phần"
                : defaultText(enrollment.getCourseSection().getSectionCode());
        String subjectName = enrollment.getCourseSection() == null || enrollment.getCourseSection().getSubject() == null
                ? ""
                : defaultText(enrollment.getCourseSection().getSubject().getSubjectName());
        String sectionText = subjectName.isBlank() ? sectionCode : sectionCode + " - " + subjectName;

        if (selectedStudent != null) {
            return sectionText;
        }

        Student student = resolveStudent(enrollment);
        String studentText = student == null ? "Chưa cập nhật sinh viên" : formatStudentDisplay(student);
        return studentText + " | " + sectionText;
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
     * Trả về sinh viên của enrollment hiện tại.
     */
    private Student resolveStudent(Enrollment enrollment) {
        return enrollment == null ? null : enrollment.getStudent();
    }

    /**
     * Trả về văn bản mặc định.
     */
    private String defaultText(String value) {
        return value == null || value.isBlank() ? "Chưa cập nhật" : value;
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
     * Xử lý model điểm biểu mẫu dữ liệu.
     */
    public record ScoreFormModel(
            String title,
            List<Enrollment> enrollments,
            Enrollment selectedEnrollment,
            String processScore,
            String midtermScore,
            String finalScore
    ) {
    }

    /**
     * Xử lý điểm kết quả biểu mẫu.
     */
    public record ScoreFormResult(
            Enrollment enrollment,
            String processScore,
            String midtermScore,
            String finalScore
    ) {
    }
}
