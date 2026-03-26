package com.qlsv.view.admin;

import com.qlsv.controller.CourseSectionController;
import com.qlsv.controller.FacultyController;
import com.qlsv.controller.LecturerController;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Faculty;
import com.qlsv.model.Lecturer;
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
import java.util.List;
import java.util.stream.Collectors;

public class LecturerManagementPanel extends AbstractCrudPanel<Lecturer> {

    private static final String FILTER_NONE = "Chọn điều kiện lọc";
    private static final String FILTER_ALL = "Tất cả giảng viên";
    private static final String FILTER_FACULTY = "Theo khoa";

    private final LecturerController lecturerController = new LecturerController();
    private final FacultyController facultyController = new FacultyController();
    private final CourseSectionController courseSectionController = new CourseSectionController();

    private final JComboBox<String> filterTypeComboBox = new JComboBox<>(new String[]{FILTER_NONE, FILTER_ALL, FILTER_FACULTY});
    private final JComboBox<FilterOption<?>> filterValueComboBox = new JComboBox<>();
    private final DetailSectionPanel detailSectionPanel = new DetailSectionPanel(
            "Chi tiết thông tin giảng viên",
            "Vui lòng chọn giảng viên để xem chi tiết."
    );

    private boolean filterReady;

    public LecturerManagementPanel() {
        super("Quản lý giảng viên");
        setFilterPanel(buildFilterPanel());
        setDetailPanel(detailSectionPanel);
        reloadFilterValues();
        refreshData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Mã giảng viên", "Họ và tên", "Email", "Khoa", "Trạng thái"};
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
        String rooms = DisplayTextUtil.joinUniqueTexts(assignedCourseSections.stream()
                .map(courseSection -> courseSection.getRoom() == null ? null : courseSection.getRoom().getRoomName())
                .collect(Collectors.toList()));

        detailSectionPanel.showFields(new String[][]{
                {"Mã giảng viên", DisplayTextUtil.defaultText(selectedItem.getLecturerCode())},
                {"Họ và tên", DisplayTextUtil.defaultText(selectedItem.getFullName())},
                {"Giới tính", "Chưa cập nhật"},
                {"Ngày sinh", "Chưa cập nhật"},
                {"Số điện thoại", DisplayTextUtil.defaultText(selectedItem.getPhone())},
                {"Email", DisplayTextUtil.defaultText(selectedItem.getEmail())},
                {"Địa chỉ", DisplayTextUtil.defaultText(selectedItem.getAddress())},
                {"Khoa", selectedItem.getFaculty() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(selectedItem.getFaculty().getFacultyName())},
                {"Chức vụ", "Chưa cập nhật"},
                {"Môn giảng dạy", subjects},
                {"Phòng học phụ trách", rooms},
                {"Trạng thái", DisplayTextUtil.formatStatus(selectedItem.getStatus())}
        });
    }

    @Override
    protected Lecturer promptForEntity(Lecturer existingItem) {
        JTextField userIdField = new JTextField(existingItem == null || existingItem.getUserId() == null ? "" : String.valueOf(existingItem.getUserId()));
        JTextField codeField = new JTextField(existingItem == null ? "" : existingItem.getLecturerCode());
        JTextField nameField = new JTextField(existingItem == null ? "" : existingItem.getFullName());
        JTextField emailField = new JTextField(existingItem == null ? "" : existingItem.getEmail());
        JTextField phoneField = new JTextField(existingItem == null ? "" : existingItem.getPhone());
        JTextField addressField = new JTextField(existingItem == null ? "" : existingItem.getAddress());
        JComboBox<Faculty> facultyComboBox = new JComboBox<>(facultyController.getFacultiesForSelection().toArray(new Faculty[0]));
        JComboBox<FilterOption<String>> statusComboBox = new JComboBox<>(new DefaultComboBoxModel<>(new FilterOption[]{
                new FilterOption<>("Đang hoạt động", "ACTIVE"),
                new FilterOption<>("Ngừng hoạt động", "INACTIVE")
        }));

        if (existingItem != null && existingItem.getFaculty() != null) {
            facultyComboBox.setSelectedItem(existingItem.getFaculty());
        }
        selectStatus(statusComboBox, existingItem == null ? "ACTIVE" : existingItem.getStatus());

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.add(new JLabel("ID người dùng"));
        formPanel.add(userIdField);
        formPanel.add(new JLabel("Mã giảng viên"));
        formPanel.add(codeField);
        formPanel.add(new JLabel("Họ và tên"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Email"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Số điện thoại"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Địa chỉ"));
        formPanel.add(addressField);
        formPanel.add(new JLabel("Khoa"));
        formPanel.add(facultyComboBox);
        formPanel.add(new JLabel("Trạng thái"));
        formPanel.add(statusComboBox);

        int result = JOptionPane.showConfirmDialog(
                this,
                formPanel,
                existingItem == null ? "Thêm giảng viên" : "Cập nhật giảng viên",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        Lecturer lecturer = existingItem == null ? new Lecturer() : existingItem;
        lecturer.setUserId(userIdField.getText().isBlank() ? null : Long.parseLong(userIdField.getText().trim()));
        lecturer.setLecturerCode(codeField.getText().trim());
        lecturer.setFullName(nameField.getText().trim());
        lecturer.setEmail(emailField.getText().trim());
        lecturer.setPhone(phoneField.getText().trim());
        lecturer.setAddress(addressField.getText().trim());
        lecturer.setFaculty((Faculty) facultyComboBox.getSelectedItem());
        FilterOption<String> selectedStatus = (FilterOption<String>) statusComboBox.getSelectedItem();
        lecturer.setStatus(selectedStatus == null ? "ACTIVE" : selectedStatus.value());
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
