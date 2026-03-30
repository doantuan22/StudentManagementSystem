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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StudentSchedulePanel extends BasePanel {

    private final ScheduleController scheduleController = new ScheduleController();
    private DashboardCard totalSessionsCard;
    private DashboardCard subjectsCard;
    private JPanel weekSchedulePanel;
    private final String[] daysOfWeek = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};

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

        // Tạo panel lịch tuần - layout dọc (7 hàng)
        weekSchedulePanel = new JPanel();
        weekSchedulePanel.setLayout(new BoxLayout(weekSchedulePanel, BoxLayout.Y_AXIS));
        weekSchedulePanel.setOpaque(false);
        
        JScrollPane scrollPane = new JScrollPane(weekSchedulePanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        add(topWrapper, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Làm mới dữ liệu đang hiển thị.
     */
    @Override
    public void reloadData() {
        try {
            List<Schedule> schedules = scheduleController.getCurrentStudentSchedules();
            
            // Sắp xếp lịch học theo thứ trong tuần và thời gian
            schedules.sort((s1, s2) -> {
                int day1 = getDayOrder(s1.getDayOfWeek());
                int day2 = getDayOrder(s2.getDayOfWeek());
                
                if (day1 != day2) {
                    return Integer.compare(day1, day2);
                }
                
                Integer start1 = s1.getStartPeriod() != null ? s1.getStartPeriod() : 0;
                Integer start2 = s2.getStartPeriod() != null ? s2.getStartPeriod() : 0;
                
                if (!start1.equals(start2)) {
                    return Integer.compare(start1, start2);
                }
                
                Integer end1 = s1.getEndPeriod() != null ? s1.getEndPeriod() : 0;
                Integer end2 = s2.getEndPeriod() != null ? s2.getEndPeriod() : 0;
                return Integer.compare(end1, end2);
            });
            
            totalSessionsCard.setValue(String.valueOf(schedules.size()));
            subjectsCard.setValue(String.valueOf(
                    schedules.stream()
                            .map(schedule -> schedule.getCourseSection() == null ? null : schedule.getCourseSection().getId())
                            .filter(java.util.Objects::nonNull)
                            .distinct()
                            .count()
            ));

            // Nhóm lịch học theo thứ
            Map<Integer, List<Schedule>> schedulesByDay = new HashMap<>();
            for (int i = 1; i <= 7; i++) {
                schedulesByDay.put(i, new ArrayList<>());
            }
            
            for (Schedule schedule : schedules) {
                int dayOrder = getDayOrder(schedule.getDayOfWeek());
                if (dayOrder >= 1 && dayOrder <= 7) {
                    schedulesByDay.get(dayOrder).add(schedule);
                }
            }

            // Xây dựng giao diện lịch tuần - mỗi ngày là một hàng ngang
            weekSchedulePanel.removeAll();
            for (int i = 0; i < 7; i++) {
                JPanel dayRow = createDayRow(daysOfWeek[i], schedulesByDay.get(i + 1));
                weekSchedulePanel.add(dayRow);
                if (i < 6) {
                    weekSchedulePanel.add(Box.createVerticalStrut(12));
                }
            }
            
            weekSchedulePanel.revalidate();
            weekSchedulePanel.repaint();
            
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }
    
    /**
     * Tạo hàng ngang cho một ngày trong tuần.
     */
    private JPanel createDayRow(String dayName, List<Schedule> schedules) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(true);
        row.setBackground(AppColors.CARD_BACKGROUND);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        // Panel bên trái: Tên ngày
        JPanel dayPanel = new JPanel(new BorderLayout());
        dayPanel.setOpaque(false);
        dayPanel.setPreferredSize(new Dimension(100, 0));
        
        JLabel dayLabel = new JLabel(dayName);
        dayLabel.setFont(dayLabel.getFont().deriveFont(Font.BOLD, 16f));
        dayLabel.setForeground(AppColors.BUTTON_PRIMARY);
        dayLabel.setHorizontalAlignment(JLabel.CENTER);
        dayPanel.add(dayLabel, BorderLayout.CENTER);

        // Panel bên phải: Danh sách môn học ngang
        JPanel schedulesPanel = new JPanel();
        schedulesPanel.setLayout(new BoxLayout(schedulesPanel, BoxLayout.X_AXIS));
        schedulesPanel.setOpaque(false);

        if (schedules.isEmpty()) {
            JLabel emptyLabel = new JLabel("Không có lịch học");
            emptyLabel.setFont(emptyLabel.getFont().deriveFont(Font.ITALIC, 13f));
            emptyLabel.setForeground(AppColors.CARD_MUTED_TEXT);
            schedulesPanel.add(emptyLabel);
        } else {
            for (int i = 0; i < schedules.size(); i++) {
                Schedule schedule = schedules.get(i);
                JPanel scheduleCard = createScheduleCard(schedule);
                schedulesPanel.add(scheduleCard);
                if (i < schedules.size() - 1) {
                    schedulesPanel.add(Box.createHorizontalStrut(10));
                }
            }
        }

        JScrollPane scrollPane = new JScrollPane(schedulesPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        row.add(dayPanel, BorderLayout.WEST);
        row.add(scrollPane, BorderLayout.CENTER);
        
        return row;
    }
    
    /**
     * Tạo card cho một môn học.
     */
    private JPanel createScheduleCard(Schedule schedule) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(249, 250, 251));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        card.setPreferredSize(new Dimension(220, 120));
        card.setMaximumSize(new Dimension(220, 120));
        card.setMinimumSize(new Dimension(220, 120));
        
        // Thêm hiệu ứng hover và click
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(243, 244, 246));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(AppColors.BUTTON_PRIMARY, 2),
                        BorderFactory.createEmptyBorder(9, 11, 9, 11)
                ));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(249, 250, 251));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12)
                ));
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showScheduleDetail(schedule);
            }
        });

        // Tên môn học
        String subjectName = schedule.getCourseSection() == null 
                || schedule.getCourseSection().getSubject() == null
                ? "Chưa cập nhật"
                : schedule.getCourseSection().getSubject().getSubjectName();
        
        JLabel subjectLabel = new JLabel("<html><b>" + truncateText(subjectName, 25) + "</b></html>");
        subjectLabel.setFont(subjectLabel.getFont().deriveFont(13f));
        subjectLabel.setForeground(AppColors.CARD_TITLE_TEXT);
        subjectLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        card.add(subjectLabel);
        card.add(Box.createVerticalStrut(6));

        // Mã học phần
        String sectionCode = schedule.getCourseSection() == null
                ? ""
                : schedule.getCourseSection().getSectionCode();
        if (!sectionCode.isEmpty()) {
            JLabel codeLabel = new JLabel(sectionCode);
            codeLabel.setFont(codeLabel.getFont().deriveFont(11.5f));
            codeLabel.setForeground(AppColors.BUTTON_PRIMARY);
            codeLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
            card.add(codeLabel);
            card.add(Box.createVerticalStrut(4));
        }

        // Tiết học
        String period = DisplayTextUtil.formatPeriod(schedule.getStartPeriod(), schedule.getEndPeriod());
        JLabel periodLabel = new JLabel("⏰ " + period);
        periodLabel.setFont(periodLabel.getFont().deriveFont(12f));
        periodLabel.setForeground(AppColors.CARD_VALUE_TEXT);
        periodLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        card.add(periodLabel);
        card.add(Box.createVerticalStrut(4));

        // Phòng học
        String room = schedule.getRoom() == null ? "Chưa cập nhật" : schedule.getRoom().getRoomName();
        JLabel roomLabel = new JLabel("📍 " + room);
        roomLabel.setFont(roomLabel.getFont().deriveFont(12f));
        roomLabel.setForeground(AppColors.CARD_VALUE_TEXT);
        roomLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        card.add(roomLabel);

        return card;
    }
    
    /**
     * Cắt ngắn text nếu quá dài.
     */
    private String truncateText(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Hiển thị dialog chi tiết lịch học.
     */
    private void showScheduleDetail(Schedule schedule) {
        javax.swing.JDialog dialog = new javax.swing.JDialog(
                javax.swing.SwingUtilities.getWindowAncestor(this),
                "Chi tiết lịch học",
                javax.swing.JDialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Tiêu đề
        String subjectName = schedule.getCourseSection() == null 
                || schedule.getCourseSection().getSubject() == null
                ? "Chưa cập nhật"
                : schedule.getCourseSection().getSubject().getSubjectName();
        
        JLabel titleLabel = new JLabel(subjectName);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(AppColors.CARD_TITLE_TEXT);
        titleLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Thông tin chi tiết
        addDetailRow(contentPanel, "Mã học phần", 
                schedule.getCourseSection() == null ? "" : schedule.getCourseSection().getSectionCode());
        
        addDetailRow(contentPanel, "Môn học", subjectName);
        
        addDetailRow(contentPanel, "Số tín chỉ", 
                schedule.getCourseSection() == null || schedule.getCourseSection().getSubject() == null
                ? "" : String.valueOf(schedule.getCourseSection().getSubject().getCredits()));
        
        addDetailRow(contentPanel, "Giảng viên", 
                schedule.getCourseSection() == null || schedule.getCourseSection().getLecturer() == null
                ? "Chưa cập nhật" : schedule.getCourseSection().getLecturer().getFullName());
        
        addDetailRow(contentPanel, "Học kỳ", 
                schedule.getCourseSection() == null ? "" 
                : DisplayTextUtil.defaultText(schedule.getCourseSection().getSemester()));
        
        addDetailRow(contentPanel, "Năm học", 
                schedule.getCourseSection() == null ? "" 
                : DisplayTextUtil.defaultText(schedule.getCourseSection().getSchoolYear()));
        
        addDetailRow(contentPanel, "Thứ", DisplayTextUtil.defaultText(schedule.getDayOfWeek()));
        
        addDetailRow(contentPanel, "Tiết học", 
                DisplayTextUtil.formatPeriod(schedule.getStartPeriod(), schedule.getEndPeriod()));
        
        addDetailRow(contentPanel, "Phòng học", 
                schedule.getRoom() == null ? "Chưa cập nhật" : schedule.getRoom().getRoomName());
        
        addDetailRow(contentPanel, "Mã phòng", 
                schedule.getRoom() == null ? "Chưa cập nhật" : schedule.getRoom().getRoomCode());
        
        if (schedule.getNote() != null && !schedule.getNote().trim().isEmpty()) {
            addDetailRow(contentPanel, "Ghi chú", schedule.getNote());
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        dialog.add(scrollPane, BorderLayout.CENTER);

        // Nút đóng
        JButton closeButton = new JButton("Đóng");
        styleNeutralButton(closeButton);
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel footer = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        footer.add(closeButton);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setPreferredSize(new Dimension(500, 550));
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    /**
     * Thêm một hàng thông tin chi tiết.
     */
    private void addDetailRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JLabel labelComponent = new JLabel(label + ":");
        labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD, 13f));
        labelComponent.setForeground(AppColors.CARD_MUTED_TEXT);
        labelComponent.setPreferredSize(new Dimension(120, 0));

        JLabel valueComponent = new JLabel(value == null || value.isEmpty() ? "Chưa cập nhật" : value);
        valueComponent.setFont(valueComponent.getFont().deriveFont(Font.PLAIN, 13f));
        valueComponent.setForeground(AppColors.CARD_VALUE_TEXT);

        row.add(labelComponent, BorderLayout.WEST);
        row.add(valueComponent, BorderLayout.CENTER);

        panel.add(row);
        panel.add(Box.createVerticalStrut(8));
    }
    
    /**
     * Chuyển đổi tên thứ sang số thứ tự để sắp xếp.
     */
    private int getDayOrder(String dayOfWeek) {
        if (dayOfWeek == null) {
            return 8;
        }
        
        String day = dayOfWeek.trim().toUpperCase();
        
        if (day.contains("2") || day.contains("HAI")) {
            return 1;
        } else if (day.contains("3") || day.contains("BA")) {
            return 2;
        } else if (day.contains("4") || day.contains("TƯ")) {
            return 3;
        } else if (day.contains("5") || day.contains("NĂM")) {
            return 4;
        } else if (day.contains("6") || day.contains("SÁU")) {
            return 5;
        } else if (day.contains("7") || day.contains("BẢY")) {
            return 6;
        } else if (day.contains("CN") || day.contains("CHỦ NHẬT")) {
            return 7;
        }
        
        switch (day) {
            case "MONDAY":
            case "MON":
                return 1;
            case "TUESDAY":
            case "TUE":
                return 2;
            case "WEDNESDAY":
            case "WED":
                return 3;
            case "THURSDAY":
            case "THU":
                return 4;
            case "FRIDAY":
            case "FRI":
                return 5;
            case "SATURDAY":
            case "SAT":
                return 6;
            case "SUNDAY":
            case "SUN":
                return 7;
            default:
                return 8;
        }
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
