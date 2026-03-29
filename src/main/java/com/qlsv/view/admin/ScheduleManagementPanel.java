/**
 * Màn hình quản trị cho quản lý lịch.
 */
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
import com.qlsv.view.dialog.BaseDetailDialog;
import com.qlsv.view.dialog.ScheduleDetailDialog;
import com.qlsv.view.dialog.ScheduleFormDialog;

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

    /**
     * Khởi tạo quản lý lịch.
     */
    public ScheduleManagementPanel() {
        super("Quản lý lịch học");
        configureActionButtons(false, true, false, false);
        filterTypeComboBox.setSelectedItem(FILTER_ALL);
        setFilterPanel(buildFilterPanel());
        setDetailPanel(detailSectionPanel);
        reloadFilterValues();
        refreshData();
    }

    /**
     * Trả về column names.
     */
    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Học phần", "Môn học", "Học kỳ", "Năm học", "Giảng viên", "Thứ", "Tiết", "Phòng", "Ghi chú"};
    }

    /**
     * Nạp items.
     */
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

    /**
     * Xử lý to row.
     */
    @Override
    protected Object[] toRow(Schedule item) {
        CourseSection courseSection = item.getCourseSection();
        boolean isDummy = item.getId() == null;
        return new Object[]{
                isDummy ? "" : item.getId(),
                courseSection == null ? "" : courseSection.getSectionCode(),
                courseSection == null || courseSection.getSubject() == null ? "" : courseSection.getSubject().getSubjectName(),
                courseSection == null ? "" : DisplayTextUtil.defaultText(courseSection.getSemester()),
                courseSection == null ? "" : DisplayTextUtil.defaultText(courseSection.getSchoolYear()),
                courseSection == null || courseSection.getLecturer() == null ? "" : courseSection.getLecturer().getFullName(),
                isDummy ? "Chưa có lịch" : item.getDayOfWeek(),
                isDummy ? "" : DisplayTextUtil.formatPeriod(item.getStartPeriod(), item.getEndPeriod()),
                item.getRoom() == null ? "" : item.getRoom().getRoomName(),
                item.getNote()
        };
    }

    /**
     * Trả về trạng thái trống thông báo.
     */
    @Override
    protected String getEmptyStateMessage() {
        return filterReady
                ? "Không tìm thấy lịch học phù hợp với điều kiện lọc hiện tại."
                : "Vui lòng chọn điều kiện lọc để hiển thị danh sách lịch học.";
    }

    /**
     * Xử lý on selection changed.
     */
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

    /**
     * Tạo hộp thoại chi tiết.
     */
    @Override
    protected BaseDetailDialog createDetailDialog(javax.swing.JComponent detailPanel) {
        return new ScheduleDetailDialog(detailPanel);
    }

    /**
     * Xử lý prompt for entity.
     */
    @Override
    protected Schedule promptForEntity(Schedule existingItem) {
        boolean isDummy = existingItem != null && existingItem.getId() == null;

        ScheduleFormDialog.ScheduleFormResult formResult = ScheduleFormDialog.showDialog(
                this,
                new ScheduleFormDialog.ScheduleFormModel(
                        "Cập nhật lịch học",
                        screenController.loadCourseSections(),
                        existingItem == null ? null : existingItem.getCourseSection(),
                        existingItem == null || existingItem.getCourseSection() == null,
                        DAYS_OF_WEEK,
                        existingItem != null && existingItem.getDayOfWeek() != null && !existingItem.getDayOfWeek().isBlank()
                                ? existingItem.getDayOfWeek()
                                : DAYS_OF_WEEK[0],
                        PERIOD_OPTIONS,
                        existingItem != null && existingItem.getStartPeriod() != null && !isDummy ? existingItem.getStartPeriod() : 1,
                        existingItem != null && existingItem.getEndPeriod() != null && !isDummy ? existingItem.getEndPeriod() : 3,
                        screenController.loadRooms(),
                        existingItem != null && !isDummy ? existingItem.getRoom() : null,
                        existingItem == null || isDummy ? "" : existingItem.getNote()
                )
        );
        if (formResult == null) {
            return null;
        }

        return screenController.applyFormData(
                existingItem,
                new ScheduleManagementScreenController.ScheduleFormData(
                        formResult.courseSection(),
                        formResult.dayOfWeek(),
                        formResult.startPeriod(),
                        formResult.endPeriod(),
                        formResult.room(),
                        formResult.note()
                )
        );
    }

    /**
     * Lưu entity.
     */
    @Override
    protected void saveEntity(Schedule item) {
        screenController.saveSchedule(item);
    }

    /**
     * Xóa entity.
     */
    @Override
    protected void deleteEntity(Schedule item) {
        screenController.deleteSchedule(item);
    }

    /**
     * Tạo panel lọc.
     */
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

    /**
     * Làm mới lọc values bất đồng bộ.
     */
    private void reloadFilterValues() {
        String filterType = (String) filterTypeComboBox.getSelectedItem();
        filterReady = FILTER_ALL.equals(filterType);
        filterValueComboBox.removeAllItems();

        if (FILTER_NONE.equals(filterType) || FILTER_ALL.equals(filterType)) {
            filterValueComboBox.setEnabled(false);
            return;
        }

        setLoadingState(true);
        new javax.swing.SwingWorker<List<?>, Void>() {
            @Override
            protected List<?> doInBackground() {
                if (FILTER_SECTION_CODE.equals(filterType)) return screenController.loadCourseSections();
                if (FILTER_ROOM.equals(filterType)) return screenController.loadRooms();
                if (FILTER_FACULTY.equals(filterType)) return screenController.loadFaculties();
                return List.of();
            }

            @Override
            protected void done() {
                try {
                    List<?> results = get();
                    filterValueComboBox.setEnabled(true);
                    if (FILTER_SECTION_CODE.equals(filterType)) {
                        filterValueComboBox.addItem(new FilterOption<>("Chọn học phần", null));
                        for (Object obj : results) {
                            CourseSection section = (CourseSection) obj;
                            filterValueComboBox.addItem(new FilterOption<>(section.getSectionCode(), section));
                        }
                    } else if (FILTER_ROOM.equals(filterType)) {
                        filterValueComboBox.addItem(new FilterOption<>("Chọn phòng học", null));
                        for (Object obj : results) {
                            Room room = (Room) obj;
                            filterValueComboBox.addItem(new FilterOption<>(room.getRoomName(), room));
                        }
                    } else if (FILTER_FACULTY.equals(filterType)) {
                        filterValueComboBox.addItem(new FilterOption<>("Chọn khoa", null));
                        for (Object obj : results) {
                            Faculty faculty = (Faculty) obj;
                            filterValueComboBox.addItem(new FilterOption<>(faculty.getFacultyCode() + " - " + faculty.getFacultyName(), faculty));
                        }
                    }
                } catch (Exception exception) {
                    DialogUtil.showError(ScheduleManagementPanel.this, "Lỗi khi tải danh mục: " + exception.getMessage());
                } finally {
                    setLoadingState(false);
                }
            }
        }.execute();
    }

    /**
     * Xử lý reset lọc.
     */
    private void resetFilter() {
        filterTypeComboBox.setSelectedItem(FILTER_ALL);
        reloadFilterValues();
        refreshData();
    }

    /**
     * Kiểm tra valid lọc selection.
     */
    private boolean hasValidFilterSelection() {
        String filterType = (String) filterTypeComboBox.getSelectedItem();
        if (FILTER_ALL.equals(filterType)) {
            return true;
        }
        FilterOption<?> selectedOption = (FilterOption<?>) filterValueComboBox.getSelectedItem();
        return selectedOption != null && selectedOption.value() != null;
    }

    /**
     * Trả về lọc value đã chọn.
     */
    private <T> T getSelectedFilterValue(Class<T> type) {
        FilterOption<?> selectedOption = (FilterOption<?>) filterValueComboBox.getSelectedItem();
        if (selectedOption == null || selectedOption.value() == null || !type.isInstance(selectedOption.value())) {
            return null;
        }
        return type.cast(selectedOption.value());
    }
}
