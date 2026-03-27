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

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    public EnrollmentManagementPanel() {
        super("Quản lý đăng ký học phần");
        setFilterPanel(buildFilterPanel());
        setDetailPanel(detailSectionPanel);
        reloadFilterValues();
        refreshData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Sinh viên", "Học phần", "Trạng thái", "Thời gian đăng ký"};
    }

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

    @Override
    protected String getEmptyStateMessage() {
        return filterReady
                ? "Không tìm thấy đăng ký học phần phù hợp với điều kiện lọc hiện tại."
                : "Vui lòng chọn điều kiện lọc để hiển thị danh sách đăng ký học phần.";
    }

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

    @Override
    protected Enrollment promptForEntity(Enrollment existingItem) {
        List<Student> allStudents = screenController.loadStudents();
        StudentSelectionState selectionState = new StudentSelectionState(existingItem == null ? null : existingItem.getStudent());

        JTextField studentSearchField = new JTextField(selectionState.selectedStudent == null
                ? ""
                : formatStudentDisplay(selectionState.selectedStudent));
        DefaultListModel<Student> suggestionModel = new DefaultListModel<>();
        JList<Student> suggestionList = new JList<>(suggestionModel);
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setVisibleRowCount(6);
        suggestionList.setFixedCellHeight(26);

        JScrollPane suggestionScrollPane = new JScrollPane(suggestionList);
        suggestionScrollPane.setPreferredSize(new Dimension(280, 120));

        JLabel selectedStudentLabel = new JLabel();
        updateSelectedStudentLabel(selectedStudentLabel, selectionState.selectedStudent);

        JComboBox<CourseSection> sectionComboBox = new JComboBox<>(screenController.loadCourseSections().toArray(new CourseSection[0]));
        JComboBox<FilterOption<String>> statusComboBox = new JComboBox<>(new DefaultComboBoxModel<>(new FilterOption[]{
                new FilterOption<>("Đã đăng ký", "REGISTERED"),
                new FilterOption<>("Đã hủy", "CANCELLED")
        }));

        if (existingItem != null && existingItem.getCourseSection() != null) {
            sectionComboBox.setSelectedItem(existingItem.getCourseSection());
        }
        selectStatus(statusComboBox, existingItem == null ? "REGISTERED" : existingItem.getStatus());

        boolean[] applyingSelection = {false};
        Runnable refreshSuggestions = () -> {
            suggestionModel.clear();
            List<Student> matchedStudents = filterStudentsByKeyword(allStudents, studentSearchField.getText());
            for (Student student : matchedStudents) {
                suggestionModel.addElement(student);
            }
            if (selectionState.selectedStudent != null) {
                suggestionList.setSelectedValue(selectionState.selectedStudent, true);
            } else if (!suggestionModel.isEmpty()) {
                suggestionList.setSelectedIndex(0);
            }
        };

        Runnable commitSelectedStudent = () -> {
            Student selectedStudent = suggestionList.getSelectedValue();
            if (selectedStudent == null) {
                return;
            }
            selectionState.selectedStudent = selectedStudent;
            applyingSelection[0] = true;
            studentSearchField.setText(formatStudentDisplay(selectedStudent));
            applyingSelection[0] = false;
            updateSelectedStudentLabel(selectedStudentLabel, selectedStudent);
            refreshSuggestions.run();
        };

        studentSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                handleSearchChange();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                handleSearchChange();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                handleSearchChange();
            }

            private void handleSearchChange() {
                if (!applyingSelection[0]) {
                    selectionState.selectedStudent = null;
                    updateSelectedStudentLabel(selectedStudentLabel, null);
                }
                refreshSuggestions.run();
            }
        });

        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    commitSelectedStudent.run();
                }
            }
        });
        suggestionList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    event.consume();
                    commitSelectedStudent.run();
                }
            }
        });

        refreshSuggestions.run();
        updateSelectedStudentLabel(selectedStudentLabel, selectionState.selectedStudent);

        JPanel studentPickerPanel = new JPanel(new BorderLayout(0, 6));
        studentPickerPanel.setOpaque(false);
        studentPickerPanel.add(studentSearchField, BorderLayout.NORTH);
        studentPickerPanel.add(suggestionScrollPane, BorderLayout.CENTER);
        studentPickerPanel.add(selectedStudentLabel, BorderLayout.SOUTH);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.add(new JLabel("Tìm sinh viên"));
        formPanel.add(studentPickerPanel);
        formPanel.add(new JLabel("Học phần"));
        formPanel.add(sectionComboBox);
        formPanel.add(new JLabel("Trạng thái"));
        formPanel.add(statusComboBox);

        while (true) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    formPanel,
                    existingItem == null ? "Thêm đăng ký học phần" : "Cập nhật đăng ký học phần",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
            if (result != JOptionPane.OK_OPTION) {
                return null;
            }
            if (selectionState.selectedStudent == null) {
                DialogUtil.showError(this, "Hãy chọn một sinh viên từ danh sách gợi ý.");
                continue;
            }

            FilterOption<String> selectedStatus = (FilterOption<String>) statusComboBox.getSelectedItem();
            return screenController.applyFormData(
                    existingItem,
                    new EnrollmentManagementScreenController.EnrollmentFormData(
                            selectionState.selectedStudent,
                            (CourseSection) sectionComboBox.getSelectedItem(),
                            selectedStatus == null ? "REGISTERED" : selectedStatus.value()
                    )
            );
        }
    }

    @Override
    protected void saveEntity(Enrollment item) {
        screenController.saveEnrollment(item);
    }

    @Override
    protected void deleteEntity(Enrollment item) {
        screenController.deleteEnrollment(item);
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
                BorderFactory.createTitledBorder("Bộ lọc đăng ký học phần"),
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
        if (FILTER_SECTION_CODE.equals(filterType)) {
            filterValueComboBox.setEnabled(true);
            filterValueComboBox.addItem(new FilterOption<>("Chọn học phần", null));
            for (CourseSection courseSection : screenController.loadCourseSections()) {
                filterValueComboBox.addItem(new FilterOption<>(courseSection.getSectionCode(), courseSection));
            }
            return;
        }

        if (FILTER_CLASS_ROOM.equals(filterType)) {
            filterValueComboBox.setEnabled(true);
            filterValueComboBox.addItem(new FilterOption<>("Chọn lớp", null));
            for (ClassRoom classRoom : screenController.loadClassRooms()) {
                filterValueComboBox.addItem(new FilterOption<>(classRoom.getClassCode() + " - " + classRoom.getClassName(), classRoom));
            }
            return;
        }

        if (FILTER_FACULTY.equals(filterType)) {
            filterValueComboBox.setEnabled(true);
            filterValueComboBox.addItem(new FilterOption<>("Chọn khoa", null));
            for (Faculty faculty : screenController.loadFaculties()) {
                filterValueComboBox.addItem(new FilterOption<>(faculty.getFacultyCode() + " - " + faculty.getFacultyName(), faculty));
            }
            return;
        }

        filterValueComboBox.setEnabled(false);
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

    private String formatStudentDisplay(Student student) {
        if (student == null) {
            return "";
        }
        return student.getStudentCode() + " - " + student.getFullName();
    }

    private void updateSelectedStudentLabel(JLabel label, Student student) {
        label.setText(student == null
                ? "Chưa chọn sinh viên"
                : "Đã chọn: " + formatStudentDisplay(student));
    }

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

        private StudentSelectionState(Student selectedStudent) {
            this.selectedStudent = selectedStudent;
        }
    }
}
