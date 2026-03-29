/**
 * Màn hình giảng viên cho điểm.
 */
package com.qlsv.view.lecturer;

import com.qlsv.controller.CourseSectionController;
import com.qlsv.controller.LecturerController;
import com.qlsv.controller.ScoreController;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Lecturer;
import com.qlsv.model.Score;
import com.qlsv.service.LecturerScoreAnalysisService;
import com.qlsv.utils.DialogUtil;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BasePanel;
import com.qlsv.view.dialog.AnalysisResultDialog;
import com.qlsv.view.dialog.LecturerScoreDetailDialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class LecturerScorePanel extends BasePanel {

    private static final String[] SCORE_TABLE_COLUMNS = {
            "ID", "Mã SV", "Họ và tên", "Học phần", "QT", "GK", "CK", "Tổng kết", "Kết quả"
    };
    private static final String[] EDIT_TABLE_COLUMNS = {
            "Mã SV", "Họ và tên", "Học phần", "QT", "GK", "CK", "Tổng kết", "Kết quả"
    };
    private static final int CONTROL_HEIGHT = 36;
    private static final int TABLE_ROW_HEIGHT = 30;
    private static final int SCORE_TABLE_VIEWPORT_HEIGHT = 260;
    private static final int EDIT_TABLE_VIEWPORT_HEIGHT = 104;

    private final ScoreController scoreController = new ScoreController();
    private final LecturerController lecturerController = new LecturerController();
    private final CourseSectionController courseSectionController = new CourseSectionController();
    private final LecturerScoreAnalysisService lecturerScoreAnalysisService = new LecturerScoreAnalysisService();

    private final DefaultTableModel scoreTableModel = new DefaultTableModel(SCORE_TABLE_COLUMNS, 0) {
        /**
         * Xác định ô có cho phép chỉnh sửa hay không.
         */
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel editTableModel = new DefaultTableModel(EDIT_TABLE_COLUMNS, 0) {
        /**
         * Xác định ô có cho phép chỉnh sửa hay không.
         */
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
    private JPanel editPanel;
    private LecturerScoreDetailDialog detailDialog;
    private AnalysisResultDialog analysisDialog;

    private Score selectedScore;
    private Long currentCourseSectionId;
    private String currentKeyword = "";
    private boolean suppressDetailDialogOpening;

    /**
     * Khởi tạo điểm giảng viên.
     */
    public LecturerScorePanel() {
        configureScoreTable();
        configureEditTable();
        initializeEditRow();

        courseComboBox.addItem("Tất cả học phần");

        JButton filterButton = new JButton("Lọc");
        JButton reloadButton = new JButton("Tải lại");
        JButton analyzeButton = new JButton("Phân tích điểm");
        styleSecondaryButton(filterButton);
        styleSecondaryButton(reloadButton);
        stylePrimaryButton(analyzeButton);
        styleSaveButton(saveButton);
        saveButton.setEnabled(false);

        filterButton.addActionListener(event -> {
            currentCourseSectionId = resolveCourseSectionIdFromComboSelection();
            applyFilters(getSelectedEnrollmentId());
        });
        reloadButton.addActionListener(event -> reloadData());
        analyzeButton.addActionListener(event -> handleAnalyzeScores(analyzeButton));
        saveButton.addActionListener(event -> handleSaveScore());

        searchField.setToolTipText("Tìm theo mã sinh viên hoặc họ tên trong danh sách đang lọc.");
        searchField.setPreferredSize(new Dimension(220, CONTROL_HEIGHT));
        searchField.setMinimumSize(new Dimension(180, CONTROL_HEIGHT));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.INPUT_BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        courseComboBox.setPreferredSize(new Dimension(220, CONTROL_HEIGHT));
        courseComboBox.setMinimumSize(new Dimension(180, CONTROL_HEIGHT));
        courseComboBox.setFont(courseComboBox.getFont().deriveFont(Font.PLAIN, 13.5f));

        searchDebounceTimer = new Timer(250, event -> {
            currentKeyword = normalize(searchField.getText());
            applyFilters(getSelectedEnrollmentId());
        });
        searchDebounceTimer.setRepeats(false);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            /**
             * Phản ứng khi nội dung vừa được chèn.
             */
            @Override
            public void insertUpdate(DocumentEvent event) {
                restartSearchDebounce();
            }

            /**
             * Phản ứng khi nội dung vừa bị xóa.
             */
            @Override
            public void removeUpdate(DocumentEvent event) {
                restartSearchDebounce();
            }

            /**
             * Phản ứng khi thuộc tính tài liệu thay đổi.
             */
            @Override
            public void changedUpdate(DocumentEvent event) {
                restartSearchDebounce();
            }
        });

        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 8);

        gbc.gridx = 0;
        filterPanel.add(new JLabel("Học phần:"), gbc);

        gbc.gridx = 1;
        filterPanel.add(courseComboBox, gbc);

        gbc.gridx = 2;
        filterPanel.add(filterButton, gbc);

        gbc.gridx = 3;
        filterPanel.add(new JLabel("Từ khóa:"), gbc);

        gbc.gridx = 4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        filterPanel.add(searchField, gbc);

        JPanel actionPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 4));
        actionPanel.setOpaque(false);
        actionPanel.add(reloadButton);
        actionPanel.add(analyzeButton);

        JPanel headerPanel = createSectionCard(new BorderLayout(12, 8));
        JLabel titleLabel = new JLabel("Nhập điểm sinh viên");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        JLabel subtitleLabel = new JLabel("Lọc theo học phần và cập nhật nhanh điểm thành phần của sinh viên đang chọn.");
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 12.5f));
        subtitleLabel.setForeground(AppColors.CARD_MUTED_TEXT);

        JPanel controlPanel = new JPanel(new BorderLayout(10, 8));
        controlPanel.setOpaque(false);
        controlPanel.add(filterPanel, BorderLayout.CENTER);
        controlPanel.add(actionPanel, BorderLayout.EAST);

        JPanel headerTextPanel = new JPanel(new BorderLayout(0, 4));
        headerTextPanel.setOpaque(false);
        headerTextPanel.add(titleLabel, BorderLayout.NORTH);
        headerTextPanel.add(subtitleLabel, BorderLayout.CENTER);

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

        editPanel = createSectionCard(new BorderLayout(0, 12));
        JLabel editTitle = createSectionTitle("Bảng nhập/sửa điểm theo sinh viên đang chọn");
        JLabel editSubtitle = createMutedDescription("Nhập lại các cột QT, GK, CK rồi lưu để cập nhật đúng bản ghi hiện tại.");

        JPanel editTopPanel = new JPanel(new BorderLayout(0, 8));
        editTopPanel.setOpaque(false);
        editTopPanel.add(editTitle, BorderLayout.NORTH);
        editTopPanel.add(editSubtitle, BorderLayout.CENTER);

        JScrollPane editTableScrollPane = createTableScrollPane(editTable);
        JPanel editActionPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        editActionPanel.setOpaque(false);
        editActionPanel.add(saveButton);

        editPanel.add(editTopPanel, BorderLayout.NORTH);
        editPanel.add(editTableScrollPane, BorderLayout.CENTER);
        editPanel.add(editActionPanel, BorderLayout.SOUTH);

        scoreListPanel.setMinimumSize(new Dimension(0, 200));
        editPanel.setMinimumSize(new Dimension(0, 180));

        add(headerPanel, BorderLayout.NORTH);
        add(scoreListPanel, BorderLayout.CENTER);

        scoreTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                onScoreSelected();
            }
        });
        scoreTable.addMouseListener(new MouseAdapter() {
            /**
             * Xử lý thao tác nhấp chuột trên giao diện.
             */
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && scoreTable.getSelectedRow() >= 0) {
                    showDetailDialog();
                }
            }
        });

        reloadData();
    }

    /**
     * Giải phóng tài nguyên khi thành phần bị gỡ.
     */
    @Override
    public void removeNotify() {
        disposeDetailDialog();
        disposeAnalysisDialog();
        super.removeNotify();
    }

    /**
     * Thiết lập bảng điểm.
     */
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

    /**
     * Thiết lập bảng edit.
     */
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

    /**
     * Thiết lập bảng cơ sở.
     */
    private void configureBaseTable(JTable table) {
        table.setRowHeight(TABLE_ROW_HEIGHT);
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

    /**
     * Khởi tạo ialize edit row.
     */
    private void initializeEditRow() {
        editTableModel.setRowCount(0);
        editTableModel.addRow(new Object[]{"", "", "", "", "", "", "", ""});
    }

    /**
     * Tạo card phần.
     */
    private JPanel createSectionCard(BorderLayout layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(true);
        panel.setBackground(AppColors.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        return panel;
    }

    /**
     * Tạo phần title.
     */
    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 16f));
        label.setForeground(AppColors.CARD_TITLE_TEXT);
        return label;
    }

    /**
     * Tạo muted description.
     */
    private JLabel createMutedDescription(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 12.5f));
        label.setForeground(AppColors.CARD_MUTED_TEXT);
        return label;
    }

    /**
     * Tạo bảng scroll pane.
     */
    private JScrollPane createTableScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.CARD_BORDER));
        scrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        Dimension preferredSize = scrollPane.getPreferredSize();
        if (table == editTable) {
            scrollPane.setPreferredSize(new Dimension(preferredSize.width, EDIT_TABLE_VIEWPORT_HEIGHT));
            scrollPane.setMinimumSize(new Dimension(0, EDIT_TABLE_VIEWPORT_HEIGHT));
        } else if (table == scoreTable) {
            scrollPane.setPreferredSize(new Dimension(preferredSize.width, SCORE_TABLE_VIEWPORT_HEIGHT));
            scrollPane.setMinimumSize(new Dimension(0, 190));
        }
        return scrollPane;
    }

    /**
     * Áp dụng kiểu cho nút secondary.
     */
    private void styleSecondaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(AppColors.BUTTON_NEUTRAL);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
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
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        button.setFont(button.getFont().deriveFont(Font.BOLD));
    }

    /**
     * Áp dụng kiểu cho nút lưu.
     */
    private void styleSaveButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(AppColors.BUTTON_SUCCESS);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setFont(button.getFont().deriveFont(Font.BOLD));
    }

    /**
     * Xử lý restart tìm kiếm debounce.
     */
    private void restartSearchDebounce() {
        searchDebounceTimer.restart();
    }

    /**
     * Xử lý on điểm đã chọn.
     */
    private void onScoreSelected() {
        int selectedRow = scoreTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= filteredScores.size()) {
            selectedScore = null;
            fillEditRow(null);
            saveButton.setEnabled(false);
            hideDetailDialog();
            return;
        }

        selectedScore = filteredScores.get(selectedRow);
        fillEditRow(selectedScore);
        saveButton.setEnabled(true);
        if (!suppressDetailDialogOpening || isDetailDialogVisible()) {
            showDetailDialog();
        }
    }

    /**
     * Xử lý fill edit row.
     */
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

    /**
     * Xử lý lưu điểm.
     */
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

    /**
     * Phân tích điểm value.
     */
    private double parseScoreValue(Object value) {
        String normalizedValue = value == null ? "" : String.valueOf(value).trim();
        return Double.parseDouble(normalizedValue);
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
            detailDialog = new LecturerScoreDetailDialog(editPanel);
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
     * Giải phóng hộp thoại phân tích.
     */
    private void disposeAnalysisDialog() {
        if (analysisDialog != null) {
            analysisDialog.dispose();
            analysisDialog = null;
        }
    }

    /**
     * Xử lý phân tích điểm.
     */
    private void handleAnalyzeScores(JButton analyzeButton) {
        LecturerScoreAnalysisService.LecturerScoreAnalysisSnapshot snapshot;
        try {
            snapshot = lecturerScoreAnalysisService.prepareSnapshot(filteredScores, buildAnalysisFilterLabel());
        } catch (ValidationException exception) {
            DialogUtil.showInfo(this, exception.getMessage());
            return;
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
            return;
        }

        analyzeButton.setEnabled(false);
        String originalText = analyzeButton.getText();
        analyzeButton.setText("Đang phân tích...");

        LecturerScoreAnalysisService.LecturerScoreAnalysisSnapshot immutableSnapshot = snapshot;
        new SwingWorker<String, Void>() {
            /**
             * Xử lý do in background.
             */
            @Override
            protected String doInBackground() {
                return lecturerScoreAnalysisService.analyzeSnapshot(immutableSnapshot);
            }

            /**
             * Xử lý done.
             */
            @Override
            protected void done() {
                try {
                    showAnalysisDialog(immutableSnapshot, get());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    DialogUtil.showError(LecturerScorePanel.this, "Đã gián đoạn quá trình phân tích điểm.");
                } catch (ExecutionException exception) {
                    DialogUtil.showError(LecturerScorePanel.this, resolveAsyncErrorMessage(exception));
                } finally {
                    analyzeButton.setEnabled(true);
                    analyzeButton.setText(originalText);
                }
            }
        }.execute();
    }

    /**
     * Hiển thị hộp thoại phân tích.
     */
    private void showAnalysisDialog(
            LecturerScoreAnalysisService.LecturerScoreAnalysisSnapshot snapshot,
            String analysisText
    ) {
        if (analysisDialog == null) {
            analysisDialog = new AnalysisResultDialog(snapshot.dialogTitle());
        }
        analysisDialog.setDialogTitle(snapshot.dialogTitle());
        analysisDialog.setAnalysisText(buildAnalysisDialogContent(snapshot, analysisText));
        analysisDialog.openDialog();
    }

    /**
     * Tạo phân tích hộp thoại content.
     */
    private String buildAnalysisDialogContent(
            LecturerScoreAnalysisService.LecturerScoreAnalysisSnapshot snapshot,
            String analysisText
    ) {
        StringBuilder builder = new StringBuilder();
        builder.append("Phạm vi bộ lọc: ")
                .append(snapshot.filterLabel().isBlank() ? "Danh sách đang hiển thị" : snapshot.filterLabel())
                .append('\n');
        builder.append("Số lượng sinh viên: ").append(snapshot.studentCount()).append('\n');
        builder.append(String.format(
                Locale.US,
                "Điểm trung bình: %.2f | Đạt: %d | Chưa đạt: %d | Thấp nhất: %.2f | Cao nhất: %.2f%n%n",
                snapshot.averageScore(),
                snapshot.passCount(),
                snapshot.failCount(),
                snapshot.minScore(),
                snapshot.maxScore()
        ));
        builder.append(analysisText == null ? "" : analysisText.trim());
        return builder.toString();
    }

    /**
     * Tạo label phân tích lọc.
     */
    private String buildAnalysisFilterLabel() {
        String courseLabel = resolveCurrentCourseLabel();
        if (currentKeyword.isBlank()) {
            return courseLabel;
        }
        return courseLabel + " | Từ khóa: " + currentKeyword;
    }

    /**
     * Xác định label khóa học hiện tại.
     */
    private String resolveCurrentCourseLabel() {
        if (currentCourseSectionId == null) {
            return "Tất cả học phần đang hiển thị";
        }

        for (int index = 0; index < courseComboBox.getItemCount(); index++) {
            Object item = courseComboBox.getItemAt(index);
            if (item instanceof CourseSection section && currentCourseSectionId.equals(section.getId())) {
                return "Học phần " + DisplayTextUtil.defaultText(section.getSectionCode());
            }
        }
        return "Học phần đang hiển thị";
    }

    /**
     * Xác định async error thông báo.
     */
    private String resolveAsyncErrorMessage(ExecutionException exception) {
        Throwable cause = exception.getCause();
        if (cause != null && cause.getMessage() != null && !cause.getMessage().isBlank()) {
            return cause.getMessage();
        }
        return "Không thể phân tích dữ liệu điểm ở thời điểm hiện tại.";
    }

    /**
     * Nạp học phần.
     */
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

    /**
     * Làm mới dữ liệu đang hiển thị.
     */
    @Override
    public void reloadData() {
        refreshScoreData(true, getSelectedEnrollmentId());
    }

    /**
     * Xử lý refresh điểm dữ liệu.
     */
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

    /**
     * Xử lý apply filters.
     */
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

        suppressDetailDialogOpening = !isDetailDialogVisible();
        try {
            restoreSelectedRow(preferredEnrollmentId);
        } finally {
            suppressDetailDialogOpening = false;
        }
        if (scoreTable.getSelectedRow() < 0) {
            onScoreSelected();
        }
    }

    /**
     * Xử lý matches hiện tại khóa học lọc.
     */
    private boolean matchesCurrentCourseFilter(Score score) {
        if (currentCourseSectionId == null) {
            return true;
        }
        return score.getEnrollment() != null
                && score.getEnrollment().getCourseSection() != null
                && currentCourseSectionId.equals(score.getEnrollment().getCourseSection().getId());
    }

    /**
     * Xử lý matches hiện tại keyword.
     */
    private boolean matchesCurrentKeyword(Score score) {
        if (currentKeyword.isBlank()) {
            return true;
        }
        return containsIgnoreCase(getStudentCode(score), currentKeyword)
                || containsIgnoreCase(getStudentName(score), currentKeyword);
    }

    /**
     * Xử lý restore đã chọn row.
     */
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

    /**
     * Xử lý restore khóa học selection.
     */
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

    /**
     * Xác định học phần id from chọn selection.
     */
    private Long resolveCourseSectionIdFromComboSelection() {
        Object selectedItem = courseComboBox.getSelectedItem();
        if (selectedItem instanceof CourseSection section) {
            return section.getId();
        }
        return null;
    }

    /**
     * Trả về đăng ký id đã chọn.
     */
    private Long getSelectedEnrollmentId() {
        return selectedScore == null ? null : getEnrollmentId(selectedScore);
    }

    /**
     * Trả về đăng ký id.
     */
    private Long getEnrollmentId(Score score) {
        return score == null || score.getEnrollment() == null ? null : score.getEnrollment().getId();
    }

    /**
     * Trả về sinh viên mã.
     */
    private String getStudentCode(Score score) {
        if (score == null || score.getEnrollment() == null || score.getEnrollment().getStudent() == null) {
            return "";
        }
        return DisplayTextUtil.defaultText(score.getEnrollment().getStudent().getStudentCode());
    }

    /**
     * Trả về sinh viên tên.
     */
    private String getStudentName(Score score) {
        if (score == null || score.getEnrollment() == null || score.getEnrollment().getStudent() == null) {
            return "";
        }
        return DisplayTextUtil.defaultText(score.getEnrollment().getStudent().getFullName());
    }

    /**
     * Trả về học phần mã.
     */
    private String getCourseSectionCode(Score score) {
        if (score == null || score.getEnrollment() == null || score.getEnrollment().getCourseSection() == null) {
            return "";
        }
        return DisplayTextUtil.defaultText(score.getEnrollment().getCourseSection().getSectionCode());
    }

    /**
     * Xử lý contains ignore case.
     */
    private boolean containsIgnoreCase(String source, String keyword) {
        return normalize(source).contains(keyword);
    }

    /**
     * Chuẩn hóa dữ liệu hiện tại.
     */
    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
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
            return getPreferredSize().width < getParent().getWidth();
        }
    }
}
