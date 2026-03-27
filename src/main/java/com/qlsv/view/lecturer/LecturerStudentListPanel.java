package com.qlsv.view.lecturer;

import com.qlsv.controller.CourseSectionController;
import com.qlsv.controller.EnrollmentController;
import com.qlsv.controller.LecturerController;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Lecturer;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.utils.DialogUtil;
import com.qlsv.utils.PDFExportUtil;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BasePanel;
import com.qlsv.view.common.DetailSectionPanel;

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
import java.awt.FlowLayout;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LecturerStudentListPanel extends BasePanel {

    private final LecturerController lecturerController = new LecturerController();
    private final EnrollmentController enrollmentController = new EnrollmentController();
    private final CourseSectionController courseSectionController = new CourseSectionController();

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JComboBox<Object> courseComboBox;
    private final List<Enrollment> allEnrollments = new ArrayList<>();
    private final DetailSectionPanel detailSectionPanel;

    public LecturerStudentListPanel() {
        tableModel = new DefaultTableModel(
                new String[]{"Học phần", "Mã sinh viên", "Sinh viên", "Email", "Trạng thái đăng ký"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionBackground(AppColors.TABLE_SELECTION_BACKGROUND);
        table.getTableHeader().setBackground(AppColors.TABLE_HEADER_BACKGROUND);

        detailSectionPanel = new DetailSectionPanel(
                "Chi tiết sinh viên",
                "Chọn một sinh viên từ danh sách để xem chi tiết."
        );

        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                updateDetailPanel();
            }
        });

        courseComboBox = new JComboBox<>();
        courseComboBox.addItem("Tất cả học phần");

        JButton filterButton = new JButton("Lọc");
        JButton exportButton = new JButton("Xuất PDF");
        JButton reloadButton = new JButton("Tải lại");

        filterButton.addActionListener(event -> filterData());
        exportButton.addActionListener(event -> exportToPdf());
        reloadButton.addActionListener(event -> reloadData());

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setOpaque(false);
        filterPanel.add(new JLabel("Học phần:"));
        filterPanel.add(courseComboBox);
        filterPanel.add(filterButton);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionPanel.setOpaque(false);
        actionPanel.add(exportButton);
        actionPanel.add(reloadButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(filterPanel, BorderLayout.WEST);
        topPanel.add(actionPanel, BorderLayout.EAST);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(new JScrollPane(table));
        
        // Bọc detailSectionPanel vào JScrollPane để có thể cuộn khi nội dung dài hoặc cửa sổ nhỏ
        JScrollPane detailScrollPane = new JScrollPane(detailSectionPanel);
        detailScrollPane.setBorder(null);
        detailScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        detailScrollPane.getViewport().setBackground(detailSectionPanel.getBackground());
        
        splitPane.setBottomComponent(detailScrollPane);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.6);

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        loadCourseSections();
        reloadData();
    }

    private void updateDetailPanel() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            detailSectionPanel.showMessage("Chọn một sinh viên từ danh sách để xem chi tiết.");
            return;
        }

        // Tim enrollment tuong ung
        String sectionCode = (String) tableModel.getValueAt(selectedRow, 0);
        String studentCode = (String) tableModel.getValueAt(selectedRow, 1);

        Enrollment selectedEnrollment = allEnrollments.stream()
                .filter(e -> e.getCourseSection() != null && e.getCourseSection().getSectionCode().equals(sectionCode)
                        && e.getStudent() != null && e.getStudent().getStudentCode().equals(studentCode))
                .findFirst()
                .orElse(null);

        if (selectedEnrollment != null && selectedEnrollment.getStudent() != null) {
            var student = selectedEnrollment.getStudent();
            detailSectionPanel.showFields(new String[][]{
                    {"Mã sinh viên", student.getStudentCode()},
                    {"Họ và tên", student.getFullName()},
                    {"Giới tính", DisplayTextUtil.formatGender(student.getGender())},
                    {"Ngày sinh", DisplayTextUtil.formatDate(student.getDateOfBirth())},
                    {"Email", student.getEmail()},
                    {"Số điện thoại", student.getPhone()},
                    {"Khoa", student.getFaculty() != null ? student.getFaculty().getFacultyName() : "Chưa cập nhật"},
                    {"Lớp", student.getClassRoom() != null ? student.getClassRoom().getClassName() : "Chưa cập nhật"},
                    {"Niên khóa", student.getAcademicYear()},
                    {"Trạng thái", DisplayTextUtil.formatStatus(student.getStatus())}
            });
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
            DialogUtil.showError(this, "Không thể tải danh sách học phần phụ trách: " + exception.getMessage());
        }
    }

    @Override
    public void reloadData() {
        try {
            loadCourseSections();
            Lecturer lecturer = lecturerController.getCurrentLecturer();
            allEnrollments.clear();
            allEnrollments.addAll(enrollmentController.getLecturerEnrollments(lecturer.getId()));
            filterData();
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void filterData() {
        Object selected = courseComboBox.getSelectedItem();
        tableModel.setRowCount(0);

        for (Enrollment enrollment : allEnrollments) {
            boolean matches = false;
            if (selected instanceof String || selected == null) {
                matches = true; // "Tất cả học phần" selected
            } else if (selected instanceof CourseSection section) {
                matches = enrollment.getCourseSection() != null 
                        && enrollment.getCourseSection().getId().equals(section.getId());
            }

            if (matches) {
                tableModel.addRow(new Object[]{
                        enrollment.getCourseSection() == null ? "" : enrollment.getCourseSection().getSectionCode(),
                        enrollment.getStudent() == null ? "" : enrollment.getStudent().getStudentCode(),
                        enrollment.getStudent() == null ? "" : enrollment.getStudent().getFullName(),
                        enrollment.getStudent() == null ? "" : enrollment.getStudent().getEmail(),
                        DisplayTextUtil.formatStatus(enrollment.getStatus())
                });
            }
        }
    }

    private void exportToPdf() {
        if (table.getRowCount() == 0) {
            DialogUtil.showError(this, "Không có dữ liệu để xuất PDF.");
            return;
        }

        Object selected = courseComboBox.getSelectedItem();
        String title = "Danh sách sinh viên - " + (selected instanceof CourseSection section ? section.getSectionCode() : "Tất cả học phần");

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu file PDF");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setSelectedFile(new File("danh_sach_sinh_vien_" + timestamp + ".pdf"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                PDFExportUtil.exportTable(title, table, fileChooser.getSelectedFile());
                DialogUtil.showInfo(this, "Xuất PDF thành công.");
            } catch (Exception exception) {
                DialogUtil.showError(this, "Xuất PDF thất bại: " + exception.getMessage());
            }
        }
    }
}
