package com.qlsv.view.admin;

import com.qlsv.controller.CourseSectionController;
import com.qlsv.controller.FacultyController;
import com.qlsv.controller.LecturerController;
import com.qlsv.controller.UserController;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Faculty;
import com.qlsv.model.Lecturer;
import com.qlsv.model.Role;
import com.qlsv.utils.DateUtil;
import com.qlsv.utils.DialogUtil;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.utils.ValidationUtil;
import com.qlsv.view.auth.ChangePasswordDialog;
import com.qlsv.view.common.AbstractCrudPanel;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.DetailSectionPanel;
import com.qlsv.view.common.FilterOption;
import com.qlsv.view.dialog.BaseDetailDialog;
import com.qlsv.view.dialog.LecturerDetailDialog;
import com.qlsv.view.dialog.LecturerFormDialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.stream.Collectors;

public class LecturerManagementPanel extends AbstractCrudPanel<Lecturer> {

    private static final String FILTER_NONE = "Chọn điều kiện lọc";
    private static final String FILTER_ALL = "Tất cả giảng viên";
    private static final String FILTER_FACULTY = "Theo khoa";

    private final LecturerController lecturerController = new LecturerController();
    private final FacultyController facultyController = new FacultyController();
    private final CourseSectionController courseSectionController = new CourseSectionController();
    private final UserController userController = new UserController();

    private final JComboBox<String> filterTypeComboBox = new JComboBox<>(new String[]{FILTER_NONE, FILTER_ALL, FILTER_FACULTY});
    private final JComboBox<FilterOption<?>> filterValueComboBox = new JComboBox<>();
    private final DetailSectionPanel detailSectionPanel = new DetailSectionPanel(
            "Chi tiết thông tin giảng viên",
            "Vui lòng chọn giảng viên để xem chi tiết."
    );

    private boolean filterReady;

    public LecturerManagementPanel() {
        super("Quản lý giảng viên");
        filterTypeComboBox.setSelectedItem(FILTER_ALL);
        setFilterPanel(buildFilterPanel());
        setDetailPanel(detailSectionPanel);
        reloadFilterValues();
        refreshData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Mã giảng viên", "Họ và tên", "Giới tính", "Ngày sinh", "Email", "Khoa", "Trạng thái"};
    }

    @Override
    protected void configureCustomActionButtons(JPanel actionPanel) {
        JButton changePasswordButton = new JButton("Đổi MK");
        styleActionButton(changePasswordButton, AppColors.BUTTON_PRIMARY);
        actionPanel.add(changePasswordButton);
        changePasswordButton.addActionListener(event -> openAdminChangePasswordDialog());
    }

    @Override
    protected List<Lecturer> loadItems() {
        if (!filterReady) {
            return List.of();
        }

        String filterType = (String) filterTypeComboBox.getSelectedItem();
        return switch (filterType == null ? FILTER_NONE : filterType) {
            case FILTER_ALL -> lecturerController.getAllLecturers();
            case FILTER_FACULTY -> {
                Faculty faculty = getSelectedFilterValue(Faculty.class);
                yield faculty == null ? List.of() : lecturerController.getLecturersByFaculty(faculty.getId());
            }
            default -> List.of();
        };
    }

    @Override
    protected Object[] toRow(Lecturer item) {
        return new Object[]{
                item.getId(),
                item.getLecturerCode(),
                item.getFullName(),
                DisplayTextUtil.formatGender(item.getGender()),
                DisplayTextUtil.formatDate(item.getDateOfBirth()),
                item.getEmail(),
                item.getFaculty() == null ? "" : item.getFaculty().getFacultyName(),
                DisplayTextUtil.formatStatus(item.getStatus())
        };
    }

    @Override
    protected String getEmptyStateMessage() {
        return filterReady
                ? "Không tìm thấy giảng viên phù hợp với điều kiện lọc hiện tại."
                : "Vui lòng chọn điều kiện lọc để hiển thị danh sách giảng viên.";
    }

    @Override
    protected void onSelectionChanged(Lecturer selectedItem) {
        if (selectedItem == null) {
            detailSectionPanel.showMessage("Vui lòng chọn giảng viên để xem chi tiết.");
            return;
        }

        List<CourseSection> assignedCourseSections = courseSectionController.getCourseSectionsByLecturer(selectedItem.getId());
        String subjects = DisplayTextUtil.joinUniqueTexts(assignedCourseSections.stream()
                .map(courseSection -> courseSection.getSubject() == null ? null : courseSection.getSubject().getSubjectName())
                .collect(Collectors.toList()));

        detailSectionPanel.showFields(new String[][]{
                {"Mã giảng viên", DisplayTextUtil.defaultText(selectedItem.getLecturerCode())},
                {"Họ và tên", DisplayTextUtil.defaultText(selectedItem.getFullName())},
                {"Giới tính", DisplayTextUtil.formatGender(selectedItem.getGender())},
                {"Ngày sinh", DisplayTextUtil.formatDate(selectedItem.getDateOfBirth())},
                {"Số điện thoại", DisplayTextUtil.defaultText(selectedItem.getPhone())},
                {"Email", DisplayTextUtil.defaultText(selectedItem.getEmail())},
                {"Địa chỉ", DisplayTextUtil.defaultText(selectedItem.getAddress())},
                {"Khoa", selectedItem.getFaculty() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(selectedItem.getFaculty().getFacultyName())},
                {"Môn giảng dạy", subjects},
                {"Trạng thái", DisplayTextUtil.formatStatus(selectedItem.getStatus())}
        });
    }

    @Override
    protected BaseDetailDialog createDetailDialog(javax.swing.JComponent detailPanel) {
        return new LecturerDetailDialog(detailPanel);
    }

    @Override
    protected Lecturer promptForEntity(Lecturer existingItem) {
        LecturerFormDialog.LecturerFormResult formResult = LecturerFormDialog.showDialog(
                this,
                new LecturerFormDialog.LecturerFormModel(
                        existingItem == null ? "Thêm giảng viên" : "Cập nhật giảng viên",
                        existingItem == null ? "" : existingItem.getLecturerCode(),
                        existingItem == null ? "" : existingItem.getFullName(),
                        existingItem == null ? "Nam" : DisplayTextUtil.formatGender(existingItem.getGender()),
                        existingItem == null ? "" : DateUtil.formatForInput(existingItem.getDateOfBirth()),
                        existingItem == null ? "" : existingItem.getEmail(),
                        existingItem == null ? "" : existingItem.getPhone(),
                        existingItem == null ? "" : existingItem.getAddress(),
                        facultyController.getFacultiesForSelection(),
                        existingItem == null ? null : existingItem.getFaculty(),
                        existingItem == null ? "ACTIVE" : existingItem.getStatus()
                )
        );
        if (formResult == null) {
            return null;
        }

        Lecturer lecturer = existingItem == null ? new Lecturer() : existingItem;
        lecturer.setLecturerCode(ValidationUtil.normalizeCodePrefix(formResult.lecturerCode(), "GV", "MÃ£ giáº£ng viÃªn"));
        lecturer.setFullName(formResult.fullName().trim());
        lecturer.setGender(formResult.gender());
        lecturer.setDateOfBirth(DateUtil.parseRequiredDate(formResult.dateOfBirth(), "Ngày sinh"));
        lecturer.setEmail(formResult.email().trim());
        lecturer.setPhone(formResult.phone().trim());
        lecturer.setAddress(formResult.address().trim());
        lecturer.setFaculty(formResult.faculty());
        lecturer.setStatus(formResult.status());
        return lecturer;
    }

    @Override
    protected void saveEntity(Lecturer item) {
        lecturerController.saveLecturer(item);
    }

    @Override
    protected void deleteEntity(Lecturer item) {
        lecturerController.deleteLecturer(item.getId());
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
        filterTypeComboBox.setPreferredSize(new Dimension(170, 36));
        filterValueComboBox.setPreferredSize(new Dimension(220, 36));
        filterValueComboBox.setMinimumSize(new Dimension(220, 36));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Bộ lọc giảng viên"),
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
        String filterType = (String) filterTypeComboBox.getSelectedItem();
        filterReady = FILTER_ALL.equals(filterType);
        filterValueComboBox.removeAllItems();

        if (!FILTER_FACULTY.equals(filterType)) {
            filterValueComboBox.setEnabled(false);
            return;
        }

        filterValueComboBox.setEnabled(true);
        filterValueComboBox.addItem(new FilterOption<>("Chọn khoa", null));
        for (Faculty faculty : facultyController.getFacultiesForSelection()) {
            filterValueComboBox.addItem(new FilterOption<>(faculty.getFacultyCode() + " - " + faculty.getFacultyName(), faculty));
        }
    }

    private void resetFilter() {
        filterTypeComboBox.setSelectedItem(FILTER_ALL);
        reloadFilterValues();
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
        Lecturer selectedItem = getSelectedItem();
        if (selectedItem == null) {
            DialogUtil.showError(this, "Hãy chọn đúng 1 giảng viên để đổi mật khẩu.");
            return;
        }
        if (selectedItem.getUserId() == null) {
            DialogUtil.showError(this, "Giảng viên được chọn chưa liên kết tài khoản đăng nhập.");
            return;
        }

        ChangePasswordDialog.PasswordChangeRequest request = ChangePasswordDialog.showAdminResetDialog(
                this,
                "Đổi mật khẩu giảng viên"
        );
        if (request == null) {
            return;
        }

        try {
            userController.adminChangePassword(
                    selectedItem.getUserId(),
                    Role.LECTURER,
                    request.newPassword(),
                    request.confirmPassword()
            );
            DialogUtil.showInfo(this, "Đổi mật khẩu giảng viên thành công.");
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
