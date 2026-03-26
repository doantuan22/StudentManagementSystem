package com.qlsv.view.admin;

import com.qlsv.controller.CourseSectionController;
import com.qlsv.controller.FacultyController;
import com.qlsv.controller.RoomController;
import com.qlsv.controller.ScheduleController;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Faculty;
import com.qlsv.model.Room;
import com.qlsv.model.Schedule;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ScheduleManagementPanel extends AbstractCrudPanel<Schedule> {

    private static final String FILTER_NONE = "Chọn điều kiện lọc";
    private static final String FILTER_ALL = "Tất cả lịch học";
    private static final String FILTER_SECTION_CODE = "Theo mã học phần";
    private static final String FILTER_ROOM = "Theo phòng học";
    private static final String FILTER_FACULTY = "Theo khoa";

    private static final String[] DAYS_OF_WEEK = {
            "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"
    };
    private static final Integer[] PERIOD_OPTIONS = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

    private final ScheduleController scheduleController = new ScheduleController();
    private final CourseSectionController courseSectionController = new CourseSectionController();
    private final FacultyController facultyController = new FacultyController();
    private final RoomController roomController = new RoomController();

    private final JComboBox<String> filterTypeComboBox = new JComboBox<>(
            new String[]{FILTER_NONE, FILTER_ALL, FILTER_SECTION_CODE, FILTER_ROOM, FILTER_FACULTY}
    );
    private final JComboBox<FilterOption<?>> filterValueComboBox = new JComboBox<>();
    private final DetailSectionPanel detailSectionPanel = new DetailSectionPanel(
            "Chi tiết lịch học",
            "Vui lòng chọn lịch học để xem chi tiết."
    );

    private boolean filterReady;

    public ScheduleManagementPanel() {
        super("Quản lý lịch học");
        configureActionButtons(false, true, false, false);
        setFilterPanel(buildFilterPanel());
        setDetailPanel(detailSectionPanel);
        reloadFilterValues();
        refreshData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Học phần", "Môn học", "Giảng viên", "Thứ", "Tiết", "Phòng", "Ghi chú"};
    }

    @Override
    protected List<Schedule> loadItems() {
        if (!filterReady) {
            return List.of();
        }

        String filterType = (String) filterTypeComboBox.getSelectedItem();
        List<Schedule> schedules = new java.util.ArrayList<>(switch (filterType == null ? FILTER_NONE : filterType) {
            case FILTER_ALL -> scheduleController.getAllSchedules();
            case FILTER_SECTION_CODE -> {
                CourseSection courseSection = getSelectedFilterValue(CourseSection.class);
                yield courseSection == null ? List.of() : scheduleController.getSchedulesByCourseSection(courseSection.getId());
            }
            case FILTER_ROOM -> {
                Room room = getSelectedFilterValue(Room.class);
                yield room == null ? List.of() : scheduleController.getSchedulesByRoom(room.getId());
            }
            case FILTER_FACULTY -> {
                Faculty faculty = getSelectedFilterValue(Faculty.class);
                yield faculty == null ? List.of() : scheduleController.getSchedulesByFaculty(faculty.getId());
            }
            default -> List.of();
        });

        Set<Long> scheduledCourseSectionIds = schedules.stream()
                .map(schedule -> schedule.getCourseSection() != null ? schedule.getCourseSection().getId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<CourseSection> allSections;
        if (FILTER_SECTION_CODE.equals(filterType)) {
            CourseSection courseSection = getSelectedFilterValue(CourseSection.class);
            allSections = courseSection == null ? List.of() : List.of(courseSection);
        } else if (FILTER_ROOM.equals(filterType)) {
            Room room = getSelectedFilterValue(Room.class);
            allSections = room == null ? List.of() : courseSectionController.getCourseSectionsByRoom(room.getId());
        } else if (FILTER_FACULTY.equals(filterType)) {
            Faculty faculty = getSelectedFilterValue(Faculty.class);
            allSections = faculty == null ? List.of() : courseSectionController.getCourseSectionsByFaculty(faculty.getId());
        } else {
            allSections = courseSectionController.getAllCourseSectionsForSelection();
        }

        for (CourseSection section : allSections) {
            if (!scheduledCourseSectionIds.contains(section.getId())) {
                Schedule dummy = new Schedule();
                dummy.setCourseSection(section);
                schedules.add(dummy);
            }
        }
        return schedules;
    }

    @Override
    protected Object[] toRow(Schedule item) {
        CourseSection courseSection = item.getCourseSection();
        boolean isDummy = item.getId() == null;
        return new Object[]{
                isDummy ? "" : item.getId(),
                courseSection == null ? "" : courseSection.getSectionCode(),
                courseSection == null || courseSection.getSubject() == null ? "" : courseSection.getSubject().getSubjectName(),
                courseSection == null || courseSection.getLecturer() == null ? "" : courseSection.getLecturer().getFullName(),
                isDummy ? "Chưa có lịch" : item.getDayOfWeek(),
                isDummy ? "" : DisplayTextUtil.formatPeriod(item.getStartPeriod(), item.getEndPeriod()),
                item.getRoom() == null ? "" : item.getRoom().getRoomName(),
                item.getNote()
        };
    }

    @Override
    protected String getEmptyStateMessage() {
        return filterReady
                ? "Không tìm thấy lịch học phù hợp với điều kiện lọc hiện tại."
                : "Vui lòng chọn điều kiện lọc để hiển thị danh sách lịch học.";
    }

    @Override
    protected void onSelectionChanged(Schedule selectedItem) {
        if (selectedItem == null) {
            detailSectionPanel.showMessage("Vui lòng chọn lịch học để xem chi tiết.");
            return;
        }

        CourseSection courseSection = selectedItem.getCourseSection();
        boolean isDummy = selectedItem.getId() == null;
        detailSectionPanel.showFields(new String[][]{
                {"Mã học phần", courseSection == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(courseSection.getSectionCode())},
                {"Môn học", courseSection == null || courseSection.getSubject() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(courseSection.getSubject().getSubjectName())},
                {"Giảng viên", courseSection == null || courseSection.getLecturer() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(courseSection.getLecturer().getFullName())},
                {"Thứ học", isDummy ? "Chưa có lịch" : DisplayTextUtil.defaultText(selectedItem.getDayOfWeek())},
                {"Tiết học", isDummy ? "" : DisplayTextUtil.formatPeriod(selectedItem.getStartPeriod(), selectedItem.getEndPeriod())},
                {"Phòng học", selectedItem.getRoom() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(selectedItem.getRoom().getRoomName())},
                {"Ghi chú", DisplayTextUtil.defaultText(selectedItem.getNote())}
        });
    }

    @Override
    protected Schedule promptForEntity(Schedule existingItem) {
        boolean isDummy = existingItem != null && existingItem.getId() == null;

        JComboBox<CourseSection> sectionComboBox = new JComboBox<>(
                courseSectionController.getAllCourseSectionsForSelection().toArray(new CourseSection[0])
        );
        JComboBox<String> dayComboBox = new JComboBox<>(DAYS_OF_WEEK);
        JComboBox<Integer> startPeriodComboBox = new JComboBox<>(PERIOD_OPTIONS);
        JComboBox<Integer> endPeriodComboBox = new JComboBox<>(PERIOD_OPTIONS);
        JComboBox<Room> roomComboBox = new JComboBox<>(roomController.getRoomsForSelection().toArray(new Room[0]));
        JTextField noteField = new JTextField(existingItem == null || isDummy ? "" : existingItem.getNote());

        if (existingItem != null && existingItem.getCourseSection() != null) {
            sectionComboBox.setSelectedItem(existingItem.getCourseSection());
            sectionComboBox.setEnabled(false);
        }
        if (existingItem != null && existingItem.getDayOfWeek() != null && !existingItem.getDayOfWeek().isBlank()) {
            dayComboBox.setSelectedItem(existingItem.getDayOfWeek());
        } else {
            dayComboBox.setSelectedItem(DAYS_OF_WEEK[0]);
        }
        if (existingItem != null && existingItem.getStartPeriod() != null && !isDummy) {
            startPeriodComboBox.setSelectedItem(existingItem.getStartPeriod());
        } else {
            startPeriodComboBox.setSelectedItem(1);
        }
        if (existingItem != null && existingItem.getEndPeriod() != null && !isDummy) {
            endPeriodComboBox.setSelectedItem(existingItem.getEndPeriod());
        } else {
            endPeriodComboBox.setSelectedItem(3);
        }
        if (existingItem != null && existingItem.getRoom() != null && !isDummy) {
            roomComboBox.setSelectedItem(existingItem.getRoom());
        }

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.add(new JLabel("Học phần"));
        formPanel.add(sectionComboBox);
        formPanel.add(new JLabel("Thứ học"));
        formPanel.add(dayComboBox);
        formPanel.add(new JLabel("Tiết bắt đầu"));
        formPanel.add(startPeriodComboBox);
        formPanel.add(new JLabel("Tiết kết thúc"));
        formPanel.add(endPeriodComboBox);
        formPanel.add(new JLabel("Phòng học"));
        formPanel.add(roomComboBox);
        formPanel.add(new JLabel("Ghi chú"));
        formPanel.add(noteField);

        int result = JOptionPane.showConfirmDialog(
                this,
                formPanel,
                "Cập nhật lịch học",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        Integer startPeriod = (Integer) startPeriodComboBox.getSelectedItem();
        Integer endPeriod = (Integer) endPeriodComboBox.getSelectedItem();
        if (startPeriod == null || endPeriod == null || startPeriod >= endPeriod) {
            JOptionPane.showMessageDialog(
                    this,
                    "Tiết bắt đầu phải nhỏ hơn tiết kết thúc.",
                    "Dữ liệu chưa hợp lệ",
                    JOptionPane.WARNING_MESSAGE
            );
            return null;
        }

        Schedule schedule = existingItem == null || isDummy ? new Schedule() : existingItem;
        schedule.setCourseSection((CourseSection) sectionComboBox.getSelectedItem());
        schedule.setDayOfWeek((String) dayComboBox.getSelectedItem());
        schedule.setStartPeriod(startPeriod);
        schedule.setEndPeriod(endPeriod);
        schedule.setRoom((Room) roomComboBox.getSelectedItem());
        schedule.setNote(noteField.getText().trim());
        return schedule;
    }

    @Override
    protected void saveEntity(Schedule item) {
        scheduleController.saveSchedule(item);
    }

    @Override
    protected void deleteEntity(Schedule item) {
        if (item.getId() == null) {
            JOptionPane.showMessageDialog(this, "Học phần này chưa có lịch học để xóa.", "Lỗi xóa", JOptionPane.WARNING_MESSAGE);
            return;
        }
        scheduleController.deleteSchedule(item.getId());
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
                BorderFactory.createTitledBorder("Bộ lọc lịch học"),
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
            filterValueComboBox.addItem(new FilterOption<>("Chọn học phần", null));
            for (CourseSection courseSection : courseSectionController.getAllCourseSectionsForSelection()) {
                filterValueComboBox.addItem(new FilterOption<>(courseSection.getSectionCode(), courseSection));
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
