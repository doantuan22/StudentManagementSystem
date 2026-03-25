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
import com.qlsv.view.common.BasePanel;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
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
    private final DefaultTableModel tableModel = new DefaultTableModel();
    private final JTable table = new JTable(tableModel);
    private final JLabel statisticsLabel = new JLabel("-");

    public ReportManagementPanel() {
        JButton loadButton = new JButton("Tải báo cáo");
        JButton exportButton = new JButton("Xuất PDF");
        loadButton.addActionListener(event -> loadReport());
        exportButton.addActionListener(event -> exportCurrentTable());
        reportTypeComboBox.addActionListener(event -> refreshFilterOptions());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topPanel.add(new JLabel("Loại báo cáo"));
        topPanel.add(reportTypeComboBox);
        topPanel.add(new JLabel("Bộ lọc"));
        topPanel.add(filterComboBox);
        topPanel.add(loadButton);
        topPanel.add(exportButton);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(statisticsLabel, BorderLayout.SOUTH);

        refreshFilterOptions();
        loadStatistics();
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
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void loadStatistics() {
        try {
            SystemStatistics statistics = reportController.getSystemStatistics();
            statisticsLabel.setText("Tổng quan nhanh: "
                    + statistics.getTotalStudents() + " sinh viên, "
                    + statistics.getTotalLecturers() + " giảng viên, "
                    + statistics.getTotalSubjects() + " môn học, "
                    + statistics.getTotalCourseSections() + " học phần.");
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void exportCurrentTable() {
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
}
