package com.qlsv.view.admin;

import com.qlsv.controller.FacultyController;
import com.qlsv.model.Faculty;
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
import java.util.List;

public class FacultyManagementPanel extends AbstractCrudPanel<Faculty> {

    private static final String FILTER_NONE = "Chọn điều kiện lọc";
    private static final String FILTER_ALL = "Tất cả khoa";
    private static final String FILTER_CODE = "Theo mã khoa";

    private final FacultyController facultyController = new FacultyController();
    private final JComboBox<String> filterTypeComboBox = new JComboBox<>(new String[]{FILTER_NONE, FILTER_ALL, FILTER_CODE});
    private final JComboBox<FilterOption<?>> filterValueComboBox = new JComboBox<>();
    private final DetailSectionPanel detailSectionPanel = new DetailSectionPanel(
            "Chi tiết khoa",
            "Vui lòng chọn khoa để xem chi tiết."
    );

    private boolean filterReady;

    public FacultyManagementPanel() {
        super("Quản lý khoa");
        setFilterPanel(buildFilterPanel());
        setDetailPanel(detailSectionPanel);
        reloadFilterValues();
        refreshData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Mã khoa", "Tên khoa", "Mô tả"};
    }

    @Override
    protected List<Faculty> loadItems() {
        if (!filterReady) {
            return List.of();
        }

        String filterType = (String) filterTypeComboBox.getSelectedItem();
        if (FILTER_ALL.equals(filterType)) {
            return facultyController.getAllFaculties();
        }

        String facultyCode = getSelectedFilterValue(String.class);
        return facultyCode == null ? List.of() : facultyController.getFacultiesByCode(facultyCode);
    }

    @Override
    protected Object[] toRow(Faculty item) {
        return new Object[]{item.getId(), item.getFacultyCode(), item.getFacultyName(), item.getDescription()};
    }

    @Override
    protected String getEmptyStateMessage() {
        return filterReady
                ? "Không tìm thấy khoa phù hợp với điều kiện lọc hiện tại."
                : "Vui lòng chọn điều kiện lọc để hiển thị danh sách khoa.";
    }

    @Override
    protected void onSelectionChanged(Faculty selectedItem) {
        if (selectedItem == null) {
            detailSectionPanel.showMessage("Vui lòng chọn khoa để xem chi tiết.");
            return;
        }
        detailSectionPanel.showFields(new String[][]{
                {"Mã khoa", DisplayTextUtil.defaultText(selectedItem.getFacultyCode())},
                {"Tên khoa", DisplayTextUtil.defaultText(selectedItem.getFacultyName())},
                {"Mô tả", DisplayTextUtil.defaultText(selectedItem.getDescription())}
        });
    }

    @Override
    protected Faculty promptForEntity(Faculty existingItem) {
        JTextField codeField = new JTextField(existingItem == null ? "" : existingItem.getFacultyCode());
        JTextField nameField = new JTextField(existingItem == null ? "" : existingItem.getFacultyName());
        JTextField descriptionField = new JTextField(existingItem == null ? "" : existingItem.getDescription());

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.add(new JLabel("Mã khoa"));
        formPanel.add(codeField);
        formPanel.add(new JLabel("Tên khoa"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Mô tả"));
        formPanel.add(descriptionField);

        int result = JOptionPane.showConfirmDialog(
                this,
                formPanel,
                existingItem == null ? "Thêm khoa" : "Cập nhật khoa",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        Faculty faculty = existingItem == null ? new Faculty() : existingItem;
        faculty.setFacultyCode(codeField.getText().trim());
        faculty.setFacultyName(nameField.getText().trim());
        faculty.setDescription(descriptionField.getText().trim());
        return faculty;
    }

    @Override
    protected void saveEntity(Faculty item) {
        facultyController.saveFaculty(item);
    }

    @Override
    protected void deleteEntity(Faculty item) {
        facultyController.deleteFaculty(item.getId());
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
                BorderFactory.createTitledBorder("Bộ lọc khoa"),
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

        if (!FILTER_CODE.equals(filterTypeComboBox.getSelectedItem())) {
            filterValueComboBox.setEnabled(false);
            return;
        }

        filterValueComboBox.setEnabled(true);
        filterValueComboBox.addItem(new FilterOption<>("Chọn mã khoa", null));
        for (Faculty faculty : facultyController.getFacultiesForSelection()) {
            filterValueComboBox.addItem(new FilterOption<>(faculty.getFacultyCode(), faculty.getFacultyCode()));
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
}
