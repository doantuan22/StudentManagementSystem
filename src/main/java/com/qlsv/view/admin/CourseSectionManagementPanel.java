/**
 * Màn hình quản trị cho quản lý học phần.
 */
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
import com.qlsv.view.dialog.BaseDetailDialog;
import com.qlsv.view.dialog.CourseSectionDetailDialog;
import com.qlsv.view.dialog.CourseSectionFormDialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.FlowLayout;
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

    /**
     * Khởi tạo quản lý học phần.
     */
    public CourseSectionManagementPanel() {
        super("Quan ly hoc phan");
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
        return new String[]{"ID", "Mã học phần", "ôn học", "Giảng viên", "ọc kỳ", "ăm học", "ịch học"};
    }

    /**
     * Tải danh sách học phần dựa trên các điều kiện lọc đang được áp dụng.
     */
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

    /**
     * Chuyển đổi dữ liệu học phần sang mảng đối tượng để hiển thị trên từng dòng của bảng.
     */
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

    /**
     * Trả về trạng thái trống thông báo.
     */
    @Override
    protected String getEmptyStateMessage() {
        return filterReady
                ? "Không tìm thấy học phần phù hợp với điều kiện lọc hiện tại."
                : "Vui lòng chọn điều kiện lọc để hiển thị danh sách học phần.";
    }

    /**
     * Xử lý hiển thị thông tin chi tiết của học phần khi người dùng chọn một dòng trên bảng.
     */
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

    /**
     * Tạo hộp thoại chi tiết.
     */
    @Override
    protected BaseDetailDialog createDetailDialog(javax.swing.JComponent detailPanel) {
        return new CourseSectionDetailDialog(detailPanel);
    }

    /**
     * Hiển thị hộp thoại để người dùng nhập thông tin học phần mới hoặc cập nhật học phần hiện có.
     */
    @Override
    protected CourseSection promptForEntity(CourseSection existingItem) {
        List<Subject> subjects = screenController.loadSubjects();
        List<Lecturer> lecturers = screenController.loadLecturers();

        CourseSectionFormDialog.CourseSectionFormResult formResult = CourseSectionFormDialog.showDialog(
                this,
                new CourseSectionFormDialog.CourseSectionFormModel(
                        existingItem == null ? "Thêm học phần" : "Cập nhật học phần",
                        existingItem == null ? "" : existingItem.getSectionCode(),
                        subjects,
                        existingItem == null ? null : existingItem.getSubject(),
                        lecturers,
                        existingItem == null ? null : existingItem.getLecturer(),
                        screenController.loadLecturersBySubject(subjects),
                        AcademicFormatUtil.getFixedSemesters(),
                        existingItem == null ? "HK1" : AcademicFormatUtil.formatSemester(existingItem.getSemester()),
                        existingItem == null ? "2025 - 2026" : AcademicFormatUtil.formatAcademicYear(existingItem.getSchoolYear()),
                        existingItem == null || existingItem.getMaxStudents() == null ? "50" : String.valueOf(existingItem.getMaxStudents()),
                       "Lịch học và phòng học được quản lý tại màn hình lịch học sau khi tạo học phần."
                )
        );
        if (formResult == null) {
            return null;
        }

        return screenController.applyFormData(
                existingItem,
                new CourseSectionManagementScreenController.CourseSectionFormData(
                        formResult.sectionCode(),
                        formResult.subject(),
                        formResult.lecturer(),
                        formResult.semester(),
                        formResult.schoolYear(),
                        formResult.maxStudents()
                )
        );
    }

    /**
     * Thực hiện lưu thông tin học phần vào cơ sở dữ liệu.
     */
    @Override
    protected void saveEntity(CourseSection item) {
        screenController.saveCourseSection(item);
    }

    /**
     * Thực hiện xóa học phần được chọn khỏi hệ thống.
     */
    @Override
    protected void deleteEntity(CourseSection item) {
        screenController.deleteCourseSection(item);
    }

    /**
     * Khởi tạo giao diện cho thanh công cụ lọc học phần phía trên bảng dữ liệu.
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

    /**
     * Cập nhật danh sách các giá trị lọc (phòng học, khoa, mã học phần) dựa trên loại bộ lọc đã chọn.
     */
    private void reloadFilterValues() {
        String filterType = (String) filterTypeComboBox.getSelectedItem();
        filterReady = FILTER_ALL.equals(filterType);
        filterValueComboBox.removeAllItems();
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

    /**
     * Đưa tất cả các điều kiện lọc về trạng thái mặc định.
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
