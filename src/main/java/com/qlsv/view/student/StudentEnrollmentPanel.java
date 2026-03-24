package com.qlsv.view.student;

import com.qlsv.controller.CourseSectionController;
import com.qlsv.controller.EnrollmentController;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.BasePanel;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

public class StudentEnrollmentPanel extends BasePanel {

    private final EnrollmentController enrollmentController = new EnrollmentController();
    private final CourseSectionController courseSectionController = new CourseSectionController();
    private final JComboBox<CourseSection> courseSectionComboBox = new JComboBox<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Hoc phan", "Mon hoc", "Giang vien", "Trang thai", "Dang ky luc"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public StudentEnrollmentPanel() {
        initComponents();
        reloadData();
    }

    private void initComponents() {
        JButton registerButton = new JButton("Dang ky hoc phan");
        registerButton.addActionListener(event -> registerSelectedCourseSection());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topPanel.add(courseSectionComboBox);
        topPanel.add(registerButton);

        JTable table = new JTable(tableModel);
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void registerSelectedCourseSection() {
        CourseSection selectedCourseSection = (CourseSection) courseSectionComboBox.getSelectedItem();
        if (selectedCourseSection == null) {
            DialogUtil.showError(this, "Chua co hoc phan nao de dang ky.");
            return;
        }
        try {
            enrollmentController.registerCurrentStudent(selectedCourseSection.getId());
            DialogUtil.showInfo(this, "Dang ky hoc phan thanh cong.");
            reloadData();
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void reloadData() {
        try {
            courseSectionComboBox.removeAllItems();
            for (CourseSection courseSection : courseSectionController.getAllCourseSectionsForSelection()) {
                courseSectionComboBox.addItem(courseSection);
            }

            tableModel.setRowCount(0);
            List<Enrollment> enrollments = enrollmentController.getCurrentStudentEnrollments();
            for (Enrollment enrollment : enrollments) {
                tableModel.addRow(new Object[]{
                        enrollment.getId(),
                        enrollment.getCourseSection() == null ? "" : enrollment.getCourseSection().getSectionCode(),
                        enrollment.getCourseSection() == null || enrollment.getCourseSection().getSubject() == null
                                ? "" : enrollment.getCourseSection().getSubject().getSubjectName(),
                        enrollment.getCourseSection() == null || enrollment.getCourseSection().getLecturer() == null
                                ? "" : enrollment.getCourseSection().getLecturer().getFullName(),
                        enrollment.getStatus(),
                        enrollment.getEnrolledAt()
                });
            }
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }
}
