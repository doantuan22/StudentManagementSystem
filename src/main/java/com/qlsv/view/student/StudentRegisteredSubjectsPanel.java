package com.qlsv.view.student;

import com.qlsv.controller.EnrollmentController;
import com.qlsv.model.Enrollment;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.BasePanel;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class StudentRegisteredSubjectsPanel extends BasePanel {

    private final EnrollmentController enrollmentController = new EnrollmentController();

    public StudentRegisteredSubjectsPanel() {
        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"Học phần", "Môn học", "Giảng viên", "Trạng thái", "Lịch học"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), java.awt.BorderLayout.CENTER);

        try {
            for (Enrollment enrollment : enrollmentController.getCurrentStudentEnrollments()) {
                tableModel.addRow(new Object[]{
                        enrollment.getCourseSection() == null ? "" : enrollment.getCourseSection().getSectionCode(),
                        enrollment.getCourseSection() == null || enrollment.getCourseSection().getSubject() == null
                                ? "" : enrollment.getCourseSection().getSubject().getSubjectName(),
                        enrollment.getCourseSection() == null || enrollment.getCourseSection().getLecturer() == null
                                ? "" : enrollment.getCourseSection().getLecturer().getFullName(),
                        DisplayTextUtil.formatStatus(enrollment.getStatus()),
                        enrollment.getCourseSection() == null ? "" : enrollment.getCourseSection().getScheduleText()
                });
            }
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }
}
