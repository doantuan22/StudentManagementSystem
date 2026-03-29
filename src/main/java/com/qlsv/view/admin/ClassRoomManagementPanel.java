package com.qlsv.view.admin;

import com.qlsv.controller.ClassRoomController;
import com.qlsv.controller.FacultyController;
import com.qlsv.controller.StudentController;
import com.qlsv.model.ClassRoom;
import com.qlsv.model.Faculty;
import com.qlsv.utils.AcademicFormatUtil;
import com.qlsv.view.common.AbstractCrudPanel;
import com.qlsv.view.common.FilterOption;
import com.qlsv.view.dialog.ClassRoomFormDialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassRoomManagementPanel extends AbstractCrudPanel<ClassRoom> {

    private static final String FILTER_NONE = "Chọn điều kiện lọc";
    private static final String FILTER_ALL = "Tất cả lớp";
    private static final String FILTER_FACULTY = "Theo khoa";
    private static final String FILTER_ACADEMIC_YEAR = "Theo niên khóa";

    private final ClassRoomController classRoomController = new ClassRoomController();
    private final FacultyController facultyController = new FacultyController();
    private final StudentController studentController = new StudentController();
    private final JComboBox<String> filterTypeComboBox = new JComboBox<>(
            new String[]{FILTER_NONE, FILTER_ALL, FILTER_FACULTY, FILTER_ACADEMIC_YEAR}
    );
    private final JComboBox<FilterOption<?>> filterValueComboBox = new JComboBox<>();
    private final Map<Long, Integer> studentCountsByClassRoomId = new HashMap<>();

    private boolean filterReady;

    public ClassRoomManagementPanel() {
        super("Quản lý lớp");
        filterTypeComboBox.setSelectedItem(FILTER_ALL);
        setFilterPanel(buildFilterPanel());
        reloadFilterValues();
        refreshData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Mã lớp", "Tên lớp", "Niên khóa", "Khoa", "Sĩ số sinh viên"};
    }

    @Override
    protected List<ClassRoom> loadItems() {
        List<ClassRoom> classRooms;
        if (!filterReady) {
            classRooms = List.of();
        } else {
            String filterType = (String) filterTypeComboBox.getSelectedItem();
            classRooms = switch (filterType == null ? FILTER_NONE : filterType) {
                case FILTER_ALL -> classRoomController.getAllClassRooms();
                case FILTER_FACULTY -> {
                    Faculty faculty = getSelectedFilterValue(Faculty.class);
                    yield faculty == null ? List.of() : classRoomController.getClassRoomsByFaculty(faculty.getId());
                }
                case FILTER_ACADEMIC_YEAR -> {
                    String academicYear = getSelectedFilterValue(String.class);
                    yield academicYear == null ? List.of() : classRoomController.getClassRoomsByAcademicYear(academicYear);
                }
                default -> List.of();
            };
        }

        updateStudentCounts(classRooms);
        return classRooms;
    }

    @Override
    protected Object[] toRow(ClassRoom item) {
        return new Object[]{
                item.getId(),
                item.getClassCode(),
                item.getClassName(),
                AcademicFormatUtil.formatAcademicYear(item.getAcademicYear()),
                item.getFaculty() == null ? "" : item.getFaculty().getFacultyName(),
                studentCountsByClassRoomId.getOrDefault(item.getId(), 0)
        };
    }

    @Override
    protected String getEmptyStateMessage() {
        return filterReady
                ? "Không tìm thấy lớp phù hợp với điều kiện lọc hiện tại."
                : "Vui lòng chọn điều kiện lọc để hiển thị danh sách lớp.";
    }

    @Override
    protected ClassRoom promptForEntity(ClassRoom existingItem) {
        ClassRoomFormDialog.ClassRoomFormResult formResult = ClassRoomFormDialog.showDialog(
                this,
                new ClassRoomFormDialog.ClassRoomFormModel(
                        existingItem == null ? "Thêm lớp" : "Cập nhật lớp",
                        existingItem == null ? "" : existingItem.getClassCode(),
                        existingItem == null ? "" : existingItem.getClassName(),
                        existingItem == null ? "" : AcademicFormatUtil.formatAcademicYear(existingItem.getAcademicYear()),
                        facultyController.getFacultiesForSelection(),
                        existingItem == null ? null : existingItem.getFaculty()
                )
        );
        if (formResult == null) {
            return null;
        }

        ClassRoom classRoom = existingItem == null ? new ClassRoom() : existingItem;
        classRoom.setClassCode(formResult.classCode().trim());
        classRoom.setClassName(formResult.className().trim());
        classRoom.setAcademicYear(AcademicFormatUtil.normalizeAcademicYear(formResult.academicYear(), "Niên khóa"));
        classRoom.setFaculty(formResult.faculty());
        return classRoom;
    }

    @Override
    protected void saveEntity(ClassRoom item) {
        classRoomController.saveClassRoom(item);
    }

    @Override
    protected void deleteEntity(ClassRoom item) {
        classRoomController.deleteClassRoom(item.getId());
    }

    /**
     * Cập nhật số lượng sinh viên hiện có cho từng lớp học trong danh sách hiển thị.
     */
    private void updateStudentCounts(List<ClassRoom> classRooms) {
        studentCountsByClassRoomId.clear();
        if (classRooms.isEmpty()) {
            return;
        }

        Set<Long> classRoomIds = new LinkedHashSet<>();
        for (ClassRoom classRoom : classRooms) {
            if (classRoom != null && classRoom.getId() != null) {
                classRoomIds.add(classRoom.getId());
            }
        }

        for (var student : studentController.getAllStudents()) {
            if (student.getClassRoom() == null || student.getClassRoom().getId() == null) {
                continue;
            }
            Long classRoomId = student.getClassRoom().getId();
            if (classRoomIds.contains(classRoomId)) {
                studentCountsByClassRoomId.merge(classRoomId, 1, Integer::sum);
            }
        }
    }

    /**
     * Xây dựng giao diện thanh công cụ lọc danh sách lớp học.
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
                BorderFactory.createTitledBorder("Bộ lọc lớp"),
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
     * Tải lại các giá trị lọc (khoa, niên khóa) vào ComboBox dựa trên loại lọc đã chọn.
     */
    private void reloadFilterValues() {
        String filterType = (String) filterTypeComboBox.getSelectedItem();
        filterReady = FILTER_ALL.equals(filterType);
        filterValueComboBox.removeAllItems();
        if (FILTER_FACULTY.equals(filterType)) {
            filterValueComboBox.setEnabled(true);
            filterValueComboBox.addItem(new FilterOption<>("Chọn khoa", null));
            for (Faculty faculty : facultyController.getFacultiesForSelection()) {
                filterValueComboBox.addItem(new FilterOption<>(faculty.getFacultyCode() + " - " + faculty.getFacultyName(), faculty));
            }
            return;
        }

        if (FILTER_ACADEMIC_YEAR.equals(filterType)) {
            filterValueComboBox.setEnabled(true);
            filterValueComboBox.addItem(new FilterOption<>("Chọn niên khóa", null));
            Set<String> academicYears = new LinkedHashSet<>();
            for (ClassRoom classRoom : classRoomController.getClassRoomsForSelection()) {
                String academicYear = AcademicFormatUtil.formatAcademicYear(classRoom.getAcademicYear());
                if (!academicYear.isBlank()) {
                    academicYears.add(academicYear);
                }
            }
            for (String academicYear : academicYears) {
                filterValueComboBox.addItem(new FilterOption<>(academicYear, academicYear));
            }
            return;
        }

        filterValueComboBox.setEnabled(false);
    }

    /**
     * Đưa bộ lọc về trạng thái mặc định (hiển thị tất cả).
     */
    private void resetFilter() {
        filterTypeComboBox.setSelectedItem(FILTER_ALL);
        reloadFilterValues();
        refreshData();
    }

    /**
     * Kiểm tra xem người dùng đã chọn giá trị lọc hợp lệ hay chưa.
     */
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
