package com.qlsv.view.lecturer;

import com.qlsv.controller.CourseSectionController;
import com.qlsv.controller.LecturerController;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Lecturer;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BasePanel;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.view.common.DetailSectionPanel;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class LecturerCourseSectionPanel extends BasePanel {

    private final LecturerController lecturerController = new LecturerController();
    private final CourseSectionController courseSectionController = new CourseSectionController();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final DetailSectionPanel detailSectionPanel;
    private final List<CourseSection> currentSections = new ArrayList<>();

    public LecturerCourseSectionPanel() {
        tableModel = new DefaultTableModel(
                new String[]{"Mã học phần", "Môn học", "Phòng học", "Học kỳ", "Năm học", "Lịch học"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionBackground(AppColors.TABLE_SELECTION_BACKGROUND);
        table.getTableHeader().setBackground(AppColors.TABLE_HEADER_BACKGROUND);

        detailSectionPanel = new DetailSectionPanel(
                "Chi tiết học phần",
                "Chọn một học phần để xem chi tiết."
        );

        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                updateDetailPanel();
            }
        });
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(new JScrollPane(table));
        
        // Wrap detailSectionPanel vao JScrollPane de co the cuon khi noi dung dai
        JScrollPane detailScrollPane = new JScrollPane(detailSectionPanel);
        detailScrollPane.setBorder(null);
        detailScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        detailScrollPane.getViewport().setBackground(detailSectionPanel.getBackground());
        
        splitPane.setBottomComponent(detailScrollPane);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.6);

        add(splitPane, java.awt.BorderLayout.CENTER);
        reloadData();
    }

    private void updateDetailPanel() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            detailSectionPanel.showMessage("Chọn một học phần để xem chi tiết.");
            return;
        }

        String sectionCode = (String) tableModel.getValueAt(selectedRow, 0);
        CourseSection selectedSection = currentSections.stream()
                .filter(cs -> cs.getSectionCode().equals(sectionCode))
                .findFirst()
                .orElse(null);

        if (selectedSection != null) {
            detailSectionPanel.showFields(new String[][]{
                    {"Mã học phần", selectedSection.getSectionCode()},
                    {"Môn học", selectedSection.getSubject() != null ? selectedSection.getSubject().getSubjectName() : ""},
                    {"Số tín chỉ", selectedSection.getSubject() != null ? String.valueOf(selectedSection.getSubject().getCredits()) : ""},
                    {"Giảng viên", selectedSection.getLecturer() != null ? selectedSection.getLecturer().getFullName() : ""},
                    {"Phòng học", selectedSection.getRoom() != null ? selectedSection.getRoom().getRoomName() : ""},
                    {"Học kỳ", selectedSection.getSemester()},
                    {"Năm học", selectedSection.getSchoolYear()},
                    {"Lịch học", selectedSection.getScheduleText()},
                    {"Số sinh viên tối đa", String.valueOf(selectedSection.getMaxStudents())}
            });
        }
    }

    @Override
    public void reloadData() {
        try {
            tableModel.setRowCount(0);
            currentSections.clear();
            Lecturer lecturer = lecturerController.getCurrentLecturer();
            List<CourseSection> sections = courseSectionController.getCourseSectionsByLecturer(lecturer.getId());
            currentSections.addAll(sections);
            
            for (CourseSection courseSection : sections) {
                tableModel.addRow(new Object[]{
                        courseSection.getSectionCode(),
                        courseSection.getSubject() == null ? "" : courseSection.getSubject().getSubjectName(),
                        courseSection.getRoom() != null ? courseSection.getRoom().getRoomName() : "",
                        courseSection.getSemester(),
                        courseSection.getSchoolYear(),
                        courseSection.getScheduleText()
                });
            }
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }
}
