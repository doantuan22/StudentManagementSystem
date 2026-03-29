/**
 * Hộp thoại học phần biểu mẫu dialog.
 */
package com.qlsv.view.dialog;

import com.qlsv.model.Lecturer;
import com.qlsv.model.Subject;
import com.qlsv.view.common.AppColors;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CourseSectionFormDialog extends JDialog {

    private final JTextField sectionCodeField = new JTextField();
    private final JComboBox<Subject> subjectComboBox = new JComboBox<>();
    private final JComboBox<Lecturer> lecturerComboBox = new JComboBox<>();
    private final JComboBox<String> semesterComboBox = new JComboBox<>();
    private final JTextField schoolYearField = new JTextField();
    private final JTextField maxStudentsField = new JTextField();
    private final JTextArea noteArea = new JTextArea();

    private List<Lecturer> allLecturers = List.of();
    private Map<Long, List<Lecturer>> lecturersBySubjectId = Map.of();
    private boolean updatingLecturerOptions;

    private CourseSectionFormResult result;

    /**
     * Khởi tạo học phần biểu mẫu.
     */
    private CourseSectionFormDialog(Component parent, CourseSectionFormModel model) {
        super(resolveOwner(parent), model.title(), Dialog.ModalityType.APPLICATION_MODAL);
        initComponents(model);
    }

    /**
     * Hiển thị hộp thoại và trả về kết quả cấu hình học phần sau khi người dùng xác nhận.
     */
    public static CourseSectionFormResult showDialog(Component parent, CourseSectionFormModel model) {
        CourseSectionFormDialog dialog = new CourseSectionFormDialog(parent, model);
        dialog.setVisible(true);
        return dialog.result;
    }

    /**
     * Khởi tạo các thành phần giao diện cho form quản lý học phần.
     */
    private void initComponents(CourseSectionFormModel model) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        subjectComboBox.addActionListener(event -> {
            if (!updatingLecturerOptions) {
                refreshLecturerOptions((Lecturer) lecturerComboBox.getSelectedItem(), false);
            }
        });

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
        bodyPanel.add(createOverviewSection());
        bodyPanel.add(Box.createVerticalStrut(24));
        bodyPanel.add(createAcademicSection());
        bodyPanel.add(Box.createVerticalStrut(24));
        bodyPanel.add(createNoteSection());

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
        bindModel(model);

        getRootPane().setDefaultButton(saveButton);
        setMinimumSize(new Dimension(720, 580));
        setSize(new Dimension(760, 620));
        setLocationRelativeTo(getOwner());
    }

    /**
     * Đổ dữ liệu từ model vào các trường tương ứng trên giao diện.
     */
    private void bindModel(CourseSectionFormModel model) {
        sectionCodeField.setText(model.sectionCode());
        schoolYearField.setText(model.schoolYear());
        maxStudentsField.setText(model.maxStudents());
        noteArea.setText(model.infoMessage());
        allLecturers = model.lecturers() == null ? List.of() : List.copyOf(model.lecturers());
        lecturersBySubjectId = model.lecturersBySubjectId() == null ? Map.of() : model.lecturersBySubjectId();

        subjectComboBox.removeAllItems();
        for (Subject subject : model.subjects()) {
            subjectComboBox.addItem(subject);
        }
        if (model.selectedSubject() != null) {
            subjectComboBox.setSelectedItem(model.selectedSubject());
        }

        refreshLecturerOptions(model.selectedLecturer(), true);

        semesterComboBox.removeAllItems();
        for (String semester : model.semesters()) {
            semesterComboBox.addItem(semester);
        }
        if (model.selectedSemester() != null) {
            semesterComboBox.setSelectedItem(model.selectedSemester());
        }

        SwingUtilities.invokeLater(() -> sectionCodeField.requestFocusInWindow());
    }

    /**
     * Tạo overview phần.
     */
    private JPanel createOverviewSection() {
        JPanel contentPanel = createFieldGridPanel();
        contentPanel.add(createField("Mã học phần", styleTextField(sectionCodeField)), fieldConstraints(0, 0));
        contentPanel.add(createField("Môn học", styleComboBox(subjectComboBox)), fieldConstraints(1, 0));
        contentPanel.add(createField("Giảng viên", styleComboBox(lecturerComboBox)), fieldConstraints(0, 1, 2));
        return createSection("Thông tin cơ bản", "Cấu hình học phần và giảng viên phụ trách.", contentPanel);
    }

    /**
     * Tạo học vụ phần.
     */
    private JPanel createAcademicSection() {
        JPanel contentPanel = createFieldGridPanel();
        contentPanel.add(createField("Học kỳ", styleComboBox(semesterComboBox)), fieldConstraints(0, 0));
        contentPanel.add(createField("Năm học", styleTextField(schoolYearField)), fieldConstraints(1, 0));
        contentPanel.add(createField("Sĩ số tối đa", styleTextField(maxStudentsField)), fieldConstraints(0, 1, 2));
        return createSection("Thông tin học vụ", "Thiết lập học kỳ, năm học và sức chứa lớp.", contentPanel);
    }

    /**
     * Tạo note phần.
     */
    private JPanel createNoteSection() {
        noteArea.setEditable(false);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setRows(3);
        noteArea.setFont(noteArea.getFont().deriveFont(Font.PLAIN, 13f));
        noteArea.setForeground(AppColors.CARD_MUTED_TEXT);
        noteArea.setBackground(AppColors.CARD_BACKGROUND);
        noteArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JScrollPane scrollPane = new JScrollPane(noteArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.CARD_BORDER));
        scrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);

        return createSection(
                "Ghi chú",
               "Lịch học và phòng học tiếp tục được quản lý tại màn hình lịch học.",
                scrollPane
        );
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
        comboBox.setPreferredSize(new Dimension(comboBox.getPreferredSize().width, 40));
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
     * Cập nhật danh sách giảng viên khả dụng dựa trên môn học đã chọn.
     */
    private void refreshLecturerOptions(Lecturer preferredLecturer, boolean allowLegacySelection) {
        updatingLecturerOptions = true;
        try {
            List<Lecturer> availableLecturers = resolveAvailableLecturers(preferredLecturer, allowLegacySelection);
            Lecturer lecturerToSelect = preferredLecturer == null
                    ? (Lecturer) lecturerComboBox.getSelectedItem()
                    : preferredLecturer;

            lecturerComboBox.removeAllItems();
            for (Lecturer lecturer : availableLecturers) {
                lecturerComboBox.addItem(lecturer);
            }

            if (lecturerToSelect != null && containsLecturer(availableLecturers, lecturerToSelect)) {
                lecturerComboBox.setSelectedItem(lecturerToSelect);
            } else if (lecturerComboBox.getItemCount() > 0) {
                lecturerComboBox.setSelectedIndex(0);
            }
        } finally {
            updatingLecturerOptions = false;
        }
    }

    /**
     * Xác định danh sách giảng viên phù hợp cho môn học đang được chọn.
     */
    private List<Lecturer> resolveAvailableLecturers(Lecturer preferredLecturer, boolean allowLegacySelection) {
        Subject selectedSubject = (Subject) subjectComboBox.getSelectedItem();
        List<Lecturer> filteredLecturers = selectedSubject == null || selectedSubject.getId() == null
                ? List.of()
                : lecturersBySubjectId.getOrDefault(selectedSubject.getId(), List.of());

        List<Lecturer> availableLecturers = new ArrayList<>();
        if (filteredLecturers == null || filteredLecturers.isEmpty()) {
            availableLecturers.addAll(allLecturers);
        } else {
            availableLecturers.addAll(filteredLecturers);
        }

        if (allowLegacySelection
                && preferredLecturer != null
                && !containsLecturer(availableLecturers, preferredLecturer)) {
            availableLecturers.add(preferredLecturer);
        }
        return availableLecturers;
    }

    /**
     * Xử lý contains giảng viên.
     */
    private boolean containsLecturer(List<Lecturer> lecturers, Lecturer target) {
        if (target == null || lecturers == null) {
            return false;
        }
        for (Lecturer lecturer : lecturers) {
            if (target.equals(lecturer)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Thu thập dữ liệu từ các trường nhập liệu và đóng hộp thoại.
     */
    private void handleSave() {
        result = new CourseSectionFormResult(
                sectionCodeField.getText(),
                (Subject) subjectComboBox.getSelectedItem(),
                (Lecturer) lecturerComboBox.getSelectedItem(),
                (String) semesterComboBox.getSelectedItem(),
                schoolYearField.getText(),
                maxStudentsField.getText()
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
     * Xử lý model học phần biểu mẫu.
     */
    public record CourseSectionFormModel(
            String title,
            String sectionCode,
            List<Subject> subjects,
            Subject selectedSubject,
            List<Lecturer> lecturers,
            Lecturer selectedLecturer,
            Map<Long, List<Lecturer>> lecturersBySubjectId,
            List<String> semesters,
            String selectedSemester,
            String schoolYear,
            String maxStudents,
            String infoMessage
    ) {
    }

    /**
     * Xử lý học phần kết quả biểu mẫu.
     */
    public record CourseSectionFormResult(
            String sectionCode,
            Subject subject,
            Lecturer lecturer,
            String semester,
            String schoolYear,
            String maxStudents
    ) {
    }
}
