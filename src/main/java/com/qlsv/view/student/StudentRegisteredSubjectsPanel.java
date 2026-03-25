package com.qlsv.view.student;

import com.qlsv.controller.EnrollmentController;
import com.qlsv.model.Enrollment;
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

public class StudentRegisteredSubjectsPanel extends BasePanel {

    private final EnrollmentController enrollmentController = new EnrollmentController();

    public StudentRegisteredSubjectsPanel() {
        setBackground(AppColors.CONTENT_BACKGROUND);
        initComponents();
        reloadData();
    }

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Học phần", "Môn học", "Tín chỉ", "Giảng viên", "Trạng thái", "Lịch học", "Phòng"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JLabel subjectsCountLabel = new JLabel("Tổng số môn học đã đăng ký");
    private DashboardCard totalSubjectsCard;
    private DashboardCard totalCreditsCard;

    private void initComponents() {
        JLabel titleLabel = new JLabel("Học phần đã đăng ký");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));

        totalSubjectsCard = new DashboardCard("Tổng số học phần", AppColors.STAT_CARD_SUBJECTS);
        totalCreditsCard = new DashboardCard("Tổng số tín chỉ", AppColors.STAT_CARD_SECTIONS);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 2, 12, 12));
        cardsPanel.setOpaque(false);
        cardsPanel.add(totalSubjectsCard);
        cardsPanel.add(totalCreditsCard);

        JTable table = new JTable(tableModel);
        table.setRowHeight(24);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(true);
        tablePanel.setBackground(AppColors.CARD_BACKGROUND);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        tablePanel.add(subjectsCountLabel, BorderLayout.NORTH);
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
            List<Enrollment> enrollments = enrollmentController.getCurrentStudentEnrollments();
            totalSubjectsCard.setValue(String.valueOf(enrollments.size()));
            totalCreditsCard.setValue(String.valueOf(calculateTotalCredits(enrollments)));

            tableModel.setRowCount(0);
            for (Enrollment enrollment : enrollments) {
                tableModel.addRow(new Object[]{
                        enrollment.getCourseSection() == null ? "" : enrollment.getCourseSection().getSectionCode(),
                        enrollment.getCourseSection() == null || enrollment.getCourseSection().getSubject() == null
                                ? "" : enrollment.getCourseSection().getSubject().getSubjectName(),
                        enrollment.getCourseSection() == null || enrollment.getCourseSection().getSubject() == null
                                ? "" : enrollment.getCourseSection().getSubject().getCredits(),
                        enrollment.getCourseSection() == null || enrollment.getCourseSection().getLecturer() == null
                                ? "" : enrollment.getCourseSection().getLecturer().getFullName(),
                        DisplayTextUtil.formatStatus(enrollment.getStatus()),
                        enrollment.getCourseSection() == null ? "" : enrollment.getCourseSection().getScheduleText(),
                        enrollment.getCourseSection() == null || enrollment.getCourseSection().getRoom() == null
                                ? "Chưa cập nhật" : enrollment.getCourseSection().getRoom().getRoomName()
                });
            }
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private int calculateTotalCredits(List<Enrollment> enrollments) {
        int total = 0;
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getCourseSection() != null
                    && enrollment.getCourseSection().getSubject() != null
                    && enrollment.getCourseSection().getSubject().getCredits() != null) {
                total += enrollment.getCourseSection().getSubject().getCredits();
            }
        }
        return total;
    }
}
