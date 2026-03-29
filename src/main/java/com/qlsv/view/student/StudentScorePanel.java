/**
 * Màn hình sinh viên cho điểm.
 */
package com.qlsv.view.student;

import com.qlsv.controller.ScoreController;
import com.qlsv.model.Score;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BasePanel;
import com.qlsv.view.common.DashboardCard;

import com.qlsv.service.GroqService;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class StudentScorePanel extends BasePanel {

    private final ScoreController scoreController = new ScoreController();
    private final GroqService groqService = new GroqService();
    private List<Score> currentScores;
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Học phần", "Môn học", "QT", "GK", "CK", "Tổng kết", "Kết quả"}, 0) {
        /**
         * Xác định ô có cho phép chỉnh sửa hay không.
         */
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private DashboardCard avgCard;
    private DashboardCard passCard;
    private DashboardCard failCard;
    private final JLabel summaryLabel = new JLabel("Đang tải bảng điểm...");

    /**
     * Khởi tạo điểm sinh viên.
     */
    public StudentScorePanel() {
        setBackground(AppColors.CONTENT_BACKGROUND);
        initComponents();
        reloadData();
    }

    /**
     * Khởi tạo các thành phần giao diện.
     */
    private void initComponents() {
        JLabel titleLabel = new JLabel("Kết quả học tập");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);


        avgCard = new DashboardCard("Điểm trung bình tổng kết", AppColors.STAT_CARD_LECTURERS);
        passCard = new DashboardCard("Số môn đạt", AppColors.STAT_CARD_SUBJECTS);
        failCard = new DashboardCard("Số môn chưa đạt", AppColors.STAT_CARD_ENROLLMENTS);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 12, 12));
        cardsPanel.setOpaque(false);
        cardsPanel.add(avgCard);
        cardsPanel.add(passCard);
        cardsPanel.add(failCard);

        JTable table = new JTable(tableModel);
        configureTable(table);

        JPanel tablePanel = createTableCard("Bảng điểm chi tiết", summaryLabel, new JScrollPane(table));

        JButton reloadButton = new JButton("Tải lại");
        styleNeutralButton(reloadButton);
        reloadButton.addActionListener(event -> reloadData());

        JButton analyzeButton = new JButton("Phân tích điểm (AI)");
        stylePrimaryButton(analyzeButton);
        analyzeButton.addActionListener(event -> handleAnalyzeScores(analyzeButton, reloadButton));

        JPanel titlePanel = new JPanel(new BorderLayout(0, 6));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.NORTH);

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonGroup.setOpaque(false);
        buttonGroup.add(analyzeButton);
        buttonGroup.add(reloadButton);

        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setOpaque(false);
        actionPanel.add(titlePanel, BorderLayout.WEST);
        actionPanel.add(buttonGroup, BorderLayout.EAST);

        JPanel topWrapper = new JPanel(new BorderLayout(0, 12));
        topWrapper.setOpaque(false);
        topWrapper.add(actionPanel, BorderLayout.NORTH);
        topWrapper.add(cardsPanel, BorderLayout.CENTER);

        add(topWrapper, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    /**
     * Làm mới dữ liệu đang hiển thị.
     */
    @Override
    public void reloadData() {
        try {
            currentScores = scoreController.getCurrentStudentScores();
            List<Score> scores = currentScores;
            avgCard.setValue(calculateAverageScore(scores));
            passCard.setValue(String.valueOf(scores.stream().filter(score -> "PASS".equalsIgnoreCase(score.getResult())).count()));
            failCard.setValue(String.valueOf(scores.stream().filter(score -> "FAIL".equalsIgnoreCase(score.getResult())).count()));
            summaryLabel.setText(scores.size() + " kết quả đã ghi nhận");

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

    /**
     * Tính trung bình điểm.
     */
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

    /**
     * Tạo card bảng.
     */
    private JPanel createTableCard(String title, JLabel summaryLabel, JScrollPane scrollPane) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(true);
        panel.setBackground(AppColors.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(AppColors.CARD_TITLE_TEXT);

        summaryLabel.setForeground(AppColors.CARD_MUTED_TEXT);
        summaryLabel.setFont(summaryLabel.getFont().deriveFont(Font.PLAIN, 12.5f));

        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setOpaque(false);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(summaryLabel, BorderLayout.EAST);

        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.CARD_BORDER));
        scrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Thiết lập bảng.
     */
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

    /**
     * Áp dụng kiểu cho nút neutral.
     */
    private void styleNeutralButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(AppColors.BUTTON_NEUTRAL);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
    }

    /**
     * Áp dụng kiểu cho nút primary.
     */
    private void stylePrimaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(AppColors.BUTTON_PRIMARY);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
    }

    /**
     * Xử lý phân tích điểm.
     */
    private void handleAnalyzeScores(JButton analyzeButton, JButton reloadButton) {
        if (currentScores == null || currentScores.isEmpty()) {
            DialogUtil.showInfo(this, "Không có dữ liệu điểm để phân tích.");
            return;
        }

        analyzeButton.setEnabled(false);
        reloadButton.setEnabled(false);
        String originalText = analyzeButton.getText();
        analyzeButton.setText("Đang phân tích...");

        new SwingWorker<String, Void>() {
            /**
             * Xử lý do in background.
             */
            @Override
            protected String doInBackground() {
                return groqService.analyzeScores(currentScores);
            }

            /**
             * Xử lý done.
             */
            @Override
            protected void done() {
                try {
                    String result = get();
                    showAnalysisDialog(result);
                } catch (InterruptedException | ExecutionException e) {
                    DialogUtil.showError(StudentScorePanel.this, "Lỗi khi phân tích dữ liệu: " + e.getMessage());
                } finally {
                    analyzeButton.setEnabled(true);
                    reloadButton.setEnabled(true);
                    analyzeButton.setText(originalText);
                }
            }
        }.execute();
    }

    /**
     * Hiển thị hộp thoại phân tích.
     */
    private void showAnalysisDialog(String text) {
        JDialog dialog = new JDialog(javax.swing.SwingUtilities.getWindowAncestor(this), "Phân tích kết quả học tập AI", JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());

        JTextArea textArea = new JTextArea(text);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(null);

        dialog.add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Đóng");
        styleNeutralButton(closeButton);
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        footer.add(closeButton);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setPreferredSize(new Dimension(600, 450));
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
