package com.qlsv.view.admin;

import com.qlsv.controller.ClassRoomController;
import com.qlsv.controller.CourseSectionController;
import com.qlsv.controller.EnrollmentController;
import com.qlsv.controller.ScoreController;
import com.qlsv.model.ClassRoom;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Score;
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

public class ScoreManagementPanel extends AbstractCrudPanel<Score> {

    private static final String FILTER_NONE = "Chọn điều kiện lọc";
    private static final String FILTER_ALL = "Tất cả bảng điểm";
    private static final String FILTER_SECTION_CODE = "Theo mã học phần";
    private static final String FILTER_CLASS_ROOM = "Theo lớp";

    private final ScoreController scoreController = new ScoreController();
    private final EnrollmentController enrollmentController = new EnrollmentController();
    private final CourseSectionController courseSectionController = new CourseSectionController();
    private final ClassRoomController classRoomController = new ClassRoomController();

    private final JComboBox<String> filterTypeComboBox = new JComboBox<>(
            new String[]{FILTER_NONE, FILTER_ALL, FILTER_SECTION_CODE, FILTER_CLASS_ROOM}
    );
    private final JComboBox<FilterOption<?>> filterValueComboBox = new JComboBox<>();
    private final DetailSectionPanel detailSectionPanel = new DetailSectionPanel(
            "Chi tiết điểm",
            "Vui lòng chọn bản ghi điểm để xem chi tiết."
    );

    private boolean filterReady;

    public ScoreManagementPanel() {
        super("Quản lý điểm");
        setFilterPanel(buildFilterPanel());
        setDetailPanel(detailSectionPanel);
        reloadFilterValues();
        refreshData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Sinh viên", "Học phần", "QT", "GK", "CK", "Tổng kết", "Kết quả"};
    }

    @Override
    protected List<Score> loadItems() {
        if (!filterReady) {
            return List.of();
        }

        String filterType = (String) filterTypeComboBox.getSelectedItem();
        return switch (filterType == null ? FILTER_NONE : filterType) {
            case FILTER_ALL -> scoreController.getAllScores();
            case FILTER_SECTION_CODE -> {
                CourseSection courseSection = getSelectedFilterValue(CourseSection.class);
                yield courseSection == null ? List.of() : scoreController.getScoresByCourseSection(courseSection.getId());
            }
            case FILTER_CLASS_ROOM -> {
                ClassRoom classRoom = getSelectedFilterValue(ClassRoom.class);
                yield classRoom == null ? List.of() : scoreController.getScoresByClassRoom(classRoom.getId());
            }
            default -> List.of();
        };
    }

    @Override
    protected Object[] toRow(Score item) {
        Enrollment enrollment = item.getEnrollment();
        String studentName = enrollment == null || enrollment.getStudent() == null ? "" : enrollment.getStudent().getFullName();
        String sectionCode = enrollment == null || enrollment.getCourseSection() == null ? "" : enrollment.getCourseSection().getSectionCode();
        return new Object[]{
                item.getId(),
                studentName,
                sectionCode,
                item.getProcessScore(),
                item.getMidtermScore(),
                item.getFinalScore(),
                item.getTotalScore(),
                DisplayTextUtil.formatStatus(item.getResult())
        };
    }

    @Override
    protected String getEmptyStateMessage() {
        return filterReady
                ? "Không tìm thấy bản ghi điểm phù hợp với điều kiện lọc hiện tại."
                : "Vui lòng chọn điều kiện lọc để hiển thị danh sách điểm.";
    }

    @Override
    protected void onSelectionChanged(Score selectedItem) {
        if (selectedItem == null) {
            detailSectionPanel.showMessage("Vui lòng chọn bản ghi điểm để xem chi tiết.");
            return;
        }

        Enrollment enrollment = selectedItem.getEnrollment();
        detailSectionPanel.showFields(new String[][]{
                {"Mã sinh viên", enrollment == null || enrollment.getStudent() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(enrollment.getStudent().getStudentCode())},
                {"Sinh viên", enrollment == null || enrollment.getStudent() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(enrollment.getStudent().getFullName())},
                {"Lớp", enrollment == null || enrollment.getStudent() == null || enrollment.getStudent().getClassRoom() == null
                        ? "Chưa cập nhật" : DisplayTextUtil.defaultText(enrollment.getStudent().getClassRoom().getClassName())},
                {"Mã học phần", enrollment == null || enrollment.getCourseSection() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(enrollment.getCourseSection().getSectionCode())},
                {"Môn học", enrollment == null || enrollment.getCourseSection() == null || enrollment.getCourseSection().getSubject() == null
                        ? "Chưa cập nhật" : DisplayTextUtil.defaultText(enrollment.getCourseSection().getSubject().getSubjectName())},
                {"Phòng học", enrollment == null || enrollment.getCourseSection() == null
                        ? "Chưa cập nhật" : DisplayTextUtil.defaultText(enrollment.getCourseSection().getRoom())},
                {"Điểm quá trình", DisplayTextUtil.defaultText(selectedItem.getProcessScore())},
                {"Điểm giữa kỳ", DisplayTextUtil.defaultText(selectedItem.getMidtermScore())},
                {"Điểm cuối kỳ", DisplayTextUtil.defaultText(selectedItem.getFinalScore())},
                {"Điểm tổng kết", DisplayTextUtil.defaultText(selectedItem.getTotalScore())},
                {"Kết quả", DisplayTextUtil.formatStatus(selectedItem.getResult())}
        });
    }

    @Override
    protected Score promptForEntity(Score existingItem) {
        JComboBox<Enrollment> enrollmentComboBox = new JComboBox<>(enrollmentController.getAllEnrollments().toArray(new Enrollment[0]));
        JTextField processField = new JTextField(existingItem == null || existingItem.getProcessScore() == null ? "" : String.valueOf(existingItem.getProcessScore()));
        JTextField midtermField = new JTextField(existingItem == null || existingItem.getMidtermScore() == null ? "" : String.valueOf(existingItem.getMidtermScore()));
        JTextField finalField = new JTextField(existingItem == null || existingItem.getFinalScore() == null ? "" : String.valueOf(existingItem.getFinalScore()));

        if (existingItem != null && existingItem.getEnrollment() != null) {
            enrollmentComboBox.setSelectedItem(existingItem.getEnrollment());
        }

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.add(new JLabel("Đăng ký học phần"));
        formPanel.add(enrollmentComboBox);
        formPanel.add(new JLabel("Điểm quá trình"));
        formPanel.add(processField);
        formPanel.add(new JLabel("Điểm giữa kỳ"));
        formPanel.add(midtermField);
        formPanel.add(new JLabel("Điểm cuối kỳ"));
        formPanel.add(finalField);

        int result = JOptionPane.showConfirmDialog(
                this,
                formPanel,
                existingItem == null ? "Thêm điểm" : "Cập nhật điểm",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        Score score = existingItem == null ? new Score() : existingItem;
        score.setEnrollment((Enrollment) enrollmentComboBox.getSelectedItem());
        score.setProcessScore(parseScore(processField.getText()));
        score.setMidtermScore(parseScore(midtermField.getText()));
        score.setFinalScore(parseScore(finalField.getText()));
        return score;
    }

    @Override
    protected void saveEntity(Score item) {
        scoreController.saveScore(item);
    }

    @Override
    protected void deleteEntity(Score item) {
        scoreController.deleteScore(item.getId());
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
                BorderFactory.createTitledBorder("Bộ lọc điểm"),
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

    private Double parseScore(String rawValue) {
        return rawValue == null || rawValue.isBlank() ? 0.0 : Double.parseDouble(rawValue.trim());
    }
}
