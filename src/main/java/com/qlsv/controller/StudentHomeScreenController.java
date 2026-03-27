package com.qlsv.controller;

import com.qlsv.dto.DisplayDtoMapper;
import com.qlsv.dto.ScheduleDisplayDto;
import com.qlsv.dto.StudentHomeDto;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Schedule;
import com.qlsv.model.Score;
import com.qlsv.model.Student;
import com.qlsv.utils.DisplayTextUtil;

import java.util.Comparator;
import java.util.List;

public class StudentHomeScreenController {

    private final StudentController studentController = new StudentController();
    private final EnrollmentController enrollmentController = new EnrollmentController();
    private final ScoreController scoreController = new ScoreController();
    private final ScheduleController scheduleController = new ScheduleController();

    public StudentHomeDto loadHomeData() {
        Student student = studentController.getCurrentStudent();
        List<Enrollment> enrollments = enrollmentController.getCurrentStudentEnrollments();
        List<Score> scores = scoreController.getCurrentStudentScores();
        List<Schedule> schedules = scheduleController.getCurrentStudentSchedules();

        String subtitle = "Xin chào " + DisplayTextUtil.defaultText(student.getFullName())
                + " | Mã sinh viên: " + DisplayTextUtil.defaultText(student.getStudentCode());

        List<DisplayField> infoFields = List.of(
                new DisplayField("Họ và tên", DisplayTextUtil.defaultText(student.getFullName())),
                new DisplayField("Mã sinh viên", DisplayTextUtil.defaultText(student.getStudentCode())),
                new DisplayField("Lớp", student.getClassRoom() == null ? "Chưa cập nhật" : student.getClassRoom().getClassName()),
                new DisplayField("Khoa", student.getFaculty() == null ? "Chưa cập nhật" : student.getFaculty().getFacultyName()),
                new DisplayField("Niên khóa", DisplayTextUtil.defaultText(student.getAcademicYear())),
                new DisplayField("Trạng thái", DisplayTextUtil.formatStatus(student.getStatus()))
        );

        List<ScheduleDisplayDto> scheduleRows = schedules.stream()
                .sorted(Comparator.comparing(Schedule::getDayOfWeek, Comparator.nullsLast(String::compareTo))
                        .thenComparing(Schedule::getStartPeriod, Comparator.nullsLast(Integer::compareTo)))
                .limit(5)
                .map(DisplayDtoMapper::toScheduleDisplayDto)
                .toList();

        return new StudentHomeDto(
                subtitle,
                String.valueOf(enrollments.size()),
                String.valueOf(calculateTotalCredits(enrollments)),
                formatAverageScore(scores),
                String.valueOf(schedules.size()),
                infoFields,
                scheduleRows,
                buildScoreSummary(scores)
        );
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

    private String buildScoreSummary(List<Score> scores) {
        long passCount = scores.stream().filter(score -> "PASS".equalsIgnoreCase(score.getResult())).count();
        long failCount = scores.stream().filter(score -> "FAIL".equalsIgnoreCase(score.getResult())).count();

        return "- Tổng số môn đã có điểm: " + scores.size() + "\n"
                + "- Số môn đạt: " + passCount + "\n"
                + "- Số môn chưa đạt: " + failCount + "\n"
                + "- Điểm trung bình hiện tại: " + formatAverageScore(scores) + "\n"
                + "- Gợi ý: theo dõi kỹ các học phần có điểm tổng kết thấp để chủ động cải thiện kết quả học tập.";
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
