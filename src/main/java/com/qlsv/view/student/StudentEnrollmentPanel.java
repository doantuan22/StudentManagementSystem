/**
 * Màn hình sinh viên cho đăng ký.
 */
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
import javax.swing.Box;
import javax.swing.BoxLayout;
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
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
    private static final int CONTROL_GAP = 8;

    private final StudentEnrollmentScreenController screenController = new StudentEnrollmentScreenController();

    private final JTextField searchField = new JTextField();
    private final JComboBox<String> semesterFilterComboBox = new JComboBox<>(new String[]{ALL_SEMESTERS});
    private final JLabel availableSummaryLabel = new JLabel("Đang tải dữ liệu...");
    private final JLabel registeredSummaryLabel = new JLabel("Đang tải dữ liệu...");

    private final List<CourseSection> displayedCourseSections = new ArrayList<>();
    private final List<Enrollment> currentEnrollments = new ArrayList<>();

    private final DefaultTableModel courseSectionTableModel = new DefaultTableModel(
            new String[]{"ID", "Học phần", "Môn học", "Tín chỉ", "Sĩ số", "Giảng viên", "Học kỳ", "Năm học", "Lịch"}, 0) {
        /**
         * Xác định ô có cho phép chỉnh sửa hay không.
         */
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final DefaultTableModel enrollmentTableModel = new DefaultTableModel(
            new String[]{"ID", "Học phần", "Môn học", "Giảng viên", "Học kỳ", "Năm học", "Trạng thái", "Đăng ký lúc"}, 0) {
        /**
         * Xác định ô có cho phép chỉnh sửa hay không.
         */
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable courseSectionTable = new JTable(courseSectionTableModel);
    private final JTable enrollmentTable = new JTable(enrollmentTableModel);

    /**
     * Khởi tạo sinh viên đăng ký.
     */
    public StudentEnrollmentPanel() {
        setOpaque(true);
        setBackground(AppColors.CONTENT_BACKGROUND);
        initComponents();
        reloadData();
    }

    /**
     * Khởi tạo các thành phần giao diện.
     */
    private void initComponents() {
        JLabel titleLabel = new JLabel("Đăng ký học phần");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        JLabel subtitleLabel = new JLabel("Tìm lớp học phần phù hợp và theo dõi các đăng ký hiện có ngay trên cùng màn hình.");
        subtitleLabel.setForeground(AppColors.CARD_MUTED_TEXT);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitleLabel);

        JButton filterButton = new JButton("Tìm kiếm");
        JButton reloadButton = new JButton("Tải lại");
        JButton registerButton = new JButton("Đăng ký học phần");
        JButton cancelButton = new JButton("Hủy đăng ký");

        styleTextField(searchField, 280);
        searchField.setMinimumSize(new Dimension(220, 38));
        styleComboBox(semesterFilterComboBox, 180);
        semesterFilterComboBox.setMinimumSize(new Dimension(150, 38));
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
        gbc.insets = new Insets(0, 0, 0, CONTROL_GAP);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        filterFieldsPanel.add(createCaptionLabel("Tìm kiếm"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filterFieldsPanel.add(searchField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        filterFieldsPanel.add(createCaptionLabel("Học kỳ"), gbc);

        gbc.gridx = 3;
        gbc.insets = new Insets(0, 0, 0, 0);
        filterFieldsPanel.add(semesterFilterComboBox, gbc);

        JPanel utilityActionPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, CONTROL_GAP, 0));
        utilityActionPanel.setOpaque(false);
        utilityActionPanel.add(filterButton);
        utilityActionPanel.add(reloadButton);

        JPanel enrollmentActionPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, CONTROL_GAP, 0));
        enrollmentActionPanel.setOpaque(false);
        enrollmentActionPanel.add(registerButton);
        enrollmentActionPanel.add(cancelButton);

        JPanel controlFooterPanel = new JPanel(new BorderLayout(12, 8));
        controlFooterPanel.setOpaque(false);
        controlFooterPanel.add(utilityActionPanel, BorderLayout.WEST);
        controlFooterPanel.add(enrollmentActionPanel, BorderLayout.EAST);

        JPanel controlCard = createSurfaceCard(new BorderLayout(0, 12));
        controlCard.add(filterFieldsPanel, BorderLayout.NORTH);
        controlCard.add(controlFooterPanel, BorderLayout.SOUTH);

        configureTable(courseSectionTable);
        configureTable(enrollmentTable);
        configureCourseSectionColumns();
        configureEnrollmentColumns();
        courseSectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        enrollmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane courseScrollPane = createTableScrollPane(courseSectionTable);
        JScrollPane enrollmentScrollPane = createTableScrollPane(enrollmentTable);

        JPanel coursePanel = createSectionPanel(
                "Danh sách học phần mở",
                "Xem nhanh các học phần có thể đăng ký theo bộ lọc hiện tại.",
                availableSummaryLabel,
                courseScrollPane
        );
        JPanel enrollmentPanel = createSectionPanel(
                "Các học phần đã đăng ký",
                "Theo dõi trạng thái đăng ký hiện có và hủy khi cần.",
                registeredSummaryLabel,
                enrollmentScrollPane
        );

        coursePanel.setMinimumSize(new Dimension(0, 240));
        enrollmentPanel.setMinimumSize(new Dimension(0, 220));

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

    /**
     * Đăng ký học phần đã chọn.
     */
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

    /**
     * Kiểm tra khả năng cel đã chọn đăng ký.
     */
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

    /**
     * Làm mới dữ liệu đang hiển thị.
     */
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

    /**
     * Đồng bộ học kỳ options.
     */
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

    /**
     * Xử lý bảng refill khóa học.
     */
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

    /**
     * Xử lý bảng refill đăng ký.
     */
    private void refillEnrollmentTable(List<EnrollmentDisplayDto> rows) {
        enrollmentTableModel.setRowCount(0);
        for (EnrollmentDisplayDto row : rows) {
            enrollmentTableModel.addRow(new Object[]{
                    row.id(),
                    row.sectionCode(),
                    row.subjectName(),
                    row.lecturerName(),
                    row.semester(),
                    row.schoolYear(),
                    row.statusText(),
                    row.enrolledAtText()
            });
        }
    }

    /**
     * Tạo label caption.
     */
    private JLabel createCaptionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(AppColors.CARD_TITLE_TEXT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        return label;
    }

    /**
     * Tạo card surface.
     */
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

    /**
     * Tạo panel phần.
     */
    private JPanel createSectionPanel(String title, String description, JLabel summaryLabel, JComponent content) {
        JPanel panel = createSurfaceCard(new BorderLayout(0, 12));

        summaryLabel.setForeground(AppColors.CARD_MUTED_TEXT);
        summaryLabel.setFont(summaryLabel.getFont().deriveFont(Font.PLAIN, 12.5f));
        summaryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel headingPanel = new JPanel();
        headingPanel.setOpaque(false);
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(AppColors.CARD_TITLE_TEXT);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setForeground(AppColors.CARD_MUTED_TEXT);
        descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(Font.PLAIN, 12.5f));
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        headingPanel.add(titleLabel);
        headingPanel.add(Box.createVerticalStrut(4));
        headingPanel.add(descriptionLabel);
        headingPanel.add(Box.createVerticalStrut(6));
        headingPanel.add(summaryLabel);

        panel.add(headingPanel, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Tạo bảng scroll pane.
     */
    private JScrollPane createTableScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.CARD_BORDER));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.setMinimumSize(new Dimension(0, 180));
        return scrollPane;
    }

    /**
     * Thiết lập bảng.
     */
    private void configureTable(JTable table) {
        table.setRowHeight(34);
        table.setFillsViewportHeight(true);
        table.setGridColor(AppColors.CARD_BORDER);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(AppColors.TABLE_SELECTION_BACKGROUND);
        table.setSelectionForeground(AppColors.CARD_VALUE_TEXT);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(AppColors.TABLE_HEADER_BACKGROUND);
        table.getTableHeader().setForeground(AppColors.CARD_VALUE_TEXT);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 13f));
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));
    }

    /**
     * Thiết lập học phần columns.
     */
    private void configureCourseSectionColumns() {
        TableColumnModel columnModel = courseSectionTable.getColumnModel();
        int[] widths = {70, 130, 250, 80, 100, 180, 110, 110, 240};
        for (int index = 0; index < widths.length && index < columnModel.getColumnCount(); index++) {
            columnModel.getColumn(index).setPreferredWidth(widths[index]);
        }
    }

    /**
     * Thiết lập đăng ký columns.
     */
    private void configureEnrollmentColumns() {
        TableColumnModel columnModel = enrollmentTable.getColumnModel();
        int[] widths = {70, 130, 260, 180, 100, 100, 130, 170};
        for (int index = 0; index < widths.length && index < columnModel.getColumnCount(); index++) {
            columnModel.getColumn(index).setPreferredWidth(widths[index]);
        }
    }

    /**
     * Ẩn column.
     */
    private void hideColumn(JTable table, int columnIndex) {
        if (table.getColumnModel().getColumnCount() <= columnIndex) {
            return;
        }
        table.getColumnModel().getColumn(columnIndex).setMinWidth(0);
        table.getColumnModel().getColumn(columnIndex).setMaxWidth(0);
        table.getColumnModel().getColumn(columnIndex).setPreferredWidth(0);
    }

    /**
     * Áp dụng kiểu cho trường văn bản.
     */
    private void styleTextField(JTextField textField, int preferredWidth) {
        textField.setPreferredSize(new Dimension(preferredWidth, 38));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.INPUT_BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        textField.setFont(textField.getFont().deriveFont(Font.PLAIN, 13.5f));
    }

    /**
     * Áp dụng kiểu cho chọn box.
     */
    private void styleComboBox(JComboBox<String> comboBox, int preferredWidth) {
        comboBox.setPreferredSize(new Dimension(preferredWidth, 38));
        comboBox.setFont(comboBox.getFont().deriveFont(Font.PLAIN, 13.5f));
    }

    /**
     * Áp dụng kiểu cho nút thao tác.
     */
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
