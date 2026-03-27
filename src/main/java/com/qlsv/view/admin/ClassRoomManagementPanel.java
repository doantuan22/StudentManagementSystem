package com.qlsv.view.admin;

import com.qlsv.controller.ClassRoomController;
import com.qlsv.controller.FacultyController;
import com.qlsv.model.ClassRoom;
import com.qlsv.model.Faculty;
import com.qlsv.utils.AcademicFormatUtil;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ClassRoomManagementPanel extends AbstractCrudPanel<ClassRoom> {

    private static final String FILTER_NONE = "Chọn điều kiện lọc";
    private static final String FILTER_ALL = "Tất cả lớp";
    private static final String FILTER_FACULTY = "Theo khoa";
    private static final String FILTER_ACADEMIC_YEAR = "Theo niên khóa";

    private final ClassRoomController classRoomController = new ClassRoomController();
    private final FacultyController facultyController = new FacultyController();
    private final JComboBox<String> filterTypeComboBox = new JComboBox<>(
            new String[]{FILTER_NONE, FILTER_ALL, FILTER_FACULTY, FILTER_ACADEMIC_YEAR}
    );
    private final JComboBox<FilterOption<?>> filterValueComboBox = new JComboBox<>();
    private final DetailSectionPanel detailSectionPanel = new DetailSectionPanel(
            "Chi tiết lớp",
            "Vui lòng chọn lớp để xem chi tiết."
    );

    private boolean filterReady;

    public ClassRoomManagementPanel() {
        super("Quản lý lớp");
        setFilterPanel(buildFilterPanel());
        setDetailPanel(detailSectionPanel);
        reloadFilterValues();
        refreshData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Mã lớp", "Tên lớp", "Niên khóa", "Khoa"};
    }

    @Override
    protected List<ClassRoom> loadItems() {
        if (!filterReady) {
            return List.of();
        }

        String filterType = (String) filterTypeComboBox.getSelectedItem();
        return switch (filterType == null ? FILTER_NONE : filterType) {
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

    @Override
    protected Object[] toRow(ClassRoom item) {
        return new Object[]{
                item.getId(),
                item.getClassCode(),
                item.getClassName(),
                AcademicFormatUtil.formatAcademicYear(item.getAcademicYear()),
                item.getFaculty() == null ? "" : item.getFaculty().getFacultyName()
        };
    }

    @Override
    protected String getEmptyStateMessage() {
        return filterReady
                ? "Không tìm thấy lớp phù hợp với điều kiện lọc hiện tại."
                : "Vui lòng chọn điều kiện lọc để hiển thị danh sách lớp.";
    }

    @Override
    protected void onSelectionChanged(ClassRoom selectedItem) {
        if (selectedItem == null) {
            detailSectionPanel.showMessage("Vui lòng chọn lớp để xem chi tiết.");
            return;
        }

        detailSectionPanel.showFields(new String[][]{
                {"Mã lớp", DisplayTextUtil.defaultText(selectedItem.getClassCode())},
                {"Tên lớp", DisplayTextUtil.defaultText(selectedItem.getClassName())},
                {"Niên khóa", DisplayTextUtil.defaultText(AcademicFormatUtil.formatAcademicYear(selectedItem.getAcademicYear()))},
                {"Khoa", selectedItem.getFaculty() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(selectedItem.getFaculty().getFacultyName())}
        });
    }

    @Override
    protected ClassRoom promptForEntity(ClassRoom existingItem) {
        JTextField codeField = new JTextField(existingItem == null ? "" : existingItem.getClassCode());
        JTextField nameField = new JTextField(existingItem == null ? "" : existingItem.getClassName());
        JTextField yearField = new JTextField(existingItem == null ? "" : AcademicFormatUtil.formatAcademicYear(existingItem.getAcademicYear()));
        JComboBox<Faculty> facultyComboBox = new JComboBox<>(facultyController.getFacultiesForSelection().toArray(new Faculty[0]));
        if (existingItem != null && existingItem.getFaculty() != null) {
            facultyComboBox.setSelectedItem(existingItem.getFaculty());
        }

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.add(new JLabel("Mã lớp"));
        formPanel.add(codeField);
        formPanel.add(new JLabel("Tên lớp"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Niên khóa"));
        formPanel.add(yearField);
        formPanel.add(new JLabel("Khoa"));
        formPanel.add(facultyComboBox);

        int result = JOptionPane.showConfirmDialog(
                this,
                formPanel,
                existingItem == null ? "Thêm lớp" : "Cập nhật lớp",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        ClassRoom classRoom = existingItem == null ? new ClassRoom() : existingItem;
        classRoom.setClassCode(codeField.getText().trim());
        classRoom.setClassName(nameField.getText().trim());
        classRoom.setAcademicYear(AcademicFormatUtil.normalizeAcademicYear(yearField.getText(), "Niên khóa"));
        classRoom.setFaculty((Faculty) facultyComboBox.getSelectedItem());
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

    private void reloadFilterValues() {
        filterReady = false;
        filterValueComboBox.removeAllItems();

        String filterType = (String) filterTypeComboBox.getSelectedItem();
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
