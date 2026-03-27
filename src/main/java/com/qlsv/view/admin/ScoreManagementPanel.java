package com.qlsv.view.admin;

import com.qlsv.controller.ScoreManagementScreenController;
import com.qlsv.dto.ScoreDisplayDto;
import com.qlsv.model.ClassRoom;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Score;
import com.qlsv.model.Student;
import com.qlsv.utils.AcademicFormatUtil;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BasePanel;
import com.qlsv.view.common.FilterOption;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScoreManagementPanel extends BasePanel {

    private static final String FILTER_NONE = "Chọn điều kiện lọc";
    private static final String FILTER_ALL = "Tất cả sinh viên";
    private static final String FILTER_SECTION_CODE = "Theo mã học phần";
    private static final String FILTER_CLASS_ROOM = "Theo lớp";
    private static final int TOOLBAR_GAP = 8;
    private static final int DETAIL_TABLE_VIEWPORT_HEIGHT = 220;
    private static final int DETAIL_SECTION_MIN_HEIGHT = 300;

    private final ScoreManagementScreenController screenController = new ScoreManagementScreenController();

    private final JComboBox<String> filterTypeComboBox = new JComboBox<>(
            new String[]{FILTER_NONE, FILTER_ALL, FILTER_SECTION_CODE, FILTER_CLASS_ROOM}
    );
    private final JComboBox<FilterOption<?>> filterValueComboBox = new JComboBox<>();
    private final JTextField keywordField = new JTextField(24);

    private final JButton applyButton = new JButton("Áp dụng");
    private final JButton resetButton = new JButton("Đặt lại");
    private final JButton reloadButton = new JButton("Tải lại");
    private final JButton addButton = new JButton("Thêm điểm");
    private final JButton editButton = new JButton("Sửa điểm");
    private final JButton deleteButton = new JButton("Xóa điểm");

    private final DefaultTableModel studentTableModel = new DefaultTableModel(
            new String[]{"Mã SV", "Họ tên", "Lớp", "Khoa", "Số môn", "Điểm TB"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel detailTableModel = new DefaultTableModel(
            new String[]{"Mã học phần", "Tên môn", "Học kỳ", "QT", "GK", "CK", "Tổng kết", "Trạng thái"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable studentTable = new JTable(studentTableModel);
    private final JTable detailTable = new JTable(detailTableModel);
    private final JLabel selectedStudentLabel = new JLabel("Chưa chọn sinh viên");
    private final JLabel selectedStudentHintLabel = new JLabel("Chọn một sinh viên trong bảng trên để xem toàn bộ điểm chi tiết.");
    private final JLabel studentCodeValueLabel = new JLabel("Chưa chọn");
    private final JLabel studentNameValueLabel = new JLabel("Chưa chọn");
    private final JLabel classRoomValueLabel = new JLabel("Chưa chọn");
    private final JLabel facultyValueLabel = new JLabel("Chưa chọn");
    private final JLabel scoreCountValueLabel = new JLabel("0");
    private final JLabel averageScoreValueLabel = new JLabel("0.00");

    private JPanel filterFieldsPanel;
    private JPanel toolbarButtonPanel;
    private JPanel secondaryActionPanel;
    private JPanel controlCard;

    private final List<Score> loadedScores = new ArrayList<>();
    private final List<StudentScoreSummary> studentRows = new ArrayList<>();
    private final List<Score> selectedStudentScores = new ArrayList<>();

    private boolean filterReady;

    public ScoreManagementPanel() {
        setOpaque(true);
        setBackground(AppColors.CONTENT_BACKGROUND);
        initComponents();
        filterTypeComboBox.setSelectedItem(FILTER_ALL);
        reloadFilterValues();
        reloadData();
    }

    @Override
    public void reloadData() {
        try {
            applyCurrentFilters(getSelectedStudentId());
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    @Override
    public void setVisible(boolean visible) {
        boolean wasVisible = isVisible();
        super.setVisible(visible);
        if (visible && !wasVisible) {
            SwingUtilities.invokeLater(this::reloadData);
        }
    }

    private void initComponents() {
        JLabel titleLabel = new JLabel("Quản lý điểm");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        JLabel subtitleLabel = new JLabel("Theo dõi danh sách sinh viên có điểm và xem chi tiết theo từng môn học.");
        subtitleLabel.setForeground(AppColors.CARD_MUTED_TEXT);

        JPanel headerPanel = new JPanel(new GridLayout(0, 1, 0, 4));
        headerPanel.setOpaque(false);
        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);

        styleTextField(keywordField);
        keywordField.setPreferredSize(new Dimension(280, 40));
        keywordField.setMinimumSize(new Dimension(220, 40));
        keywordField.addActionListener(event -> reloadData());

        filterTypeComboBox.setPreferredSize(new Dimension(170, 36));
        filterTypeComboBox.setMinimumSize(new Dimension(150, 36));
        filterValueComboBox.setPreferredSize(new Dimension(160, 36));
        filterValueComboBox.setMinimumSize(new Dimension(150, 36));
        filterValueComboBox.setEnabled(false);

        styleActionButton(applyButton, AppColors.BUTTON_PRIMARY);
        styleActionButton(resetButton, AppColors.BUTTON_NEUTRAL);
        styleActionButton(reloadButton, AppColors.BUTTON_NEUTRAL);
        styleActionButton(addButton, AppColors.BUTTON_SUCCESS);
        styleActionButton(editButton, AppColors.BUTTON_WARNING);
        styleActionButton(deleteButton, AppColors.BUTTON_DANGER);

        applyButton.addActionListener(event -> {
            filterReady = hasValidFilterSelection();
            reloadData();
        });
        resetButton.addActionListener(event -> resetFilters());
        reloadButton.addActionListener(event -> reloadData());
        addButton.addActionListener(event -> handleAddScore());
        editButton.addActionListener(event -> handleEditScore());
        deleteButton.addActionListener(event -> handleDeleteScore());
        filterTypeComboBox.addActionListener(event -> {
            reloadFilterValues();
        });

        filterFieldsPanel = new JPanel(new GridBagLayout());
        filterFieldsPanel.setOpaque(false);
        GridBagConstraints filterConstraints = new GridBagConstraints();
        filterConstraints.gridy = 0;
        filterConstraints.insets = new Insets(0, 0, 0, TOOLBAR_GAP);
        filterConstraints.anchor = GridBagConstraints.WEST;

        filterConstraints.gridx = 0;
        filterFieldsPanel.add(new JLabel("Điều kiện"), filterConstraints);

        filterConstraints.gridx = 1;
        filterFieldsPanel.add(filterTypeComboBox, filterConstraints);

        filterConstraints.gridx = 2;
        filterFieldsPanel.add(new JLabel("Giá trị"), filterConstraints);

        filterConstraints.gridx = 3;
        filterFieldsPanel.add(filterValueComboBox, filterConstraints);

        filterConstraints.gridx = 4;
        filterFieldsPanel.add(new JLabel("Từ khóa"), filterConstraints);

        filterConstraints.gridx = 5;
        filterConstraints.weightx = 1.0;
        filterConstraints.fill = GridBagConstraints.HORIZONTAL;
        filterConstraints.insets = new Insets(0, 0, 0, 0);
        filterFieldsPanel.add(keywordField, filterConstraints);

        toolbarButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, TOOLBAR_GAP, 0));
        toolbarButtonPanel.setOpaque(false);
        toolbarButtonPanel.add(applyButton);
        toolbarButtonPanel.add(resetButton);
        toolbarButtonPanel.add(reloadButton);

        secondaryActionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, TOOLBAR_GAP, 0));
        secondaryActionPanel.setOpaque(false);
        secondaryActionPanel.add(addButton);
        secondaryActionPanel.add(editButton);
        secondaryActionPanel.add(deleteButton);

        JPanel toolbarMainRowPanel = new JPanel(new BorderLayout(12, 8));
        toolbarMainRowPanel.setOpaque(false);
        toolbarMainRowPanel.add(filterFieldsPanel, BorderLayout.CENTER);
        toolbarMainRowPanel.add(toolbarButtonPanel, BorderLayout.EAST);

        controlCard = createSurfaceCard(new BorderLayout(0, 12));
        controlCard.add(toolbarMainRowPanel, BorderLayout.NORTH);
        controlCard.add(secondaryActionPanel, BorderLayout.SOUTH);

        configureTable(studentTable);
        configureTable(detailTable);
        studentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        detailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configureDetailTableColumns();
        studentTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                bindStudentDetail(getSelectedStudentSummary());
            }
        });

        JScrollPane studentTableScrollPane = createTableScrollPane(studentTable, false);
        JScrollPane detailTableScrollPane = createDetailTableScrollPane();

        JPanel studentSection = createSectionPanel(
                "Danh sách sinh viên",
                "Chọn sinh viên để xem toàn bộ điểm bên dưới.",
                studentTableScrollPane
        );
        JPanel detailSection = createDetailSectionPanel(detailTableScrollPane);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, studentSection, detailSection);
        splitPane.setResizeWeight(0.52);
        splitPane.setBorder(null);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerSize(10);
        splitPane.setOpaque(false);
        splitPane.setBackground(AppColors.CONTENT_BACKGROUND);

        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(controlCard, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> {
            splitPane.setDividerLocation(0.52);
        });
    }

    private void applyCurrentFilters(Long preferredStudentId) {
        List<Score> scores = screenController.loadItems(
                filterReady,
                (String) filterTypeComboBox.getSelectedItem(),
                getSelectedFilterValue(Object.class),
                FILTER_ALL,
                FILTER_SECTION_CODE,
                FILTER_CLASS_ROOM
        );

        loadedScores.clear();
        String normalizedKeyword = normalizeSearchText(keywordField.getText());
        for (Score score : scores) {
            if (matchesKeyword(score, normalizedKeyword)) {
                loadedScores.add(score);
            }
        }

        bindStudentRows(buildStudentRows(loadedScores), preferredStudentId);
    }

    private void bindStudentRows(List<StudentScoreSummary> summaries, Long preferredStudentId) {
        studentRows.clear();
        studentRows.addAll(summaries);
        studentTableModel.setRowCount(0);

        for (StudentScoreSummary summary : studentRows) {
            studentTableModel.addRow(toStudentRow(summary));
        }

        if (studentRows.isEmpty()) {
            studentTable.clearSelection();
            bindStudentDetail(null);
            selectedStudentHintLabel.setText(filterReady
                    ? "Không tìm thấy sinh viên có điểm phù hợp với điều kiện hiện tại."
                    : "Vui lòng chọn điều kiện lọc để hiển thị danh sách điểm.");
            return;
        }

        int preferredIndex = 0;
        if (preferredStudentId != null) {
            for (int index = 0; index < studentRows.size(); index++) {
                StudentScoreSummary summary = studentRows.get(index);
                if (preferredStudentId.equals(summary.studentId())) {
                    preferredIndex = index;
                    break;
                }
            }
        }

        studentTable.setRowSelectionInterval(preferredIndex, preferredIndex);
        studentTable.scrollRectToVisible(studentTable.getCellRect(preferredIndex, 0, true));
    }

    private void bindStudentDetail(StudentScoreSummary summary) {
        selectedStudentScores.clear();
        detailTableModel.setRowCount(0);
        updateSelectedStudentLabels(summary);

        if (summary == null) {
            detailTable.clearSelection();
            return;
        }

        selectedStudentScores.addAll(summary.scores());
        selectedStudentHintLabel.setText("Chi tiết toàn bộ môn học đã có điểm của sinh viên được chọn.");

        for (Score score : selectedStudentScores) {
            ScoreDisplayDto displayDto = screenController.toDisplayDto(score);
            detailTableModel.addRow(new Object[]{
                    safeSectionCode(score),
                    displayDto.subjectName(),
                    score.getEnrollment() == null || score.getEnrollment().getCourseSection() == null
                            ? ""
                            : AcademicFormatUtil.formatSemester(score.getEnrollment().getCourseSection().getSemester()),
                    displayDto.processScore(),
                    displayDto.midtermScore(),
                    displayDto.finalScore(),
                    displayDto.totalScore(),
                    displayDto.resultText()
            });
        }

        if (!selectedStudentScores.isEmpty()) {
            detailTable.setRowSelectionInterval(0, 0);
        }
    }

    private void handleAddScore() {
        try {
            Score score = promptForScore(null);
            if (score == null) {
                return;
            }
            screenController.saveScore(score);
            DialogUtil.showInfo(this, "Lưu điểm thành công.");
            applyCurrentFilters(score.getEnrollment() == null || score.getEnrollment().getStudent() == null
                    ? null
                    : score.getEnrollment().getStudent().getId());
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void handleEditScore() {
        Score selectedScore = getSelectedDetailScore();
        if (selectedScore == null) {
            DialogUtil.showError(this, "Hãy chọn một môn học trong bảng chi tiết để sửa điểm.");
            return;
        }

        try {
            Score updatedScore = promptForScore(selectedScore);
            if (updatedScore == null) {
                return;
            }
            screenController.saveScore(updatedScore);
            DialogUtil.showInfo(this, "Cập nhật điểm thành công.");
            applyCurrentFilters(updatedScore.getEnrollment() == null || updatedScore.getEnrollment().getStudent() == null
                    ? null
                    : updatedScore.getEnrollment().getStudent().getId());
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void handleDeleteScore() {
        Score selectedScore = getSelectedDetailScore();
        if (selectedScore == null) {
            DialogUtil.showError(this, "Hãy chọn một môn học trong bảng chi tiết để xóa điểm.");
            return;
        }
        if (!DialogUtil.confirm(this, "Bạn có chắc chắn muốn xóa bản ghi điểm này không?")) {
            return;
        }

        Long preferredStudentId = selectedScore.getEnrollment() == null || selectedScore.getEnrollment().getStudent() == null
                ? null
                : selectedScore.getEnrollment().getStudent().getId();
        try {
            screenController.deleteScore(selectedScore);
            DialogUtil.showInfo(this, "Xóa điểm thành công.");
            applyCurrentFilters(preferredStudentId);
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private Score promptForScore(Score existingItem) {
        JComboBox<Enrollment> enrollmentComboBox = new JComboBox<>(screenController.loadEnrollments().toArray(new Enrollment[0]));
        JTextField processField = new JTextField(existingItem == null || existingItem.getProcessScore() == null
                ? ""
                : String.valueOf(existingItem.getProcessScore()));
        JTextField midtermField = new JTextField(existingItem == null || existingItem.getMidtermScore() == null
                ? ""
                : String.valueOf(existingItem.getMidtermScore()));
        JTextField finalField = new JTextField(existingItem == null || existingItem.getFinalScore() == null
                ? ""
                : String.valueOf(existingItem.getFinalScore()));

        if (existingItem != null && existingItem.getEnrollment() != null) {
            enrollmentComboBox.setSelectedItem(existingItem.getEnrollment());
        }

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.add(new JLabel("Đăng ký học phần"));
        formPanel.add(enrollmentComboBox);
        formPanel.add(new JLabel("Điểm quá trình"));
        formPanel.add(processField);
        formPanel.add(new JLabel("Điểm giữa kỳ"));
        formPanel.add(midtermField);
        formPanel.add(new JLabel("Điểm cuối kỳ"));
        formPanel.add(finalField);

        while (true) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    formPanel,
                    existingItem == null ? "Thêm điểm" : "Cập nhật điểm",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
            if (result != JOptionPane.OK_OPTION) {
                return null;
            }

            try {
                return screenController.applyFormData(
                        existingItem,
                        new ScoreManagementScreenController.ScoreFormData(
                                (Enrollment) enrollmentComboBox.getSelectedItem(),
                                processField.getText(),
                                midtermField.getText(),
                                finalField.getText()
                        )
                );
            } catch (Exception exception) {
                DialogUtil.showError(this, exception.getMessage());
            }
        }
    }

    private void reloadFilterValues() {
        filterReady = FILTER_ALL.equals(filterTypeComboBox.getSelectedItem());
        filterValueComboBox.removeAllItems();

        String filterType = (String) filterTypeComboBox.getSelectedItem();
        if (FILTER_SECTION_CODE.equals(filterType)) {
            filterValueComboBox.setEnabled(true);
            filterValueComboBox.addItem(new FilterOption<>("Chọn học phần", null));
            for (CourseSection courseSection : screenController.loadCourseSections()) {
                filterValueComboBox.addItem(new FilterOption<>(courseSection.getSectionCode(), courseSection));
            }
            return;
        }

        if (FILTER_CLASS_ROOM.equals(filterType)) {
            filterValueComboBox.setEnabled(true);
            filterValueComboBox.addItem(new FilterOption<>("Chọn lớp", null));
            for (ClassRoom classRoom : screenController.loadClassRooms()) {
                filterValueComboBox.addItem(new FilterOption<>(classRoom.getClassCode() + " - " + classRoom.getClassName(), classRoom));
            }
            return;
        }

        filterValueComboBox.setEnabled(false);
    }

    private void resetFilters() {
        filterTypeComboBox.setSelectedItem(FILTER_ALL);
        keywordField.setText("");
        reloadFilterValues();
        reloadData();
    }

    private boolean hasValidFilterSelection() {
        String filterType = (String) filterTypeComboBox.getSelectedItem();
        if (FILTER_ALL.equals(filterType)) {
            return true;
        }
        FilterOption<?> selectedOption = (FilterOption<?>) filterValueComboBox.getSelectedItem();
        return selectedOption != null && selectedOption.value() != null;
    }

    private <T> T getSelectedFilterValue(Class<T> type) {
        FilterOption<?> selectedOption = (FilterOption<?>) filterValueComboBox.getSelectedItem();
        if (selectedOption == null || selectedOption.value() == null || !type.isInstance(selectedOption.value())) {
            return null;
        }
        return type.cast(selectedOption.value());
    }

    private StudentScoreSummary getSelectedStudentSummary() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= studentRows.size()) {
            return null;
        }
        return studentRows.get(selectedRow);
    }

    private Long getSelectedStudentId() {
        StudentScoreSummary summary = getSelectedStudentSummary();
        return summary == null ? null : summary.studentId();
    }

    private Score getSelectedDetailScore() {
        int selectedRow = detailTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= selectedStudentScores.size()) {
            return null;
        }
        return selectedStudentScores.get(selectedRow);
    }

    private List<StudentScoreSummary> buildStudentRows(List<Score> scores) {
        Map<Long, List<Score>> scoresByStudentId = new LinkedHashMap<>();
        for (Score score : scores) {
            Student student = score.getEnrollment() == null ? null : score.getEnrollment().getStudent();
            if (student == null || student.getId() == null) {
                continue;
            }
            scoresByStudentId.computeIfAbsent(student.getId(), ignored -> new ArrayList<>()).add(score);
        }

        List<StudentScoreSummary> summaries = new ArrayList<>();
        for (List<Score> studentScores : scoresByStudentId.values()) {
            summaries.add(toStudentRow(studentScores));
        }
        summaries.sort(Comparator.comparing(
                StudentScoreSummary::studentCode,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
        ));
        return summaries;
    }

    private StudentScoreSummary toStudentRow(List<Score> studentScores) {
        Score firstScore = studentScores.get(0);
        Student student = firstScore.getEnrollment().getStudent();
        String classRoomName = student.getClassRoom() == null ? "" : student.getClassRoom().getClassName();
        String facultyName = student.getFaculty() == null ? "" : student.getFaculty().getFacultyName();
        double averageScore = studentScores.stream()
                .map(Score::getTotalScore)
                .filter(value -> value != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        return new StudentScoreSummary(
                student.getId(),
                student.getStudentCode(),
                student.getFullName(),
                classRoomName,
                facultyName,
                studentScores.size(),
                averageScore,
                new ArrayList<>(studentScores)
        );
    }

    private Object[] toStudentRow(StudentScoreSummary summary) {
        return new Object[]{
                summary.studentCode(),
                summary.studentName(),
                summary.classRoomName(),
                summary.facultyName(),
                summary.scoreCount(),
                formatScore(summary.averageScore())
        };
    }

    private boolean matchesKeyword(Score score, String normalizedKeyword) {
        if (normalizedKeyword.isBlank()) {
            return true;
        }

        ScoreDisplayDto displayDto = screenController.toDisplayDto(score);
        return normalizeSearchText(displayDto.studentCode()).contains(normalizedKeyword)
                || normalizeSearchText(displayDto.studentName()).contains(normalizedKeyword)
                || normalizeSearchText(displayDto.classRoomName()).contains(normalizedKeyword)
                || normalizeSearchText(displayDto.sectionCode()).contains(normalizedKeyword)
                || normalizeSearchText(displayDto.subjectName()).contains(normalizedKeyword)
                || normalizeSearchText(displayDto.resultText()).contains(normalizedKeyword);
    }

    private void updateSelectedStudentLabels(StudentScoreSummary summary) {
        if (summary == null) {
            selectedStudentLabel.setText("Chưa chọn sinh viên");
            selectedStudentHintLabel.setText("Chọn một sinh viên trong bảng trên để xem toàn bộ điểm chi tiết.");
            studentCodeValueLabel.setText("Chưa chọn");
            studentNameValueLabel.setText("Chưa chọn");
            classRoomValueLabel.setText("Chưa chọn");
            facultyValueLabel.setText("Chưa chọn");
            scoreCountValueLabel.setText("0");
            averageScoreValueLabel.setText("0.00");
            return;
        }

        selectedStudentLabel.setText("Chi tiết điểm của " + summary.studentName());
        studentCodeValueLabel.setText(summary.studentCode());
        studentNameValueLabel.setText(summary.studentName());
        classRoomValueLabel.setText(summary.classRoomName());
        facultyValueLabel.setText(summary.facultyName());
        scoreCountValueLabel.setText(String.valueOf(summary.scoreCount()));
        averageScoreValueLabel.setText(formatScore(summary.averageScore()));
    }

    private String safeSectionCode(Score score) {
        if (score == null || score.getEnrollment() == null || score.getEnrollment().getCourseSection() == null) {
            return "";
        }
        return score.getEnrollment().getCourseSection().getSectionCode();
    }

    private String normalizeSearchText(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }

    private JPanel createDetailSectionPanel(JScrollPane detailTableScrollPane) {
        JPanel contentPanel = new JPanel(new BorderLayout(0, 12));
        contentPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(0, 1, 0, 4));
        titlePanel.setOpaque(false);
        selectedStudentLabel.setFont(selectedStudentLabel.getFont().deriveFont(Font.BOLD, 17f));
        selectedStudentLabel.setForeground(AppColors.CARD_VALUE_TEXT);
        selectedStudentHintLabel.setForeground(AppColors.CARD_MUTED_TEXT);
        titlePanel.add(selectedStudentLabel);
        titlePanel.add(selectedStudentHintLabel);

        JPanel infoGridPanel = new JPanel(new GridLayout(0, 3, 12, 12));
        infoGridPanel.setOpaque(false);
        infoGridPanel.add(createInfoCard("Mã sinh viên", studentCodeValueLabel));
        infoGridPanel.add(createInfoCard("Họ tên", studentNameValueLabel));
        infoGridPanel.add(createInfoCard("Lớp", classRoomValueLabel));
        infoGridPanel.add(createInfoCard("Khoa", facultyValueLabel));
        infoGridPanel.add(createInfoCard("Số môn có điểm", scoreCountValueLabel));
        infoGridPanel.add(createInfoCard("Điểm trung bình", averageScoreValueLabel));

        contentPanel.add(titlePanel, BorderLayout.NORTH);
        contentPanel.add(infoGridPanel, BorderLayout.CENTER);

        JPanel sectionPanel = createSectionPanel(
                "Thông tin điểm chi tiết",
                "Bảng bên dưới hiển thị toàn bộ các môn học mà sinh viên đã có điểm.",
                detailTableScrollPane
        );
        sectionPanel.setMinimumSize(new Dimension(0, DETAIL_SECTION_MIN_HEIGHT));

        JPanel container = createSurfaceCard(new BorderLayout(0, 12));
        container.add(contentPanel, BorderLayout.NORTH);
        container.add(sectionPanel, BorderLayout.CENTER);
        return container;
    }

    private JPanel createInfoCard(String label, JLabel valueLabel) {
        JPanel cardPanel = new JPanel(new BorderLayout(0, 6));
        cardPanel.setOpaque(true);
        cardPanel.setBackground(AppColors.CONTENT_BACKGROUND);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD, 12.5f));
        labelComponent.setForeground(AppColors.CARD_TITLE_TEXT);

        valueLabel.setForeground(AppColors.CARD_VALUE_TEXT);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.PLAIN, 13.5f));

        cardPanel.add(labelComponent, BorderLayout.NORTH);
        cardPanel.add(valueLabel, BorderLayout.CENTER);
        return cardPanel;
    }

    private JPanel createSectionPanel(String title, String description, JComponent content) {
        JPanel panel = createSurfaceCard(new BorderLayout(0, 12));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 17f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setForeground(AppColors.CARD_MUTED_TEXT);

        JPanel headerPanel = new JPanel(new GridLayout(0, 1, 0, 4));
        headerPanel.setOpaque(false);
        headerPanel.add(titleLabel);
        headerPanel.add(descriptionLabel);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane createTableScrollPane(JTable table, boolean alwaysShowVerticalBar) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.CARD_BORDER));
        scrollPane.setVerticalScrollBarPolicy(alwaysShowVerticalBar
                ? JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
                : JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.setMinimumSize(new Dimension(0, 180));
        return scrollPane;
    }

    private JScrollPane createDetailTableScrollPane() {
        JScrollPane scrollPane = createTableScrollPane(detailTable, true);
        Dimension preferredSize = scrollPane.getPreferredSize();
        scrollPane.setPreferredSize(new Dimension(preferredSize.width, DETAIL_TABLE_VIEWPORT_HEIGHT));
        scrollPane.setMinimumSize(new Dimension(0, DETAIL_TABLE_VIEWPORT_HEIGHT));
        return scrollPane;
    }

    private JPanel createSurfaceCard(LayoutManager layoutManager) {
        JPanel panel = new JPanel(layoutManager);
        panel.setOpaque(true);
        panel.setBackground(AppColors.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));
        return panel;
    }

    private void configureTable(JTable table) {
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setGridColor(AppColors.CARD_BORDER);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(AppColors.TABLE_SELECTION_BACKGROUND);
        table.setSelectionForeground(AppColors.CARD_VALUE_TEXT);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(AppColors.TABLE_HEADER_BACKGROUND);
        table.getTableHeader().setForeground(AppColors.CARD_VALUE_TEXT);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 13f));
    }

    private void configureDetailTableColumns() {
        TableColumnModel columnModel = detailTable.getColumnModel();
        int[] preferredWidths = {150, 320, 130, 85, 85, 85, 110, 150};
        for (int index = 0; index < preferredWidths.length && index < columnModel.getColumnCount(); index++) {
            columnModel.getColumn(index).setPreferredWidth(preferredWidths[index]);
        }
    }

    private void styleTextField(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.INPUT_BORDER),
                BorderFactory.createEmptyBorder(9, 10, 9, 10)
        ));
    }

    private void styleActionButton(JButton button, Color background) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(background);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 13f));
    }

    private String formatScore(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private record StudentScoreSummary(
            Long studentId,
            String studentCode,
            String studentName,
            String classRoomName,
            String facultyName,
            int scoreCount,
            double averageScore,
            List<Score> scores
    ) {
    }
}
