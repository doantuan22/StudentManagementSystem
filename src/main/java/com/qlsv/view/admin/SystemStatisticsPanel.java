package com.qlsv.view.admin;

import com.qlsv.controller.ReportController;
import com.qlsv.model.SystemStatistics;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BasePanel;
import com.qlsv.view.common.DashboardCard;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class SystemStatisticsPanel extends BasePanel {

    private final ReportController reportController = new ReportController();
    private final DashboardCard studentsCard = new DashboardCard("Tổng sinh viên", AppColors.STAT_CARD_STUDENTS);
    private final DashboardCard lecturersCard = new DashboardCard("Tổng giảng viên", AppColors.STAT_CARD_LECTURERS);
    private final DashboardCard subjectsCard = new DashboardCard("Tổng môn học", AppColors.STAT_CARD_SUBJECTS);
    private final DashboardCard sectionsCard = new DashboardCard("Tổng học phần", AppColors.STAT_CARD_SECTIONS);
    private final DashboardCard enrollmentsCard = new DashboardCard("Tổng đăng ký học phần", AppColors.STAT_CARD_ENROLLMENTS);

    public SystemStatisticsPanel() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        JPanel gridPanel = new JPanel(new GridLayout(0, 3, 16, 16));
        gridPanel.setOpaque(false);
        gridPanel.add(studentsCard);
        gridPanel.add(lecturersCard);
        gridPanel.add(subjectsCard);
        gridPanel.add(sectionsCard);
        gridPanel.add(enrollmentsCard);

        add(gridPanel, BorderLayout.CENTER);
        reloadStatistics();
    }

    public final void reloadStatistics() {
        try {
            SystemStatistics systemStatistics = reportController.getSystemStatistics();
            studentsCard.setValue(String.valueOf(systemStatistics.getTotalStudents()));
            lecturersCard.setValue(String.valueOf(systemStatistics.getTotalLecturers()));
            subjectsCard.setValue(String.valueOf(systemStatistics.getTotalSubjects()));
            sectionsCard.setValue(String.valueOf(systemStatistics.getTotalCourseSections()));
            enrollmentsCard.setValue(String.valueOf(systemStatistics.getTotalEnrollments()));
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }
}
