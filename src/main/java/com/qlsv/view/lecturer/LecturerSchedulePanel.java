package com.qlsv.view.lecturer;

import com.qlsv.controller.ScheduleController;
import com.qlsv.model.Schedule;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.BasePanel;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class LecturerSchedulePanel extends BasePanel {

    private final ScheduleController scheduleController = new ScheduleController();

    public LecturerSchedulePanel() {
        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"Hoc phan", "Mon hoc", "Lop", "Thu", "Tiet", "Phong", "Ghi chu"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), java.awt.BorderLayout.CENTER);

        try {
            for (Schedule schedule : scheduleController.getCurrentLecturerSchedules()) {
                tableModel.addRow(new Object[]{
                        schedule.getCourseSection() == null ? "" : schedule.getCourseSection().getSectionCode(),
                        schedule.getCourseSection() == null || schedule.getCourseSection().getSubject() == null
                                ? "" : schedule.getCourseSection().getSubject().getSubjectName(),
                        schedule.getCourseSection() == null || schedule.getCourseSection().getClassRoom() == null
                                ? "" : schedule.getCourseSection().getClassRoom().getClassName(),
                        schedule.getDayOfWeek(),
                        schedule.getStartPeriod() + "-" + schedule.getEndPeriod(),
                        schedule.getRoom(),
                        schedule.getNote()
                });
            }
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }
}
