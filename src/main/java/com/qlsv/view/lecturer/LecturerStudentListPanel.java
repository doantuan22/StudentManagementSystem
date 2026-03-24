package com.qlsv.view.lecturer;

import com.qlsv.controller.EnrollmentController;
import com.qlsv.controller.LecturerController;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Lecturer;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.BasePanel;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class LecturerStudentListPanel extends BasePanel {

    private final LecturerController lecturerController = new LecturerController();
    private final EnrollmentController enrollmentController = new EnrollmentController();

    public LecturerStudentListPanel() {
        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"Hoc phan", "Ma SV", "Sinh vien", "Email", "Trang thai dang ky"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), java.awt.BorderLayout.CENTER);

        try {
            Lecturer lecturer = lecturerController.getCurrentLecturer();
            for (Enrollment enrollment : enrollmentController.getLecturerEnrollments(lecturer.getId())) {
                tableModel.addRow(new Object[]{
                        enrollment.getCourseSection() == null ? "" : enrollment.getCourseSection().getSectionCode(),
                        enrollment.getStudent() == null ? "" : enrollment.getStudent().getStudentCode(),
                        enrollment.getStudent() == null ? "" : enrollment.getStudent().getFullName(),
                        enrollment.getStudent() == null ? "" : enrollment.getStudent().getEmail(),
                        enrollment.getStatus()
                });
            }
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }
}
