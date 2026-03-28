package com.qlsv.view.admin;

import com.qlsv.controller.DisplayField;
import com.qlsv.controller.StudentManagementScreenController;
import com.qlsv.controller.UserController;
import com.qlsv.dto.StudentDisplayDto;
import com.qlsv.model.ClassRoom;
import com.qlsv.model.Faculty;
import com.qlsv.model.Role;
import com.qlsv.model.Student;
import com.qlsv.utils.AcademicFormatUtil;
import com.qlsv.utils.DialogUtil;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.view.auth.ChangePasswordDialog;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.AbstractCrudPanel;
import com.qlsv.view.common.DetailSectionPanel;
import com.qlsv.view.common.FilterOption;
import com.qlsv.view.dialog.StudentFormDialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.util.List;

public class StudentManagementPanel extends AbstractCrudPanel<Student> {

    private static final String FILTER_NONE = "Chọn điều kiện lọc";
    private static final String FILTER_ALL = "Tất cả sinh viên";
    private static final String FILTER_FACULTY = "Theo khoa";
    private static final String FILTER_CLASS_ROOM = "Theo lớp";
    private static final String FILTER_ACADEMIC_YEAR = "Theo niên khóa";

    private final StudentManagementScreenController screenController = new StudentManagementScreenController();
    private final UserController userController = new UserController();

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
    protected void configureCustomActionButtons(JPanel actionPanel) {
        JButton changePasswordButton = new JButton("Đổi MK");
        styleActionButton(changePasswordButton, AppColors.BUTTON_PRIMARY);
        actionPanel.add(changePasswordButton);
        changePasswordButton.addActionListener(event -> openAdminChangePasswordDialog());
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
        return screenController.loadItems(
                filterReady,
                (String) filterTypeComboBox.getSelectedItem(),
                getSelectedFilterValue(Object.class),
                FILTER_ALL,
                FILTER_FACULTY,
                FILTER_CLASS_ROOM,
                FILTER_ACADEMIC_YEAR
        );
    }

    @Override
    protected Object[] toRow(Student item) {
        StudentDisplayDto displayDto = screenController.toDisplayDto(item);
        return new Object[]{
                displayDto.id(),
                displayDto.studentCode(),
                displayDto.fullName(),
                displayDto.email(),
                displayDto.classRoomName(),
                displayDto.facultyName(),
                displayDto.academicYear(),
                displayDto.statusText()
        };
    }

    @Override
    protected List<Student> performSearch(String keyword, List<Student> loadedItems) {
        if (keyword == null || keyword.isBlank()) {
            return loadedItems;
        }
        return screenController.searchItems(
                filterReady,
                (String) filterTypeComboBox.getSelectedItem(),
                getSelectedFilterValue(Object.class),
                FILTER_ALL,
                FILTER_FACULTY,
                FILTER_CLASS_ROOM,
                FILTER_ACADEMIC_YEAR,
                keyword
        );
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
        try {
            displayStudent = screenController.resolveSelection(selectedItem);
            if (displayStudent != null) {
                selectedItem.setEmail(displayStudent.getEmail());
                selectedItem.setPhone(displayStudent.getPhone());
                selectedItem.setAddress(displayStudent.getAddress());

                int selectedRow = getTable().getSelectedRow();
                if (selectedRow >= 0) {
                    getTable().setValueAt(displayStudent.getEmail(), selectedRow, 3);
                }
            }
        } catch (Exception ignored) {
            displayStudent = selectedItem;
        }

        List<DisplayField> detailFields = screenController.buildDetailFields(displayStudent);
        detailSectionPanel.showFields(detailFields.stream()
                .map(field -> new String[]{field.label(), field.value()})
                .toArray(String[][]::new));
    }

    @Override
    protected Student promptForEntity(Student existingItem) {
        List<ClassRoom> allClassRooms = screenController.loadClassRooms();
        StudentFormDialog.StudentFormResult formResult = StudentFormDialog.showDialog(
                this,
                new StudentFormDialog.StudentFormModel(
                        existingItem == null ? "Thêm sinh viên" : "Cập nhật sinh viên",
                        existingItem == null ? "" : existingItem.getStudentCode(),
                        existingItem == null ? "" : existingItem.getFullName(),
                        existingItem == null ? "Nam" : DisplayTextUtil.formatGender(existingItem.getGender()),
                        existingItem == null || existingItem.getDateOfBirth() == null ? "" : existingItem.getDateOfBirth().toString(),
                        existingItem == null ? "" : existingItem.getEmail(),
                        existingItem == null ? "" : existingItem.getPhone(),
                        existingItem == null ? "" : existingItem.getAddress(),
                        existingItem == null ? "" : AcademicFormatUtil.formatAcademicYear(existingItem.getAcademicYear()),
                        existingItem == null ? "ACTIVE" : existingItem.getStatus(),
                        screenController.loadFaculties(),
                        allClassRooms,
                        existingItem == null ? null : existingItem.getFaculty(),
                        existingItem == null ? null : existingItem.getClassRoom()
                )
        );
        if (formResult == null) {
            return null;
        }

        return screenController.applyFormData(
                existingItem,
                new StudentManagementScreenController.StudentFormData(
                        formResult.studentCode(),
                        formResult.fullName(),
                        formResult.gender(),
                        formResult.dateOfBirth(),
                        formResult.email(),
                        formResult.phone(),
                        formResult.address(),
                        formResult.faculty(),
                        formResult.classRoom(),
                        formResult.academicYear(),
                        formResult.status()
                )
        );
    }

    @Override
    protected void saveEntity(Student item) {
        screenController.saveStudent(item);
    }

    @Override
    protected void deleteEntity(Student item) {
        screenController.deleteStudent(item);
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
            for (Faculty faculty : screenController.loadFaculties()) {
                filterValueComboBox.addItem(new FilterOption<>(faculty.getFacultyCode() + " - " + faculty.getFacultyName(), faculty));
            }
        } else if (FILTER_CLASS_ROOM.equals(filterType)) {
            filterValueComboBox.addItem(new FilterOption<>("Chọn lớp", null));
            for (ClassRoom classRoom : screenController.loadClassRooms()) {
                filterValueComboBox.addItem(new FilterOption<>(classRoom.getClassCode() + " - " + classRoom.getClassName(), classRoom));
            }
        } else if (FILTER_ACADEMIC_YEAR.equals(filterType)) {
            filterValueComboBox.addItem(new FilterOption<>("Chọn niên khóa", null));
            for (String academicYear : screenController.loadAcademicYears()) {
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

    private void openAdminChangePasswordDialog() {
        Student selectedItem = getSelectedItem();
        if (selectedItem == null) {
            DialogUtil.showError(this, "Hãy chọn đúng 1 sinh viên để đổi mật khẩu.");
            return;
        }
        if (selectedItem.getUserId() == null) {
            DialogUtil.showError(this, "Sinh viên được chọn chưa liên kết tài khoản đăng nhập.");
            return;
        }

        ChangePasswordDialog.PasswordChangeRequest request = ChangePasswordDialog.showAdminResetDialog(
                this,
                "Đổi mật khẩu sinh viên"
        );
        if (request == null) {
            return;
        }

        try {
            userController.adminChangePassword(
                    selectedItem.getUserId(),
                    Role.STUDENT,
                    request.newPassword(),
                    request.confirmPassword()
            );
            DialogUtil.showInfo(this, "Đổi mật khẩu sinh viên thành công.");
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void styleActionButton(JButton button, java.awt.Color background) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(background);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
    }
}
