/**
 * Hộp thoại lịch biểu mẫu dialog.
 */
package com.qlsv.view.dialog;

import com.qlsv.model.CourseSection;
import com.qlsv.model.Room;
import com.qlsv.view.common.AppColors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.List;

public class ScheduleFormDialog extends JDialog {

    private final JComboBox<CourseSection> courseSectionComboBox = new JComboBox<>();
    private final JComboBox<String> dayOfWeekComboBox = new JComboBox<>();
    private final JComboBox<Integer> startPeriodComboBox = new JComboBox<>();
    private final JComboBox<Integer> endPeriodComboBox = new JComboBox<>();
    private final JComboBox<Room> roomComboBox = new JComboBox<>();
    private final JTextArea noteArea = new JTextArea();

    private ScheduleFormResult result;

    /**
     * Khởi tạo lịch biểu mẫu.
     */
    private ScheduleFormDialog(Component parent, ScheduleFormModel model) {
        super(resolveOwner(parent), model.title(), Dialog.ModalityType.APPLICATION_MODAL);
        initComponents(model);
    }

    /**
     * Hiển thị hộp thoại.
     */
    public static ScheduleFormResult showDialog(Component parent, ScheduleFormModel model) {
        ScheduleFormDialog dialog = new ScheduleFormDialog(parent, model);
        dialog.setVisible(true);
        return dialog.result;
    }

    /**
     * Khởi tạo các thành phần giao diện.
     */
    private void initComponents(ScheduleFormModel model) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(AppColors.CARD_BACKGROUND);

        JPanel headerPanel = new JPanel(new BorderLayout(0, 6));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 0, 24));

        JLabel titleLabel = new JLabel(model.title());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);


        headerPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel bodyPanel = new JPanel();
        bodyPanel.setOpaque(false);
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(16, 24, 20, 24));
        bodyPanel.add(createCourseSectionSection());
        bodyPanel.add(Box.createVerticalStrut(24));
        bodyPanel.add(createScheduleSection());
        bodyPanel.add(Box.createVerticalStrut(24));
        bodyPanel.add(createNoteSection());

        JScrollPane scrollPane = new JScrollPane(bodyPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JButton cancelButton = new JButton("Hủy");
        styleSecondaryButton(cancelButton);
        cancelButton.addActionListener(event -> {
            result = null;
            dispose();
        });

        JButton saveButton = new JButton("Lưu");
        stylePrimaryButton(saveButton);
        saveButton.addActionListener(event -> handleSave());

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 20, 24));
        footerPanel.add(cancelButton);
        footerPanel.add(saveButton);

        rootPanel.add(headerPanel, BorderLayout.NORTH);
        rootPanel.add(scrollPane, BorderLayout.CENTER);
        rootPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
        bindModel(model);

        getRootPane().setDefaultButton(saveButton);
        setMinimumSize(new Dimension(720, 620));
        setSize(new Dimension(760, 660));
        setLocationRelativeTo(getOwner());
    }

    /**
     * Xử lý model bind.
     */
    private void bindModel(ScheduleFormModel model) {
        courseSectionComboBox.removeAllItems();
        for (CourseSection courseSection : model.courseSections()) {
            courseSectionComboBox.addItem(courseSection);
        }
        if (model.selectedCourseSection() != null) {
            courseSectionComboBox.setSelectedItem(model.selectedCourseSection());
        }
        courseSectionComboBox.setEnabled(model.courseSectionEditable());

        dayOfWeekComboBox.removeAllItems();
        for (String dayOfWeek : model.daysOfWeek()) {
            dayOfWeekComboBox.addItem(dayOfWeek);
        }
        dayOfWeekComboBox.setSelectedItem(model.selectedDayOfWeek());

        startPeriodComboBox.removeAllItems();
        endPeriodComboBox.removeAllItems();
        for (Integer option : model.periodOptions()) {
            startPeriodComboBox.addItem(option);
            endPeriodComboBox.addItem(option);
        }
        startPeriodComboBox.setSelectedItem(model.selectedStartPeriod());
        endPeriodComboBox.setSelectedItem(model.selectedEndPeriod());

        roomComboBox.removeAllItems();
        for (Room room : model.rooms()) {
            roomComboBox.addItem(room);
        }
        if (model.selectedRoom() != null) {
            roomComboBox.setSelectedItem(model.selectedRoom());
        }

        noteArea.setText(model.note());
        SwingUtilities.invokeLater(() -> {
            if (courseSectionComboBox.isEnabled()) {
                courseSectionComboBox.requestFocusInWindow();
            } else {
                dayOfWeekComboBox.requestFocusInWindow();
            }
        });
    }

    /**
     * Tạo học phần phần.
     */
    private JPanel createCourseSectionSection() {
        JPanel contentPanel = createFieldGridPanel();
        contentPanel.add(createField("Học phần", styleComboBox(courseSectionComboBox)), fieldConstraints(0, 0, 2));
        return createSection("Thông tin học phần", "Giữ nguyên học phần đang chọn khi cập nhật lịch hiện hữu.", contentPanel);
    }

    /**
     * Tạo lịch phần.
     */
    private JPanel createScheduleSection() {
        JPanel contentPanel = createFieldGridPanel();
        contentPanel.add(createField("Thứ học", styleComboBox(dayOfWeekComboBox)), fieldConstraints(0, 0));
        contentPanel.add(createField("Phòng học", styleComboBox(roomComboBox)), fieldConstraints(1, 0));
        contentPanel.add(createField("Tiết bắt đầu", styleComboBox(startPeriodComboBox)), fieldConstraints(0, 1));
        contentPanel.add(createField("Tiết kết thúc", styleComboBox(endPeriodComboBox)), fieldConstraints(1, 1));
        return createSection("Lịch học", "Thiết lập ngày học, tiết học và phòng học.", contentPanel);
    }

    /**
     * Tạo note phần.
     */
    private JPanel createNoteSection() {
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setRows(4);
        noteArea.setFont(noteArea.getFont().deriveFont(Font.PLAIN, 13.5f));
        noteArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JScrollPane scrollPane = new JScrollPane(noteArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.INPUT_BORDER));
        scrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);

        return createSection("Ghi chú", "Ghi nhận thêm nếu cần cho buổi học hoặc học phần này.", scrollPane);
    }

    /**
     * Tạo phần.
     */
    private JPanel createSection(String title, String subtitle, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(true);
        panel.setBackground(AppColors.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        JPanel headingPanel = new JPanel(new BorderLayout(0, 4));
        headingPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 12.5f));
        subtitleLabel.setForeground(AppColors.CARD_MUTED_TEXT);

        headingPanel.add(titleLabel, BorderLayout.NORTH);
        headingPanel.add(subtitleLabel, BorderLayout.CENTER);

        panel.add(headingPanel, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Tạo panel trường grid.
     */
    private JPanel createFieldGridPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        return panel;
    }

    /**
     * Xử lý trường constraints.
     */
    private GridBagConstraints fieldConstraints(int x, int y) {
        return fieldConstraints(x, y, 1);
    }

    /**
     * Xử lý trường constraints.
     */
    private GridBagConstraints fieldConstraints(int x, int y, int width) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = width;
        constraints.weightx = width;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(0, 0, 12, width == 2 ? 0 : (x == 0 ? 12 : 0));
        return constraints;
    }

    /**
     * Tạo trường.
     */
    private JPanel createField(String labelText, Component inputComponent) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12.5f));
        label.setForeground(AppColors.CARD_TITLE_TEXT);

        panel.add(label, BorderLayout.NORTH);
        panel.add(inputComponent, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Áp dụng kiểu cho chọn box.
     */
    private <T> JComboBox<T> styleComboBox(JComboBox<T> comboBox) {
        comboBox.setFont(comboBox.getFont().deriveFont(Font.PLAIN, 13.5f));
        comboBox.setBorder(BorderFactory.createLineBorder(AppColors.INPUT_BORDER));
        comboBox.setPreferredSize(new Dimension(comboBox.getPreferredSize().width, 40));
        return comboBox;
    }

    /**
     * Áp dụng kiểu cho nút primary.
     */
    private void stylePrimaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(AppColors.BUTTON_PRIMARY);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
    }

    /**
     * Áp dụng kiểu cho nút secondary.
     */
    private void styleSecondaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(AppColors.BUTTON_NEUTRAL);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
    }

    /**
     * Xử lý lưu.
     */
    private void handleSave() {
        result = new ScheduleFormResult(
                (CourseSection) courseSectionComboBox.getSelectedItem(),
                (String) dayOfWeekComboBox.getSelectedItem(),
                (Integer) startPeriodComboBox.getSelectedItem(),
                (Integer) endPeriodComboBox.getSelectedItem(),
                (Room) roomComboBox.getSelectedItem(),
                noteArea.getText()
        );
        dispose();
    }

    /**
     * Xác định owner.
     */
    private static Window resolveOwner(Component parent) {
        if (parent == null) {
            return null;
        }
        return SwingUtilities.getWindowAncestor(parent);
    }

    /**
     * Xử lý model lịch biểu mẫu.
     */
    public record ScheduleFormModel(
            String title,
            List<CourseSection> courseSections,
            CourseSection selectedCourseSection,
            boolean courseSectionEditable,
            String[] daysOfWeek,
            String selectedDayOfWeek,
            Integer[] periodOptions,
            Integer selectedStartPeriod,
            Integer selectedEndPeriod,
            List<Room> rooms,
            Room selectedRoom,
            String note
    ) {
    }

    /**
     * Xử lý lịch kết quả biểu mẫu.
     */
    public record ScheduleFormResult(
            CourseSection courseSection,
            String dayOfWeek,
            Integer startPeriod,
            Integer endPeriod,
            Room room,
            String note
    ) {
    }
}
