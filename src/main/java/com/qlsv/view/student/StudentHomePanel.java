package com.qlsv.view.student;

import com.qlsv.controller.EnrollmentController;
import com.qlsv.controller.ScheduleController;
import com.qlsv.controller.ScoreController;
import com.qlsv.controller.StudentController;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Schedule;
import com.qlsv.model.Score;
import com.qlsv.model.Student;
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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Comparator;
import java.util.List;

public class StudentHomePanel extends BasePanel {

    private final StudentController studentController = new StudentController();
    private final EnrollmentController enrollmentController = new EnrollmentController();
    private final ScoreController scoreController = new ScoreController();
    private final ScheduleController scheduleController = new ScheduleController();

    private final JLabel subtitleLabel = new JLabel();
    private final DashboardCard enrollmentCard = new DashboardCard("Số học phần đã đăng ký", AppColors.STAT_CARD_ENROLLMENTS);
    private final DashboardCard creditCard = new DashboardCard("Tổng số tín chỉ đang theo dõi", AppColors.STAT_CARD_SUBJECTS);
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

    private void initComponents() {
        JPanel headerPanel = new JPanel(new BorderLayout(0, 8));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Tổng quan sinh viên");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));

        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 13f));
        subtitleLabel.setForeground(AppColors.CARD_MUTED_TEXT);

        JButton reloadButton = new JButton("Tải lại");
        reloadButton.addActionListener(event -> reloadData());
        JPanel titleActionPanel = new JPanel(new BorderLayout());
        titleActionPanel.setOpaque(false);
        titleActionPanel.add(titleLabel, BorderLayout.WEST);
        titleActionPanel.add(reloadButton, BorderLayout.EAST);

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
        centerPanel.add(wrapWithTitle("Thông tin học tập nhanh", infoFieldsPanel));
        centerPanel.add(wrapWithTitle("Lịch học sắp tới", scheduleContent));

        JPanel mainContentPanel = new JPanel(new BorderLayout(12, 12));
        mainContentPanel.setOpaque(false);
        mainContentPanel.add(headerPanel, BorderLayout.NORTH);
        mainContentPanel.add(cardsPanel, BorderLayout.CENTER);

        JPanel bottomWrapper = new JPanel(new BorderLayout(0, 12));
        bottomWrapper.setOpaque(false);
        bottomWrapper.add(centerPanel, BorderLayout.NORTH);
        bottomWrapper.add(wrapWithTitle("Tóm tắt kết quả học tập", scoreContent), BorderLayout.CENTER);

        mainContentPanel.add(bottomWrapper, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(mainContentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void reloadData() {
        try {
            Student student = studentController.getCurrentStudent();
            List<Enrollment> enrollments = enrollmentController.getCurrentStudentEnrollments();
            List<Score> scores = scoreController.getCurrentStudentScores();
            List<Schedule> schedules = scheduleController.getCurrentStudentSchedules();

            subtitleLabel.setText(
                    "Xin chào " + DisplayTextUtil.defaultText(student.getFullName())
                            + " | Mã sinh viên: " + DisplayTextUtil.defaultText(student.getStudentCode())
            );

            enrollmentCard.setValue(String.valueOf(enrollments.size()));
            creditCard.setValue(String.valueOf(calculateTotalCredits(enrollments)));
            scoreCard.setValue(formatAverageScore(scores));
            scheduleCard.setValue(String.valueOf(schedules.size()));

            updateInfoPanel(student);
            updateScheduleTable(schedules);
            updateScoreSummary(scores);
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void updateInfoPanel(Student student) {
        infoFieldsPanel.removeAll();
        addField(infoFieldsPanel, "Họ và tên", DisplayTextUtil.defaultText(student.getFullName()));
        addField(infoFieldsPanel, "Mã sinh viên", DisplayTextUtil.defaultText(student.getStudentCode()));
        addField(infoFieldsPanel, "Lớp", student.getClassRoom() == null ? "Chưa cập nhật" : student.getClassRoom().getClassName());
        addField(infoFieldsPanel, "Khoa", student.getFaculty() == null ? "Chưa cập nhật" : student.getFaculty().getFacultyName());
        addField(infoFieldsPanel, "Niên khóa", DisplayTextUtil.defaultText(student.getAcademicYear()));
        addField(infoFieldsPanel, "Trạng thái", DisplayTextUtil.formatStatus(student.getStatus()));
        infoFieldsPanel.revalidate();
        infoFieldsPanel.repaint();
    }

    private void addField(JPanel panel, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        panel.add(l);
        panel.add(new JLabel(value));
    }

    private void updateScheduleTable(List<Schedule> schedules) {
        scheduleTableModel.setRowCount(0);
        schedules.stream()
                .sorted(Comparator.comparing(Schedule::getDayOfWeek, Comparator.nullsLast(String::compareTo))
                        .thenComparing(Schedule::getStartPeriod, Comparator.nullsLast(Integer::compareTo)))
                .limit(5)
                .forEach(schedule -> scheduleTableModel.addRow(new Object[]{
                        schedule.getCourseSection() == null ? "" : schedule.getCourseSection().getSectionCode(),
                        DisplayTextUtil.defaultText(schedule.getDayOfWeek()),
                        DisplayTextUtil.formatPeriod(schedule.getStartPeriod(), schedule.getEndPeriod()),
                        schedule.getRoom() == null ? "Chưa cập nhật" : schedule.getRoom().getRoomName()
                }));
    }

    private void updateScoreSummary(List<Score> scores) {
        long passCount = scores.stream().filter(score -> "PASS".equalsIgnoreCase(score.getResult())).count();
        long failCount = scores.stream().filter(score -> "FAIL".equalsIgnoreCase(score.getResult())).count();

        scoreSummaryTextArea.setText(
                "- Tổng số môn đã có điểm: " + scores.size() + "\n"
                        + "- Số môn đạt: " + passCount + "\n"
                        + "- Số môn chưa đạt: " + failCount + "\n"
                        + "- Điểm trung bình hiện tại: " + formatAverageScore(scores) + "\n"
                        + "- Gợi ý: theo dõi kỹ các học phần có điểm tổng kết thấp để chủ động cải thiện kết quả học tập."
        );
    }

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

    private int calculateTotalCredits(List<Enrollment> enrollments) {
        int total = 0;
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getCourseSection() != null
                    && enrollment.getCourseSection().getSubject() != null
                    && enrollment.getCourseSection().getSubject().getCredits() != null) {
                total += enrollment.getCourseSection().getSubject().getCredits();
            }
        }
        return total;
    }

    private String formatAverageScore(List<Score> scores) {
        double sum = 0;
        int count = 0;
        for (Score score : scores) {
            if (score.getTotalScore() != null) {
                sum += score.getTotalScore();
                count++;
            }
        }
        if (count == 0) {
            return "Chưa có";
        }
        return String.format("%.2f", sum / count);
    }
}
