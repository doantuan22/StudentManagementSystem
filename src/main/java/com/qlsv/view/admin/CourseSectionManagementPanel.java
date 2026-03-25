package com.qlsv.view.admin;

import com.qlsv.controller.CourseSectionController;
import com.qlsv.controller.FacultyController;
import com.qlsv.controller.LecturerController;
import com.qlsv.controller.RoomController;
import com.qlsv.controller.SubjectController;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Faculty;
import com.qlsv.model.Lecturer;
import com.qlsv.model.Room;
import com.qlsv.model.Subject;
import com.qlsv.utils.DisplayTextUtil;
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

    private final CourseSectionController courseSectionController = new CourseSectionController();
    private final SubjectController subjectController = new SubjectController();
    private final LecturerController lecturerController = new LecturerController();
    private final FacultyController facultyController = new FacultyController();
    private final RoomController roomController = new RoomController();

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
        return new String[]{"ID", "Mã học phần", "Môn học", "Giảng viên", "Phòng học", "Học kỳ", "Năm học"};
    }

    @Override
    protected List<CourseSection> loadItems() {
        if (!filterReady) {
            return List.of();
        }

        String filterType = (String) filterTypeComboBox.getSelectedItem();
        return switch (filterType == null ? FILTER_NONE : filterType) {
            case FILTER_ALL -> courseSectionController.getAllCourseSections();
            case FILTER_SECTION_CODE -> {
                String sectionCode = getSelectedFilterValue(String.class);
                yield sectionCode == null ? List.of() : courseSectionController.getCourseSectionsBySectionCode(sectionCode);
            }
            case FILTER_ROOM -> {
                Room room = getSelectedFilterValue(Room.class);
                yield room == null ? List.of() : courseSectionController.getCourseSectionsByRoom(room.getId());
            }
            case FILTER_FACULTY -> {
                Faculty faculty = getSelectedFilterValue(Faculty.class);
                yield faculty == null ? List.of() : courseSectionController.getCourseSectionsByFaculty(faculty.getId());
            }
            default -> List.of();
        };
    }

    @Override
    protected Object[] toRow(CourseSection item) {
        return new Object[]{
                item.getId(),
                item.getSectionCode(),
                item.getSubject() == null ? "" : item.getSubject().getSubjectName(),
                item.getLecturer() == null ? "" : item.getLecturer().getFullName(),
                item.getRoom() == null ? "" : item.getRoom().getRoomName(),
                item.getSemester(),
                item.getSchoolYear()
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

        detailSectionPanel.showFields(new String[][]{
                {"Mã học phần", DisplayTextUtil.defaultText(selectedItem.getSectionCode())},
                {"Môn học", selectedItem.getSubject() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(selectedItem.getSubject().getSubjectName())},
                {"Giảng viên", selectedItem.getLecturer() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(selectedItem.getLecturer().getFullName())},
                {"Phòng học", selectedItem.getRoom() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(selectedItem.getRoom().getRoomName())},
                {"Học kỳ", DisplayTextUtil.defaultText(selectedItem.getSemester())},
                {"Năm học", DisplayTextUtil.defaultText(selectedItem.getSchoolYear())},
                {"Lịch học", DisplayTextUtil.defaultText(selectedItem.getScheduleText())},
                {"Sĩ số tối đa", DisplayTextUtil.defaultText(selectedItem.getMaxStudents())}
        });
    }

    @Override
    protected CourseSection promptForEntity(CourseSection existingItem) {
        JTextField codeField = new JTextField(existingItem == null ? "" : existingItem.getSectionCode());
        JTextField semesterField = new JTextField(existingItem == null ? "HK1" : existingItem.getSemester());
        JTextField schoolYearField = new JTextField(existingItem == null ? "2025 - 2026" : existingItem.getSchoolYear());
        JComboBox<Room> roomComboBox = new JComboBox<>(roomController.getRoomsForSelection().toArray(new Room[0]));
        JTextField scheduleField = new JTextField(existingItem == null ? "" : existingItem.getScheduleText());
        JTextField maxStudentsField = new JTextField(existingItem == null ? "50" : String.valueOf(existingItem.getMaxStudents()));

        JComboBox<Subject> subjectComboBox = new JComboBox<>(subjectController.getSubjectsForSelection().toArray(new Subject[0]));
        JComboBox<Lecturer> lecturerComboBox = new JComboBox<>(lecturerController.getLecturersForSelection().toArray(new Lecturer[0]));

        if (existingItem != null) {
            if (existingItem.getSubject() != null) {
                subjectComboBox.setSelectedItem(existingItem.getSubject());
            }
            if (existingItem.getLecturer() != null) {
                lecturerComboBox.setSelectedItem(existingItem.getLecturer());
            }
            if (existingItem.getRoom() != null) {
                roomComboBox.setSelectedItem(existingItem.getRoom());
            }
        }

        JPanel schedulePanel = new JPanel(new java.awt.BorderLayout());
        schedulePanel.add(scheduleField, java.awt.BorderLayout.CENTER);
        JLabel hintLabel = new JLabel("VD format: T2 1-3, T4 4-6 (Có thể để trống)");
        hintLabel.setFont(hintLabel.getFont().deriveFont(java.awt.Font.ITALIC, 11f));
        hintLabel.setForeground(java.awt.Color.GRAY);
        schedulePanel.add(hintLabel, java.awt.BorderLayout.SOUTH);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.add(new JLabel("Mã học phần"));
        formPanel.add(codeField);
        formPanel.add(new JLabel("Môn học"));
        formPanel.add(subjectComboBox);
        formPanel.add(new JLabel("Giảng viên"));
        formPanel.add(lecturerComboBox);
        formPanel.add(new JLabel("Phòng học"));
        formPanel.add(roomComboBox);
        formPanel.add(new JLabel("Học kỳ"));
        formPanel.add(semesterField);
        formPanel.add(new JLabel("Năm học"));
        formPanel.add(schoolYearField);
        formPanel.add(new JLabel("Lịch học"));
        formPanel.add(schedulePanel);
        formPanel.add(new JLabel("Sĩ số tối đa"));
        formPanel.add(maxStudentsField);

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

        CourseSection courseSection = existingItem == null ? new CourseSection() : existingItem;
        courseSection.setSectionCode(codeField.getText().trim());
        courseSection.setSubject((Subject) subjectComboBox.getSelectedItem());
        courseSection.setLecturer((Lecturer) lecturerComboBox.getSelectedItem());
        courseSection.setRoom((Room) roomComboBox.getSelectedItem());
        courseSection.setSemester(semesterField.getText().trim());
        courseSection.setSchoolYear(schoolYearField.getText().trim());
        courseSection.setScheduleText(scheduleField.getText().trim());
        courseSection.setMaxStudents(Integer.parseInt(maxStudentsField.getText().trim()));
        return courseSection;
    }

    @Override
    protected void saveEntity(CourseSection item) {
        courseSectionController.saveCourseSection(item);
    }

    @Override
    protected void deleteEntity(CourseSection item) {
        courseSectionController.deleteCourseSection(item.getId());
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
            for (CourseSection courseSection : courseSectionController.getAllCourseSectionsForSelection()) {
                filterValueComboBox.addItem(new FilterOption<>(courseSection.getSectionCode(), courseSection.getSectionCode()));
            }
            return;
        }

        if (FILTER_ROOM.equals(filterType)) {
            filterValueComboBox.setEnabled(true);
            filterValueComboBox.addItem(new FilterOption<>("Chọn phòng học", null));
            for (Room room : roomController.getRoomsForSelection()) {
                filterValueComboBox.addItem(new FilterOption<>(room.getRoomName(), room));
            }
            return;
        }

        if (FILTER_FACULTY.equals(filterType)) {
            filterValueComboBox.setEnabled(true);
            filterValueComboBox.addItem(new FilterOption<>("Chọn khoa", null));
            for (Faculty faculty : facultyController.getFacultiesForSelection()) {
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
