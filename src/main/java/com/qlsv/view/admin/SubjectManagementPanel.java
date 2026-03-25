package com.qlsv.view.admin;

import com.qlsv.controller.FacultyController;
import com.qlsv.controller.SubjectController;
import com.qlsv.model.Faculty;
import com.qlsv.model.Subject;
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

    public SubjectManagementPanel() {
        super("Quản lý môn học");
        setFilterPanel(buildFilterPanel());
        setDetailPanel(detailSectionPanel);
        reloadFilterValues();
        refreshData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Mã môn học", "Tên môn học", "Số tín chỉ", "Khoa"};
    }

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

    @Override
    protected String getEmptyStateMessage() {
        return filterReady
                ? "Không tìm thấy môn học phù hợp với điều kiện lọc hiện tại."
                : "Vui lòng chọn điều kiện lọc để hiển thị danh sách môn học.";
    }

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

    @Override
    protected Subject promptForEntity(Subject existingItem) {
        JTextField codeField = new JTextField(existingItem == null ? "" : existingItem.getSubjectCode());
        JTextField nameField = new JTextField(existingItem == null ? "" : existingItem.getSubjectName());
        JTextField creditsField = new JTextField(existingItem == null ? "" : String.valueOf(existingItem.getCredits()));
        JTextField descriptionField = new JTextField(existingItem == null ? "" : existingItem.getDescription());
        JComboBox<Faculty> facultyComboBox = new JComboBox<>(facultyController.getFacultiesForSelection().toArray(new Faculty[0]));
        if (existingItem != null && existingItem.getFaculty() != null) {
            facultyComboBox.setSelectedItem(existingItem.getFaculty());
        }

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.add(new JLabel("Mã môn học"));
        formPanel.add(codeField);
        formPanel.add(new JLabel("Tên môn học"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Số tín chỉ"));
        formPanel.add(creditsField);
        formPanel.add(new JLabel("Khoa"));
        formPanel.add(facultyComboBox);
        formPanel.add(new JLabel("Mô tả"));
        formPanel.add(descriptionField);

        int result = JOptionPane.showConfirmDialog(
                this,
                formPanel,
                existingItem == null ? "Thêm môn học" : "Cập nhật môn học",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        Subject subject = existingItem == null ? new Subject() : existingItem;
        subject.setSubjectCode(codeField.getText().trim());
        subject.setSubjectName(nameField.getText().trim());
        subject.setCredits(Integer.parseInt(creditsField.getText().trim()));
        subject.setFaculty((Faculty) facultyComboBox.getSelectedItem());
        subject.setDescription(descriptionField.getText().trim());
        return subject;
    }

    @Override
    protected void saveEntity(Subject item) {
        subjectController.saveSubject(item);
    }

    @Override
    protected void deleteEntity(Subject item) {
        subjectController.deleteSubject(item.getId());
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

    private void reloadFilterValues() {
        filterReady = false;
        filterValueComboBox.removeAllItems();

        if (!FILTER_FACULTY.equals(filterTypeComboBox.getSelectedItem())) {
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
