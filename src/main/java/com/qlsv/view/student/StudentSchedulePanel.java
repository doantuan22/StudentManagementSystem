package com.qlsv.view.student;

import com.qlsv.controller.ScheduleController;
import com.qlsv.model.Schedule;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BasePanel;
import com.qlsv.view.common.DashboardCard;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

public class StudentSchedulePanel extends BasePanel {

    private final ScheduleController scheduleController = new ScheduleController();

    public StudentSchedulePanel() {
        setBackground(AppColors.CONTENT_BACKGROUND);
        initComponents();
        reloadData();
    }

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Học phần", "Môn học", "Giảng viên", "Thứ", "Tiết", "Phòng", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private DashboardCard totalSessionsCard;
    private DashboardCard subjectsCard;

    private void initComponents() {
        JLabel titleLabel = new JLabel("Lịch học sinh viên");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));

        totalSessionsCard = new DashboardCard("Tổng số lịch học", AppColors.STAT_CARD_SECTIONS);
        subjectsCard = new DashboardCard("Số học phần có lịch", AppColors.STAT_CARD_SUBJECTS);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 2, 12, 12));
        cardsPanel.setOpaque(false);
        cardsPanel.add(totalSessionsCard);
        cardsPanel.add(subjectsCard);

        JTable table = new JTable(tableModel);
        table.setRowHeight(24);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(true);
        tablePanel.setBackground(AppColors.CARD_BACKGROUND);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        tablePanel.add(new JLabel("Thời gian học theo danh sách"), BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton reloadButton = new JButton("Tải lại");
        reloadButton.addActionListener(event -> reloadData());

        JPanel actionPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        actionPanel.setOpaque(false);
        actionPanel.add(reloadButton);

        JPanel topWrapper = new JPanel(new BorderLayout(0, 12));
        topWrapper.setOpaque(false);
        topWrapper.add(titleLabel, BorderLayout.NORTH);
        topWrapper.add(actionPanel, BorderLayout.CENTER);
        topWrapper.add(cardsPanel, BorderLayout.SOUTH);

        add(topWrapper, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    public void reloadData() {
        try {
            List<Schedule> schedules = scheduleController.getCurrentStudentSchedules();
            totalSessionsCard.setValue(String.valueOf(schedules.size()));
            subjectsCard.setValue(String.valueOf(
                    schedules.stream()
                            .map(schedule -> schedule.getCourseSection() == null ? null : schedule.getCourseSection().getId())
                            .filter(java.util.Objects::nonNull)
                            .distinct()
                            .count()
            ));

            tableModel.setRowCount(0);
            for (Schedule schedule : schedules) {
                tableModel.addRow(new Object[]{
                        schedule.getCourseSection() == null ? "" : schedule.getCourseSection().getSectionCode(),
                        schedule.getCourseSection() == null || schedule.getCourseSection().getSubject() == null
                                ? "" : schedule.getCourseSection().getSubject().getSubjectName(),
                        schedule.getCourseSection() == null || schedule.getCourseSection().getLecturer() == null
                                ? "" : schedule.getCourseSection().getLecturer().getFullName(),
                        DisplayTextUtil.defaultText(schedule.getDayOfWeek()),
                        DisplayTextUtil.formatPeriod(schedule.getStartPeriod(), schedule.getEndPeriod()),
                        schedule.getRoom() == null ? "Chưa cập nhật" : schedule.getRoom().getRoomName(),
                        DisplayTextUtil.defaultText(schedule.getNote())
                });
            }
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }
}
