package com.qlsv.view.student;

import com.qlsv.controller.ScoreController;
import com.qlsv.model.Score;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.BasePanel;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class StudentScorePanel extends BasePanel {

    private final ScoreController scoreController = new ScoreController();

    public StudentScorePanel() {
        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"Học phần", "Môn học", "QT", "GK", "CK", "Tổng kết", "Kết quả"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), java.awt.BorderLayout.CENTER);

        try {
            for (Score score : scoreController.getCurrentStudentScores()) {
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
}
