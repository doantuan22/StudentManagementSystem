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
import javax.swing.SwingConstants;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LecturerScorePanel extends BasePanel {

    private static final String[] SCORE_TABLE_COLUMNS = {
            "ID", "Mã SV", "Họ và tên", "Học phần", "QT", "GK", "CK", "Tổng kết", "Kết quả"
    };
    private static final String[] EDIT_TABLE_COLUMNS = {
            "Mã SV", "Họ và tên", "Học phần", "QT", "GK", "CK", "Tổng kết", "Kết quả"
    };

    private final ScoreController scoreController = new ScoreController();
    private final LecturerController lecturerController = new LecturerController();
    private final CourseSectionController courseSectionController = new CourseSectionController();

    private final DefaultTableModel scoreTableModel = new DefaultTableModel(SCORE_TABLE_COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel editTableModel = new DefaultTableModel(EDIT_TABLE_COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return row == 0 && column >= 3 && column <= 5;
        }
    };

    private final List<Score> allScores = new ArrayList<>();
    private final List<Score> courseFilteredScores = new ArrayList<>();
    private final List<Score> filteredScores = new ArrayList<>();

    private final JTable scoreTable = new ResponsiveTable(scoreTableModel);
    private final JTable editTable = new ResponsiveTable(editTableModel);
    private final JComboBox<Object> courseComboBox = new JComboBox<>();
    private final JTextField searchField = new JTextField(24);
    private final JButton saveButton = new JButton("Lưu điểm");
    private final Timer searchDebounceTimer;

    private Score selectedScore;
    private Long currentCourseSectionId;
    private String currentKeyword = "";

    public LecturerScorePanel() {
        configureScoreTable();
        configureEditTable();
        initializeEditRow();

        courseComboBox.addItem("Tất cả học phần");

        JButton filterButton = new JButton("Lọc");
        JButton reloadButton = new JButton("Tải lại");
        styleSecondaryButton(filterButton);
        styleSecondaryButton(reloadButton);
        styleSaveButton(saveButton);
        saveButton.setEnabled(false);

        filterButton.addActionListener(event -> {
            currentCourseSectionId = resolveCourseSectionIdFromComboSelection();
            applyFilters(getSelectedEnrollmentId());
        });
        reloadButton.addActionListener(event -> reloadData());
        saveButton.addActionListener(event -> handleSaveScore());

        searchField.setToolTipText("Tìm theo mã sinh viên hoặc họ tên trong danh sách đang lọc.");
        searchField.setPreferredSize(new Dimension(240, 36));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.INPUT_BORDER),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
        courseComboBox.setPreferredSize(new Dimension(220, 36));
        courseComboBox.setFont(courseComboBox.getFont().deriveFont(Font.PLAIN, 13.5f));

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

        JPanel headerPanel = createSectionCard(new BorderLayout(12, 8));
        JLabel titleLabel = new JLabel("Nhập điểm sinh viên");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);



        JPanel controlPanel = new JPanel(new BorderLayout(10, 8));
        controlPanel.setOpaque(false);
        controlPanel.add(filterPanel, BorderLayout.CENTER);
        controlPanel.add(actionPanel, BorderLayout.EAST);

        JPanel headerTextPanel = new JPanel(new BorderLayout(0, 4));
        headerTextPanel.setOpaque(false);
        headerTextPanel.add(titleLabel, BorderLayout.NORTH);

        headerPanel.add(headerTextPanel, BorderLayout.NORTH);
        headerPanel.add(controlPanel, BorderLayout.CENTER);

        JPanel scoreListPanel = createSectionCard(new BorderLayout(0, 12));
        JLabel scoreListTitle = createSectionTitle("Bảng thông tin điểm sinh viên");
        JLabel scoreListDescription = createMutedDescription("Danh sách điểm được giữ theo bộ lọc hiện tại để chọn nhanh sinh viên cần cập nhật.");
        JPanel scoreListHeading = new JPanel(new BorderLayout(0, 4));
        scoreListHeading.setOpaque(false);
        scoreListHeading.add(scoreListTitle, BorderLayout.NORTH);
        scoreListHeading.add(scoreListDescription, BorderLayout.CENTER);
        JScrollPane scoreTableScrollPane = createTableScrollPane(scoreTable);
        scoreListPanel.add(scoreListHeading, BorderLayout.NORTH);
        scoreListPanel.add(scoreTableScrollPane, BorderLayout.CENTER);

        JPanel editPanel = createSectionCard(new BorderLayout(0, 12));
        JLabel editTitle = createSectionTitle("Bảng nhập/sửa điểm theo sinh viên đang chọn");

        JPanel editHintPanel = new JPanel(new BorderLayout());
        editHintPanel.setOpaque(false);
    

        JPanel editTopPanel = new JPanel(new BorderLayout(0, 8));
        editTopPanel.setOpaque(false);
        editTopPanel.add(editTitle, BorderLayout.NORTH);
        editTopPanel.add(editHintPanel, BorderLayout.CENTER);

        JScrollPane editTableScrollPane = createTableScrollPane(editTable);
        JPanel editActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        editActionPanel.setOpaque(false);
        editActionPanel.add(saveButton);

        editPanel.add(editTopPanel, BorderLayout.NORTH);
        editPanel.add(editTableScrollPane, BorderLayout.CENTER);
        editPanel.add(editActionPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scoreListPanel, editPanel);
        splitPane.setDividerLocation(360);
        splitPane.setResizeWeight(0.63);
        splitPane.setBorder(null);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerSize(10);
        scoreListPanel.setMinimumSize(new Dimension(0, 220));
        editPanel.setMinimumSize(new Dimension(0, 220));

        add(headerPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        scoreTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                onScoreSelected();
            }
        });

        reloadData();
    }

    private void configureScoreTable() {
        configureBaseTable(scoreTable);
        scoreTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        scoreTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        scoreTable.getColumnModel().getColumn(1).setPreferredWidth(110);
        scoreTable.getColumnModel().getColumn(2).setPreferredWidth(230);
        scoreTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        scoreTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        scoreTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        scoreTable.getColumnModel().getColumn(6).setPreferredWidth(90);
        scoreTable.getColumnModel().getColumn(7).setPreferredWidth(110);
        scoreTable.getColumnModel().getColumn(8).setPreferredWidth(120);
    }

    private void configureEditTable() {
        configureBaseTable(editTable);
        editTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        editTable.setRowSelectionAllowed(false);
        editTable.setCellSelectionEnabled(true);
        editTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        editTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        editTable.getColumnModel().getColumn(1).setPreferredWidth(230);
        editTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        editTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        editTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        editTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        editTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        editTable.getColumnModel().getColumn(7).setPreferredWidth(120);
    }

    private void configureBaseTable(JTable table) {
        table.setRowHeight(34);
        table.setFillsViewportHeight(true);
        table.setGridColor(AppColors.CARD_BORDER);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(AppColors.TABLE_SELECTION_BACKGROUND);
        table.setSelectionForeground(AppColors.CARD_VALUE_TEXT);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(AppColors.TABLE_HEADER_BACKGROUND);
        table.getTableHeader().setForeground(AppColors.CARD_VALUE_TEXT);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 13f));
    }

    private void initializeEditRow() {
        editTableModel.setRowCount(0);
        editTableModel.addRow(new Object[]{"", "", "", "", "", "", "", ""});
    }

    private JPanel createSectionCard(BorderLayout layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(true);
        panel.setBackground(AppColors.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));
        return panel;
    }

    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 16f));
        label.setForeground(AppColors.CARD_TITLE_TEXT);
        return label;
    }

    private JLabel createMutedDescription(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 12.5f));
        label.setForeground(AppColors.CARD_MUTED_TEXT);
        return label;
    }

    private JScrollPane createTableScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.CARD_BORDER));
        scrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        return scrollPane;
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

    private void styleSaveButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(AppColors.BUTTON_SUCCESS);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        button.setFont(button.getFont().deriveFont(Font.BOLD));
    }

    private void restartSearchDebounce() {
        searchDebounceTimer.restart();
    }

    private void onScoreSelected() {
        int selectedRow = scoreTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= filteredScores.size()) {
            selectedScore = null;
            fillEditRow(null);
            saveButton.setEnabled(false);
            return;
        }

        selectedScore = filteredScores.get(selectedRow);
        fillEditRow(selectedScore);
        saveButton.setEnabled(true);
    }

    private void fillEditRow(Score score) {
        if (editTable.isEditing()) {
            editTable.getCellEditor().stopCellEditing();
        }

        if (score == null) {
            editTableModel.setValueAt("", 0, 0);
            editTableModel.setValueAt("", 0, 1);
            editTableModel.setValueAt("", 0, 2);
            editTableModel.setValueAt("", 0, 3);
            editTableModel.setValueAt("", 0, 4);
            editTableModel.setValueAt("", 0, 5);
            editTableModel.setValueAt("", 0, 6);
            editTableModel.setValueAt("", 0, 7);
            return;
        }

        editTableModel.setValueAt(getStudentCode(score), 0, 0);
        editTableModel.setValueAt(getStudentName(score), 0, 1);
        editTableModel.setValueAt(getCourseSectionCode(score), 0, 2);
        editTableModel.setValueAt(score.getProcessScore(), 0, 3);
        editTableModel.setValueAt(score.getMidtermScore(), 0, 4);
        editTableModel.setValueAt(score.getFinalScore(), 0, 5);
        editTableModel.setValueAt(score.getTotalScore(), 0, 6);
        editTableModel.setValueAt(DisplayTextUtil.formatStatus(score.getResult()), 0, 7);
    }

    private void handleSaveScore() {
        if (selectedScore == null) {
            return;
        }

        try {
            if (editTable.isEditing()) {
                editTable.getCellEditor().stopCellEditing();
            }

            Long selectedEnrollmentId = getEnrollmentId(selectedScore);

            selectedScore.setProcessScore(parseScoreValue(editTableModel.getValueAt(0, 3)));
            selectedScore.setMidtermScore(parseScoreValue(editTableModel.getValueAt(0, 4)));
            selectedScore.setFinalScore(parseScoreValue(editTableModel.getValueAt(0, 5)));

            selectedScore = scoreController.saveScore(selectedScore);
            DialogUtil.showInfo(this, "Lưu điểm thành công cho sinh viên " + getStudentName(selectedScore));
            refreshScoreData(false, selectedEnrollmentId);
        } catch (NumberFormatException exception) {
            DialogUtil.showError(this, "Vui lòng nhập điểm là số hợp lệ ở các cột QT, GK, CK.");
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private double parseScoreValue(Object value) {
        String normalizedValue = value == null ? "" : String.valueOf(value).trim();
        return Double.parseDouble(normalizedValue);
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
        scoreTableModel.setRowCount(0);

        for (Score score : allScores) {
            if (matchesCurrentCourseFilter(score)) {
                courseFilteredScores.add(score);
            }
        }

        for (Score score : courseFilteredScores) {
            if (matchesCurrentKeyword(score)) {
                filteredScores.add(score);
                scoreTableModel.addRow(new Object[]{
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
        if (scoreTable.getSelectedRow() < 0) {
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
            scoreTable.clearSelection();
            return;
        }

        for (int row = 0; row < filteredScores.size(); row++) {
            if (preferredEnrollmentId.equals(getEnrollmentId(filteredScores.get(row)))) {
                scoreTable.setRowSelectionInterval(row, row);
                scoreTable.scrollRectToVisible(scoreTable.getCellRect(row, 0, true));
                return;
            }
        }
        scoreTable.clearSelection();
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

    private static final class ResponsiveTable extends JTable {

        private ResponsiveTable(DefaultTableModel model) {
            super(model);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return getPreferredSize().width < getParent().getWidth();
        }
    }
}
