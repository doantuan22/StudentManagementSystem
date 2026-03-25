package com.qlsv.view.admin;

import com.qlsv.controller.CourseSectionController;
import com.qlsv.controller.FacultyController;
import com.qlsv.controller.ScheduleController;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Faculty;
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

public class ScheduleManagementPanel extends AbstractCrudPanel<Schedule> {

    private static final String FILTER_NONE = "Chọn điều kiện lọc";
    private static final String FILTER_ALL = "Tất cả lịch học";
    private static final String FILTER_SECTION_CODE = "Theo mã học phần";
    private static final String FILTER_ROOM = "Theo phòng học";
    private static final String FILTER_FACULTY = "Theo khoa";

    private final ScheduleController scheduleController = new ScheduleController();
    private final CourseSectionController courseSectionController = new CourseSectionController();
    private final FacultyController facultyController = new FacultyController();

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
        return switch (filterType == null ? FILTER_NONE : filterType) {
            case FILTER_ALL -> scheduleController.getAllSchedules();
            case FILTER_SECTION_CODE -> {
                CourseSection courseSection = getSelectedFilterValue(CourseSection.class);
                yield courseSection == null ? List.of() : scheduleController.getSchedulesByCourseSection(courseSection.getId());
            }
            case FILTER_ROOM -> {
                String room = getSelectedFilterValue(String.class);
                yield room == null ? List.of() : scheduleController.getSchedulesByRoom(room);
            }
            case FILTER_FACULTY -> {
                Faculty faculty = getSelectedFilterValue(Faculty.class);
                yield faculty == null ? List.of() : scheduleController.getSchedulesByFaculty(faculty.getId());
            }
            default -> List.of();
        };
    }

    @Override
    protected Object[] toRow(Schedule item) {
        CourseSection courseSection = item.getCourseSection();
        return new Object[]{
                item.getId(),
                courseSection == null ? "" : courseSection.getSectionCode(),
                courseSection == null || courseSection.getSubject() == null ? "" : courseSection.getSubject().getSubjectName(),
                courseSection == null || courseSection.getLecturer() == null ? "" : courseSection.getLecturer().getFullName(),
                item.getDayOfWeek(),
                DisplayTextUtil.formatPeriod(item.getStartPeriod(), item.getEndPeriod()),
                item.getRoom(),
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
        detailSectionPanel.showFields(new String[][]{
                {"Mã học phần", courseSection == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(courseSection.getSectionCode())},
                {"Môn học", courseSection == null || courseSection.getSubject() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(courseSection.getSubject().getSubjectName())},
                {"Giảng viên", courseSection == null || courseSection.getLecturer() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(courseSection.getLecturer().getFullName())},
                {"Thứ học", DisplayTextUtil.defaultText(selectedItem.getDayOfWeek())},
                {"Tiết học", DisplayTextUtil.formatPeriod(selectedItem.getStartPeriod(), selectedItem.getEndPeriod())},
                {"Phòng học", DisplayTextUtil.defaultText(selectedItem.getRoom())},
                {"Ghi chú", DisplayTextUtil.defaultText(selectedItem.getNote())}
        });
    }

    @Override
    protected Schedule promptForEntity(Schedule existingItem) {
        JComboBox<CourseSection> sectionComboBox = new JComboBox<>(
                courseSectionController.getAllCourseSectionsForSelection().toArray(new CourseSection[0]));
        JTextField dayField = new JTextField(existingItem == null ? "Thứ 2" : existingItem.getDayOfWeek());
        JTextField startPeriodField = new JTextField(existingItem == null ? "1" : String.valueOf(existingItem.getStartPeriod()));
        JTextField endPeriodField = new JTextField(existingItem == null ? "3" : String.valueOf(existingItem.getEndPeriod()));
        JTextField roomField = new JTextField(existingItem == null ? "" : existingItem.getRoom());
        JTextField noteField = new JTextField(existingItem == null ? "" : existingItem.getNote());

        if (existingItem != null && existingItem.getCourseSection() != null) {
            sectionComboBox.setSelectedItem(existingItem.getCourseSection());
        }

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.add(new JLabel("Học phần"));
        formPanel.add(sectionComboBox);
        formPanel.add(new JLabel("Thứ học"));
        formPanel.add(dayField);
        formPanel.add(new JLabel("Tiết bắt đầu"));
        formPanel.add(startPeriodField);
        formPanel.add(new JLabel("Tiết kết thúc"));
        formPanel.add(endPeriodField);
        formPanel.add(new JLabel("Phòng học"));
        formPanel.add(roomField);
        formPanel.add(new JLabel("Ghi chú"));
        formPanel.add(noteField);

        int result = JOptionPane.showConfirmDialog(
                this,
                formPanel,
                existingItem == null ? "Thêm lịch học" : "Cập nhật lịch học",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        Schedule schedule = existingItem == null ? new Schedule() : existingItem;
        schedule.setCourseSection((CourseSection) sectionComboBox.getSelectedItem());
        schedule.setDayOfWeek(dayField.getText().trim());
        schedule.setStartPeriod(Integer.parseInt(startPeriodField.getText().trim()));
        schedule.setEndPeriod(Integer.parseInt(endPeriodField.getText().trim()));
        schedule.setRoom(roomField.getText().trim());
        schedule.setNote(noteField.getText().trim());
        return schedule;
    }

    @Override
    protected void saveEntity(Schedule item) {
        scheduleController.saveSchedule(item);
    }

    @Override
    protected void deleteEntity(Schedule item) {
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
            scheduleController.getAllSchedules().stream()
                    .map(Schedule::getRoom)
                    .filter(room -> room != null && !room.isBlank())
                    .distinct()
                    .forEach(room -> filterValueComboBox.addItem(new FilterOption<>(room, room)));
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
