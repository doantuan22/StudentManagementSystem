package com.qlsv.view.admin;

import com.qlsv.controller.DisplayField;
import com.qlsv.controller.ScheduleManagementScreenController;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Faculty;
import com.qlsv.model.Room;
import com.qlsv.model.Schedule;
import com.qlsv.utils.DialogUtil;
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

    private static final String[] DAYS_OF_WEEK = {
            "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"
    };
    private static final Integer[] PERIOD_OPTIONS = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

    private final ScheduleManagementScreenController screenController = new ScheduleManagementScreenController();

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

        List<DisplayField> detailFields = screenController.buildDetailFields(selectedItem);
        detailSectionPanel.showFields(detailFields.stream()
                .map(field -> new String[]{field.label(), field.value()})
                .toArray(String[][]::new));
    }

    @Override
    protected Schedule promptForEntity(Schedule existingItem) {
        boolean isDummy = existingItem != null && existingItem.getId() == null;

        JComboBox<CourseSection> sectionComboBox = new JComboBox<>(
                screenController.loadCourseSections().toArray(new CourseSection[0])
        );
        JComboBox<String> dayComboBox = new JComboBox<>(DAYS_OF_WEEK);
        JComboBox<Integer> startPeriodComboBox = new JComboBox<>(PERIOD_OPTIONS);
        JComboBox<Integer> endPeriodComboBox = new JComboBox<>(PERIOD_OPTIONS);
        JComboBox<Room> roomComboBox = new JComboBox<>(screenController.loadRooms().toArray(new Room[0]));
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

        return screenController.applyFormData(
                existingItem,
                new ScheduleManagementScreenController.ScheduleFormData(
                        (CourseSection) sectionComboBox.getSelectedItem(),
                        (String) dayComboBox.getSelectedItem(),
                        (Integer) startPeriodComboBox.getSelectedItem(),
                        (Integer) endPeriodComboBox.getSelectedItem(),
                        (Room) roomComboBox.getSelectedItem(),
                        noteField.getText()
                )
        );
    }

    @Override
    protected void saveEntity(Schedule item) {
        screenController.saveSchedule(item);
    }

    @Override
    protected void deleteEntity(Schedule item) {
        screenController.deleteSchedule(item);
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
            for (CourseSection courseSection : screenController.loadCourseSections()) {
                filterValueComboBox.addItem(new FilterOption<>(courseSection.getSectionCode(), courseSection));
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
