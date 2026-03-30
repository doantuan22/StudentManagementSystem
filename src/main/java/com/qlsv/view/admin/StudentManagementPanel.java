/**
 * Màn hình quản trị cho quản lý sinh viên.
 */
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
import com.qlsv.view.common.AbstractCrudPanel;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.DetailSectionPanel;
import com.qlsv.view.common.FilterOption;
import com.qlsv.view.dialog.BaseDetailDialog;
import com.qlsv.view.dialog.StudentDetailDialog;
import com.qlsv.view.dialog.StudentFormDialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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

    /**
     * Khởi tạo quản lý sinh viên.
     */
    public StudentManagementPanel() {
        super("Quản lý sinh viên");
        filterTypeComboBox.setSelectedItem(FILTER_ALL);
        setFilterPanel(buildFilterPanel());
        setDetailPanel(detailSectionPanel);
        configureTableLayout();
        reloadFilterValues();
        refreshData();
    }

    /**
     * Trả về column names.
     */
    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Mã sinh viên", "Họ và tên", "Email", "Lớp", "Khoa", "Niên khóa", "Trạng thái"};
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
     * Làm mới dữ liệu đang hiển thị.
     */
    @Override
    public void reloadData() {
        refreshData();
    }

    /**
     * Cập nhật visible.
     */
    @Override
    public void setVisible(boolean visible) {
        boolean wasVisible = isVisible();
        super.setVisible(visible);
        if (visible && !wasVisible) {
            reloadData();
        }
    }

    /**
     * Tòa danh sách sinh viên từ cơ sở dữ liệu dựa trên điều kiện lọc hiện tại.
     */
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

    /**
     * Chuyển đổi thông tin sinh viên sang dạng mảng để hiển thị lên các cột của bảng.
     */
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

    /**
     * Thực hiện tìm kiếm sinh viên theo từ khóa (hỗ trợ tìm kiếm theo tên, mã sinh viên, email).
     */
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

    /**
     * Trả về trạng thái trống thông báo.
     */
    @Override
    protected String getEmptyStateMessage() {
        return filterReady
                ? "Không tìm thấy sinh viên phù hợp với điều kiện lọc hiện tại."
                : "Vui lòng chọn điều kiện lọc để hiển thị danh sách sinh viên.";
    }

    /**
     * Hiển thị thông tin chi tiết và cập nhật các liên kết dữ liệu khi người dùng chọn một sinh viên.
     */
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

    /**
     * Trả về ID của student để preserve selection.
     */
    @Override
    protected Object getItemId(Student item) {
        return item != null ? item.getId() : null;
    }

    /**
     * Tạo hộp thoại chi tiết.
     */
    @Override
    protected BaseDetailDialog createDetailDialog(javax.swing.JComponent detailPanel) {
        return new StudentDetailDialog(detailPanel);
    }

    /**
     * Mở form nhập liệu để thêm mới sinh viên hoặc chỉnh sửa thông tin sinh viên hiện có.
     */
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

    /**
     * Lưu entity.
     */
    @Override
    protected void saveEntity(Student item) {
        screenController.saveStudent(item);
    }

    /**
     * Xóa entity.
     */
    @Override
    protected void deleteEntity(Student item) {
        screenController.deleteStudent(item);
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

        filterTypeComboBox.setPreferredSize(new java.awt.Dimension(170, 36));
        filterValueComboBox.setPreferredSize(new java.awt.Dimension(220, 36));
        filterValueComboBox.setMinimumSize(new java.awt.Dimension(220, 36));

        JPanel filterPanel = new JPanel(new BorderLayout(12, 8));
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Bộ lọc sinh viên"),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        JPanel fieldPanel = new JPanel(new GridBagLayout());
        fieldPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 8);

        gbc.gridx = 0;
        fieldPanel.add(new JLabel("Điều kiện"), gbc);

        gbc.gridx = 1;
        fieldPanel.add(filterTypeComboBox, gbc);

        gbc.gridx = 2;
        fieldPanel.add(new JLabel("Giá trị"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 0);
        fieldPanel.add(filterValueComboBox, gbc);

        gbc.gridx = 4;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 8, 0, 8);
        fieldPanel.add(applyButton, gbc);

        gbc.gridx = 5;
        gbc.insets = new Insets(0, 0, 0, 0);
        fieldPanel.add(resetButton, gbc);

        gbc.gridx = 6;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        fieldPanel.add(spacer, gbc);

        filterPanel.add(fieldPanel, BorderLayout.CENTER);
        return filterPanel;
    }

    /**
     * Thiết lập bảng layout.
     */
    private void configureTableLayout() {
        JTable table = getTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] widths = {70, 140, 220, 220, 170, 180, 120, 140};
        for (int index = 0; index < widths.length && index < table.getColumnModel().getColumnCount(); index++) {
            table.getColumnModel().getColumn(index).setPreferredWidth(widths[index]);
        }
    }

    /**
     * Tải lại các giá trị cho ô chọn điều kiện lọc (danh sách khoa, lớp, niên khóa) bất đồng bộ.
     */
    private void reloadFilterValues() {
        String filterType = (String) filterTypeComboBox.getSelectedItem();
        filterReady = FILTER_ALL.equals(filterType);
        filterValueComboBox.removeAllItems();

        if (FILTER_NONE.equals(filterType) || FILTER_ALL.equals(filterType)) {
            filterValueComboBox.setEnabled(false);
            return;
        }

        // Disable controls trong lúc load
        filterTypeComboBox.setEnabled(false);
        filterValueComboBox.setEnabled(false);
        setLoadingState(true);
        
        new javax.swing.SwingWorker<List<?>, Void>() {
            @Override
            protected List<?> doInBackground() {
                if (FILTER_FACULTY.equals(filterType)) return screenController.loadFaculties();
                if (FILTER_CLASS_ROOM.equals(filterType)) return screenController.loadClassRooms();
                if (FILTER_ACADEMIC_YEAR.equals(filterType)) return screenController.loadAcademicYears();
                return List.of();
            }

            @Override
            protected void done() {
                try {
                    List<?> results = get();
                    if (FILTER_FACULTY.equals(filterType)) {
                        filterValueComboBox.addItem(new FilterOption<>("Chọn khoa", null));
                        for (Object obj : results) {
                            Faculty faculty = (Faculty) obj;
                            filterValueComboBox.addItem(new FilterOption<>(faculty.getFacultyCode() + " - " + faculty.getFacultyName(), faculty));
                        }
                    } else if (FILTER_CLASS_ROOM.equals(filterType)) {
                        filterValueComboBox.addItem(new FilterOption<>("Chọn lớp", null));
                        for (Object obj : results) {
                            ClassRoom classRoom = (ClassRoom) obj;
                            filterValueComboBox.addItem(new FilterOption<>(classRoom.getClassCode() + " - " + classRoom.getClassName(), classRoom));
                        }
                    } else if (FILTER_ACADEMIC_YEAR.equals(filterType)) {
                        filterValueComboBox.addItem(new FilterOption<>("Chọn niên khóa", null));
                        for (Object obj : results) {
                            String academicYear = (String) obj;
                            filterValueComboBox.addItem(new FilterOption<>(academicYear, academicYear));
                        }
                    }
                    filterValueComboBox.setEnabled(true);
                } catch (Exception exception) {
                    DialogUtil.showError(StudentManagementPanel.this, "Lỗi khi tải danh mục: " + exception.getMessage());
                } finally {
                    filterTypeComboBox.setEnabled(true);
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

    /**
     * Mở hộp thoại cho phép quản trị viên đặt lại mật khẩu cho tài khoản của sinh viên.
     */
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
