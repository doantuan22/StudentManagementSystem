package com.qlsv.view.admin;

import com.qlsv.controller.ClassRoomController;
import com.qlsv.controller.FacultyController;
import com.qlsv.controller.StudentController;
import com.qlsv.model.ClassRoom;
import com.qlsv.model.Faculty;
import com.qlsv.model.Student;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.view.common.AbstractCrudPanel;
import com.qlsv.view.common.DetailSectionPanel;
import com.qlsv.view.common.FilterOption;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;

public class StudentManagementPanel extends AbstractCrudPanel<Student> {

    private static final String FILTER_NONE = "Chọn điều kiện lọc";
    private static final String FILTER_ALL = "Tất cả sinh viên";
    private static final String FILTER_FACULTY = "Theo khoa";
    private static final String FILTER_CLASS_ROOM = "Theo lớp";
    private static final String FILTER_ACADEMIC_YEAR = "Theo niên khóa";

    private final StudentController studentController = new StudentController();
    private final FacultyController facultyController = new FacultyController();
    private final ClassRoomController classRoomController = new ClassRoomController();

    private final JComboBox<String> filterTypeComboBox = new JComboBox<>(
            new String[]{FILTER_NONE, FILTER_ALL, FILTER_FACULTY, FILTER_CLASS_ROOM, FILTER_ACADEMIC_YEAR}
    );
    private final JComboBox<FilterOption<?>> filterValueComboBox = new JComboBox<>();
    private final DetailSectionPanel detailSectionPanel = new DetailSectionPanel(
            "Chi tiết thông tin sinh viên",
            "Vui lòng chọn sinh viên để xem chi tiết."
    );

    private boolean filterReady;

    public StudentManagementPanel() {
        super("Quản lý sinh viên");
        setFilterPanel(buildFilterPanel());
        setDetailPanel(detailSectionPanel);
        reloadFilterValues();
        refreshData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Mã sinh viên", "Họ và tên", "Email", "Lớp", "Khoa", "Niên khóa", "Trạng thái"};
    }

    @Override
    public void reloadData() {
        refreshData();
    }

    @Override
    public void setVisible(boolean visible) {
        boolean wasVisible = isVisible();
        super.setVisible(visible);
        if (visible && !wasVisible) {
            reloadData();
        }
    }

    @Override
    protected List<Student> loadItems() {
        if (!filterReady) {
            return List.of();
        }

        String filterType = (String) filterTypeComboBox.getSelectedItem();
        return switch (filterType == null ? FILTER_NONE : filterType) {
            case FILTER_ALL -> studentController.getAllStudents();
            case FILTER_FACULTY -> {
                Faculty faculty = getSelectedFilterValue(Faculty.class);
                yield faculty == null ? List.of() : studentController.getStudentsByFaculty(faculty.getId());
            }
            case FILTER_CLASS_ROOM -> {
                ClassRoom classRoom = getSelectedFilterValue(ClassRoom.class);
                yield classRoom == null ? List.of() : studentController.getStudentsByClassRoom(classRoom.getId());
            }
            case FILTER_ACADEMIC_YEAR -> {
                String academicYear = getSelectedFilterValue(String.class);
                yield academicYear == null ? List.of() : studentController.getStudentsByAcademicYear(academicYear);
            }
            default -> List.of();
        };
    }

    @Override
    protected Object[] toRow(Student item) {
        return new Object[]{
                item.getId(),
                item.getStudentCode(),
                item.getFullName(),
                item.getEmail(),
                item.getClassRoom() == null ? "" : item.getClassRoom().getClassName(),
                item.getFaculty() == null ? "" : item.getFaculty().getFacultyName(),
                item.getAcademicYear(),
                DisplayTextUtil.formatStatus(item.getStatus())
        };
    }

    @Override
    protected String getEmptyStateMessage() {
        return filterReady
                ? "Không tìm thấy sinh viên phù hợp với điều kiện lọc hiện tại."
                : "Vui lòng chọn điều kiện lọc để hiển thị danh sách sinh viên.";
    }

    @Override
    protected void onSelectionChanged(Student selectedItem) {
        if (selectedItem == null) {
            detailSectionPanel.showMessage("Vui lòng chọn sinh viên để xem chi tiết.");
            return;
        }

        Student displayStudent = selectedItem;
        if (selectedItem.getId() != null) {
            try {
                displayStudent = studentController.getStudentById(selectedItem.getId());
                selectedItem.setEmail(displayStudent.getEmail());
                selectedItem.setPhone(displayStudent.getPhone());
                selectedItem.setAddress(displayStudent.getAddress());

                int selectedRow = getTable().getSelectedRow();
                if (selectedRow >= 0) {
                    getTable().setValueAt(displayStudent.getEmail(), selectedRow, 3);
                }
            } catch (Exception ignored) {
                displayStudent = selectedItem;
            }
        }

        detailSectionPanel.showFields(new String[][]{
                {"Mã sinh viên", DisplayTextUtil.defaultText(displayStudent.getStudentCode())},
                {"Họ và tên", DisplayTextUtil.defaultText(displayStudent.getFullName())},
                {"Giới tính", DisplayTextUtil.formatGender(displayStudent.getGender())},
                {"Ngày sinh", DisplayTextUtil.formatDate(displayStudent.getDateOfBirth())},
                {"Số điện thoại", DisplayTextUtil.defaultText(displayStudent.getPhone())},
                {"Email", DisplayTextUtil.defaultText(displayStudent.getEmail())},
                {"Khoa", displayStudent.getFaculty() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(displayStudent.getFaculty().getFacultyName())},
                {"Lớp", displayStudent.getClassRoom() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(displayStudent.getClassRoom().getClassName())},
                {"Niên khóa", DisplayTextUtil.defaultText(displayStudent.getAcademicYear())},
                {"Trạng thái", DisplayTextUtil.formatStatus(displayStudent.getStatus())},
                {"Địa chỉ", DisplayTextUtil.defaultText(displayStudent.getAddress())},
                {"ID người dùng", DisplayTextUtil.formatUserReference(displayStudent.getUserId())}
        });
    }

    @Override
    protected Student promptForEntity(Student existingItem) {
        JTextField codeField = new JTextField(existingItem == null ? "" : existingItem.getStudentCode());
        JTextField nameField = new JTextField(existingItem == null ? "" : existingItem.getFullName());
        JComboBox<String> genderComboBox = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        JTextField birthField = new JTextField(existingItem == null || existingItem.getDateOfBirth() == null ? "" : existingItem.getDateOfBirth().toString());
        JTextField emailField = new JTextField(existingItem == null ? "" : existingItem.getEmail());
        JTextField phoneField = new JTextField(existingItem == null ? "" : existingItem.getPhone());
        JTextField addressField = new JTextField(existingItem == null ? "" : existingItem.getAddress());
        JTextField academicYearField = new JTextField(existingItem == null ? "" : existingItem.getAcademicYear());

        JComboBox<Faculty> facultyComboBox = new JComboBox<>(facultyController.getFacultiesForSelection().toArray(new Faculty[0]));
        JComboBox<ClassRoom> classRoomComboBox = new JComboBox<>();
        JComboBox<FilterOption<String>> statusComboBox = new JComboBox<>(new DefaultComboBoxModel<>(new FilterOption[]{
                new FilterOption<>("Đang hoạt động", "ACTIVE"),
                new FilterOption<>("Ngừng hoạt động", "INACTIVE")
        }));

        List<ClassRoom> allClassRooms = classRoomController.getClassRoomsForSelection();

        if (existingItem != null) {
            genderComboBox.setSelectedItem(DisplayTextUtil.formatGender(existingItem.getGender()));
            if (existingItem.getFaculty() != null) {
                facultyComboBox.setSelectedItem(existingItem.getFaculty());
            }
        }

        facultyComboBox.addActionListener(event -> reloadClassRoomOptions(
                classRoomComboBox,
                allClassRooms,
                (Faculty) facultyComboBox.getSelectedItem(),
                null
        ));
        reloadClassRoomOptions(classRoomComboBox, allClassRooms, (Faculty) facultyComboBox.getSelectedItem(),
                existingItem == null ? null : existingItem.getClassRoom());

        if (existingItem != null) {
            selectStatus(statusComboBox, existingItem.getStatus());
        } else {
            selectStatus(statusComboBox, "ACTIVE");
        }

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.add(new JLabel("Mã sinh viên"));
        formPanel.add(codeField);
        formPanel.add(new JLabel("Họ và tên"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Giới tính"));
        formPanel.add(genderComboBox);
        formPanel.add(new JLabel("Ngày sinh (yyyy-MM-dd)"));
        formPanel.add(birthField);
        formPanel.add(new JLabel("Email"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Số điện thoại"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Địa chỉ"));
        formPanel.add(addressField);
        formPanel.add(new JLabel("Khoa"));
        formPanel.add(facultyComboBox);
        formPanel.add(new JLabel("Lớp"));
        formPanel.add(classRoomComboBox);
        formPanel.add(new JLabel("Niên khóa"));
        formPanel.add(academicYearField);
        formPanel.add(new JLabel("Trạng thái"));
        formPanel.add(statusComboBox);

        int result = JOptionPane.showConfirmDialog(
                this,
                formPanel,
                existingItem == null ? "Thêm sinh viên" : "Cập nhật sinh viên",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        Student student = existingItem == null ? new Student() : existingItem;
        student.setStudentCode(codeField.getText().trim());
        student.setFullName(nameField.getText().trim());
        student.setGender((String) genderComboBox.getSelectedItem());
        student.setDateOfBirth(birthField.getText().isBlank() ? null : LocalDate.parse(birthField.getText().trim()));
        student.setEmail(emailField.getText().trim());
        student.setPhone(phoneField.getText().trim());
        student.setAddress(addressField.getText().trim());
        student.setFaculty((Faculty) facultyComboBox.getSelectedItem());
        student.setClassRoom((ClassRoom) classRoomComboBox.getSelectedItem());
        student.setAcademicYear(academicYearField.getText().trim());
        FilterOption<String> selectedStatus = (FilterOption<String>) statusComboBox.getSelectedItem();
        student.setStatus(selectedStatus == null ? "ACTIVE" : selectedStatus.value());
        return student;
    }

    @Override
    protected void saveEntity(Student item) {
        studentController.saveStudent(item);
    }

    @Override
    protected void deleteEntity(Student item) {
        studentController.deleteStudent(item.getId());
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
                BorderFactory.createTitledBorder("Bộ lọc sinh viên"),
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
        boolean requiresValue = FILTER_FACULTY.equals(filterType)
                || FILTER_CLASS_ROOM.equals(filterType)
                || FILTER_ACADEMIC_YEAR.equals(filterType);
        filterValueComboBox.setEnabled(requiresValue);

        if (FILTER_FACULTY.equals(filterType)) {
            filterValueComboBox.addItem(new FilterOption<>("Chọn khoa", null));
            for (Faculty faculty : facultyController.getFacultiesForSelection()) {
                filterValueComboBox.addItem(new FilterOption<>(faculty.getFacultyCode() + " - " + faculty.getFacultyName(), faculty));
            }
        } else if (FILTER_CLASS_ROOM.equals(filterType)) {
            filterValueComboBox.addItem(new FilterOption<>("Chọn lớp", null));
            for (ClassRoom classRoom : classRoomController.getClassRoomsForSelection()) {
                filterValueComboBox.addItem(new FilterOption<>(classRoom.getClassCode() + " - " + classRoom.getClassName(), classRoom));
            }
        } else if (FILTER_ACADEMIC_YEAR.equals(filterType)) {
            filterValueComboBox.addItem(new FilterOption<>("Chọn niên khóa", null));
            for (String academicYear : studentController.getAcademicYearsForSelection()) {
                filterValueComboBox.addItem(new FilterOption<>(academicYear, academicYear));
            }
        }
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

    private void reloadClassRoomOptions(
            JComboBox<ClassRoom> classRoomComboBox,
            List<ClassRoom> allClassRooms,
            Faculty selectedFaculty,
            ClassRoom preferredClassRoom
    ) {
        classRoomComboBox.removeAllItems();
        for (ClassRoom classRoom : allClassRooms) {
            if (selectedFaculty == null
                    || selectedFaculty.getId() == null
                    || classRoom.getFaculty() == null
                    || classRoom.getFaculty().getId() == null
                    || selectedFaculty.getId().equals(classRoom.getFaculty().getId())) {
                classRoomComboBox.addItem(classRoom);
            }
        }
        if (preferredClassRoom != null) {
            classRoomComboBox.setSelectedItem(preferredClassRoom);
        }
    }

    private void selectStatus(JComboBox<FilterOption<String>> statusComboBox, String statusCode) {
        for (int index = 0; index < statusComboBox.getItemCount(); index++) {
            FilterOption<String> option = statusComboBox.getItemAt(index);
            if (option != null && option.value().equalsIgnoreCase(statusCode == null ? "" : statusCode)) {
                statusComboBox.setSelectedIndex(index);
                return;
            }
        }
        statusComboBox.setSelectedIndex(0);
    }
}
