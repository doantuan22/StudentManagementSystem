package com.qlsv.view.lecturer;

import com.qlsv.controller.ScheduleController;
import com.qlsv.model.Schedule;
import com.qlsv.utils.DialogUtil;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BasePanel;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class LecturerSchedulePanel extends BasePanel {

    private final ScheduleController scheduleController = new ScheduleController();
    private final DefaultTableModel tableModel;

    public LecturerSchedulePanel() {
        tableModel = new DefaultTableModel(
                new String[]{"Học phần", "Môn học", "Thứ", "Tiết", "Phòng", "Ghi chú"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setSelectionBackground(AppColors.TABLE_SELECTION_BACKGROUND);
        table.getTableHeader().setBackground(AppColors.TABLE_HEADER_BACKGROUND);

        add(new JScrollPane(table), java.awt.BorderLayout.CENTER);
        reloadData();
    }

    @Override
    public void reloadData() {
        try {
            tableModel.setRowCount(0);
            for (Schedule schedule : scheduleController.getCurrentLecturerSchedules()) {
                tableModel.addRow(new Object[]{
                        schedule.getCourseSection() == null ? "" : schedule.getCourseSection().getSectionCode(),
                        schedule.getCourseSection() == null || schedule.getCourseSection().getSubject() == null
                                ? "" : schedule.getCourseSection().getSubject().getSubjectName(),
                        schedule.getDayOfWeek(),
                        DisplayTextUtil.formatPeriod(schedule.getStartPeriod(), schedule.getEndPeriod()),
                        schedule.getRoom(),
                        schedule.getNote()
                });
            }
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }
}
