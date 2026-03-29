package com.qlsv.view.student;

import com.qlsv.controller.DisplayField;
import com.qlsv.controller.StudentHomeScreenController;
import com.qlsv.dto.ScheduleDisplayDto;
import com.qlsv.dto.StudentHomeDto;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BasePanel;
import com.qlsv.view.common.DashboardCard;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

public class StudentHomePanel extends BasePanel {

    private final StudentHomeScreenController screenController = new StudentHomeScreenController();

    private final JLabel subtitleLabel = new JLabel();
    private final DashboardCard enrollmentCard = new DashboardCard("Số học phần đã đăng ký", AppColors.STAT_CARD_ENROLLMENTS);
    private final DashboardCard creditCard = new DashboardCard("Tổng số tín chỉ", AppColors.STAT_CARD_SUBJECTS);
    private final DashboardCard scoreCard = new DashboardCard("Điểm tổng kết trung bình", AppColors.STAT_CARD_LECTURERS);
    private final DashboardCard scheduleCard = new DashboardCard("Số buổi học trong lịch", AppColors.STAT_CARD_SECTIONS);

    private final JPanel infoFieldsPanel = new JPanel(new GridLayout(0, 2, 8, 8));
    private final DefaultTableModel scheduleTableModel = new DefaultTableModel(
            new String[]{"Học phần", "Thứ", "Tiết", "Phòng"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTextArea scoreSummaryTextArea = new JTextArea();

    public StudentHomePanel() {
        setBackground(AppColors.CONTENT_BACKGROUND);
        initComponents();
        reloadData();
    }

    /**
     * Khởi tạo và sắp xếp các thành phần giao diện của trang chủ sinh viên.
     */
    private void initComponents() {
        JPanel headerPanel = new JPanel(new BorderLayout(0, 8));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Trang tổng quan cá nhân");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));

        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 13f));
        subtitleLabel.setForeground(AppColors.CARD_MUTED_TEXT);


        JPanel titleActionPanel = new JPanel(new BorderLayout());
        titleActionPanel.setOpaque(false);
        titleActionPanel.add(titleLabel, BorderLayout.WEST);

        headerPanel.add(titleActionPanel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.CENTER);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 12, 12));
        cardsPanel.setOpaque(false);
        cardsPanel.add(enrollmentCard);
        cardsPanel.add(creditCard);
        cardsPanel.add(scoreCard);
        cardsPanel.add(scheduleCard);

        infoFieldsPanel.setOpaque(true);
        infoFieldsPanel.setBackground(AppColors.CARD_BACKGROUND);
        infoFieldsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JTable scheduleTable = new JTable(scheduleTableModel);
        scheduleTable.setRowHeight(24);
        JPanel scheduleContent = new JPanel(new BorderLayout());
        scheduleContent.setOpaque(false);
        scheduleContent.add(new JScrollPane(scheduleTable), BorderLayout.CENTER);

        scoreSummaryTextArea.setEditable(false);
        scoreSummaryTextArea.setLineWrap(true);
        scoreSummaryTextArea.setWrapStyleWord(true);
        scoreSummaryTextArea.setOpaque(false);
        scoreSummaryTextArea.setFont(scoreSummaryTextArea.getFont().deriveFont(Font.PLAIN, 13f));
        JPanel scoreContent = new JPanel(new BorderLayout());
        scoreContent.setOpaque(false);
        scoreContent.add(scoreSummaryTextArea, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 12, 12));
        centerPanel.setOpaque(false);
        centerPanel.add(wrapWithTitle("Thông tin học tập chính", infoFieldsPanel));
        centerPanel.add(wrapWithTitle("Lịch học sắp tới", scheduleContent));

        JPanel mainContentPanel = new JPanel(new BorderLayout(12, 12));
        mainContentPanel.setOpaque(false);
        mainContentPanel.add(headerPanel, BorderLayout.NORTH);
        mainContentPanel.add(cardsPanel, BorderLayout.CENTER);

        JPanel bottomWrapper = new JPanel(new BorderLayout(0, 12));
        bottomWrapper.setOpaque(false);
        bottomWrapper.add(centerPanel, BorderLayout.NORTH);
        bottomWrapper.add(wrapWithTitle("Kết quả học tập tóm tắt", scoreContent), BorderLayout.CENTER);

        mainContentPanel.add(bottomWrapper, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(mainContentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Tải lại toàn bộ dữ liệu thống kê và thông tin cá nhân của sinh viên từ service.
     */
    public void reloadData() {
        try {
            StudentHomeDto homeData = screenController.loadHomeData();
            subtitleLabel.setText(homeData.subtitle());
            enrollmentCard.setValue(homeData.enrollmentCount());
            creditCard.setValue(homeData.totalCredits());
            scoreCard.setValue(homeData.averageScore());
            scheduleCard.setValue(homeData.scheduleCount());
            updateInfoPanel(homeData.infoFields());
            updateScheduleTable(homeData.scheduleRows());
            scoreSummaryTextArea.setText(homeData.scoreSummary());
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    /**
     * Cập nhật các trường thông tin học tập lên bảng hiển thị.
     */
    private void updateInfoPanel(List<DisplayField> infoFields) {
        infoFieldsPanel.removeAll();
        for (DisplayField field : infoFields) {
            addField(infoFieldsPanel, field.label(), field.value());
        }
        infoFieldsPanel.revalidate();
        infoFieldsPanel.repaint();
    }

    private void addField(JPanel panel, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD, 12f));
        panel.add(labelComponent);
        panel.add(new JLabel(value));
    }

    /**
     * Cập nhật bảng dữ liệu lịch học sắp tới của sinh viên.
     */
    private void updateScheduleTable(List<ScheduleDisplayDto> scheduleRows) {
        scheduleTableModel.setRowCount(0);
        for (ScheduleDisplayDto scheduleRow : scheduleRows) {
            scheduleTableModel.addRow(new Object[]{
                    scheduleRow.sectionCode(),
                    scheduleRow.dayOfWeek(),
                    scheduleRow.periodText(),
                    scheduleRow.roomName()
            });
        }
    }

    /**
     * Bọc một thành phần nội dung trong một Panel có tiêu đề và viền kiểu Card.
     */
    private JPanel wrapWithTitle(String title, JPanel content) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setOpaque(true);
        wrapper.setBackground(AppColors.CARD_BACKGROUND);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(AppColors.CARD_TITLE_TEXT);

        wrapper.add(titleLabel, BorderLayout.NORTH);
        wrapper.add(content, BorderLayout.CENTER);
        return wrapper;
    }
}
