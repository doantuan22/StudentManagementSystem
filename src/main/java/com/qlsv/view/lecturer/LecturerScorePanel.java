package com.qlsv.view.lecturer;

import com.qlsv.controller.CourseSectionController;
import com.qlsv.controller.LecturerController;
import com.qlsv.controller.ScoreController;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Lecturer;
import com.qlsv.model.Score;
import com.qlsv.utils.DialogUtil;
import com.qlsv.utils.DisplayTextUtil;
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
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LecturerScorePanel extends BasePanel {

    private final ScoreController scoreController = new ScoreController();
    private final LecturerController lecturerController = new LecturerController();
    private final CourseSectionController courseSectionController = new CourseSectionController();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Mã SV", "Họ và tên", "Học phần", "QT", "GK", "CK", "Tổng kết", "Kết quả"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final List<Score> allScores = new ArrayList<>();
    private final List<Score> courseFilteredScores = new ArrayList<>();
    private final List<Score> filteredScores = new ArrayList<>();

    private final JTable table = new JTable(tableModel);
    private final JComboBox<Object> courseComboBox = new JComboBox<>();
    private final JPanel scoreEntryPanel = new JPanel(new BorderLayout());
    private final JTextField searchField = new JTextField(24);
    private final Timer searchDebounceTimer;

    private JTextField processField;
    private JTextField midtermField;
    private JTextField finalField;
    private JLabel studentNameLabel;
    private JLabel subjectLabel;
    private JButton saveButton;
    private Score selectedScore;

    private Long currentCourseSectionId;
    private String currentKeyword = "";

    public LecturerScorePanel() {
        configureTable();

        courseComboBox.addItem("Tất cả học phần");

        JButton filterButton = new JButton("Lọc");
        JButton reloadButton = new JButton("Tải lại");
        styleSecondaryButton(filterButton);
        styleSecondaryButton(reloadButton);

        filterButton.addActionListener(event -> {
            currentCourseSectionId = resolveCourseSectionIdFromComboSelection();
            applyFilters(getSelectedEnrollmentId());
        });
        reloadButton.addActionListener(event -> reloadData());

        searchField.setToolTipText("Tìm theo mã sinh viên hoặc họ tên trong danh sách đang lọc.");
        searchField.setPreferredSize(new Dimension(240, 36));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.INPUT_BORDER),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));

        searchDebounceTimer = new Timer(250, event -> {
            currentKeyword = normalize(searchField.getText());
            applyFilters(getSelectedEnrollmentId());
        });
        searchDebounceTimer.setRepeats(false);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                restartSearchDebounce();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                restartSearchDebounce();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                restartSearchDebounce();
            }
        });

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setOpaque(false);
        filterPanel.add(new JLabel("Học phần:"));
        filterPanel.add(courseComboBox);
        filterPanel.add(filterButton);
        filterPanel.add(new JLabel("Từ khóa:"));
        filterPanel.add(searchField);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionPanel.setOpaque(false);
        actionPanel.add(reloadButton);

        JPanel topPanel = createSectionCard();
        topPanel.setLayout(new BorderLayout(12, 8));

        JLabel titleLabel = new JLabel("Nhập điểm sinh viên");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        JPanel controlPanel = new JPanel(new BorderLayout(10, 8));
        controlPanel.setOpaque(false);
        controlPanel.add(filterPanel, BorderLayout.WEST);
        controlPanel.add(actionPanel, BorderLayout.EAST);

        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(controlPanel, BorderLayout.CENTER);

        buildScoreEntryPanel();

        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(AppColors.CARD_BORDER));
        tableScrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);
        tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        tableScrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        JScrollPane scoreEntryScrollPane = new JScrollPane(scoreEntryPanel);
        scoreEntryScrollPane.setBorder(null);
        scoreEntryScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scoreEntryScrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, scoreEntryScrollPane);
        splitPane.setDividerLocation(360);
        splitPane.setResizeWeight(0.62);
        splitPane.setBorder(null);
        splitPane.setContinuousLayout(true);

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                onScoreSelected();
            }
        });

        reloadData();
    }

    private void configureTable() {
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setGridColor(AppColors.CARD_BORDER);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(AppColors.TABLE_SELECTION_BACKGROUND);
        table.setSelectionForeground(AppColors.CARD_VALUE_TEXT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(AppColors.TABLE_HEADER_BACKGROUND);
        table.getTableHeader().setForeground(AppColors.CARD_VALUE_TEXT);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 13f));

        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);
        table.getColumnModel().getColumn(8).setPreferredWidth(120);
    }

    private JPanel createSectionCard() {
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(AppColors.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));
        return panel;
    }

    private void styleSecondaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(AppColors.BUTTON_NEUTRAL);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
    }

    private void buildScoreEntryPanel() {
        scoreEntryPanel.setBackground(AppColors.CARD_BACKGROUND);
        scoreEntryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        JLabel titleLabel = new JLabel("Nhập/Sửa điểm sinh viên");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
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
        saveButton.addActionListener(event -> handleSaveScore());
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
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setForeground(AppColors.CARD_MUTED_TEXT);
        return label;
    }

    private void styleScoreField(JTextField field) {
        field.setPreferredSize(new Dimension(120, 32));
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
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setFont(button.getFont().deriveFont(Font.BOLD));
    }

    private void restartSearchDebounce() {
        searchDebounceTimer.restart();
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
        studentNameLabel.setText(getStudentName(selectedScore));
        subjectLabel.setText(getCourseSectionCode(selectedScore));
        processField.setText(String.valueOf(selectedScore.getProcessScore()));
        midtermField.setText(String.valueOf(selectedScore.getMidtermScore()));
        finalField.setText(String.valueOf(selectedScore.getFinalScore()));
        saveButton.setEnabled(true);
    }

    private void handleSaveScore() {
        if (selectedScore == null) {
            return;
        }

        try {
            Long selectedEnrollmentId = getEnrollmentId(selectedScore);

            selectedScore.setProcessScore(Double.parseDouble(processField.getText().trim()));
            selectedScore.setMidtermScore(Double.parseDouble(midtermField.getText().trim()));
            selectedScore.setFinalScore(Double.parseDouble(finalField.getText().trim()));

            selectedScore = scoreController.saveScore(selectedScore);
            DialogUtil.showInfo(this, "Lưu điểm thành công cho sinh viên " + studentNameLabel.getText());
            refreshScoreData(false, selectedEnrollmentId);
        } catch (NumberFormatException exception) {
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
            restoreCourseSelection();
        } catch (Exception exception) {
            DialogUtil.showError(this, "Không thể tải danh sách học phần: " + exception.getMessage());
        }
    }

    @Override
    public void reloadData() {
        refreshScoreData(true, getSelectedEnrollmentId());
    }

    private void refreshScoreData(boolean reloadCourseSections, Long preferredEnrollmentId) {
        try {
            if (reloadCourseSections) {
                loadCourseSections();
            }

            allScores.clear();
            allScores.addAll(scoreController.getCurrentLecturerScores());
            applyFilters(preferredEnrollmentId);
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void applyFilters(Long preferredEnrollmentId) {
        courseFilteredScores.clear();
        filteredScores.clear();
        tableModel.setRowCount(0);

        for (Score score : allScores) {
            if (matchesCurrentCourseFilter(score)) {
                courseFilteredScores.add(score);
            }
        }

        for (Score score : courseFilteredScores) {
            if (matchesCurrentKeyword(score)) {
                filteredScores.add(score);
                tableModel.addRow(new Object[]{
                        score.getId(),
                        getStudentCode(score),
                        getStudentName(score),
                        getCourseSectionCode(score),
                        score.getProcessScore(),
                        score.getMidtermScore(),
                        score.getFinalScore(),
                        score.getTotalScore(),
                        DisplayTextUtil.formatStatus(score.getResult())
                });
            }
        }

        restoreSelectedRow(preferredEnrollmentId);
        if (table.getSelectedRow() < 0) {
            onScoreSelected();
        }
    }

    private boolean matchesCurrentCourseFilter(Score score) {
        if (currentCourseSectionId == null) {
            return true;
        }
        return score.getEnrollment() != null
                && score.getEnrollment().getCourseSection() != null
                && currentCourseSectionId.equals(score.getEnrollment().getCourseSection().getId());
    }

    private boolean matchesCurrentKeyword(Score score) {
        if (currentKeyword.isBlank()) {
            return true;
        }
        return containsIgnoreCase(getStudentCode(score), currentKeyword)
                || containsIgnoreCase(getStudentName(score), currentKeyword);
    }

    private void restoreSelectedRow(Long preferredEnrollmentId) {
        if (preferredEnrollmentId == null) {
            table.clearSelection();
            return;
        }

        for (int row = 0; row < filteredScores.size(); row++) {
            if (preferredEnrollmentId.equals(getEnrollmentId(filteredScores.get(row)))) {
                table.setRowSelectionInterval(row, row);
                table.scrollRectToVisible(table.getCellRect(row, 0, true));
                return;
            }
        }
        table.clearSelection();
    }

    private void restoreCourseSelection() {
        if (currentCourseSectionId == null) {
            courseComboBox.setSelectedIndex(0);
            return;
        }

        for (int index = 0; index < courseComboBox.getItemCount(); index++) {
            Object item = courseComboBox.getItemAt(index);
            if (item instanceof CourseSection section && currentCourseSectionId.equals(section.getId())) {
                courseComboBox.setSelectedIndex(index);
                return;
            }
        }
        courseComboBox.setSelectedIndex(0);
        currentCourseSectionId = null;
    }

    private Long resolveCourseSectionIdFromComboSelection() {
        Object selectedItem = courseComboBox.getSelectedItem();
        if (selectedItem instanceof CourseSection section) {
            return section.getId();
        }
        return null;
    }

    private Long getSelectedEnrollmentId() {
        return selectedScore == null ? null : getEnrollmentId(selectedScore);
    }

    private Long getEnrollmentId(Score score) {
        return score == null || score.getEnrollment() == null ? null : score.getEnrollment().getId();
    }

    private String getStudentCode(Score score) {
        if (score == null || score.getEnrollment() == null || score.getEnrollment().getStudent() == null) {
            return "";
        }
        return DisplayTextUtil.defaultText(score.getEnrollment().getStudent().getStudentCode());
    }

    private String getStudentName(Score score) {
        if (score == null || score.getEnrollment() == null || score.getEnrollment().getStudent() == null) {
            return "";
        }
        return DisplayTextUtil.defaultText(score.getEnrollment().getStudent().getFullName());
    }

    private String getCourseSectionCode(Score score) {
        if (score == null || score.getEnrollment() == null || score.getEnrollment().getCourseSection() == null) {
            return "";
        }
        return DisplayTextUtil.defaultText(score.getEnrollment().getCourseSection().getSectionCode());
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return normalize(source).contains(keyword);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
