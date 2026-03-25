package com.qlsv.view.admin;

import com.qlsv.controller.ClassRoomController;
import com.qlsv.controller.CourseSectionController;
import com.qlsv.controller.EnrollmentController;
import com.qlsv.controller.StudentController;
import com.qlsv.model.ClassRoom;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Student;
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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.util.List;

public class EnrollmentManagementPanel extends AbstractCrudPanel<Enrollment> {

    private static final String FILTER_NONE = "Chọn điều kiện lọc";
    private static final String FILTER_ALL = "Tất cả đăng ký";
    private static final String FILTER_SECTION_CODE = "Theo mã học phần";
    private static final String FILTER_CLASS_ROOM = "Theo lớp";
    private static final String FILTER_STUDENT = "Theo sinh viên";

    private final EnrollmentController enrollmentController = new EnrollmentController();
    private final StudentController studentController = new StudentController();
    private final CourseSectionController courseSectionController = new CourseSectionController();
    private final ClassRoomController classRoomController = new ClassRoomController();

    private final JComboBox<String> filterTypeComboBox = new JComboBox<>(
            new String[]{FILTER_NONE, FILTER_ALL, FILTER_SECTION_CODE, FILTER_CLASS_ROOM, FILTER_STUDENT}
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
        if (!filterReady) {
            return List.of();
        }

        String filterType = (String) filterTypeComboBox.getSelectedItem();
        return switch (filterType == null ? FILTER_NONE : filterType) {
            case FILTER_ALL -> enrollmentController.getAllEnrollments();
            case FILTER_SECTION_CODE -> {
                CourseSection courseSection = getSelectedFilterValue(CourseSection.class);
                yield courseSection == null ? List.of() : enrollmentController.getEnrollmentsByCourseSection(courseSection.getId());
            }
            case FILTER_CLASS_ROOM -> {
                ClassRoom classRoom = getSelectedFilterValue(ClassRoom.class);
                yield classRoom == null ? List.of() : enrollmentController.getEnrollmentsByClassRoom(classRoom.getId());
            }
            case FILTER_STUDENT -> {
                Student student = getSelectedFilterValue(Student.class);
                yield student == null ? List.of() : enrollmentController.getEnrollmentsByStudent(student.getId());
            }
            default -> List.of();
        };
    }

    @Override
    protected Object[] toRow(Enrollment item) {
        return new Object[]{
                item.getId(),
                item.getStudent() == null ? "" : item.getStudent().getFullName(),
                item.getCourseSection() == null ? "" : item.getCourseSection().getSectionCode(),
                DisplayTextUtil.formatStatus(item.getStatus()),
                DisplayTextUtil.formatDateTime(item.getEnrolledAt())
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

        detailSectionPanel.showFields(new String[][]{
                {"Mã sinh viên", selectedItem.getStudent() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(selectedItem.getStudent().getStudentCode())},
                {"Sinh viên", selectedItem.getStudent() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(selectedItem.getStudent().getFullName())},
                {"Lớp", selectedItem.getStudent() == null || selectedItem.getStudent().getClassRoom() == null
                        ? "Chưa cập nhật" : DisplayTextUtil.defaultText(selectedItem.getStudent().getClassRoom().getClassName())},
                {"Mã học phần", selectedItem.getCourseSection() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(selectedItem.getCourseSection().getSectionCode())},
                {"Môn học", selectedItem.getCourseSection() == null || selectedItem.getCourseSection().getSubject() == null
                        ? "Chưa cập nhật" : DisplayTextUtil.defaultText(selectedItem.getCourseSection().getSubject().getSubjectName())},
                {"Phòng học", selectedItem.getCourseSection() == null
                        ? "Chưa cập nhật" : DisplayTextUtil.defaultText(selectedItem.getCourseSection().getRoom())},
                {"Trạng thái", DisplayTextUtil.formatStatus(selectedItem.getStatus())},
                {"Thời gian đăng ký", DisplayTextUtil.formatDateTime(selectedItem.getEnrolledAt())}
        });
    }

    @Override
    protected Enrollment promptForEntity(Enrollment existingItem) {
        JComboBox<Student> studentComboBox = new JComboBox<>(studentController.getStudentsForSelection().toArray(new Student[0]));
        JComboBox<CourseSection> sectionComboBox = new JComboBox<>(courseSectionController.getAllCourseSectionsForSelection().toArray(new CourseSection[0]));
        JComboBox<FilterOption<String>> statusComboBox = new JComboBox<>(new DefaultComboBoxModel<>(new FilterOption[]{
                new FilterOption<>("Đã đăng ký", "REGISTERED"),
                new FilterOption<>("Đã hủy", "CANCELLED")
        }));

        if (existingItem != null) {
            if (existingItem.getStudent() != null) {
                studentComboBox.setSelectedItem(existingItem.getStudent());
            }
            if (existingItem.getCourseSection() != null) {
                sectionComboBox.setSelectedItem(existingItem.getCourseSection());
            }
        }
        selectStatus(statusComboBox, existingItem == null ? "REGISTERED" : existingItem.getStatus());

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.add(new JLabel("Sinh viên"));
        formPanel.add(studentComboBox);
        formPanel.add(new JLabel("Học phần"));
        formPanel.add(sectionComboBox);
        formPanel.add(new JLabel("Trạng thái"));
        formPanel.add(statusComboBox);

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

        Enrollment enrollment = existingItem == null ? new Enrollment() : existingItem;
        enrollment.setStudent((Student) studentComboBox.getSelectedItem());
        enrollment.setCourseSection((CourseSection) sectionComboBox.getSelectedItem());
        FilterOption<String> selectedStatus = (FilterOption<String>) statusComboBox.getSelectedItem();
        enrollment.setStatus(selectedStatus == null ? "REGISTERED" : selectedStatus.value());
        if (enrollment.getEnrolledAt() == null) {
            enrollment.setEnrolledAt(LocalDateTime.now());
        }
        return enrollment;
    }

    @Override
    protected void saveEntity(Enrollment item) {
        enrollmentController.saveEnrollment(item);
    }

    @Override
    protected void deleteEntity(Enrollment item) {
        enrollmentController.deleteEnrollment(item.getId());
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
            for (CourseSection courseSection : courseSectionController.getAllCourseSectionsForSelection()) {
                filterValueComboBox.addItem(new FilterOption<>(courseSection.getSectionCode(), courseSection));
            }
            return;
        }

        if (FILTER_CLASS_ROOM.equals(filterType)) {
            filterValueComboBox.setEnabled(true);
            filterValueComboBox.addItem(new FilterOption<>("Chọn lớp", null));
            for (ClassRoom classRoom : classRoomController.getClassRoomsForSelection()) {
                filterValueComboBox.addItem(new FilterOption<>(classRoom.getClassCode() + " - " + classRoom.getClassName(), classRoom));
            }
            return;
        }

        if (FILTER_STUDENT.equals(filterType)) {
            filterValueComboBox.setEnabled(true);
            filterValueComboBox.addItem(new FilterOption<>("Chọn sinh viên", null));
            for (Student student : studentController.getStudentsForSelection()) {
                filterValueComboBox.addItem(new FilterOption<>(student.getStudentCode() + " - " + student.getFullName(), student));
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
}
