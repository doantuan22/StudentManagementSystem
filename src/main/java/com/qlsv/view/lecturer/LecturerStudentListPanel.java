package com.qlsv.view.lecturer;

import com.qlsv.controller.CourseSectionController;
import com.qlsv.controller.EnrollmentController;
import com.qlsv.controller.LecturerController;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Lecturer;
import com.qlsv.utils.AcademicFormatUtil;
import com.qlsv.utils.DialogUtil;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.utils.PDFExportUtil;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BasePanel;
import com.qlsv.view.common.DetailSectionPanel;
import com.qlsv.view.dialog.LecturerStudentDetailDialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
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
import java.util.ArrayList;
import java.util.List;

public class LecturerStudentListPanel extends BasePanel {

    private final LecturerController lecturerController = new LecturerController();
    private final EnrollmentController enrollmentController = new EnrollmentController();
    private final CourseSectionController courseSectionController = new CourseSectionController();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Học phần", "Mã sinh viên", "Sinh viên", "Email", "Trạng thái đăng ký"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new ResponsiveTable(tableModel);
    private final JComboBox<Object> courseComboBox = new JComboBox<>();
    private final JButton filterButton = new JButton("Lọc");
    private final JButton exportButton = new JButton("Xuất PDF");
    private final JButton reloadButton = new JButton("Tải lại");
    private final List<Enrollment> allEnrollments = new ArrayList<>();
    private final List<Enrollment> visibleEnrollments = new ArrayList<>();
    private LecturerStudentDetailDialog detailDialog;
    private final DetailSectionPanel detailSectionPanel = new DetailSectionPanel(
            "Chi tiết sinh viên",
            "Chọn một sinh viên từ danh sách để xem chi tiết."
    );
    private final JLabel summaryLabel = new JLabel("Đang tải danh sách sinh viên...");
    private SwingWorker<LoadResult, Void> loadWorker;
    private boolean loadingData;

    public LecturerStudentListPanel() {
        configureTable();

        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                handleTableSelectionChanged();
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    showDetailDialog();
                }
            }
        });

        courseComboBox.addItem("Tất cả học phần");

        styleSecondaryButton(filterButton);
        stylePrimaryButton(exportButton);
        styleSecondaryButton(reloadButton);

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

        JPanel headerPanel = createSectionCard(new BorderLayout(12, 8));
        JLabel titleLabel = new JLabel("Sinh viên theo học phần");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        JLabel subtitleLabel = new JLabel("Theo dõi danh sách sinh viên đăng ký các học phần đang phụ trách.");
        subtitleLabel.setForeground(AppColors.CARD_MUTED_TEXT);
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 13f));

        JPanel titlePanel = new JPanel(new BorderLayout(0, 6));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new BorderLayout(10, 8));
        controlPanel.setOpaque(false);
        controlPanel.add(filterPanel, BorderLayout.CENTER);
        controlPanel.add(actionPanel, BorderLayout.EAST);

        headerPanel.add(titlePanel, BorderLayout.NORTH);
        headerPanel.add(controlPanel, BorderLayout.CENTER);

        JPanel tablePanel = createSectionCard(new BorderLayout(0, 12));
        JLabel tableTitle = createSectionTitle("Danh sách sinh viên đã đăng ký");

        summaryLabel.setForeground(AppColors.CARD_MUTED_TEXT);
        summaryLabel.setFont(summaryLabel.getFont().deriveFont(Font.PLAIN, 12.5f));

        JPanel tableHeadingPanel = new JPanel(new BorderLayout(12, 0));
        tableHeadingPanel.setOpaque(false);
        tableHeadingPanel.add(tableTitle, BorderLayout.WEST);
        tableHeadingPanel.add(summaryLabel, BorderLayout.EAST);

        JScrollPane tableScrollPane = createTableScrollPane(table);

        tablePanel.add(tableHeadingPanel, BorderLayout.NORTH);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);

        reloadData();
    }

    @Override
    public void removeNotify() {
        disposeDetailDialog();
        super.removeNotify();
    }

    private void handleTableSelectionChanged() {
        updateDetailPanel();
        if (table.getSelectedRow() < 0) {
            hideDetailDialog();
            return;
        }
        showDetailDialog();
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
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));

        table.getColumnModel().getColumn(0).setPreferredWidth(140);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(240);
        table.getColumnModel().getColumn(3).setPreferredWidth(220);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
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

    private JScrollPane createTableScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.CARD_BORDER));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);
        return scrollPane;
    }

    private void stylePrimaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(AppColors.BUTTON_PRIMARY);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
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

    private void updateDetailPanel() {
        Enrollment selectedEnrollment = getSelectedEnrollment();
        if (selectedEnrollment == null || selectedEnrollment.getStudent() == null) {
            detailSectionPanel.showMessage("Chọn một sinh viên từ danh sách để xem chi tiết.");
            return;
        }

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
                {"Niên khóa", AcademicFormatUtil.formatAcademicYear(student.getAcademicYear())},
                {"Trạng thái", DisplayTextUtil.formatStatus(student.getStatus())}
        });
    }

    private void showDetailDialog() {
        if (detailDialog == null) {
            detailDialog = new LecturerStudentDetailDialog(detailSectionPanel);
        }
        detailDialog.openDialog();
    }

    private void hideDetailDialog() {
        if (detailDialog != null) {
            detailDialog.setVisible(false);
        }
    }

    private void disposeDetailDialog() {
        if (detailDialog != null) {
            detailDialog.dispose();
            detailDialog = null;
        }
    }

    @Override
    public void reloadData() {
        if (loadWorker != null && !loadWorker.isDone()) {
            loadWorker.cancel(true);
        }

        Long preferredCourseSectionId = getSelectedCourseSectionId();
        setLoadingState(true, "Đang tải danh sách sinh viên...");

        loadWorker = new SwingWorker<>() {
            @Override
            protected LoadResult doInBackground() {
                Lecturer lecturer = lecturerController.getCurrentLecturer();
                List<CourseSection> sections = courseSectionController.getCourseSectionsByLecturer(lecturer.getId());
                List<Enrollment> enrollments = enrollmentController.getLecturerEnrollments(lecturer.getId());
                return new LoadResult(sections, enrollments, preferredCourseSectionId);
            }

            @Override
            protected void done() {
                try {
                    if (isCancelled()) {
                        return;
                    }

                    LoadResult loadResult = get();
                    allEnrollments.clear();
                    allEnrollments.addAll(loadResult.enrollments());
                    bindCourseSections(loadResult.sections(), loadResult.preferredCourseSectionId());
                    filterData();
                } catch (Exception exception) {
                    summaryLabel.setText("Không thể tải danh sách sinh viên.");
                    DialogUtil.showError(LecturerStudentListPanel.this, exception.getMessage());
                } finally {
                    setLoadingState(false, summaryLabel.getText());
                }
            }
        };
        loadWorker.execute();
    }

    private void filterData() {
        Long preferredEnrollmentId = getSelectedEnrollmentId();
        Object selected = courseComboBox.getSelectedItem();
        tableModel.setRowCount(0);
        visibleEnrollments.clear();

        int visibleCount = 0;
        for (Enrollment enrollment : allEnrollments) {
            boolean matches = false;
            if (selected instanceof String || selected == null) {
                matches = true;
            } else if (selected instanceof CourseSection section) {
                matches = enrollment.getCourseSection() != null
                        && enrollment.getCourseSection().getId().equals(section.getId());
            }

            if (matches) {
                visibleCount++;
                visibleEnrollments.add(enrollment);
                tableModel.addRow(new Object[]{
                        enrollment.getCourseSection() == null ? "" : enrollment.getCourseSection().getSectionCode(),
                        enrollment.getStudent() == null ? "" : enrollment.getStudent().getStudentCode(),
                        enrollment.getStudent() == null ? "" : enrollment.getStudent().getFullName(),
                        enrollment.getStudent() == null ? "" : enrollment.getStudent().getEmail(),
                        DisplayTextUtil.formatStatus(enrollment.getStatus())
                });
            }
        }

        summaryLabel.setText(visibleCount + " sinh viên");
        exportButton.setEnabled(visibleCount > 0 && !loadingData);
        restoreSelectedEnrollment(preferredEnrollmentId);
        updateDetailPanel();
    }

    private void exportToPdf() {
        if (table.getRowCount() == 0) {
            DialogUtil.showError(this, "Không có dữ liệu để xuất PDF.");
            return;
        }

        Object selected = courseComboBox.getSelectedItem();
        String title = "Danh sách sinh viên - "
                + (selected instanceof CourseSection section ? section.getSectionCode() : "Tất cả học phần");

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

    private static final class ResponsiveTable extends JTable {

        private ResponsiveTable(DefaultTableModel model) {
            super(model);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return getPreferredSize().width < getParent().getWidth();
        }
    }

    private Enrollment getSelectedEnrollment() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= visibleEnrollments.size()) {
            return null;
        }
        return visibleEnrollments.get(selectedRow);
    }

    private Long getSelectedEnrollmentId() {
        Enrollment selectedEnrollment = getSelectedEnrollment();
        return selectedEnrollment == null ? null : selectedEnrollment.getId();
    }

    private Long getSelectedCourseSectionId() {
        Object selectedItem = courseComboBox.getSelectedItem();
        if (selectedItem instanceof CourseSection section) {
            return section.getId();
        }
        return null;
    }

    private void bindCourseSections(List<CourseSection> sections, Long preferredCourseSectionId) {
        courseComboBox.removeAllItems();
        courseComboBox.addItem("Tất cả học phần");
        for (CourseSection section : sections) {
            courseComboBox.addItem(section);
        }

        if (preferredCourseSectionId == null) {
            courseComboBox.setSelectedIndex(0);
            return;
        }

        for (int index = 0; index < courseComboBox.getItemCount(); index++) {
            Object item = courseComboBox.getItemAt(index);
            if (item instanceof CourseSection currentSection
                    && currentSection.getId() != null
                    && currentSection.getId().equals(preferredCourseSectionId)) {
                courseComboBox.setSelectedIndex(index);
                return;
            }
        }

        courseComboBox.setSelectedIndex(0);
    }

    private void restoreSelectedEnrollment(Long preferredEnrollmentId) {
        if (preferredEnrollmentId == null) {
            table.clearSelection();
            return;
        }

        for (int index = 0; index < visibleEnrollments.size(); index++) {
            Enrollment enrollment = visibleEnrollments.get(index);
            if (enrollment != null
                    && enrollment.getId() != null
                    && enrollment.getId().equals(preferredEnrollmentId)) {
                table.setRowSelectionInterval(index, index);
                table.scrollRectToVisible(table.getCellRect(index, 0, true));
                return;
            }
        }

        table.clearSelection();
    }

    private void setLoadingState(boolean loading, String message) {
        loadingData = loading;
        courseComboBox.setEnabled(!loading);
        filterButton.setEnabled(!loading);
        exportButton.setEnabled(!loading && table.getRowCount() > 0);
        reloadButton.setEnabled(!loading);
        summaryLabel.setText(message);
    }

    private record LoadResult(
            List<CourseSection> sections,
            List<Enrollment> enrollments,
            Long preferredCourseSectionId
    ) {
    }
}
