/**
 * Màn hình quản trị cho quản lý điểm.
 */
package com.qlsv.view.admin;

import com.qlsv.controller.DisplayField;
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
import com.qlsv.view.common.DetailSectionPanel;
import com.qlsv.view.common.FilterOption;
import com.qlsv.view.dialog.ScoreDetailDialog;
import com.qlsv.view.dialog.ScoreFormDialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.table.TableColumnModel;

public class ScoreManagementPanel extends BasePanel {

    private static final String FILTER_NONE = "Chọn điều kiện lọc";
    private static final String FILTER_ALL = "Tất cả sinh viên";
    private static final String FILTER_SECTION_CODE = "Theo mã học phần";
    private static final String FILTER_CLASS_ROOM = "Theo lớp";
    private static final int TOOLBAR_GAP = 8;
    private static final int TABLE_SECTION_MIN_HEIGHT = 240;
    private static final int DETAIL_TABLE_VIEWPORT_HEIGHT = 220;
    private static final int DETAIL_SECTION_MIN_HEIGHT = 300;
    private static final String LOADING_CARD = "loading";
    private static final String DATA_CARD = "table";
    private static final String EMPTY_CARD = "empty";

    private final ScoreManagementScreenController screenController = new ScoreManagementScreenController();

    private final JComboBox<String> filterTypeComboBox = new JComboBox<>(
            new String[]{FILTER_NONE, FILTER_ALL, FILTER_SECTION_CODE, FILTER_CLASS_ROOM}
    );
    private final JComboBox<FilterOption<?>> filterValueComboBox = new JComboBox<>();
    private final JTextField keywordField = new JTextField(24);

    private final JButton searchButton = new JButton("Tìm");
    private final JButton applyButton = new JButton("Áp dụng");
    private final JButton resetButton = new JButton("Đặt lại");
    private final JButton reloadButton = new JButton("Tải lại");
    private final JButton addButton = new JButton("Thêm điểm");
    private final JButton editButton = new JButton("Sửa điểm");
    private final JButton deleteButton = new JButton("Xóa điểm");
    private final JPanel mainContentPanel = new JPanel(new BorderLayout(0, 12));
    private final JPanel tableCardPanel = new JPanel(new CardLayout());
    private final CardLayout tableCardLayout = (CardLayout) tableCardPanel.getLayout();
    private final JLabel emptyStateLabel = new JLabel("", SwingConstants.CENTER);
    private final DetailSectionPanel detailSummaryPanel = new DetailSectionPanel(
            "Chi tiết thông tin điểm",
            "Vui lòng chọn sinh viên để xem chi tiết điểm."
    );

    private final DefaultTableModel studentTableModel = new DefaultTableModel(
            new String[]{"Mã SV", "Họ tên", "Lớp", "Khoa", "Số môn", "Điểm TB"}, 0
    ) {
        /**
         * Xác định ô có cho phép chỉnh sửa hay không.
         */
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel detailTableModel = new DefaultTableModel(
            new String[]{"Mã học phần", "Tên môn", "Học kỳ", "QT", "GK", "CK", "Tổng kết", "Trạng thái"}, 0
    ) {
        /**
         * Xác định ô có cho phép chỉnh sửa hay không.
         */
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable studentTable = new ResponsiveTable(studentTableModel);
    private final JTable detailTable = new ResponsiveTable(detailTableModel);

    private JPanel filterFieldsPanel;
    private JPanel toolbarButtonPanel;
    private JPanel secondaryActionPanel;
    private JPanel controlCard;
    private JPanel detailContentPanel;
    private ScoreDetailDialog detailDialog;

    private final List<Score> loadedScores = new ArrayList<>();
    private final List<StudentScoreSummary> studentRows = new ArrayList<>();
    private final List<Score> selectedStudentScores = new ArrayList<>();

    private boolean filterReady;
    private boolean suppressDetailDialogOpening;
    private boolean isLoadingData;
    private SwingWorker<?, ?> activeWorker;

    /**
     * Khởi tạo quản lý điểm.
     */
    public ScoreManagementPanel() {
        setOpaque(true);
        setBackground(AppColors.CONTENT_BACKGROUND);
        initComponents();
        filterTypeComboBox.setSelectedItem(FILTER_ALL);
        reloadFilterValues();
        reloadData();
    }

    /**
     * Làm mới dữ liệu đang hiển thị.
     */
    @Override
    public void reloadData() {
        applyCurrentFiltersAsync(getSelectedStudentId());
    }

    /**
     * Cập nhật visible.
     */
    @Override
    public void setVisible(boolean visible) {
        boolean wasVisible = isVisible();
        super.setVisible(visible);
        if (visible && !wasVisible) {
            SwingUtilities.invokeLater(this::reloadData);
        }
    }

    /**
     * Giải phóng tài nguyên khi thành phần bị gỡ.
     */
    @Override
    public void removeNotify() {
        disposeDetailDialog();
        super.removeNotify();
    }

    /**
     * Khởi tạo các thành phần giao diện.
     */
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
        keywordField.setMinimumSize(new Dimension(160, 36));
        keywordField.addActionListener(event -> reloadData());

        filterTypeComboBox.setPreferredSize(new Dimension(170, 36));
        filterTypeComboBox.setMinimumSize(new Dimension(150, 36));
        filterValueComboBox.setMinimumSize(new Dimension(160, 36));
        filterValueComboBox.setEnabled(false);

        styleActionButton(searchButton, AppColors.BUTTON_PRIMARY);
        styleActionButton(applyButton, AppColors.BUTTON_PRIMARY);
        styleActionButton(resetButton, AppColors.BUTTON_NEUTRAL);
        styleActionButton(reloadButton, AppColors.BUTTON_NEUTRAL);
        styleActionButton(addButton, AppColors.BUTTON_SUCCESS);
        styleActionButton(editButton, AppColors.BUTTON_WARNING);
        styleActionButton(deleteButton, AppColors.BUTTON_DANGER);

        searchButton.addActionListener(event -> reloadData());
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
        filterConstraints.weightx = 0.5;
        filterConstraints.fill = GridBagConstraints.HORIZONTAL;
        filterConstraints.insets = new Insets(0, 0, 0, TOOLBAR_GAP);
        filterFieldsPanel.add(filterValueComboBox, filterConstraints);

        filterConstraints.gridx = 4;
        filterConstraints.weightx = 0.0;
        filterConstraints.fill = GridBagConstraints.NONE;
        filterFieldsPanel.add(new JLabel("Từ khóa"), filterConstraints);

        filterConstraints.gridx = 5;
        filterConstraints.weightx = 0.5;
        filterConstraints.fill = GridBagConstraints.HORIZONTAL;
        filterConstraints.insets = new Insets(0, 0, 0, TOOLBAR_GAP);
        filterFieldsPanel.add(keywordField, filterConstraints);

        filterConstraints.gridx = 6;
        filterConstraints.weightx = 0.0;
        filterConstraints.fill = GridBagConstraints.NONE;
        filterFieldsPanel.add(searchButton, filterConstraints);

        toolbarButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, TOOLBAR_GAP, 0));
        toolbarButtonPanel.setOpaque(false);
        toolbarButtonPanel.add(applyButton);
        toolbarButtonPanel.add(resetButton);
        toolbarButtonPanel.add(reloadButton);

        secondaryActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, TOOLBAR_GAP, 0));
        secondaryActionPanel.setOpaque(false);
        secondaryActionPanel.add(addButton);
        secondaryActionPanel.add(editButton);
        secondaryActionPanel.add(deleteButton);

        JPanel toolbarFooterPanel = new JPanel(new BorderLayout(12, 8));
        toolbarFooterPanel.setOpaque(false);
        toolbarFooterPanel.add(toolbarButtonPanel, BorderLayout.WEST);
        toolbarFooterPanel.add(secondaryActionPanel, BorderLayout.EAST);

        controlCard = createSurfaceCard(new BorderLayout(0, 12));
        controlCard.add(filterFieldsPanel, BorderLayout.NORTH);
        controlCard.add(toolbarFooterPanel, BorderLayout.SOUTH);

        configureTable(studentTable);
        configureTable(detailTable);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        detailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configureStudentTableColumns();
        configureDetailTableColumns();
        studentTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                handleStudentSelectionChanged();
            }
        });
        studentTable.addMouseListener(new MouseAdapter() {
            /**
             * Xử lý thao tác nhấp chuột trên giao diện.
             */
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && getSelectedStudentSummary() != null) {
                    showDetailDialog();
                }
            }
        });
        detailTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                updateDetailSummary(getSelectedStudentSummary(), getSelectedDetailScore());
            }
        });

        JScrollPane studentTableScrollPane = createTableScrollPane(studentTable, false);
        JScrollPane detailTableScrollPane = createDetailTableScrollPane();
        tableCardPanel.setOpaque(false);
        mainContentPanel.setOpaque(false);
        tableCardPanel.add(studentTableScrollPane, DATA_CARD);
        tableCardPanel.add(createEmptyStatePanel(), EMPTY_CARD);
        tableCardPanel.add(createLoadingPanel(), LOADING_CARD);
        tableCardPanel.setMinimumSize(new Dimension(0, TABLE_SECTION_MIN_HEIGHT));
        detailContentPanel = createDetailContentPanel(detailTableScrollPane);
        mainContentPanel.add(tableCardPanel, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(controlCard, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(mainContentPanel, BorderLayout.CENTER);
    }

    /**
     * Xử lý apply hiện tại filters theo cách bất đồng bộ.
     */
    private void applyCurrentFiltersAsync(Long preferredStudentId) {
        if (activeWorker != null && !activeWorker.isDone()) {
            activeWorker.cancel(true);
        }

        setLoadingState(true);
        String filterType = (String) filterTypeComboBox.getSelectedItem();
        Object filterValue = getSelectedFilterValue(Object.class);
        String keyword = keywordField.getText();
        boolean ready = filterReady;

        activeWorker = new SwingWorker<List<Score>, Void>() {
            @Override
            protected List<Score> doInBackground() {
                List<Score> scores = screenController.loadItems(
                        ready,
                        filterType,
                        filterValue,
                        FILTER_ALL,
                        FILTER_SECTION_CODE,
                        FILTER_CLASS_ROOM
                );

                List<Score> filtered = new ArrayList<>();
                String normalizedKeyword = normalizeSearchText(keyword);
                for (Score score : scores) {
                    if (matchesKeyword(score, normalizedKeyword)) {
                        filtered.add(score);
                    }
                }
                return filtered;
            }

            @Override
            protected void done() {
                try {
                    if (isCancelled()) return;
                    List<Score> result = get();
                    loadedScores.clear();
                    loadedScores.addAll(result);
                    bindStudentRows(buildStudentRows(loadedScores), preferredStudentId);
                } catch (Exception exception) {
                    DialogUtil.showError(ScoreManagementPanel.this, "Lỗi khi tải dữ liệu: " + exception.getMessage());
                } finally {
                    setLoadingState(false);
                }
            }
        };
        activeWorker.execute();
    }

    /**
     * Xử lý apply hiện tại filters.
     * @deprecated Use applyCurrentFiltersAsync instead.
     */
    @Deprecated
    private void applyCurrentFilters(Long preferredStudentId) {
        applyCurrentFiltersAsync(preferredStudentId);
    }

    /**
     * Xử lý bind sinh viên rows.
     */
    private void bindStudentRows(List<StudentScoreSummary> summaries, Long preferredStudentId) {
        studentRows.clear();
        studentRows.addAll(summaries);
        studentTableModel.setRowCount(0);

        for (StudentScoreSummary summary : studentRows) {
            studentTableModel.addRow(toStudentRow(summary));
        }

        if (studentRows.isEmpty()) {
            showStudentTableState(false, filterReady
                    ? "Không tìm thấy sinh viên có điểm phù hợp với điều kiện lọc hiện tại."
                    : "Vui lòng chọn điều kiện lọc để hiển thị danh sách điểm.");
            tableCardLayout.show(tableCardPanel, EMPTY_CARD);
            studentTable.clearSelection();
            bindStudentDetail(null);
            hideDetailDialog();
            return;
        }

        showStudentTableState(true, null);
        tableCardLayout.show(tableCardPanel, DATA_CARD);
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

        suppressDetailDialogOpening = !isDetailDialogVisible();
        try {
            studentTable.setRowSelectionInterval(preferredIndex, preferredIndex);
            studentTable.scrollRectToVisible(studentTable.getCellRect(preferredIndex, 0, true));
        } finally {
            suppressDetailDialogOpening = false;
        }
    }

    /**
     * Xử lý bind sinh viên chi tiết.
     */
    private void bindStudentDetail(StudentScoreSummary summary) {
        selectedStudentScores.clear();
        detailTableModel.setRowCount(0);

        if (summary == null) {
            detailTable.clearSelection();
            updateDetailSummary(null, null);
            return;
        }

        selectedStudentScores.addAll(summary.scores());

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
            detailTable.scrollRectToVisible(detailTable.getCellRect(0, 0, true));
        } else {
            detailTable.clearSelection();
            updateDetailSummary(summary, null);
        }
    }

    /**
     * Xử lý sinh viên selection changed.
     */
    private void handleStudentSelectionChanged() {
        StudentScoreSummary summary = getSelectedStudentSummary();
        bindStudentDetail(summary);
        if (summary == null) {
            hideDetailDialog();
            return;
        }
        if (!suppressDetailDialogOpening || isDetailDialogVisible()) {
            showDetailDialog();
        }
    }

    /**
     * Xử lý add điểm.
     */
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

    /**
     * Xử lý edit điểm.
     */
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

    /**
     * Xử lý xóa điểm.
     */
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

    /**
     * Xử lý prompt for điểm.
     */
    private Score promptForScore(Score existingItem) {
        boolean isEditMode = existingItem != null;
        List<Enrollment> enrollments = screenController.loadEnrollments();
        Map<Long, Score> scoresByEnrollmentId = new java.util.HashMap<>();
        
        // Load tất cả điểm để kiểm tra enrollment nào đã có điểm
        List<Score> allScores = screenController.loadAllScores();
        for (Score score : allScores) {
            if (score.getEnrollment() != null && score.getEnrollment().getId() != null) {
                scoresByEnrollmentId.put(score.getEnrollment().getId(), score);
            }
        }
        
        ScoreFormDialog.ScoreFormResult formResult = ScoreFormDialog.showDialog(
                this,
                new ScoreFormDialog.ScoreFormModel(
                        isEditMode ? "Cập nhật điểm" : "Thêm điểm",
                        enrollments,
                        existingItem == null ? null : existingItem.getEnrollment(),
                        existingItem == null || existingItem.getProcessScore() == null ? "" : String.valueOf(existingItem.getProcessScore()),
                        existingItem == null || existingItem.getMidtermScore() == null ? "" : String.valueOf(existingItem.getMidtermScore()),
                        existingItem == null || existingItem.getFinalScore() == null ? "" : String.valueOf(existingItem.getFinalScore()),
                        scoresByEnrollmentId,
                        isEditMode
                )
        );
        if (formResult == null) {
            return null;
        }

        try {
            return screenController.applyFormData(
                    existingItem,
                    new ScoreManagementScreenController.ScoreFormData(
                            formResult.enrollment(),
                            formResult.processScore(),
                            formResult.midtermScore(),
                            formResult.finalScore()
                    )
            );
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
            return null;
        }
    }

    /**
     * Làm mới lọc values.
     */
    private void reloadFilterValues() {
        String filterType = (String) filterTypeComboBox.getSelectedItem();
        filterReady = FILTER_ALL.equals(filterType);
        filterValueComboBox.removeAllItems();

        if (!FILTER_SECTION_CODE.equals(filterType) && !FILTER_CLASS_ROOM.equals(filterType)) {
            filterValueComboBox.setEnabled(false);
            return;
        }

        if (activeWorker != null && !activeWorker.isDone()) {
            activeWorker.cancel(true);
        }

        setLoadingState(true);
        activeWorker = new SwingWorker<List<?>, Void>() {
            @Override
            protected List<?> doInBackground() {
                if (FILTER_SECTION_CODE.equals(filterType)) {
                    return screenController.loadCourseSections();
                } else {
                    return screenController.loadClassRooms();
                }
            }

            @Override
            protected void done() {
                try {
                    if (isCancelled()) return;
                    List<?> results = get();
                    filterValueComboBox.setEnabled(true);
                    if (FILTER_SECTION_CODE.equals(filterType)) {
                        filterValueComboBox.addItem(new FilterOption<>("Chọn học phần", null));
                        for (Object obj : results) {
                            CourseSection cs = (CourseSection) obj;
                            filterValueComboBox.addItem(new FilterOption<>(cs.getSectionCode(), cs));
                        }
                    } else {
                        filterValueComboBox.addItem(new FilterOption<>("Chọn lớp", null));
                        for (Object obj : results) {
                            ClassRoom cr = (ClassRoom) obj;
                            filterValueComboBox.addItem(new FilterOption<>(cr.getClassCode() + " - " + cr.getClassName(), cr));
                        }
                    }
                } catch (Exception exception) {
                    DialogUtil.showError(ScoreManagementPanel.this, "Lỗi khi tải danh mục: " + exception.getMessage());
                } finally {
                    setLoadingState(false);
                }
            }
        };
        activeWorker.execute();
    }

    /**
     * Xử lý reset filters.
     */
    private void resetFilters() {
        filterTypeComboBox.setSelectedItem(FILTER_ALL);
        keywordField.setText("");
        reloadFilterValues();
        reloadData();
    }

    /**
     * Kiểm tra valid lọc selection.
     */
    private boolean hasValidFilterSelection() {
        String filterType = (String) filterTypeComboBox.getSelectedItem();
        if (FILTER_ALL.equals(filterType)) {
            return true;
        }
        FilterOption<?> selectedOption = (FilterOption<?>) filterValueComboBox.getSelectedItem();
        return selectedOption != null && selectedOption.value() != null;
    }

    /**
     * Trả về lọc value đã chọn.
     */
    private <T> T getSelectedFilterValue(Class<T> type) {
        FilterOption<?> selectedOption = (FilterOption<?>) filterValueComboBox.getSelectedItem();
        if (selectedOption == null || selectedOption.value() == null || !type.isInstance(selectedOption.value())) {
            return null;
        }
        return type.cast(selectedOption.value());
    }

    /**
     * Trả về sinh viên tóm tắt đã chọn.
     */
    private StudentScoreSummary getSelectedStudentSummary() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= studentRows.size()) {
            return null;
        }
        return studentRows.get(selectedRow);
    }

    /**
     * Trả về sinh viên id đã chọn.
     */
    private Long getSelectedStudentId() {
        StudentScoreSummary summary = getSelectedStudentSummary();
        return summary == null ? null : summary.studentId();
    }

    /**
     * Trả về chi tiết điểm đã chọn.
     */
    private Score getSelectedDetailScore() {
        int selectedRow = detailTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= selectedStudentScores.size()) {
            return null;
        }
        return selectedStudentScores.get(selectedRow);
    }

    /**
     * Tạo sinh viên rows.
     */
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

    /**
     * Xử lý to sinh viên row.
     */
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

    /**
     * Xử lý to sinh viên row.
     */
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

    /**
     * Xử lý matches keyword.
     */
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

    /**
     * Cập nhật chi tiết tóm tắt.
     */
    private void updateDetailSummary(StudentScoreSummary summary, Score selectedScore) {
        if (summary == null) {
            detailSummaryPanel.showMessage("Vui lòng chọn sinh viên để xem chi tiết điểm.");
            return;
        }

        List<DisplayField> detailFields = buildDetailFields(summary, selectedScore);
        detailSummaryPanel.showFields(detailFields.stream()
                .map(field -> new String[]{field.label(), field.value()})
                .toArray(String[][]::new));
    }

    /**
     * Tạo chi tiết trường.
     */
    private List<DisplayField> buildDetailFields(StudentScoreSummary summary, Score selectedScore) {
        List<DisplayField> detailFields = new ArrayList<>();
        detailFields.add(new DisplayField("Mã sinh viên", defaultText(summary.studentCode())));
        detailFields.add(new DisplayField("Họ tên", defaultText(summary.studentName())));
        detailFields.add(new DisplayField("Lớp", defaultText(summary.classRoomName())));
        detailFields.add(new DisplayField("Khoa", defaultText(summary.facultyName())));
        detailFields.add(new DisplayField("Số môn có điểm", String.valueOf(summary.scoreCount())));
        detailFields.add(new DisplayField("Điểm trung bình", formatScore(summary.averageScore())));

        ScoreDisplayDto displayDto = selectedScore == null ? null : screenController.toDisplayDto(selectedScore);
        detailFields.add(new DisplayField(
                "Mã học phần",
                displayDto == null ? "Chọn một môn học trong bảng bên dưới." : defaultText(safeSectionCode(selectedScore))
        ));
        detailFields.add(new DisplayField(
                "Môn học",
                displayDto == null ? "Chọn một môn học trong bảng bên dưới." : defaultText(displayDto.subjectName())
        ));
        detailFields.add(new DisplayField(
                "Học kỳ",
                displayDto == null ? "Chọn một môn học trong bảng bên dưới." : defaultText(resolveSemesterText(selectedScore))
        ));
        detailFields.add(new DisplayField(
                "Phòng học",
                displayDto == null ? "Chọn một môn học trong bảng bên dưới." : defaultText(displayDto.roomName())
        ));
        detailFields.add(new DisplayField(
                "Điểm quá trình",
                displayDto == null ? "Chưa chọn" : defaultText(displayDto.processScore())
        ));
        detailFields.add(new DisplayField(
                "Điểm giữa kỳ",
                displayDto == null ? "Chưa chọn" : defaultText(displayDto.midtermScore())
        ));
        detailFields.add(new DisplayField(
                "Điểm cuối kỳ",
                displayDto == null ? "Chưa chọn" : defaultText(displayDto.finalScore())
        ));
        detailFields.add(new DisplayField(
                "Điểm tổng kết",
                displayDto == null ? "Chưa chọn" : defaultText(displayDto.totalScore())
        ));
        detailFields.add(new DisplayField(
                "Trạng thái",
                displayDto == null ? "Chưa chọn" : defaultText(displayDto.resultText())
        ));
        return detailFields;
    }

    /**
     * Cập nhật trạng thái loading.
     */
    private void setLoadingState(boolean loading) {
        isLoadingData = loading;
        filterTypeComboBox.setEnabled(!loading);
        filterValueComboBox.setEnabled(!loading && (FILTER_SECTION_CODE.equals(filterTypeComboBox.getSelectedItem()) || FILTER_CLASS_ROOM.equals(filterTypeComboBox.getSelectedItem())));
        keywordField.setEnabled(!loading);
        searchButton.setEnabled(!loading);
        applyButton.setEnabled(!loading);
        resetButton.setEnabled(!loading);
        reloadButton.setEnabled(!loading);
        addButton.setEnabled(!loading);
        editButton.setEnabled(!loading);
        deleteButton.setEnabled(!loading);
        studentTable.setEnabled(!loading);

        if (loading) {
            tableCardLayout.show(tableCardPanel, LOADING_CARD);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Tạo panel loading.
     */
    private JPanel createLoadingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel loadingLabel = new JLabel("Đang xử lý dữ liệu, vui lòng đợi...", SwingConstants.CENTER);
        loadingLabel.setForeground(AppColors.CARD_MUTED_TEXT);
        loadingLabel.setFont(loadingLabel.getFont().deriveFont(Font.ITALIC));
        panel.add(loadingLabel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Xử lý safe phần mã.
     */
    private String safeSectionCode(Score score) {
        if (score == null || score.getEnrollment() == null || score.getEnrollment().getCourseSection() == null) {
            return "";
        }
        return score.getEnrollment().getCourseSection().getSectionCode();
    }

    /**
     * Xác định học kỳ văn bản.
     */
    private String resolveSemesterText(Score score) {
        if (score == null || score.getEnrollment() == null || score.getEnrollment().getCourseSection() == null) {
            return "";
        }
        return AcademicFormatUtil.formatSemester(score.getEnrollment().getCourseSection().getSemester());
    }

    /**
     * Chuẩn hóa tìm kiếm văn bản.
     */
    private String normalizeSearchText(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }

    /**
     * Tạo panel chi tiết content.
     */
    private JPanel createDetailContentPanel(JScrollPane detailTableScrollPane) {
        JPanel container = new JPanel(new BorderLayout(0, 12));
        container.setOpaque(false);

        JPanel detailTablePanel = createSectionPanel(
                "Bảng điểm chi tiết",
                "Chọn một học phần trong bảng để xem thông tin điểm tương ứng.",
                detailTableScrollPane
        );
        detailTablePanel.setMinimumSize(new Dimension(0, DETAIL_SECTION_MIN_HEIGHT));

        container.add(detailSummaryPanel, BorderLayout.NORTH);
        container.add(detailTablePanel, BorderLayout.CENTER);
        return container;
    }

    /**
     * Kiểm tra hộp thoại chi tiết visible.
     */
    private boolean isDetailDialogVisible() {
        return detailDialog != null && detailDialog.isVisible();
    }

    /**
     * Hiển thị hộp thoại chi tiết.
     */
    private void showDetailDialog() {
        if (detailDialog == null) {
            detailDialog = new ScoreDetailDialog(detailContentPanel);
        }
        detailDialog.openDialog();
    }

    /**
     * Ẩn hộp thoại chi tiết.
     */
    private void hideDetailDialog() {
        if (detailDialog != null) {
            detailDialog.setVisible(false);
        }
    }

    /**
     * Giải phóng hộp thoại chi tiết.
     */
    private void disposeDetailDialog() {
        if (detailDialog != null) {
            detailDialog.dispose();
            detailDialog = null;
        }
    }

    /**
     * Tạo panel trạng thái trống.
     */
    private JPanel createEmptyStatePanel() {
        JPanel emptyStatePanel = new JPanel(new BorderLayout());
        emptyStatePanel.setOpaque(true);
        emptyStatePanel.setBackground(AppColors.CARD_BACKGROUND);
        emptyStatePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER, 2),
                BorderFactory.createEmptyBorder(48, 24, 48, 24)
        ));
        emptyStateLabel.setFont(emptyStateLabel.getFont().deriveFont(Font.ITALIC, 15f));
        emptyStateLabel.setForeground(AppColors.CARD_MUTED_TEXT);
        emptyStatePanel.add(emptyStateLabel, BorderLayout.CENTER);
        return emptyStatePanel;
    }

    /**
     * Tạo panel phần.
     */
    private JPanel createSectionPanel(String title, String description, JComponent content) {
        JPanel panel = createSurfaceCard(new BorderLayout(0, 12));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 17f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setForeground(AppColors.CARD_MUTED_TEXT);

        JPanel headerPanel = new JPanel(new BorderLayout(0, 4));
        headerPanel.setOpaque(false);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(descriptionLabel, BorderLayout.CENTER);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Tạo bảng scroll pane.
     */
    private JScrollPane createTableScrollPane(JTable table, boolean alwaysShowVerticalBar) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.CARD_BORDER, 2));
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

    /**
     * Tạo chi tiết bảng scroll pane.
     */
    private JScrollPane createDetailTableScrollPane() {
        JScrollPane scrollPane = createTableScrollPane(detailTable, true);
        Dimension preferredSize = scrollPane.getPreferredSize();
        scrollPane.setPreferredSize(new Dimension(preferredSize.width, DETAIL_TABLE_VIEWPORT_HEIGHT));
        scrollPane.setMinimumSize(new Dimension(0, DETAIL_TABLE_VIEWPORT_HEIGHT));
        return scrollPane;
    }

    /**
     * Hiển thị sinh viên bảng state.
     */
    private void showStudentTableState(boolean hasData, String message) {
        if (hasData) {
            tableCardLayout.show(tableCardPanel, "table");
            return;
        }

        emptyStateLabel.setText("<html><div style='text-align:center;'>" + defaultText(message) + "</div></html>");
        tableCardLayout.show(tableCardPanel, "empty");
    }

    /**
     * Tạo card surface.
     */
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

    /**
     * Thiết lập bảng.
     */
    private void configureTable(JTable table) {
        table.setRowHeight(34);
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

    /**
     * Thiết lập chi tiết bảng columns.
     */
    private void configureDetailTableColumns() {
        TableColumnModel columnModel = detailTable.getColumnModel();
        int[] preferredWidths = {150, 320, 130, 85, 85, 85, 110, 150};
        for (int index = 0; index < preferredWidths.length && index < columnModel.getColumnCount(); index++) {
            columnModel.getColumn(index).setPreferredWidth(preferredWidths[index]);
        }
    }

    /**
     * Thiết lập sinh viên bảng columns.
     */
    private void configureStudentTableColumns() {
        TableColumnModel columnModel = studentTable.getColumnModel();
        int[] preferredWidths = {120, 240, 170, 180, 95, 100};
        for (int index = 0; index < preferredWidths.length && index < columnModel.getColumnCount(); index++) {
            columnModel.getColumn(index).setPreferredWidth(preferredWidths[index]);
        }
    }

    /**
     * Áp dụng kiểu cho trường văn bản.
     */
    private void styleTextField(JTextField field) {
        field.setBorder(createInputBorder());
    }

    /**
     * Áp dụng kiểu cho nút thao tác.
     */
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

    /**
     * Định dạng điểm.
     */
    private String formatScore(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    /**
     * Xử lý văn bản mặc định.
     */
    private String defaultText(String value) {
        if (value == null || value.isBlank()) {
            return "Chưa cập nhật";
        }
        return value;
    }

    private static final class ResponsiveTable extends JTable {

        /**
         * Xử lý bảng responsive.
         */
        private ResponsiveTable(DefaultTableModel model) {
            super(model);
        }

        /**
         * Trả về scrollable tracks viewport width.
         */
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return getAutoResizeMode() != AUTO_RESIZE_OFF
                    || getParent() == null
                    || getPreferredSize().width < getParent().getWidth();
        }
    }

    /**
     * Xử lý điểm sinh viên tóm tắt.
     */
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
