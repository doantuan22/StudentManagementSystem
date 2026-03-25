package com.qlsv.view.lecturer;

import com.qlsv.controller.ScoreController;
import com.qlsv.model.Score;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.BasePanel;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

public class LecturerScorePanel extends BasePanel {

    private final ScoreController scoreController = new ScoreController();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Sinh viên", "Học phần", "QT", "GK", "CK", "Tổng kết", "Kết quả"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final List<Score> currentScores = new ArrayList<>();
    private final JTable table = new JTable(tableModel);

    public LecturerScorePanel() {
        JButton updateButton = new JButton("Cập nhật điểm");
        JButton reloadButton = new JButton("Tải lại");
        updateButton.addActionListener(event -> updateSelectedScore());
        reloadButton.addActionListener(event -> reloadData());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.add(updateButton);
        actionPanel.add(reloadButton);

        add(actionPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        reloadData();
    }

    private void reloadData() {
        try {
            currentScores.clear();
            currentScores.addAll(scoreController.getCurrentLecturerScores());
            tableModel.setRowCount(0);
            for (Score score : currentScores) {
                tableModel.addRow(new Object[]{
                        score.getId(),
                        score.getEnrollment() == null || score.getEnrollment().getStudent() == null
                                ? "" : score.getEnrollment().getStudent().getFullName(),
                        score.getEnrollment() == null || score.getEnrollment().getCourseSection() == null
                                ? "" : score.getEnrollment().getCourseSection().getSectionCode(),
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

    private void updateSelectedScore() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentScores.size()) {
            DialogUtil.showError(this, "Hãy chọn dòng điểm cần cập nhật.");
            return;
        }

        Score score = currentScores.get(selectedRow);
        JTextField processField = new JTextField(String.valueOf(score.getProcessScore()));
        JTextField midtermField = new JTextField(String.valueOf(score.getMidtermScore()));
        JTextField finalField = new JTextField(String.valueOf(score.getFinalScore()));

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.add(new JLabel("Điểm quá trình"));
        formPanel.add(processField);
        formPanel.add(new JLabel("Điểm giữa kỳ"));
        formPanel.add(midtermField);
        formPanel.add(new JLabel("Điểm cuối kỳ"));
        formPanel.add(finalField);

        int result = JOptionPane.showConfirmDialog(this, formPanel, "Cập nhật điểm",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            score.setProcessScore(Double.parseDouble(processField.getText().trim()));
            score.setMidtermScore(Double.parseDouble(midtermField.getText().trim()));
            score.setFinalScore(Double.parseDouble(finalField.getText().trim()));
            scoreController.saveScore(score);
            DialogUtil.showInfo(this, "Cập nhật điểm thành công.");
            reloadData();
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }
}
