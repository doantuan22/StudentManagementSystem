package com.qlsv.view.admin;

import com.qlsv.controller.ReportController;
import com.qlsv.model.SystemStatistics;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.BasePanel;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class SystemStatisticsPanel extends BasePanel {

    private final ReportController reportController = new ReportController();
    private final JLabel studentsValueLabel = new JLabel("-");
    private final JLabel lecturersValueLabel = new JLabel("-");
    private final JLabel subjectsValueLabel = new JLabel("-");
    private final JLabel sectionsValueLabel = new JLabel("-");
    private final JLabel enrollmentsValueLabel = new JLabel("-");

    public SystemStatisticsPanel() {
        JButton reloadButton = new JButton("Tai lai thong ke");
        reloadButton.addActionListener(event -> loadStatistics());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(new JLabel("Thong ke he thong"), BorderLayout.WEST);
        headerPanel.add(reloadButton, BorderLayout.EAST);

        JPanel gridPanel = new JPanel(new GridLayout(0, 2, 12, 12));
        gridPanel.add(new JLabel("Tong sinh vien"));
        gridPanel.add(studentsValueLabel);
        gridPanel.add(new JLabel("Tong giang vien"));
        gridPanel.add(lecturersValueLabel);
        gridPanel.add(new JLabel("Tong mon hoc"));
        gridPanel.add(subjectsValueLabel);
        gridPanel.add(new JLabel("Tong hoc phan"));
        gridPanel.add(sectionsValueLabel);
        gridPanel.add(new JLabel("Tong dang ky hoc phan"));
        gridPanel.add(enrollmentsValueLabel);

        add(headerPanel, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);
        loadStatistics();
    }

    protected final void loadStatistics() {
        try {
            SystemStatistics systemStatistics = reportController.getSystemStatistics();
            studentsValueLabel.setText(String.valueOf(systemStatistics.getTotalStudents()));
            lecturersValueLabel.setText(String.valueOf(systemStatistics.getTotalLecturers()));
            subjectsValueLabel.setText(String.valueOf(systemStatistics.getTotalSubjects()));
            sectionsValueLabel.setText(String.valueOf(systemStatistics.getTotalCourseSections()));
            enrollmentsValueLabel.setText(String.valueOf(systemStatistics.getTotalEnrollments()));
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }
}
