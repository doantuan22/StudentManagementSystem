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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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

    private void initComponents() {
        JLabel titleLabel = new JLabel("Báo cáo");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        JLabel subtitleLabel = new JLabel("Tổng hợp dữ liệu báo cáo theo lớp, khoa và học phần với giao diện đồng bộ cùng các màn hình quản lý.");
        subtitleLabel.setForeground(AppColors.CARD_MUTED_TEXT);
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 13f));

        JButton loadButton = new JButton("Tải báo cáo");
        JButton exportButton = new JButton("Xuất PDF");
        styleActionButton(loadButton, AppColors.BUTTON_PRIMARY);
        styleActionButton(exportButton, AppColors.BUTTON_SUCCESS);
        reportTypeComboBox.setPreferredSize(new Dimension(230, 38));
        filterComboBox.setPreferredSize(new Dimension(230, 38));
        reportTypeComboBox.setFont(reportTypeComboBox.getFont().deriveFont(Font.PLAIN, 13.5f));
        filterComboBox.setFont(filterComboBox.getFont().deriveFont(Font.PLAIN, 13.5f));

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
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(AppColors.CARD_BORDER));
        tableScrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);
        tableScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        tableScrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        JPanel tablePanel = createSurfaceCard(new BorderLayout(0, 12));
        JPanel tableHeadingPanel = new JPanel(new BorderLayout(12, 0));
        tableHeadingPanel.setOpaque(false);

        JPanel tableTitlePanel = new JPanel(new BorderLayout(0, 4));
        tableTitlePanel.setOpaque(false);

        JLabel tableTitleLabel = new JLabel("Dữ liệu báo cáo");
        tableTitleLabel.setForeground(AppColors.CARD_TITLE_TEXT);
        tableTitleLabel.setFont(tableTitleLabel.getFont().deriveFont(Font.BOLD, 16f));

        JLabel tableDescriptionLabel = new JLabel("Xem trước dữ liệu và xuất nhanh PDF từ đúng bộ lọc đang áp dụng.");
        tableDescriptionLabel.setForeground(AppColors.CARD_MUTED_TEXT);
        tableDescriptionLabel.setFont(tableDescriptionLabel.getFont().deriveFont(Font.PLAIN, 12.5f));

        tableSummaryLabel.setForeground(AppColors.CARD_MUTED_TEXT);
        tableSummaryLabel.setFont(tableSummaryLabel.getFont().deriveFont(Font.PLAIN, 12.5f));

        tableTitlePanel.add(tableTitleLabel, BorderLayout.NORTH);
        tableTitlePanel.add(tableDescriptionLabel, BorderLayout.CENTER);

        tableHeadingPanel.add(tableTitlePanel, BorderLayout.WEST);
        tableHeadingPanel.add(tableSummaryLabel, BorderLayout.EAST);

        tablePanel.add(tableHeadingPanel, BorderLayout.NORTH);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        JScrollPane detailScrollPane = new JScrollPane(reportInfoPanel);
        detailScrollPane.setBorder(BorderFactory.createLineBorder(AppColors.CARD_BORDER));
        detailScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        detailScrollPane.getViewport().setBackground(reportInfoPanel.getBackground());

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tablePanel, detailScrollPane);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        splitPane.setResizeWeight(0.72);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerSize(10);
        tablePanel.setMinimumSize(new Dimension(0, 220));
        detailScrollPane.setMinimumSize(new Dimension(0, 180));

        JPanel northPanel = new JPanel(new BorderLayout(0, 12));
        northPanel.setOpaque(false);
        northPanel.add(titlePanel, BorderLayout.NORTH);
        northPanel.add(cardsPanel, BorderLayout.CENTER);
        northPanel.add(controlCard, BorderLayout.SOUTH);

        add(northPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    private void refreshFilterOptions() {
        try {
            filterComboBox.removeAllItems();
            String reportType = (String) reportTypeComboBox.getSelectedItem();
            if (REPORT_STUDENTS_BY_CLASS.equals(reportType)) {
                for (ClassRoom classRoom : classRoomController.getClassRoomsForSelection()) {
                    filterComboBox.addItem(classRoom);
                }
            } else if (REPORT_LECTURERS_BY_FACULTY.equals(reportType)) {
                for (Faculty faculty : facultyController.getFacultiesForSelection()) {
                    filterComboBox.addItem(faculty);
                }
            } else {
                for (CourseSection courseSection : courseSectionController.getAllCourseSectionsForSelection()) {
                    filterComboBox.addItem(courseSection);
                }
            }
            loadReport();
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void loadReport() {
        try {
            String reportType = (String) reportTypeComboBox.getSelectedItem();
            List<Object[]> rows;

            if (REPORT_STUDENTS_BY_CLASS.equals(reportType)) {
                ClassRoom classRoom = (ClassRoom) filterComboBox.getSelectedItem();
                if (classRoom == null) {
                    return;
                }
                tableModel.setColumnIdentifiers(new String[]{"Mã sinh viên", "Họ và tên", "Email", "Số điện thoại", "Trạng thái"});
                rows = reportController.getStudentsByClassRoom(classRoom.getId());
            } else if (REPORT_LECTURERS_BY_FACULTY.equals(reportType)) {
                Faculty faculty = (Faculty) filterComboBox.getSelectedItem();
                if (faculty == null) {
                    return;
                }
                tableModel.setColumnIdentifiers(new String[]{"Mã giảng viên", "Họ và tên", "Email", "Số điện thoại", "Trạng thái"});
                rows = reportController.getLecturersByFaculty(faculty.getId());
            } else if (REPORT_STUDENTS_BY_SECTION.equals(reportType)) {
                CourseSection courseSection = (CourseSection) filterComboBox.getSelectedItem();
                if (courseSection == null) {
                    return;
                }
                tableModel.setColumnIdentifiers(new String[]{"Mã sinh viên", "Họ và tên", "Email", "Trạng thái", "Đăng ký lúc"});
                rows = reportController.getStudentsByCourseSection(courseSection.getId());
            } else {
                CourseSection courseSection = (CourseSection) filterComboBox.getSelectedItem();
                if (courseSection == null) {
                    return;
                }
                tableModel.setColumnIdentifiers(new String[]{"Mã sinh viên", "Họ và tên", "QT", "GK", "CK", "Tổng kết", "Kết quả"});
                rows = reportController.getScoresByCourseSection(courseSection.getId());
            }

            tableModel.setRowCount(0);
            for (Object[] row : rows) {
                tableModel.addRow(row);
            }

            tableSummaryLabel.setText(rows.size() + " dòng dữ liệu");
            updateReportInfo(rows.size());
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void loadStatistics() {
        try {
            SystemStatistics statistics = reportController.getSystemStatistics();
            studentsCard.setValue(String.valueOf(statistics.getTotalStudents()));
            lecturersCard.setValue(String.valueOf(statistics.getTotalLecturers()));
            subjectsCard.setValue(String.valueOf(statistics.getTotalSubjects()));
            sectionsCard.setValue(String.valueOf(statistics.getTotalCourseSections()));
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void exportCurrentTable() {
        if (table.getRowCount() == 0) {
            DialogUtil.showError(this, "Không có dữ liệu để xuất PDF.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setSelectedFile(new File("report_" + timestamp + ".pdf"));
        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            PDFExportUtil.exportTable((String) reportTypeComboBox.getSelectedItem(), table, fileChooser.getSelectedFile());
            DialogUtil.showInfo(this, "Xuất PDF thành công.");
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void updateReportInfo(int rowCount) {
        Object selectedFilter = filterComboBox.getSelectedItem();
        String filterText = selectedFilter == null ? "Chưa chọn" : selectedFilter.toString();
        String updatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        reportInfoPanel.showFields(new String[][]{
                {"Loại báo cáo", reportTypeComboBox.getSelectedItem() == null ? "Chưa chọn" : reportTypeComboBox.getSelectedItem().toString()},
                {"Bộ lọc đang áp dụng", filterText},
                {"Số dòng kết quả", String.valueOf(rowCount)},
                {"Cập nhật lúc", updatedAt},
                {"Ghi chú", "Có thể xuất trực tiếp báo cáo hiện tại sang PDF mà không thay đổi nghiệp vụ."}
        });
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
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(AppColors.TABLE_HEADER_BACKGROUND);
        table.getTableHeader().setForeground(AppColors.CARD_VALUE_TEXT);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 13f));
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));
    }

    private void styleActionButton(JButton button, Color background) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(background);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 13f));
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
    }
}
