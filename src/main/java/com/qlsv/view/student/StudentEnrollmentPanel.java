package com.qlsv.view.student;

import com.qlsv.controller.CourseSectionController;
import com.qlsv.controller.EnrollmentController;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BasePanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentEnrollmentPanel extends BasePanel {

    private final EnrollmentController enrollmentController = new EnrollmentController();
    private final CourseSectionController courseSectionController = new CourseSectionController();

    private final JTextField searchField = new JTextField(22);
    private final JComboBox<String> semesterFilterComboBox = new JComboBox<>(new String[]{"Tất cả học kỳ"});

    private final List<CourseSection> allCourseSections = new ArrayList<>();
    private final List<CourseSection> displayedCourseSections = new ArrayList<>();
    private final List<Enrollment> currentEnrollments = new ArrayList<>();

    private final DefaultTableModel courseSectionTableModel = new DefaultTableModel(
            new String[]{"ID", "Học phần", "Môn học", "Tín chỉ", "Sĩ số", "Giảng viên", "Học kỳ", "Năm học", "Phòng", "Lịch"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final DefaultTableModel enrollmentTableModel = new DefaultTableModel(
            new String[]{"ID", "Học phần", "Môn học", "Giảng viên", "Trạng thái", "Đăng ký lúc"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable courseSectionTable = new JTable(courseSectionTableModel);
    private final JTable enrollmentTable = new JTable(enrollmentTableModel);

    public StudentEnrollmentPanel() {
        setBackground(AppColors.CONTENT_BACKGROUND);
        initComponents();
        reloadData();
    }

    private void initComponents() {
        JLabel titleLabel = new JLabel("Đăng ký học phần");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.WEST);

        JButton filterButton = new JButton("Lọc");
        JButton registerButton = new JButton("Đăng ký học phần đã chọn");
        JButton cancelButton = new JButton("Hủy đăng ký đã chọn");
        JButton reloadButton = new JButton("Tải lại");

        filterButton.addActionListener(event -> applyFilters());
        registerButton.addActionListener(event -> registerSelectedCourseSection());
        cancelButton.addActionListener(event -> cancelSelectedEnrollment());
        reloadButton.addActionListener(event -> reloadData());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topPanel.setOpaque(false);
        topPanel.add(new JLabel("Tìm kiếm:"));
        topPanel.add(searchField);
        topPanel.add(new JLabel("Học kỳ:"));
        topPanel.add(semesterFilterComboBox);
        topPanel.add(filterButton);
        topPanel.add(registerButton);
        topPanel.add(cancelButton);
        topPanel.add(reloadButton);

        courseSectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        enrollmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseSectionTable.setRowHeight(24);
        enrollmentTable.setRowHeight(24);

        JPanel coursePanel = new JPanel(new BorderLayout(0, 8));
        coursePanel.setOpaque(true);
        coursePanel.setBackground(AppColors.CARD_BACKGROUND);
        coursePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        coursePanel.add(new JLabel("Danh sách học phần mở"), BorderLayout.NORTH);
        coursePanel.add(new JScrollPane(courseSectionTable), BorderLayout.CENTER);

        JPanel enrollmentPanel = new JPanel(new BorderLayout(0, 8));
        enrollmentPanel.setOpaque(true);
        enrollmentPanel.setBackground(AppColors.CARD_BACKGROUND);
        enrollmentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        enrollmentPanel.add(new JLabel("Các học phần đã đăng ký"), BorderLayout.NORTH);
        enrollmentPanel.add(new JScrollPane(enrollmentTable), BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new java.awt.GridLayout(2, 1, 12, 12));
        centerPanel.setOpaque(false);
        centerPanel.add(coursePanel);
        centerPanel.add(enrollmentPanel);

        JPanel headerPanel = new JPanel(new BorderLayout(0, 10));
        headerPanel.setOpaque(false);
        headerPanel.add(titlePanel, BorderLayout.NORTH);
        headerPanel.add(topPanel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void registerSelectedCourseSection() {
        int selectedRow = courseSectionTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= displayedCourseSections.size()) {
            DialogUtil.showError(this, "Hãy chọn một học phần trong danh sách để đăng ký.");
            return;
        }
        CourseSection selectedCourseSection = displayedCourseSections.get(selectedRow);
        try {
            enrollmentController.registerCurrentStudent(selectedCourseSection.getId());
            DialogUtil.showInfo(this, "Đăng ký học phần thành công.");
            reloadData();
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void cancelSelectedEnrollment() {
        int selectedRow = enrollmentTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentEnrollments.size()) {
            DialogUtil.showError(this, "Hãy chọn học phần đã đăng ký cần hủy.");
            return;
        }
        Enrollment enrollment = currentEnrollments.get(selectedRow);
        try {
            enrollmentController.cancelCurrentStudentEnrollment(enrollment.getId());
            DialogUtil.showInfo(this, "Hủy đăng ký học phần thành công.");
            reloadData();
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    @Override
    public void reloadData() {
        try {
            loadCourseSections();
            loadCurrentEnrollments();
            buildSemesterFilter();
            applyFilters();
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void loadCourseSections() {
        allCourseSections.clear();
        allCourseSections.addAll(courseSectionController.getAllCourseSectionsForSelection());
    }

    private void loadCurrentEnrollments() {
        currentEnrollments.clear();
        currentEnrollments.addAll(enrollmentController.getCurrentStudentEnrollments());

        enrollmentTableModel.setRowCount(0);
        for (Enrollment enrollment : currentEnrollments) {
            enrollmentTableModel.addRow(new Object[]{
                    enrollment.getId(),
                    enrollment.getCourseSection() == null ? "" : enrollment.getCourseSection().getSectionCode(),
                    enrollment.getCourseSection() == null || enrollment.getCourseSection().getSubject() == null
                            ? "" : enrollment.getCourseSection().getSubject().getSubjectName(),
                    enrollment.getCourseSection() == null || enrollment.getCourseSection().getLecturer() == null
                            ? "" : enrollment.getCourseSection().getLecturer().getFullName(),
                    DisplayTextUtil.formatStatus(enrollment.getStatus()),
                    DisplayTextUtil.formatDateTime(enrollment.getEnrolledAt())
            });
        }
    }

    private void buildSemesterFilter() {
        Object selected = semesterFilterComboBox.getSelectedItem();
        semesterFilterComboBox.removeAllItems();
        semesterFilterComboBox.addItem("Tất cả học kỳ");

        List<String> added = new ArrayList<>();
        for (CourseSection courseSection : allCourseSections) {
            String semester = courseSection.getSemester();
            if (semester != null && !semester.isBlank() && !added.contains(semester)) {
                added.add(semester);
                semesterFilterComboBox.addItem(semester);
            }
        }

        if (selected != null) {
            semesterFilterComboBox.setSelectedItem(selected.toString());
        }
    }

    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String semesterFilter = semesterFilterComboBox.getSelectedItem() == null
                ? "Tất cả học kỳ" : semesterFilterComboBox.getSelectedItem().toString();

        displayedCourseSections.clear();
        courseSectionTableModel.setRowCount(0);

        for (CourseSection courseSection : allCourseSections) {
            boolean matchesKeyword = keyword.isBlank()
                    || containsIgnoreCase(courseSection.getSectionCode(), keyword)
                    || (courseSection.getSubject() != null && containsIgnoreCase(courseSection.getSubject().getSubjectName(), keyword))
                    || (courseSection.getLecturer() != null && containsIgnoreCase(courseSection.getLecturer().getFullName(), keyword));

            boolean matchesSemester = "Tất cả học kỳ".equalsIgnoreCase(semesterFilter)
                    || semesterFilter.equalsIgnoreCase(DisplayTextUtil.defaultText(courseSection.getSemester()));

            if (matchesKeyword && matchesSemester) {
                displayedCourseSections.add(courseSection);
                int currentEnrollmentsCount = enrollmentController.countEnrollmentsByCourseSection(courseSection.getId());
                String slots = currentEnrollmentsCount + "/" + courseSection.getMaxStudents();

                courseSectionTableModel.addRow(new Object[]{
                        courseSection.getId(),
                        courseSection.getSectionCode(),
                        courseSection.getSubject() == null ? "" : courseSection.getSubject().getSubjectName(),
                        courseSection.getSubject() == null ? "" : courseSection.getSubject().getCredits(),
                        slots,
                        courseSection.getLecturer() == null ? "" : courseSection.getLecturer().getFullName(),
                        DisplayTextUtil.defaultText(courseSection.getSemester()),
                        DisplayTextUtil.defaultText(courseSection.getSchoolYear()),
                        courseSection.getRoom() == null ? "Chưa cập nhật" : courseSection.getRoom().getRoomName(),
                        DisplayTextUtil.defaultText(courseSection.getScheduleText())
                });
            }
        }
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
