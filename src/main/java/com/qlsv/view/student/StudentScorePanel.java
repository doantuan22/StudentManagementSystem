package com.qlsv.view.student;

import com.qlsv.controller.ScoreController;
import com.qlsv.model.Score;
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

public class StudentScorePanel extends BasePanel {

    private final ScoreController scoreController = new ScoreController();

    public StudentScorePanel() {
        setBackground(AppColors.CONTENT_BACKGROUND);
        initComponents();
        reloadData();
    }

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Học phần", "Môn học", "QT", "GK", "CK", "Tổng kết", "Kết quả"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private DashboardCard avgCard;
    private DashboardCard passCard;
    private DashboardCard failCard;

    private void initComponents() {
        JLabel titleLabel = new JLabel("Kết quả học tập");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));

        avgCard = new DashboardCard("Điểm trung bình tổng kết", AppColors.STAT_CARD_LECTURERS);
        passCard = new DashboardCard("Số môn đạt", AppColors.STAT_CARD_SUBJECTS);
        failCard = new DashboardCard("Số môn chưa đạt", AppColors.STAT_CARD_ENROLLMENTS);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 12, 12));
        cardsPanel.setOpaque(false);
        cardsPanel.add(avgCard);
        cardsPanel.add(passCard);
        cardsPanel.add(failCard);

        JTable table = new JTable(tableModel);
        table.setRowHeight(24);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(true);
        tablePanel.setBackground(AppColors.CARD_BACKGROUND);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        tablePanel.add(new JLabel("Bảng điểm chi tiết"), BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton reloadButton = new JButton("Tải lại");
        reloadButton.addActionListener(event -> reloadData());

        JPanel titleActionPanel = new JPanel(new BorderLayout());
        titleActionPanel.setOpaque(false);
        titleActionPanel.add(titleLabel, BorderLayout.WEST);
        titleActionPanel.add(reloadButton, BorderLayout.EAST);

        JPanel topWrapper = new JPanel(new BorderLayout(0, 12));
        topWrapper.setOpaque(false);
        topWrapper.add(titleActionPanel, BorderLayout.NORTH);
        topWrapper.add(cardsPanel, BorderLayout.CENTER);

        add(topWrapper, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    public void reloadData() {
        try {
            List<Score> scores = scoreController.getCurrentStudentScores();
            avgCard.setValue(calculateAverageScore(scores));
            passCard.setValue(String.valueOf(scores.stream().filter(score -> "PASS".equalsIgnoreCase(score.getResult())).count()));
            failCard.setValue(String.valueOf(scores.stream().filter(score -> "FAIL".equalsIgnoreCase(score.getResult())).count()));

            tableModel.setRowCount(0);
            for (Score score : scores) {
                tableModel.addRow(new Object[]{
                        score.getEnrollment() == null || score.getEnrollment().getCourseSection() == null
                                ? "" : score.getEnrollment().getCourseSection().getSectionCode(),
                        score.getEnrollment() == null || score.getEnrollment().getCourseSection() == null
                                || score.getEnrollment().getCourseSection().getSubject() == null
                                ? "" : score.getEnrollment().getCourseSection().getSubject().getSubjectName(),
                        score.getProcessScore(),
                        score.getMidtermScore(),
                        score.getFinalScore(),
                        score.getTotalScore(),
                        DisplayTextUtil.formatStatus(score.getResult())
                });
            }
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private String calculateAverageScore(List<Score> scores) {
        double total = 0;
        int count = 0;
        for (Score score : scores) {
            if (score.getTotalScore() != null) {
                total += score.getTotalScore();
                count++;
            }
        }
        if (count == 0) {
            return "Chưa có";
        }
        return String.format("%.2f", total / count);
    }
}
