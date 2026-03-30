/**
 * Màn hình quản trị cho đăng ký management.
 */
package com.qlsv.view.admin;

import com.qlsv.controller.DisplayField;
import com.qlsv.controller.EnrollmentManagementScreenController;
import com.qlsv.dto.EnrollmentDisplayDto;
import com.qlsv.model.ClassRoom;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Faculty;
import com.qlsv.model.Student;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.AbstractCrudPanel;
import com.qlsv.view.common.DetailSectionPanel;
import com.qlsv.view.common.FilterOption;
import com.qlsv.view.dialog.BaseDetailDialog;
import com.qlsv.view.dialog.EnrollmentDetailDialog;
import com.qlsv.view.dialog.EnrollmentFormDialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

public class EnrollmentManagementPanel extends AbstractCrudPanel<Enrollment> {

    private static final String FILTER_NONE = "Chọn điều kiện lọc";
    private static final String FILTER_ALL = "Tất cả đăng ký";
    private static final String FILTER_SECTION_CODE = "Theo mã học phần";
    private static final String FILTER_CLASS_ROOM = "Theo lớp";
    private static final String FILTER_FACULTY = "Theo khoa";

    private final EnrollmentManagementScreenController screenController = new EnrollmentManagementScreenController();

    private final JComboBox<String> filterTypeComboBox = new JComboBox<>(
            new String[]{FILTER_NONE, FILTER_ALL, FILTER_SECTION_CODE, FILTER_CLASS_ROOM, FILTER_FACULTY}
    );
    private final JComboBox<FilterOption<?>> filterValueComboBox = new JComboBox<>();
    private final DetailSectionPanel detailSectionPanel = new DetailSectionPanel(
            "Chi tiết đăng ký học phần",
            "Vui lòng chọn bản ghi đăng ký để xem chi tiết."
    );

    private boolean filterReady;

    /**
     * Khởi tạo đăng ký management.
     */
    public EnrollmentManagementPanel() {
        super("Quản lý đăng ký học phần");
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
        return new String[]{"ID", "Sinh viên", "Học phần", "Trạng thái", "Thời gian đăng ký"};
    }

    /**
     * Tải danh sách các bản ghi đăng ký học phần dựa trên bộ lọc đã chọn.
     */
    @Override
    protected List<Enrollment> loadItems() {
        return screenController.loadItems(
                filterReady,
                (String) filterTypeComboBox.getSelectedItem(),
                getSelectedFilterValue(Object.class),
                FILTER_ALL,
                FILTER_SECTION_CODE,
                FILTER_CLASS_ROOM,
                FILTER_FACULTY
        );
    }

    /**
     * Chuyển đổi dữ liệu đăng ký sang mảng đối tượng để hiển thị trên bảng.
     */
    @Override
    protected Object[] toRow(Enrollment item) {
        EnrollmentDisplayDto displayDto = screenController.toDisplayDto(item);
        return new Object[]{
                displayDto.id(),
                displayDto.studentName(),
                displayDto.sectionCode(),
                displayDto.statusText(),
                displayDto.enrolledAtText()
        };
    }

    /**
     * Trả về trạng thái trống thông báo.
     */
    @Override
    protected String getEmptyStateMessage() {
        return filterReady
                ? "Không tìm thấy đăng ký học phần phù hợp với điều kiện lọc hiện tại."
                : "Vui lòng chọn điều kiện lọc để hiển thị danh sách đăng ký học phần.";
    }

    /**
     * Hiển thị thông tin chi tiết về sinh viên và học phần khi chọn một dòng đăng ký.
     */
    @Override
    protected void onSelectionChanged(Enrollment selectedItem) {
        if (selectedItem == null) {
            detailSectionPanel.showMessage("Vui lòng chọn bản ghi đăng ký để xem chi tiết.");
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
        return new EnrollmentDetailDialog(detailPanel);
    }

    /**
     * Mở hộp thoại cho phép tạo mới hoặc chỉnh sửa thông tin đăng ký học phần.
     */
    @Override
    protected Enrollment promptForEntity(Enrollment existingItem) {
        EnrollmentFormDialog.EnrollmentFormResult formResult = EnrollmentFormDialog.showDialog(
                this,
                new EnrollmentFormDialog.EnrollmentFormModel(
                        existingItem == null ? "Thêm đăng ký học phần" : "Cập nhật đăng ký học phần",
                        screenController.loadStudents(),
                        existingItem == null ? null : existingItem.getStudent(),
                        screenController.loadCourseSections(),
                        existingItem == null ? null : existingItem.getCourseSection(),
                        existingItem == null ? "REGISTERED" : existingItem.getStatus()
                )
        );
        if (formResult == null) {
            return null;
        }

        return screenController.applyFormData(
                existingItem,
                new EnrollmentManagementScreenController.EnrollmentFormData(
                        formResult.student(),
                        formResult.courseSection(),
                        formResult.status()
                )
        );
    }

    /**
     * Thực hiện lưu bản ghi đăng ký vào hệ thống.
     */
    @Override
    protected void saveEntity(Enrollment item) {
        screenController.saveEnrollment(item);
    }

    /**
     * Thực hiện xóa bản ghi đăng ký được chọn.
     */
    @Override
    protected void deleteEntity(Enrollment item) {
        screenController.deleteEnrollment(item);
    }

    /**
     * Xây dựng giao diện cho khu vực lọc danh sách đăng ký.
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
        filterValueComboBox.setMinimumSize(new java.awt.Dimension(160, 36));

        JPanel filterPanel = new JPanel(new java.awt.GridBagLayout());
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Bộ lọc đăng ký học phần"),
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
     * Tải lại các giá trị gợi ý cho bộ lọc (danh sách học phần, lớp, khoa) bất đồng bộ.
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
                if (FILTER_CLASS_ROOM.equals(filterType)) return screenController.loadClassRooms();
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
                            CourseSection cs = (CourseSection) obj;
                            filterValueComboBox.addItem(new FilterOption<>(cs.getSectionCode(), cs));
                        }
                    } else if (FILTER_CLASS_ROOM.equals(filterType)) {
                        filterValueComboBox.addItem(new FilterOption<>("Chọn lớp", null));
                        for (Object obj : results) {
                            ClassRoom cr = (ClassRoom) obj;
                            filterValueComboBox.addItem(new FilterOption<>(cr.getClassCode() + " - " + cr.getClassName(), cr));
                        }
                    } else if (FILTER_FACULTY.equals(filterType)) {
                        filterValueComboBox.addItem(new FilterOption<>("Chọn khoa", null));
                        for (Object obj : results) {
                            Faculty faculty = (Faculty) obj;
                            filterValueComboBox.addItem(new FilterOption<>(faculty.getFacultyCode() + " - " + faculty.getFacultyName(), faculty));
                        }
                    }
                } catch (Exception exception) {
                    DialogUtil.showError(EnrollmentManagementPanel.this, "Lỗi khi tải danh mục: " + exception.getMessage());
                } finally {
                    setLoadingState(false);
                }
            }
        }.execute();
    }

    /**
     * Đặt lại các điều kiện lọc về mặc định.
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
     * Xử lý select trạng thái.
     */
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

    /**
     * Lọc sinh viên theo keyword.
     */
    private List<Student> filterStudentsByKeyword(List<Student> students, String keyword) {
        String normalizedKeyword = normalizeSearchText(keyword);
        if (normalizedKeyword.isBlank()) {
            return students.stream().limit(8).toList();
        }

        return students.stream()
                .filter(student -> normalizeSearchText(student.getStudentCode()).contains(normalizedKeyword)
                        || normalizeSearchText(student.getFullName()).contains(normalizedKeyword))
                .limit(8)
                .toList();
    }

    /**
     * Định dạng sinh viên hiển thị.
     */
    private String formatStudentDisplay(Student student) {
        if (student == null) {
            return "";
        }
        return student.getStudentCode() + " - " + student.getFullName();
    }

    /**
     * Cập nhật label sinh viên đã chọn.
     */
    private void updateSelectedStudentLabel(JLabel label, Student student) {
        label.setText(student == null
                ? "Chưa chọn sinh viên"
                : "Đã chọn: " + formatStudentDisplay(student));
    }

    /**
     * Chuẩn hóa tìm kiếm văn bản.
     */
    private String normalizeSearchText(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }

    private static final class StudentSelectionState {
        private Student selectedStudent;

        /**
         * Xử lý sinh viên selection state.
         */
        private StudentSelectionState(Student selectedStudent) {
            this.selectedStudent = selectedStudent;
        }
    }
}
