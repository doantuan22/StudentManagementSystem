/**
 * Màn hình quản trị cho thống kê hệ thống.
 */
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
    import java.awt.FlowLayout;

    public class SystemStatisticsPanel extends BasePanel {

        private final ReportController reportController = new ReportController();
        private final DashboardCard studentsCard = new DashboardCard("Tổng sinh viên", AppColors.STAT_CARD_STUDENTS);
        private final DashboardCard lecturersCard = new DashboardCard("Tổng giảng viên", AppColors.STAT_CARD_LECTURERS);
        private final DashboardCard subjectsCard = new DashboardCard("Tổng môn học", AppColors.STAT_CARD_SUBJECTS);
        private final DashboardCard sectionsCard = new DashboardCard("Tổng học phần", AppColors.STAT_CARD_SECTIONS);
        private final DashboardCard enrollmentsCard = new DashboardCard("Tổng đăng ký học phần", AppColors.STAT_CARD_ENROLLMENTS);
        private final com.qlsv.view.common.PieChartPanel pieChartPanel = new com.qlsv.view.common.PieChartPanel();

        /**
         * Khởi tạo thống kê hệ thống.
         */
        public SystemStatisticsPanel() {
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
            setLayout(new BorderLayout(0, 24));

            JPanel gridPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
            gridPanel.setOpaque(false);
            gridPanel.add(studentsCard);
            gridPanel.add(lecturersCard);
            gridPanel.add(subjectsCard);
            gridPanel.add(sectionsCard);
            gridPanel.add(enrollmentsCard);

            normalizeCardSize(gridPanel);

            pieChartPanel.setTitle("Tỷ lệ thành phần dữ liệu hệ thống");
            pieChartPanel.setPreferredSize(new java.awt.Dimension(580, 480));
            pieChartPanel.setBackground(java.awt.Color.WHITE);
            pieChartPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
            ));

            JPanel chartWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
            chartWrapper.setOpaque(false);
            chartWrapper.add(pieChartPanel);

            JPanel contentWrapper = new JPanel();
            contentWrapper.setOpaque(false);
            contentWrapper.setLayout(new javax.swing.BoxLayout(contentWrapper, javax.swing.BoxLayout.Y_AXIS));
            contentWrapper.add(gridPanel);
            contentWrapper.add(javax.swing.Box.createVerticalStrut(20));
            contentWrapper.add(chartWrapper);

            javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(contentWrapper);
            scrollPane.setBorder(null);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);

            add(scrollPane, BorderLayout.CENTER);
            reloadStatistics();
        }
        /**
         * Chuẩn hóa card size.
         */
        private void normalizeCardSize(JPanel panel) {
            int maxWidth = 0;
            int maxHeight = 0;

            for (java.awt.Component c : panel.getComponents()) {
                java.awt.Dimension d = c.getPreferredSize();
                maxWidth = Math.max(maxWidth, d.width);
                maxHeight = Math.max(maxHeight, d.height);
            }

            java.awt.Dimension maxSize = new java.awt.Dimension(maxWidth, maxHeight);

            for (java.awt.Component c : panel.getComponents()) {
                c.setPreferredSize(maxSize);
            }
        }

        /**
         * Làm mới thống kê.
         */
        public final void reloadStatistics() {
            try {
                SystemStatistics systemStatistics = reportController.getSystemStatistics();
                studentsCard.setValue(String.valueOf(systemStatistics.getTotalStudents()));
                lecturersCard.setValue(String.valueOf(systemStatistics.getTotalLecturers()));
                subjectsCard.setValue(String.valueOf(systemStatistics.getTotalSubjects()));
                sectionsCard.setValue(String.valueOf(systemStatistics.getTotalCourseSections()));
                enrollmentsCard.setValue(String.valueOf(systemStatistics.getTotalEnrollments()));

                pieChartPanel.clearSlices();
                pieChartPanel.addSlice("Sinh viên", systemStatistics.getTotalStudents(), AppColors.STAT_CARD_STUDENTS);
                pieChartPanel.addSlice("Giảng viên", systemStatistics.getTotalLecturers(), AppColors.STAT_CARD_LECTURERS);
                pieChartPanel.addSlice("Môn học", systemStatistics.getTotalSubjects(), AppColors.STAT_CARD_SUBJECTS);
                pieChartPanel.addSlice("Học phần", systemStatistics.getTotalCourseSections(), AppColors.STAT_CARD_SECTIONS);
                pieChartPanel.addSlice("Đăng ký", systemStatistics.getTotalEnrollments(), AppColors.STAT_CARD_ENROLLMENTS);
                
            } catch (Exception exception) {
                DialogUtil.showError(this, exception.getMessage());
            }
        }
    }
