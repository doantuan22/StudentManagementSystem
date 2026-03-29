/**
 * Màn hình quản trị cho quản lý môn học.
 */
package com.qlsv.view.admin;

import com.qlsv.controller.FacultyController;
import com.qlsv.controller.SubjectController;
import com.qlsv.model.Faculty;
import com.qlsv.model.Subject;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.view.common.AbstractCrudPanel;
import com.qlsv.view.common.DetailSectionPanel;
import com.qlsv.view.common.FilterOption;
import com.qlsv.view.dialog.BaseDetailDialog;
import com.qlsv.view.dialog.SubjectDetailDialog;
import com.qlsv.view.dialog.SubjectFormDialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.util.List;

public class SubjectManagementPanel extends AbstractCrudPanel<Subject> {

    private static final String FILTER_NONE = "Chọn điều kiện lọc";
    private static final String FILTER_ALL = "Tất cả môn học";
    private static final String FILTER_FACULTY = "Theo khoa";

    private final SubjectController subjectController = new SubjectController();
    private final FacultyController facultyController = new FacultyController();

    private final JComboBox<String> filterTypeComboBox = new JComboBox<>(new String[]{FILTER_NONE, FILTER_ALL, FILTER_FACULTY});
    private final JComboBox<FilterOption<?>> filterValueComboBox = new JComboBox<>();
    private final DetailSectionPanel detailSectionPanel = new DetailSectionPanel(
            "Chi tiết môn học",
            "Vui lòng chọn môn học để xem chi tiết."
    );

    private boolean filterReady;

    /**
     * Khởi tạo quản lý môn học.
     */
    public SubjectManagementPanel() {
        super("Quản lý môn học");
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
        return new String[]{"ID", "Mã môn học", "Tên môn học", "Số tín chỉ", "Khoa"};
    }

    /**
     * Nạp items.
     */
    @Override
    protected List<Subject> loadItems() {
        if (!filterReady) {
            return List.of();
        }

        String filterType = (String) filterTypeComboBox.getSelectedItem();
        return switch (filterType == null ? FILTER_NONE : filterType) {
            case FILTER_ALL -> subjectController.getAllSubjects();
            case FILTER_FACULTY -> {
                Faculty faculty = getSelectedFilterValue(Faculty.class);
                yield faculty == null ? List.of() : subjectController.getSubjectsByFaculty(faculty.getId());
            }
            default -> List.of();
        };
    }

    /**
     * Xử lý to row.
     */
    @Override
    protected Object[] toRow(Subject item) {
        return new Object[]{
                item.getId(),
                item.getSubjectCode(),
                item.getSubjectName(),
                item.getCredits(),
                item.getFaculty() == null ? "" : item.getFaculty().getFacultyName()
        };
    }

    /**
     * Trả về trạng thái trống thông báo.
     */
    @Override
    protected String getEmptyStateMessage() {
        return filterReady
                ? "Không tìm thấy môn học phù hợp với điều kiện lọc hiện tại."
                : "Vui lòng chọn điều kiện lọc để hiển thị danh sách môn học.";
    }

    /**
     * Xử lý on selection changed.
     */
    @Override
    protected void onSelectionChanged(Subject selectedItem) {
        if (selectedItem == null) {
            detailSectionPanel.showMessage("Vui lòng chọn môn học để xem chi tiết.");
            return;
        }

        detailSectionPanel.showFields(new String[][]{
                {"Mã môn học", DisplayTextUtil.defaultText(selectedItem.getSubjectCode())},
                {"Tên môn học", DisplayTextUtil.defaultText(selectedItem.getSubjectName())},
                {"Số tín chỉ", DisplayTextUtil.defaultText(selectedItem.getCredits())},
                {"Khoa phụ trách", selectedItem.getFaculty() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(selectedItem.getFaculty().getFacultyName())},
                {"Mô tả", DisplayTextUtil.defaultText(selectedItem.getDescription())},
                {"Điều kiện tiên quyết", "Chưa cập nhật"}
        });
    }

    /**
     * Tạo hộp thoại chi tiết.
     */
    @Override
    protected BaseDetailDialog createDetailDialog(javax.swing.JComponent detailPanel) {
        return new SubjectDetailDialog(detailPanel);
    }

    /**
     * Xử lý prompt for entity.
     */
    @Override
    protected Subject promptForEntity(Subject existingItem) {
        SubjectFormDialog.SubjectFormResult formResult = SubjectFormDialog.showDialog(
                this,
                new SubjectFormDialog.SubjectFormModel(
                        existingItem == null ? "Thêm môn học" : "Cập nhật môn học",
                        existingItem == null ? "" : existingItem.getSubjectCode(),
                        existingItem == null ? "" : existingItem.getSubjectName(),
                        existingItem == null || existingItem.getCredits() == null ? "" : String.valueOf(existingItem.getCredits()),
                        existingItem == null ? "" : existingItem.getDescription(),
                        facultyController.getFacultiesForSelection(),
                        existingItem == null ? null : existingItem.getFaculty()
                )
        );
        if (formResult == null) {
            return null;
        }

        Subject subject = existingItem == null ? new Subject() : existingItem;
        subject.setSubjectCode(formResult.subjectCode().trim());
        subject.setSubjectName(formResult.subjectName().trim());
        subject.setCredits(Integer.parseInt(formResult.credits().trim()));
        subject.setFaculty(formResult.faculty());
        subject.setDescription(formResult.description().trim());
        return subject;
    }

    /**
     * Lưu entity.
     */
    @Override
    protected void saveEntity(Subject item) {
        subjectController.saveSubject(item);
    }

    /**
     * Xóa entity.
     */
    @Override
    protected void deleteEntity(Subject item) {
        subjectController.deleteSubject(item.getId());
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
                BorderFactory.createTitledBorder("Bộ lọc môn học"),
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
     * Làm mới lọc values.
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
}
