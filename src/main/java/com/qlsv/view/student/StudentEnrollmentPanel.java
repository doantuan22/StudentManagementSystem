package com.qlsv.view.student;

import com.qlsv.controller.StudentEnrollmentScreenController;
import com.qlsv.dto.CourseSectionDisplayDto;
import com.qlsv.dto.EnrollmentDisplayDto;
import com.qlsv.dto.StudentEnrollmentDataDto;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BasePanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

public class StudentEnrollmentPanel extends BasePanel {

    private static final String ALL_SEMESTERS = "Tất cả học kỳ";

    private final StudentEnrollmentScreenController screenController = new StudentEnrollmentScreenController();

    private final JTextField searchField = new JTextField();
    private final JComboBox<String> semesterFilterComboBox = new JComboBox<>(new String[]{ALL_SEMESTERS});
    private final JLabel availableSummaryLabel = new JLabel("Đang tải dữ liệu...");
    private final JLabel registeredSummaryLabel = new JLabel("Đang tải dữ liệu...");

    private final List<CourseSection> displayedCourseSections = new ArrayList<>();
    private final List<Enrollment> currentEnrollments = new ArrayList<>();

    private final DefaultTableModel courseSectionTableModel = new DefaultTableModel(
            new String[]{"ID", "Học phần", "Môn học", "Tín chỉ", "Sĩ số", "Giảng viên", "Học kỳ", "Năm học", "Lịch"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final DefaultTableModel enrollmentTableModel = new DefaultTableModel(
            new String[]{"ID", "Học phần", "Môn học", "Giảng viên", "Trạng thái", "Đăng ký lúc"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable courseSectionTable = new JTable(courseSectionTableModel);
    private final JTable enrollmentTable = new JTable(enrollmentTableModel);

    public StudentEnrollmentPanel() {
        setOpaque(true);
        setBackground(AppColors.CONTENT_BACKGROUND);
        initComponents();
        reloadData();
    }

    private void initComponents() {
        JLabel titleLabel = new JLabel("Đăng ký học phần");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        JPanel titlePanel = new JPanel(new BorderLayout(0, 6));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.NORTH);

        JButton filterButton = new JButton("Lọc danh sách");
        JButton reloadButton = new JButton("Tải lại");
        JButton registerButton = new JButton("Đăng ký học phần");
        JButton cancelButton = new JButton("Hủy đăng ký");

        styleTextField(searchField, 280);
        styleComboBox(semesterFilterComboBox, 170);
        styleActionButton(filterButton, AppColors.BUTTON_PRIMARY);
        styleActionButton(reloadButton, AppColors.BUTTON_NEUTRAL);
        styleActionButton(registerButton, AppColors.BUTTON_SUCCESS);
        styleActionButton(cancelButton, AppColors.BUTTON_DANGER);

        filterButton.addActionListener(event -> reloadData());
        registerButton.addActionListener(event -> registerSelectedCourseSection());
        cancelButton.addActionListener(event -> cancelSelectedEnrollment());
        reloadButton.addActionListener(event -> {
            searchField.setText("");
            semesterFilterComboBox.setSelectedItem(ALL_SEMESTERS);
            reloadData();
        });

        JPanel filterFieldsPanel = new JPanel(new GridBagLayout());
        filterFieldsPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        filterFieldsPanel.add(createCaptionLabel("Tìm kiếm"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filterFieldsPanel.add(searchField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        filterFieldsPanel.add(createCaptionLabel("Học kỳ"), gbc);

        gbc.gridx = 3;
        filterFieldsPanel.add(semesterFilterComboBox, gbc);

        gbc.gridx = 4;
        filterFieldsPanel.add(filterButton, gbc);

        gbc.gridx = 5;
        gbc.insets = new Insets(0, 0, 0, 0);
        filterFieldsPanel.add(reloadButton, gbc);

        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setOpaque(false);
        JLabel actionHintLabel = new JLabel("Chọn một dòng ở từng bảng để đăng ký hoặc hủy đăng ký đúng học phần.");
        actionHintLabel.setForeground(AppColors.CARD_MUTED_TEXT);
        actionHintLabel.setFont(actionHintLabel.getFont().deriveFont(Font.PLAIN, 12.5f));
        actionPanel.add(actionHintLabel, BorderLayout.WEST);

        JPanel actionButtonsPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        actionButtonsPanel.setOpaque(false);
        actionButtonsPanel.add(registerButton);
        actionButtonsPanel.add(cancelButton);
        actionPanel.add(actionButtonsPanel, BorderLayout.EAST);

        JPanel controlCard = createSurfaceCard(new BorderLayout(0, 12));
        controlCard.add(filterFieldsPanel, BorderLayout.NORTH);
        controlCard.add(actionPanel, BorderLayout.SOUTH);

        configureTable(courseSectionTable);
        configureTable(enrollmentTable);
        courseSectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        enrollmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane courseScrollPane = createTableScrollPane(courseSectionTable);
        JScrollPane enrollmentScrollPane = createTableScrollPane(enrollmentTable);

        JPanel coursePanel = createSectionPanel("Danh sách học phần mở", availableSummaryLabel, courseScrollPane);
        JPanel enrollmentPanel = createSectionPanel("Các học phần đã đăng ký", registeredSummaryLabel, enrollmentScrollPane);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, coursePanel, enrollmentPanel);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        splitPane.setResizeWeight(0.58);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerSize(10);

        JPanel headerPanel = new JPanel(new BorderLayout(0, 12));
        headerPanel.setOpaque(false);
        headerPanel.add(titlePanel, BorderLayout.NORTH);
        headerPanel.add(controlCard, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.58));
    }

    private void registerSelectedCourseSection() {
        int selectedRow = courseSectionTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= displayedCourseSections.size()) {
            DialogUtil.showError(this, "Hãy chọn một học phần trong danh sách để đăng ký.");
            return;
        }
        try {
            screenController.registerCourseSection(displayedCourseSections.get(selectedRow));
            DialogUtil.showInfo(this, "Đăng ký học phần thành công.");
            reloadData();
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void cancelSelectedEnrollment() {
        int selectedRow = enrollmentTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentEnrollments.size()) {
            DialogUtil.showError(this, "Hãy chọn học phần đã đăng ký cần hủy.");
            return;
        }
        try {
            screenController.cancelEnrollment(currentEnrollments.get(selectedRow));
            DialogUtil.showInfo(this, "Hủy đăng ký học phần thành công.");
            reloadData();
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    @Override
    public void reloadData() {
        try {
            StudentEnrollmentDataDto data = screenController.loadData(
                    searchField.getText(),
                    semesterFilterComboBox.getSelectedItem() == null ? ALL_SEMESTERS : semesterFilterComboBox.getSelectedItem().toString()
            );

            syncSemesterOptions(data.semesterOptions());
            displayedCourseSections.clear();
            displayedCourseSections.addAll(data.displayedCourseSections());
            currentEnrollments.clear();
            currentEnrollments.addAll(data.currentEnrollments());

            refillCourseTable(data.availableCourseRows());
            refillEnrollmentTable(data.enrollmentRows());
            availableSummaryLabel.setText(data.availableSummary());
            registeredSummaryLabel.setText(data.registeredSummary());

            hideColumn(courseSectionTable, 0);
            hideColumn(enrollmentTable, 0);
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void syncSemesterOptions(List<String> semesterOptions) {
        Object selected = semesterFilterComboBox.getSelectedItem();
        semesterFilterComboBox.removeAllItems();
        for (String option : semesterOptions) {
            semesterFilterComboBox.addItem(option);
        }

        String preferred = selected == null ? ALL_SEMESTERS : selected.toString();
        boolean exists = semesterOptions.stream().anyMatch(option -> option.equalsIgnoreCase(preferred));
        semesterFilterComboBox.setSelectedItem(exists ? preferred : ALL_SEMESTERS);
    }

    private void refillCourseTable(List<CourseSectionDisplayDto> rows) {
        courseSectionTableModel.setRowCount(0);
        for (CourseSectionDisplayDto row : rows) {
            courseSectionTableModel.addRow(new Object[]{
                    row.id(),
                    row.sectionCode(),
                    row.subjectName(),
                    row.credits(),
                    row.slotsText(),
                    row.lecturerName(),
                    row.semester(),
                    row.schoolYear(),
                    row.scheduleText()
            });
        }
    }

    private void refillEnrollmentTable(List<EnrollmentDisplayDto> rows) {
        enrollmentTableModel.setRowCount(0);
        for (EnrollmentDisplayDto row : rows) {
            enrollmentTableModel.addRow(new Object[]{
                    row.id(),
                    row.sectionCode(),
                    row.subjectName(),
                    row.lecturerName(),
                    row.statusText(),
                    row.enrolledAtText()
            });
        }
    }

    private JLabel createCaptionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(AppColors.CARD_TITLE_TEXT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        return label;
    }

    private JPanel createSurfaceCard(BorderLayout layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(true);
        panel.setBackground(AppColors.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));
        return panel;
    }

    private JPanel createSectionPanel(String title, JLabel summaryLabel, JComponent content) {
        JPanel panel = createSurfaceCard(new BorderLayout(0, 12));

        JPanel headingPanel = new JPanel(new BorderLayout(12, 0));
        headingPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(AppColors.CARD_TITLE_TEXT);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));

        summaryLabel.setForeground(AppColors.CARD_MUTED_TEXT);
        summaryLabel.setFont(summaryLabel.getFont().deriveFont(Font.PLAIN, 12.5f));

        headingPanel.add(titleLabel, BorderLayout.WEST);
        headingPanel.add(summaryLabel, BorderLayout.EAST);

        panel.add(headingPanel, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane createTableScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.CARD_BORDER));
        scrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private void configureTable(JTable table) {
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setGridColor(AppColors.CARD_BORDER);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(AppColors.TABLE_SELECTION_BACKGROUND);
        table.setSelectionForeground(AppColors.CARD_VALUE_TEXT);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(AppColors.TABLE_HEADER_BACKGROUND);
        table.getTableHeader().setForeground(AppColors.CARD_VALUE_TEXT);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 13f));
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));
    }

    private void hideColumn(JTable table, int columnIndex) {
        if (table.getColumnModel().getColumnCount() <= columnIndex) {
            return;
        }
        table.getColumnModel().getColumn(columnIndex).setMinWidth(0);
        table.getColumnModel().getColumn(columnIndex).setMaxWidth(0);
        table.getColumnModel().getColumn(columnIndex).setPreferredWidth(0);
    }

    private void styleTextField(JTextField textField, int preferredWidth) {
        textField.setPreferredSize(new Dimension(preferredWidth, 38));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.INPUT_BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        textField.setFont(textField.getFont().deriveFont(Font.PLAIN, 13.5f));
    }

    private void styleComboBox(JComboBox<String> comboBox, int preferredWidth) {
        comboBox.setPreferredSize(new Dimension(preferredWidth, 38));
        comboBox.setFont(comboBox.getFont().deriveFont(Font.PLAIN, 13.5f));
    }

    private void styleActionButton(JButton button, Color background) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(background);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 13f));
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
    }
}
