/**
 * Màn hình quản trị cho quản lý báo cáo.
 */
package com.qlsv.view.admin;

import com.qlsv.controller.ClassRoomController;
import com.qlsv.controller.CourseSectionController;
import com.qlsv.controller.FacultyController;
import com.qlsv.controller.ReportController;
import com.qlsv.model.ClassRoom;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Faculty;
import com.qlsv.model.SystemStatistics;
import com.qlsv.utils.DialogUtil;
import com.qlsv.utils.PDFExportUtil;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BasePanel;
import com.qlsv.view.common.DashboardCard;
import com.qlsv.view.common.DetailSectionPanel;
import com.qlsv.view.dialog.ReportDetailDialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportManagementPanel extends BasePanel {

    private static final String REPORT_STUDENTS_BY_CLASS = "Danh sách sinh viên theo lớp";
    private static final String REPORT_LECTURERS_BY_FACULTY = "Danh sách giảng viên theo khoa";
    private static final String REPORT_STUDENTS_BY_SECTION = "Danh sách sinh viên trong học phần";
    private static final String REPORT_SCORES_BY_SECTION = "Bảng điểm theo học phần";

    private final ReportController reportController = new ReportController();
    private final ClassRoomController classRoomController = new ClassRoomController();
    private final FacultyController facultyController = new FacultyController();
    private final CourseSectionController courseSectionController = new CourseSectionController();

    private final JComboBox<String> reportTypeComboBox = new JComboBox<>(new String[]{
            REPORT_STUDENTS_BY_CLASS,
            REPORT_LECTURERS_BY_FACULTY,
            REPORT_STUDENTS_BY_SECTION,
            REPORT_SCORES_BY_SECTION
    });
    private final JComboBox<Object> filterComboBox = new JComboBox<>();
    private final DefaultTableModel tableModel = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JLabel tableSummaryLabel = new JLabel("Đang tải báo cáo...");
    private final DetailSectionPanel reportInfoPanel = new DetailSectionPanel(
            "Thông tin báo cáo",
            "Chọn loại báo cáo và bộ lọc để xem thông tin chi tiết."
    );

    private final DashboardCard studentsCard = new DashboardCard("Tổng sinh viên", AppColors.STAT_CARD_STUDENTS);
    private final DashboardCard lecturersCard = new DashboardCard("Tổng giảng viên", AppColors.STAT_CARD_LECTURERS);
    private final DashboardCard subjectsCard = new DashboardCard("Tổng môn học", AppColors.STAT_CARD_SUBJECTS);
    private final DashboardCard sectionsCard = new DashboardCard("Tổng học phần", AppColors.STAT_CARD_SECTIONS);

    private final JButton loadButton = new JButton("Tải báo cáo");
    private final JButton exportButton = new JButton("Xuất PDF");
    private ReportDetailDialog detailDialog;
    private SwingWorker<?, ?> activeWorker;

    /**
     * Khởi tạo quản lý báo cáo.
     */
    public ReportManagementPanel() {
        setOpaque(true);
        setBackground(AppColors.CONTENT_BACKGROUND);
        initComponents();
        refreshFilterOptions();
        loadStatistics();
    }

    @Override
    public void reloadData() {
        refreshFilterOptions();
        loadStatistics();
    }

    @Override
    public void removeNotify() {
        disposeDetailDialog();
        super.removeNotify();
    }

    private void initComponents() {
        JLabel titleLabel = new JLabel("Quản lý báo cáo & thống kê");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        JLabel subtitleLabel = new JLabel("Tổng hợp dữ liệu báo cáo theo lớp, khoa và học phần.");
        subtitleLabel.setForeground(AppColors.CARD_MUTED_TEXT);
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 13f));

        styleActionButton(loadButton, AppColors.BUTTON_PRIMARY);
        styleActionButton(exportButton, AppColors.BUTTON_SUCCESS);
        reportTypeComboBox.setPreferredSize(new Dimension(230, 38));
        filterComboBox.setPreferredSize(new Dimension(230, 38));

        loadButton.addActionListener(event -> loadReport());
        exportButton.addActionListener(event -> exportCurrentTable());
        reportTypeComboBox.addActionListener(event -> refreshFilterOptions());

        JPanel titlePanel = new JPanel(new BorderLayout(0, 6));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.CENTER);

        JPanel cardsPanel = new JPanel(new java.awt.GridLayout(1, 4, 12, 12));
        cardsPanel.setOpaque(false);
        cardsPanel.add(studentsCard);
        cardsPanel.add(lecturersCard);
        cardsPanel.add(subjectsCard);
        cardsPanel.add(sectionsCard);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);
        filterPanel.add(new JLabel("Loại báo cáo"));
        filterPanel.add(reportTypeComboBox);
        filterPanel.add(new JLabel("Bộ lọc"));
        filterPanel.add(filterComboBox);
        filterPanel.add(loadButton);
        filterPanel.add(exportButton);

        JPanel controlCard = createSurfaceCard(new BorderLayout(0, 0));
        controlCard.add(filterPanel, BorderLayout.CENTER);

        configureTable(table);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) handleTableSelectionChanged();
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && table.getSelectedRow() >= 0) showDetailDialog();
            }
        });
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(AppColors.CARD_BORDER));
        tableScrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);

        JPanel tablePanel = createSurfaceCard(new BorderLayout(0, 12));
        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel tableTitle = new JLabel("Chi tiết dữ liệu báo cáo");
        tableTitle.setForeground(AppColors.CARD_TITLE_TEXT);
        tableTitle.setFont(tableTitle.getFont().deriveFont(Font.BOLD, 16f));
        tableHeader.add(tableTitle, BorderLayout.WEST);
        tableHeader.add(tableSummaryLabel, BorderLayout.EAST);

        tablePanel.add(tableHeader, BorderLayout.NORTH);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        JPanel northPanel = new JPanel(new BorderLayout(0, 12));
        northPanel.setOpaque(false);
        northPanel.add(titlePanel, BorderLayout.NORTH);
        northPanel.add(cardsPanel, BorderLayout.CENTER);
        northPanel.add(controlCard, BorderLayout.SOUTH);

        add(northPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    private void refreshFilterOptions() {
        try {
            filterComboBox.removeAllItems();
            String reportType = (String) reportTypeComboBox.getSelectedItem();
            if (REPORT_STUDENTS_BY_CLASS.equals(reportType)) {
                for (ClassRoom classRoom : classRoomController.getClassRoomsForSelection()) filterComboBox.addItem(classRoom);
            } else if (REPORT_LECTURERS_BY_FACULTY.equals(reportType)) {
                for (Faculty faculty : facultyController.getFacultiesForSelection()) filterComboBox.addItem(faculty);
            } else {
                for (CourseSection cs : courseSectionController.getAllCourseSectionsForSelection()) filterComboBox.addItem(cs);
            }
            loadReport();
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void loadReport() {
        if (activeWorker != null && !activeWorker.isDone()) activeWorker.cancel(true);
        final String reportType = (String) reportTypeComboBox.getSelectedItem();
        final Object selectedFilter = filterComboBox.getSelectedItem();
        if (selectedFilter == null) return;

        setLoadingState(true);
        activeWorker = new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() {
                if (REPORT_STUDENTS_BY_CLASS.equals(reportType)) return reportController.getStudentsByClassRoom(((ClassRoom) selectedFilter).getId());
                if (REPORT_LECTURERS_BY_FACULTY.equals(reportType)) return reportController.getLecturersByFaculty(((Faculty) selectedFilter).getId());
                if (REPORT_STUDENTS_BY_SECTION.equals(reportType)) return reportController.getStudentsByCourseSection(((CourseSection) selectedFilter).getId());
                return reportController.getScoresByCourseSection(((CourseSection) selectedFilter).getId());
            }

            @Override
            protected void done() {
                try {
                    if (isCancelled()) return;
                    List<Object[]> rows = get();
                    if (REPORT_STUDENTS_BY_CLASS.equals(reportType)) {
                        tableModel.setColumnIdentifiers(new String[]{"Mã sinh viên", "Họ và tên", "Email", "Số điện thoại", "Trạng thái"});
                    } else if (REPORT_LECTURERS_BY_FACULTY.equals(reportType)) {
                        tableModel.setColumnIdentifiers(new String[]{"Mã giảng viên", "Họ và tên", "Email", "Số điện thoại", "Trạng thái"});
                    } else if (REPORT_STUDENTS_BY_SECTION.equals(reportType)) {
                        tableModel.setColumnIdentifiers(new String[]{"Mã sinh viên", "Họ và tên", "Email", "Trạng thái", "Đăng ký lúc"});
                    } else {
                        tableModel.setColumnIdentifiers(new String[]{"Mã sinh viên", "Họ và tên", "QT", "GK", "CK", "Tổng kết", "Kết quả"});
                    }
                    tableModel.setRowCount(0);
                    for (Object[] row : rows) tableModel.addRow(row);
                    tableSummaryLabel.setText(rows.size() + " dòng dữ liệu");
                    updateReportInfo(rows.size());
                } catch (Exception exception) {
                    DialogUtil.showError(ReportManagementPanel.this, "Lỗi tải báo cáo: " + exception.getMessage());
                } finally {
                    setLoadingState(false);
                }
            }
        };
        activeWorker.execute();
    }

    private void loadStatistics() {
        new SwingWorker<SystemStatistics, Void>() {
            @Override
            protected SystemStatistics doInBackground() {
                return reportController.getSystemStatistics();
            }

            @Override
            protected void done() {
                try {
                    SystemStatistics stats = get();
                    studentsCard.setValue(String.valueOf(stats.getTotalStudents()));
                    lecturersCard.setValue(String.valueOf(stats.getTotalLecturers()));
                    subjectsCard.setValue(String.valueOf(stats.getTotalSubjects()));
                    sectionsCard.setValue(String.valueOf(stats.getTotalCourseSections()));
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void exportCurrentTable() {
        if (table.getRowCount() == 0) {
            DialogUtil.showError(this, "Không có dữ liệu để xuất PDF.");
            return;
        }
        JFileChooser fc = new JFileChooser();
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fc.setSelectedFile(new File("report_" + ts + ".pdf"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                PDFExportUtil.exportTable((String) reportTypeComboBox.getSelectedItem(), table, fc.getSelectedFile());
                DialogUtil.showInfo(this, "Xuất PDF thành công.");
            } catch (Exception ex) {
                DialogUtil.showError(this, ex.getMessage());
            }
        }
    }

    private void updateReportInfo(int rowCount) {
        Object selected = filterComboBox.getSelectedItem();
        String filterText = selected == null ? "Chưa chọn" : selected.toString();
        String updatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        reportInfoPanel.showFields(new String[][]{
                {"Loại báo cáo", reportTypeComboBox.getSelectedItem().toString()},
                {"Bộ lọc", filterText},
                {"Số dòng", String.valueOf(rowCount)},
                {"Cập nhật lúc", updatedAt}
        });
    }

    private void handleTableSelectionChanged() {
        if (table.getSelectedRow() < 0) hideDetailDialog();
        else showDetailDialog();
    }

    private void showDetailDialog() {
        if (detailDialog == null) detailDialog = new ReportDetailDialog(reportInfoPanel);
        detailDialog.openDialog();
    }

    private void hideDetailDialog() {
        if (detailDialog != null) detailDialog.setVisible(false);
    }

    private void disposeDetailDialog() {
        if (detailDialog != null) {
            detailDialog.dispose();
            detailDialog = null;
        }
    }

    private JPanel createSurfaceCard(BorderLayout layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(true);
        panel.setBackground(AppColors.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));
        return panel;
    }

    private void configureTable(JTable table) {
        table.setRowHeight(34);
        table.setFillsViewportHeight(true);
        table.setGridColor(AppColors.CARD_BORDER);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(AppColors.TABLE_SELECTION_BACKGROUND);
        table.setSelectionForeground(AppColors.CARD_VALUE_TEXT);
        table.getTableHeader().setBackground(AppColors.TABLE_HEADER_BACKGROUND);
        table.getTableHeader().setForeground(AppColors.CARD_VALUE_TEXT);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 13f));
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));
    }

    private void styleActionButton(JButton button, Color bg) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(bg);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 13f));
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
    }

    private void setLoadingState(boolean loading) {
        reportTypeComboBox.setEnabled(!loading);
        filterComboBox.setEnabled(!loading);
        loadButton.setEnabled(!loading);
        exportButton.setEnabled(!loading);
        table.setEnabled(!loading);
        if (loading) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            tableSummaryLabel.setText("Đang tải dữ liệu...");
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }
}
