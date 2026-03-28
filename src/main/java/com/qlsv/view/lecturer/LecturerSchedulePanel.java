package com.qlsv.view.lecturer;

import com.qlsv.controller.ScheduleController;
import com.qlsv.model.Schedule;
import com.qlsv.utils.DialogUtil;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BasePanel;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

public class LecturerSchedulePanel extends BasePanel {

    private final ScheduleController scheduleController = new ScheduleController();
    private final DefaultTableModel tableModel;
    private final JLabel summaryLabel = new JLabel("Đang tải lịch dạy...");

    public LecturerSchedulePanel() {
        tableModel = new DefaultTableModel(
                new String[]{"Học phần", "Môn học", "Thứ", "Tiết", "Phòng", "Ghi chú"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JLabel titleLabel = new JLabel("Lịch dạy giảng viên");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);


        JPanel titlePanel = new JPanel(new BorderLayout(0, 6));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.NORTH);


        JTable table = new JTable(tableModel);
        configureTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.CARD_BORDER));
        scrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        JPanel tablePanel = new JPanel(new BorderLayout(0, 12));
        tablePanel.setOpaque(true);
        tablePanel.setBackground(AppColors.CARD_BACKGROUND);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JLabel headerLabel = new JLabel("Danh sách lịch dạy");
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 16f));
        headerLabel.setForeground(AppColors.CARD_TITLE_TEXT);

        summaryLabel.setForeground(AppColors.CARD_MUTED_TEXT);
        summaryLabel.setFont(summaryLabel.getFont().deriveFont(Font.PLAIN, 12.5f));

        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setOpaque(false);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(summaryLabel, BorderLayout.EAST);

        tablePanel.add(headerPanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(titlePanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        reloadData();
    }

    @Override
    public void reloadData() {
        try {
            tableModel.setRowCount(0);
            java.util.List<Schedule> schedules = scheduleController.getCurrentLecturerSchedules();
            summaryLabel.setText(schedules.size() + " buổi dạy đã xếp lịch");

            for (Schedule schedule : schedules) {
                tableModel.addRow(new Object[]{
                        schedule.getCourseSection() == null ? "" : schedule.getCourseSection().getSectionCode(),
                        schedule.getCourseSection() == null || schedule.getCourseSection().getSubject() == null
                                ? "" : schedule.getCourseSection().getSubject().getSubjectName(),
                        schedule.getDayOfWeek(),
                        DisplayTextUtil.formatPeriod(schedule.getStartPeriod(), schedule.getEndPeriod()),
                        schedule.getRoom() == null ? "" : schedule.getRoom().getRoomName(),
                        schedule.getNote()
                });
            }
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void configureTable(JTable table) {
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setGridColor(AppColors.CARD_BORDER);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(AppColors.TABLE_SELECTION_BACKGROUND);
        table.setSelectionForeground(AppColors.CARD_VALUE_TEXT);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(AppColors.TABLE_HEADER_BACKGROUND);
        table.getTableHeader().setForeground(AppColors.CARD_VALUE_TEXT);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 13f));
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));
    }
}
