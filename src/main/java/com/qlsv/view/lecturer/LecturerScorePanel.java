package com.qlsv.view.lecturer;

import com.qlsv.controller.CourseSectionController;
import com.qlsv.controller.LecturerController;
import com.qlsv.controller.ScoreController;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Lecturer;
import com.qlsv.model.Score;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BasePanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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
    private final LecturerController lecturerController = new LecturerController();
    private final CourseSectionController courseSectionController = new CourseSectionController();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Sinh viên", "Học phần", "QT", "GK", "CK", "Tổng kết", "Kết quả"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final List<Score> allScores = new ArrayList<>();
    private final List<Score> filteredScores = new ArrayList<>();
    private final JTable table = new JTable(tableModel);
    private final JComboBox<Object> courseComboBox;
    private final JPanel scoreEntryPanel = new JPanel(new BorderLayout());
    
    private JTextField processField;
    private JTextField midtermField;
    private JTextField finalField;
    private JLabel studentNameLabel;
    private JLabel subjectLabel;
    private JButton saveButton;
    private Score selectedScore;

    public LecturerScorePanel() {
        table.setSelectionBackground(AppColors.TABLE_SELECTION_BACKGROUND);
        table.getTableHeader().setBackground(AppColors.TABLE_HEADER_BACKGROUND);

        courseComboBox = new JComboBox<>();
        courseComboBox.addItem("Tất cả học phần");

        JButton reloadButton = new JButton("Tải lại");
        JButton filterButton = new JButton("Lọc");

        reloadButton.addActionListener(event -> reloadData());
        filterButton.addActionListener(event -> filterData());

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setOpaque(false);
        filterPanel.add(new JLabel("Học phần:"));
        filterPanel.add(courseComboBox);
        filterPanel.add(filterButton);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionPanel.setOpaque(false);
        actionPanel.add(reloadButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(filterPanel, BorderLayout.WEST);
        topPanel.add(actionPanel, BorderLayout.EAST);

        // Khoi tao panel nhap diem
        buildScoreEntryPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(new JScrollPane(table));
        
        // Wrap scoreEntryPanel vao JScrollPane
        JScrollPane scrollPane = new JScrollPane(scoreEntryPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);
        
        splitPane.setBottomComponent(scrollPane);
        splitPane.setDividerLocation(350);
        splitPane.setResizeWeight(0.6);

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onScoreSelected();
            }
        });

        loadCourseSections();
        reloadData();
    }

    private void buildScoreEntryPanel() {
        scoreEntryPanel.setBackground(AppColors.CARD_BACKGROUND);
        scoreEntryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        JLabel titleLabel = new JLabel("Nhập/Sửa điểm sinh viên");
        titleLabel.setFont(titleLabel.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        titleLabel.setForeground(AppColors.CARD_TITLE_TEXT);

        JPanel fieldsPanel = new JPanel(new GridLayout(0, 2, 20, 12));
        fieldsPanel.setOpaque(false);

        studentNameLabel = new JLabel("Chưa chọn");
        subjectLabel = new JLabel("Chưa chọn");
        processField = new JTextField();
        midtermField = new JTextField();
        finalField = new JTextField();

        styleScoreField(processField);
        styleScoreField(midtermField);
        styleScoreField(finalField);

        fieldsPanel.add(createLabel("Sinh viên:"));
        fieldsPanel.add(studentNameLabel);
        fieldsPanel.add(createLabel("Học phần:"));
        fieldsPanel.add(subjectLabel);
        fieldsPanel.add(createLabel("Điểm quá trình:"));
        fieldsPanel.add(processField);
        fieldsPanel.add(createLabel("Điểm giữa kỳ:"));
        fieldsPanel.add(midtermField);
        fieldsPanel.add(createLabel("Điểm cuối kỳ:"));
        fieldsPanel.add(finalField);

        saveButton = new JButton("Lưu điểm");
        styleSaveButton(saveButton);
        saveButton.addActionListener(e -> handleSaveScore());
        saveButton.setEnabled(false);

        JPanel bottomActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomActionPanel.setOpaque(false);
        bottomActionPanel.add(saveButton);

        scoreEntryPanel.add(titleLabel, BorderLayout.NORTH);
        scoreEntryPanel.add(fieldsPanel, BorderLayout.CENTER);
        scoreEntryPanel.add(bottomActionPanel, BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(java.awt.Font.BOLD));
        label.setForeground(AppColors.CARD_MUTED_TEXT);
        return label;
    }

    private void styleScoreField(JTextField field) {
        field.setPreferredSize(new java.awt.Dimension(120, 32));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.INPUT_BORDER),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
    }

    private void styleSaveButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(AppColors.BUTTON_SUCCESS);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setFont(button.getFont().deriveFont(java.awt.Font.BOLD));
    }

    private void onScoreSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= filteredScores.size()) {
            selectedScore = null;
            studentNameLabel.setText("Chưa chọn");
            subjectLabel.setText("Chưa chọn");
            processField.setText("");
            midtermField.setText("");
            finalField.setText("");
            saveButton.setEnabled(false);
            return;
        }

        selectedScore = filteredScores.get(selectedRow);
        studentNameLabel.setText(selectedScore.getEnrollment().getStudent().getFullName());
        subjectLabel.setText(selectedScore.getEnrollment().getCourseSection().getSectionCode());
        processField.setText(String.valueOf(selectedScore.getProcessScore()));
        midtermField.setText(String.valueOf(selectedScore.getMidtermScore()));
        finalField.setText(String.valueOf(selectedScore.getFinalScore()));
        saveButton.setEnabled(true);
    }

    private void handleSaveScore() {
        if (selectedScore == null) return;

        try {
            selectedScore.setProcessScore(Double.parseDouble(processField.getText().trim()));
            selectedScore.setMidtermScore(Double.parseDouble(midtermField.getText().trim()));
            selectedScore.setFinalScore(Double.parseDouble(finalField.getText().trim()));
            
            scoreController.saveScore(selectedScore);
            DialogUtil.showInfo(this, "Lưu điểm thành công cho sinh viên " + studentNameLabel.getText());
            reloadData();
        } catch (NumberFormatException e) {
            DialogUtil.showError(this, "Vui lòng nhập điểm là số hợp lệ.");
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void loadCourseSections() {
        try {
            Lecturer lecturer = lecturerController.getCurrentLecturer();
            List<CourseSection> sections = courseSectionController.getCourseSectionsByLecturer(lecturer.getId());
            courseComboBox.removeAllItems();
            courseComboBox.addItem("Tất cả học phần");
            for (CourseSection section : sections) {
                courseComboBox.addItem(section);
            }
        } catch (Exception exception) {
            DialogUtil.showError(this, "Không thể tải danh sách học phần: " + exception.getMessage());
        }
    }

    @Override
    public void reloadData() {
        try {
            allScores.clear();
            allScores.addAll(scoreController.getCurrentLecturerScores());
            filterData();
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void filterData() {
        Object selected = courseComboBox.getSelectedItem();
        tableModel.setRowCount(0);
        filteredScores.clear();

        for (Score score : allScores) {
            boolean matches = false;
            if (selected instanceof String || selected == null) {
                matches = true;
            } else if (selected instanceof CourseSection section) {
                matches = score.getEnrollment() != null 
                        && score.getEnrollment().getCourseSection() != null 
                        && score.getEnrollment().getCourseSection().getId().equals(section.getId());
            }

            if (matches) {
                filteredScores.add(score);
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
        }
        // Sau khi reload, xoa data o input panel
        onScoreSelected();
    }
}
