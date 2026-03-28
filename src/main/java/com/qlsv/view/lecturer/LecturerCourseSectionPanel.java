package com.qlsv.view.lecturer;

import com.qlsv.controller.CourseSectionController;
import com.qlsv.controller.LecturerController;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Lecturer;
import com.qlsv.utils.AcademicFormatUtil;
import com.qlsv.utils.DialogUtil;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BasePanel;
import com.qlsv.view.common.DetailSectionPanel;
import com.qlsv.view.dialog.LecturerCourseSectionDetailDialog;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class LecturerCourseSectionPanel extends BasePanel {

    private final LecturerController lecturerController = new LecturerController();
    private final CourseSectionController courseSectionController = new CourseSectionController();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final DetailSectionPanel detailSectionPanel;
    private final List<CourseSection> currentSections = new ArrayList<>();
    private LecturerCourseSectionDetailDialog detailDialog;
    private final JLabel summaryLabel = new JLabel("Đang tải học phần phụ trách...");

    public LecturerCourseSectionPanel() {
        tableModel = new DefaultTableModel(
                new String[]{"Mã học phần", "Môn học", "Học kỳ", "Năm học", "Lịch học"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        configureTable(table);

        detailSectionPanel = new DetailSectionPanel(
                "Chi tiết học phần",
                "Chọn một học phần để xem chi tiết."
        );

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

        JPanel tablePanel = new JPanel(new BorderLayout(0, 12));
        tablePanel.setOpaque(true);
        tablePanel.setBackground(AppColors.CARD_BACKGROUND);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JLabel headerLabel = new JLabel("Học phần đang phụ trách");
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 16f));
        headerLabel.setForeground(AppColors.CARD_TITLE_TEXT);

        summaryLabel.setForeground(AppColors.CARD_MUTED_TEXT);
        summaryLabel.setFont(summaryLabel.getFont().deriveFont(Font.PLAIN, 12.5f));

        JPanel headingPanel = new JPanel(new BorderLayout(12, 0));
        headingPanel.setOpaque(false);
        headingPanel.add(headerLabel, BorderLayout.WEST);
        headingPanel.add(summaryLabel, BorderLayout.EAST);

        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(AppColors.CARD_BORDER));
        tableScrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);
        tableScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        tableScrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        tablePanel.add(headingPanel, BorderLayout.NORTH);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);


        JLabel titleLabel = new JLabel("Học phần phụ trách");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        JPanel titlePanel = new JPanel(new BorderLayout(0, 6));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.NORTH);


        add(titlePanel, BorderLayout.NORTH);
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

    private void updateDetailPanel() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            detailSectionPanel.showMessage("Chọn một học phần để xem chi tiết.");
            return;
        }

        String sectionCode = (String) tableModel.getValueAt(selectedRow, 0);
        CourseSection selectedSection = currentSections.stream()
                .filter(courseSection -> courseSection.getSectionCode().equals(sectionCode))
                .findFirst()
                .orElse(null);

        if (selectedSection != null) {
            detailSectionPanel.showFields(new String[][]{
                    {"Mã học phần", selectedSection.getSectionCode()},
                    {"Môn học", selectedSection.getSubject() != null ? selectedSection.getSubject().getSubjectName() : ""},
                    {"Số tín chỉ", selectedSection.getSubject() != null ? String.valueOf(selectedSection.getSubject().getCredits()) : ""},
                    {"Giảng viên", selectedSection.getLecturer() != null ? selectedSection.getLecturer().getFullName() : ""},
                    {"Học kỳ", DisplayTextUtil.defaultText(AcademicFormatUtil.formatSemester(selectedSection.getSemester()))},
                    {"Năm học", DisplayTextUtil.defaultText(AcademicFormatUtil.formatAcademicYear(selectedSection.getSchoolYear()))},
                    {"Lịch học", DisplayTextUtil.defaultText(selectedSection.getScheduleText())},
                    {"Số sinh viên tối đa", String.valueOf(selectedSection.getMaxStudents())}
            });
        }
    }

    private void showDetailDialog() {
        if (detailDialog == null) {
            detailDialog = new LecturerCourseSectionDetailDialog(detailSectionPanel);
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
        try {
            tableModel.setRowCount(0);
            currentSections.clear();
            Lecturer lecturer = lecturerController.getCurrentLecturer();
            List<CourseSection> sections = courseSectionController.getCourseSectionsByLecturer(lecturer.getId());
            currentSections.addAll(sections);
            summaryLabel.setText(sections.size() + " học phần đang phụ trách");

            for (CourseSection courseSection : sections) {
                tableModel.addRow(new Object[]{
                        courseSection.getSectionCode(),
                        courseSection.getSubject() == null ? "" : courseSection.getSubject().getSubjectName(),
                        AcademicFormatUtil.formatSemester(courseSection.getSemester()),
                        AcademicFormatUtil.formatAcademicYear(courseSection.getSchoolYear()),
                        DisplayTextUtil.defaultText(courseSection.getScheduleText())
                });
            }
            handleTableSelectionChanged();
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void configureTable(JTable table) {
        table.setRowHeight(28);
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
}
