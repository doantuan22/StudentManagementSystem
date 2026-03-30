/**
 * Màn hình sinh viên cho lịch.
 */
package com.qlsv.view.student;

import com.qlsv.controller.ScheduleController;
import com.qlsv.model.Schedule;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BasePanel;
import com.qlsv.view.common.DashboardCard;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

public class StudentSchedulePanel extends BasePanel {

    private final ScheduleController scheduleController = new ScheduleController();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Học phần", "Môn học", "Học kỳ", "Năm học", "Giảng viên", "Thứ", "Tiết", "Phòng"}, 0) {
        /**
         * Xác định ô có cho phép chỉnh sửa hay không.
         */
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private DashboardCard totalSessionsCard;
    private DashboardCard subjectsCard;
    private final JLabel summaryLabel = new JLabel("Đang tải lịch học...");

    /**
     * Khởi tạo lịch sinh viên.
     */
    public StudentSchedulePanel() {
        setBackground(AppColors.CONTENT_BACKGROUND);
        initComponents();
        reloadData();
    }

    /**
     * Khởi tạo các thành phần giao diện.
     */
    private void initComponents() {
        JLabel titleLabel = new JLabel("Lịch học sinh viên");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);


        totalSessionsCard = new DashboardCard("Tổng số môn học", AppColors.STAT_CARD_SECTIONS);
        subjectsCard = new DashboardCard("Số học phần có lịch", AppColors.STAT_CARD_SUBJECTS);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 2, 12, 12));
        cardsPanel.setOpaque(false);
        cardsPanel.add(totalSessionsCard);
        cardsPanel.add(subjectsCard);

        JTable table = new JTable(tableModel);
        configureTable(table);

        JPanel tablePanel = createTableCard("Thời gian học theo danh sách", summaryLabel, new JScrollPane(table));

        JButton reloadButton = new JButton("Tải lại");
        styleNeutralButton(reloadButton);
        reloadButton.addActionListener(event -> reloadData());

        JPanel titlePanel = new JPanel(new BorderLayout(0, 6));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.NORTH);

        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setOpaque(false);
        actionPanel.add(titlePanel, BorderLayout.WEST);
        actionPanel.add(reloadButton, BorderLayout.EAST);

        JPanel topWrapper = new JPanel(new BorderLayout(0, 12));
        topWrapper.setOpaque(false);
        topWrapper.add(actionPanel, BorderLayout.NORTH);
        topWrapper.add(cardsPanel, BorderLayout.CENTER);

        add(topWrapper, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    /**
     * Làm mới dữ liệu đang hiển thị.
     */
    @Override
    public void reloadData() {
        try {
            List<Schedule> schedules = scheduleController.getCurrentStudentSchedules();
            totalSessionsCard.setValue(String.valueOf(schedules.size()));
            subjectsCard.setValue(String.valueOf(
                    schedules.stream()
                            .map(schedule -> schedule.getCourseSection() == null ? null : schedule.getCourseSection().getId())
                            .filter(java.util.Objects::nonNull)
                            .distinct()
                            .count()
            ));
            summaryLabel.setText(schedules.size() + " buổi học đã được xếp lịch");

            tableModel.setRowCount(0);
            for (Schedule schedule : schedules) {
                tableModel.addRow(new Object[]{
                        schedule.getCourseSection() == null ? "" : schedule.getCourseSection().getSectionCode(),
                        schedule.getCourseSection() == null || schedule.getCourseSection().getSubject() == null
                                ? "" : schedule.getCourseSection().getSubject().getSubjectName(),
                        schedule.getCourseSection() == null ? "" : DisplayTextUtil.defaultText(schedule.getCourseSection().getSemester()),
                        schedule.getCourseSection() == null ? "" : DisplayTextUtil.defaultText(schedule.getCourseSection().getSchoolYear()),
                        schedule.getCourseSection() == null || schedule.getCourseSection().getLecturer() == null
                                ? "" : schedule.getCourseSection().getLecturer().getFullName(),
                        DisplayTextUtil.defaultText(schedule.getDayOfWeek()),
                        DisplayTextUtil.formatPeriod(schedule.getStartPeriod(), schedule.getEndPeriod()),
                        schedule.getRoom() == null ? "Chưa cập nhật" : schedule.getRoom().getRoomName()
                });
            }
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    /**
     * Tạo card bảng.
     */
    private JPanel createTableCard(String title, JLabel summaryLabel, JScrollPane scrollPane) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(true);
        panel.setBackground(AppColors.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(AppColors.CARD_TITLE_TEXT);

        summaryLabel.setForeground(AppColors.CARD_MUTED_TEXT);
        summaryLabel.setFont(summaryLabel.getFont().deriveFont(Font.PLAIN, 12.5f));

        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setOpaque(false);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(summaryLabel, BorderLayout.EAST);

        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.CARD_BORDER));
        scrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Thiết lập bảng.
     */
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
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable currentTable, Object value, boolean selected,
                                                           boolean hasFocus, int row, int column) {
                Component renderer = super.getTableCellRendererComponent(currentTable, value, selected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return renderer;
            }
        };

        TableColumnModel columnModel = table.getColumnModel();
        int[] columnWidths = {120, 240, 90, 100, 200, 80, 80, 255};
        for (int i = 0; i < columnWidths.length && i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setPreferredWidth(columnWidths[i]);
            columnModel.getColumn(i).setCellRenderer(cellRenderer);
        }

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        headerRenderer.setBackground(AppColors.TABLE_HEADER_BACKGROUND);
        headerRenderer.setForeground(AppColors.CARD_VALUE_TEXT);
        headerRenderer.setFont(table.getTableHeader().getFont());
        headerRenderer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        table.getTableHeader().setDefaultRenderer(headerRenderer);
    }

    /**
     * Áp dụng kiểu cho nút neutral.
     */
    private void styleNeutralButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(AppColors.BUTTON_NEUTRAL);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
    }
}
