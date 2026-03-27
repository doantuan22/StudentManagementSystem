package com.qlsv.view.lecturer;

import com.qlsv.controller.CourseSectionController;
import com.qlsv.controller.LecturerController;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Lecturer;
import com.qlsv.utils.AcademicFormatUtil;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.BasePanel;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class LecturerClassPanel extends BasePanel {

    private final LecturerController lecturerController = new LecturerController();
    private final CourseSectionController courseSectionController = new CourseSectionController();

    public LecturerClassPanel() {
        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"Mã học phần", "Môn học", "Phòng học", "Học kỳ", "Năm học", "Lịch học"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), java.awt.BorderLayout.CENTER);

        try {
            Lecturer lecturer = lecturerController.getCurrentLecturer();
            for (CourseSection courseSection : courseSectionController.getCourseSectionsByLecturer(lecturer.getId())) {
                tableModel.addRow(new Object[]{
                        courseSection.getSectionCode(),
                        courseSection.getSubject() == null ? "" : courseSection.getSubject().getSubjectName(),
                        courseSection.getRoom(),
                        AcademicFormatUtil.formatSemester(courseSection.getSemester()),
                        AcademicFormatUtil.formatAcademicYear(courseSection.getSchoolYear()),
                        courseSection.getScheduleText()
                });
            }
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }
}
