/**
 * Màn hình quản trị cho quản lý giảng viên.
 */
package com.qlsv.view.admin;

import com.qlsv.controller.CourseSectionController;
import com.qlsv.controller.FacultyController;
import com.qlsv.controller.LecturerController;
import com.qlsv.controller.LecturerSubjectController;
import com.qlsv.controller.SubjectController;
import com.qlsv.controller.UserController;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Faculty;
import com.qlsv.model.Lecturer;
import com.qlsv.model.Role;
import com.qlsv.model.Subject;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LecturerManagementPanel extends AbstractCrudPanel<Lecturer> {

    private static final String FILTER_NONE = "Chọn điều kiện lọc";
    private static final String FILTER_ALL = "Tất cả giảng viên";
    private static final String FILTER_FACULTY = "Theo khoa";

    private final LecturerController lecturerController = new LecturerController();
    private final LecturerSubjectController lecturerSubjectController = new LecturerSubjectController();
    private final FacultyController facultyController = new FacultyController();
    private final SubjectController subjectController = new SubjectController();
    private final CourseSectionController courseSectionController = new CourseSectionController();
    private final UserController userController = new UserController();

    private final JComboBox<String> filterTypeComboBox = new JComboBox<>(new String[]{FILTER_NONE, FILTER_ALL, FILTER_FACULTY});
    private final JComboBox<FilterOption<?>> filterValueComboBox = new JComboBox<>();
    private final DetailSectionPanel detailSectionPanel = new DetailSectionPanel(
            "Chi tiết thông tin giảng viên",
            "Vui lòng chọn giảng viên để xem chi tiết."
    );

    private boolean filterReady;
    private List<Subject> pendingSelectedSubjects = List.of();

    /**
     * Khởi tạo quản lý giảng viên.
     */
    public LecturerManagementPanel() {
        super("Quan ly giang vien");
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
        return new String[]{"ID", "Mã giảng viên", "Họ và tên", "Giới tính", "Ngày sinh", "Email", "Khoa", "Trạng thái"};
    }

    /**
     * Thiết lập tùy biến thao tác nút.
     */
    @Override
    protected void configureCustomActionButtons(JPanel actionPanel) {
        JButton changePasswordButton = new JButton("Đổi MK");
        styleActionButton(changePasswordButton, AppColors.BUTTON_PRIMARY);
        actionPanel.add(changePasswordButton);
        changePasswordButton.addActionListener(event -> openAdminChangePasswordDialog());
    }

    /**
     * Tòa danh sách giảng viên từ dịch vụ dựa trên các điều kiện lọc đang áp dụng.
     */
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

    /**
     * Chuyển đổi thông tin giảng viên thành các giá trị cột để hiển thị trên bảng dữ liệu.
     */
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

    /**
     * Trả về trạng thái trống thông báo.
     */
    @Override
    protected String getEmptyStateMessage() {
        return filterReady
                ? "Không có giảng viên nào phù hợp với điều kiện lọc hiện tại."
                : "Vui lòng chọn điều kiện lọc để hiển thị danh sách giảng viên.";
    }

    /**
     * Hiển thị thông tin chi tiết và danh sách môn học giảng dạy khi chọn một giảng viên.
     */
    @Override
    protected void onSelectionChanged(Lecturer selectedItem) {
        if (selectedItem == null) {
            detailSectionPanel.showMessage("Vui lòng chọn giảng viên để xem chi tiết.");
            return;
        }

        List<Subject> whitelistSubjects = lecturerSubjectController.getSubjectsByLecturer(selectedItem.getId());
        List<String> subjectNames;
        if (whitelistSubjects.isEmpty()) {
            List<CourseSection> assignedCourseSections = courseSectionController.getCourseSectionsByLecturer(selectedItem.getId());
            subjectNames = assignedCourseSections.stream()
                    .map(courseSection -> courseSection.getSubject() == null ? null : courseSection.getSubject().getSubjectName())
                    .collect(Collectors.toList());
        } else {
            subjectNames = whitelistSubjects.stream()
                    .map(Subject::getSubjectName)
                    .collect(Collectors.toList());
        }

        detailSectionPanel.showFields(new String[][]{
                {"Mã giảng viên", DisplayTextUtil.defaultText(selectedItem.getLecturerCode())},
                {"Họ và tên", DisplayTextUtil.defaultText(selectedItem.getFullName())},
                {"Giới tính", DisplayTextUtil.formatGender(selectedItem.getGender())},
                {"Ngày sinh", DisplayTextUtil.formatDate(selectedItem.getDateOfBirth())},
                {"Số điện thoại", DisplayTextUtil.defaultText(selectedItem.getPhone())},
                {"Email", DisplayTextUtil.defaultText(selectedItem.getEmail())},
                {"Địa chỉ", DisplayTextUtil.defaultText(selectedItem.getAddress())},
                {"Khoa", selectedItem.getFaculty() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(selectedItem.getFaculty().getFacultyName())},
                {"Môn giảng dạy", DisplayTextUtil.joinUniqueTexts(subjectNames)},
                {"Trạng thái", DisplayTextUtil.formatStatus(selectedItem.getStatus())}
        });
    }

    /**
     * Tạo hộp thoại chi tiết.
     */
    @Override
    protected BaseDetailDialog createDetailDialog(javax.swing.JComponent detailPanel) {
        return new LecturerDetailDialog(detailPanel);
    }

    /**
     * Mở hộp thoại để người dùng nhập thông tin hồ sơ và chọn môn học giảng dạy cho giảng viên.
     */
    @Override
    protected Lecturer promptForEntity(Lecturer existingItem) {
        lecturerSubjectController.backfillFromCourseSectionsIfNeeded();

        List<Subject> availableSubjects = subjectController.getSubjectsForSelection();
        List<Subject> selectedSubjects = existingItem == null
                ? List.of()
                : new ArrayList<>(lecturerSubjectController.getSubjectsByLecturer(existingItem.getId()));

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
                        existingItem == null ? "ACTIVE" : existingItem.getStatus(),
                        availableSubjects,
                        selectedSubjects
                )
        );
        if (formResult == null) {
            pendingSelectedSubjects = List.of();
            return null;
        }

        pendingSelectedSubjects = new ArrayList<>(formResult.subjects());

        Lecturer lecturer = existingItem == null ? new Lecturer() : existingItem;
        lecturer.setLecturerCode(ValidationUtil.normalizeCodePrefix(formResult.lecturerCode(), "GV", "Mã giảng viên"));
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

    /**
     * Lưu thông tin giảng viên kèm theo danh sách các môn học được phân công giảng dạy.
     */
    @Override
    protected void saveEntity(Lecturer item) {
        lecturerController.saveLecturerWithSubjects(item, pendingSelectedSubjects);
        pendingSelectedSubjects = List.of();
    }

    /**
     * Thực hiện xóa giảng viên khỏi hệ thống dựa trên ID.
     */
    @Override
    protected void deleteEntity(Lecturer item) {
        lecturerController.deleteLecturer(item.getId());
    }

    /**
     * Khởi tạo giao diện thanh công cụ chứa các bộ lọc tìm kiếm giảng viên.
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
        filterTypeComboBox.setPreferredSize(new Dimension(170, 36));
        filterValueComboBox.setMinimumSize(new Dimension(160, 36));

        JPanel filterPanel = new JPanel(new java.awt.GridBagLayout());
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Bộ lọc giảng viên"),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridy = 0;
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.insets = new java.awt.Insets(0, 0, 0, 8);

        gbc.gridx = 0;
        filterPanel.add(new JLabel("Điều kiện"), gbc);

        gbc.gridx = 1;
        filterPanel.add(filterTypeComboBox, gbc);

        gbc.gridx = 2;
        filterPanel.add(new JLabel("Giá trị"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 1.0;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        filterPanel.add(filterValueComboBox, gbc);

        gbc.gridx = 4;
        gbc.weightx = 0.0;
        gbc.fill = java.awt.GridBagConstraints.NONE;
        filterPanel.add(applyButton, gbc);

        gbc.gridx = 5;
        gbc.insets = new java.awt.Insets(0, 0, 0, 0);
        filterPanel.add(resetButton, gbc);

        return filterPanel;
    }

    /**
     * Cập nhật danh sách các giá trị khả dụng cho bộ lọc theo khoa.
     */
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

    /**
     * Cho phép quản trị viên đặt lại mật khẩu đăng nhập cho giảng viên đã chọn.
     */
    private void openAdminChangePasswordDialog() {
        Lecturer selectedItem = getSelectedItem();
        if (selectedItem == null) {
            DialogUtil.showError(this, "Vui lòng chọn đúng 1 giảng viên để đổi mật khẩu.");
            return;
        }
        if (selectedItem.getUserId() == null) {
            DialogUtil.showError(this, "Giảng viên được chọn chưa liên kết tài khoản đăng nhập.");
            return;
        }

        ChangePasswordDialog.PasswordChangeRequest request = ChangePasswordDialog.showAdminResetDialog(
                this,
                "Đổi mật khẩu giảng viên: " + selectedItem.getFullName()
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

    /**
     * Áp dụng kiểu cho nút thao tác.
     */
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
