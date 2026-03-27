package com.qlsv.view.admin;

import com.qlsv.controller.CourseSectionManagementScreenController;
import com.qlsv.controller.DisplayField;
import com.qlsv.dto.CourseSectionDisplayDto;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Faculty;
import com.qlsv.model.Lecturer;
import com.qlsv.model.Room;
import com.qlsv.model.Subject;
import com.qlsv.utils.AcademicFormatUtil;
import com.qlsv.view.common.AbstractCrudPanel;
import com.qlsv.view.common.DetailSectionPanel;
import com.qlsv.view.common.FilterOption;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

public class CourseSectionManagementPanel extends AbstractCrudPanel<CourseSection> {

    private static final String FILTER_NONE = "Chọn điều kiện lọc";
    private static final String FILTER_ALL = "Tất cả học phần";
    private static final String FILTER_SECTION_CODE = "Theo mã học phần";
    private static final String FILTER_ROOM = "Theo phòng học";
    private static final String FILTER_FACULTY = "Theo khoa";

    private final CourseSectionManagementScreenController screenController = new CourseSectionManagementScreenController();

    private final JComboBox<String> filterTypeComboBox = new JComboBox<>(
            new String[]{FILTER_NONE, FILTER_ALL, FILTER_SECTION_CODE, FILTER_ROOM, FILTER_FACULTY}
    );
    private final JComboBox<FilterOption<?>> filterValueComboBox = new JComboBox<>();
    private final DetailSectionPanel detailSectionPanel = new DetailSectionPanel(
            "Chi tiết học phần",
            "Vui lòng chọn học phần để xem chi tiết."
    );

    private boolean filterReady;

    public CourseSectionManagementPanel() {
        super("Quản lý học phần");
        setFilterPanel(buildFilterPanel());
        setDetailPanel(detailSectionPanel);
        reloadFilterValues();
        refreshData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Mã học phần", "Môn học", "Giảng viên", "Học kỳ", "Năm học", "Lịch học"};
    }

    @Override
    protected List<CourseSection> loadItems() {
        return screenController.loadItems(
                filterReady,
                (String) filterTypeComboBox.getSelectedItem(),
                getSelectedFilterValue(Object.class),
                FILTER_ALL,
                FILTER_SECTION_CODE,
                FILTER_ROOM,
                FILTER_FACULTY
        );
    }

    @Override
    protected Object[] toRow(CourseSection item) {
        CourseSectionDisplayDto displayDto = screenController.toDisplayDto(item);
        return new Object[]{
                displayDto.id(),
                displayDto.sectionCode(),
                displayDto.subjectName(),
                displayDto.lecturerName(),
                displayDto.semester(),
                displayDto.schoolYear(),
                displayDto.scheduleText()
        };
    }

    @Override
    protected String getEmptyStateMessage() {
        return filterReady
                ? "Không tìm thấy học phần phù hợp với điều kiện lọc hiện tại."
                : "Vui lòng chọn điều kiện lọc để hiển thị danh sách học phần.";
    }

    @Override
    protected void onSelectionChanged(CourseSection selectedItem) {
        if (selectedItem == null) {
            detailSectionPanel.showMessage("Vui lòng chọn học phần để xem chi tiết.");
            return;
        }

        List<DisplayField> detailFields = screenController.buildDetailFields(selectedItem);
        detailSectionPanel.showFields(detailFields.stream()
                .map(field -> new String[]{field.label(), field.value()})
                .toArray(String[][]::new));
    }

    @Override
    protected CourseSection promptForEntity(CourseSection existingItem) {
        JTextField codeField = new JTextField(existingItem == null ? "" : existingItem.getSectionCode());
        JComboBox<String> semesterComboBox = new JComboBox<>(AcademicFormatUtil.getFixedSemesters().toArray(new String[0]));
        semesterComboBox.setSelectedItem(existingItem == null
                ? "HK1"
                : AcademicFormatUtil.formatSemester(existingItem.getSemester()));
        JTextField schoolYearField = new JTextField(existingItem == null
                ? "2025 - 2026"
                : AcademicFormatUtil.formatAcademicYear(existingItem.getSchoolYear()));
        JTextField maxStudentsField = new JTextField(existingItem == null ? "50" : String.valueOf(existingItem.getMaxStudents()));

        JComboBox<Subject> subjectComboBox = new JComboBox<>(screenController.loadSubjects().toArray(new Subject[0]));
        JComboBox<Lecturer> lecturerComboBox = new JComboBox<>(screenController.loadLecturers().toArray(new Lecturer[0]));

        if (existingItem != null) {
            if (existingItem.getSubject() != null) {
                subjectComboBox.setSelectedItem(existingItem.getSubject());
            }
            if (existingItem.getLecturer() != null) {
                lecturerComboBox.setSelectedItem(existingItem.getLecturer());
            }
        }

        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 0, 4));
        infoPanel.setOpaque(false);
        infoPanel.add(new JLabel("Lịch học và phòng học được quản lý tại màn hình lịch học sau khi tạo học phần."));

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.add(new JLabel("Mã học phần"));
        formPanel.add(codeField);
        formPanel.add(new JLabel("Môn học"));
        formPanel.add(subjectComboBox);
        formPanel.add(new JLabel("Giảng viên"));
        formPanel.add(lecturerComboBox);
        formPanel.add(new JLabel("Học kỳ"));
        formPanel.add(semesterComboBox);
        formPanel.add(new JLabel("Năm học"));
        formPanel.add(schoolYearField);
        formPanel.add(new JLabel("Sĩ số tối đa"));
        formPanel.add(maxStudentsField);
        formPanel.add(new JLabel("Ghi chú"));
        formPanel.add(infoPanel);

        int result = JOptionPane.showConfirmDialog(
                this,
                formPanel,
                existingItem == null ? "Thêm học phần" : "Cập nhật học phần",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        return screenController.applyFormData(
                existingItem,
                new CourseSectionManagementScreenController.CourseSectionFormData(
                        codeField.getText(),
                        (Subject) subjectComboBox.getSelectedItem(),
                        (Lecturer) lecturerComboBox.getSelectedItem(),
                        (String) semesterComboBox.getSelectedItem(),
                        schoolYearField.getText(),
                        maxStudentsField.getText()
                )
        );
    }

    @Override
    protected void saveEntity(CourseSection item) {
        screenController.saveCourseSection(item);
    }

    @Override
    protected void deleteEntity(CourseSection item) {
        screenController.deleteCourseSection(item);
    }

    private JPanel buildFilterPanel() {
        JButton applyButton = new JButton("Áp dụng");
        JButton resetButton = new JButton("Đặt lại");

        applyButton.addActionListener(event -> {
            filterReady = hasValidFilterSelection();
            refreshData();
        });
        resetButton.addActionListener(event -> resetFilter());
        filterTypeComboBox.addActionListener(event -> reloadFilterValues());

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Bộ lọc học phần"),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        filterPanel.add(new JLabel("Điều kiện"));
        filterPanel.add(filterTypeComboBox);
        filterPanel.add(new JLabel("Giá trị"));
        filterPanel.add(filterValueComboBox);
        filterPanel.add(applyButton);
        filterPanel.add(resetButton);
        return filterPanel;
    }

    private void reloadFilterValues() {
        filterReady = false;
        filterValueComboBox.removeAllItems();

        String filterType = (String) filterTypeComboBox.getSelectedItem();
        if (FILTER_SECTION_CODE.equals(filterType)) {
            filterValueComboBox.setEnabled(true);
            filterValueComboBox.addItem(new FilterOption<>("Chọn mã học phần", null));
            for (CourseSection courseSection : screenController.loadCourseSections()) {
                filterValueComboBox.addItem(new FilterOption<>(courseSection.getSectionCode(), courseSection.getSectionCode()));
            }
            return;
        }

        if (FILTER_ROOM.equals(filterType)) {
            filterValueComboBox.setEnabled(true);
            filterValueComboBox.addItem(new FilterOption<>("Chọn phòng học", null));
            for (Room room : screenController.loadRooms()) {
                filterValueComboBox.addItem(new FilterOption<>(room.getRoomName(), room));
            }
            return;
        }

        if (FILTER_FACULTY.equals(filterType)) {
            filterValueComboBox.setEnabled(true);
            filterValueComboBox.addItem(new FilterOption<>("Chọn khoa", null));
            for (Faculty faculty : screenController.loadFaculties()) {
                filterValueComboBox.addItem(new FilterOption<>(faculty.getFacultyCode() + " - " + faculty.getFacultyName(), faculty));
            }
            return;
        }

        filterValueComboBox.setEnabled(false);
    }

    private void resetFilter() {
        filterTypeComboBox.setSelectedItem(FILTER_NONE);
        filterValueComboBox.removeAllItems();
        filterValueComboBox.setEnabled(false);
        filterReady = false;
        refreshData();
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
}
