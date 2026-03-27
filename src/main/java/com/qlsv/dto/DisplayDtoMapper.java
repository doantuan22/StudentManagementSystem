package com.qlsv.dto;

import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Schedule;
import com.qlsv.model.Score;
import com.qlsv.model.Student;
import com.qlsv.utils.AcademicFormatUtil;
import com.qlsv.utils.DisplayTextUtil;

public final class DisplayDtoMapper {

    private DisplayDtoMapper() {
    }

    public static StudentDisplayDto toStudentDisplayDto(Student student) {
        return new StudentDisplayDto(
                student == null ? null : student.getId(),
                student == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(student.getStudentCode()),
                student == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(student.getFullName()),
                student == null ? "Chưa cập nhật" : DisplayTextUtil.formatGender(student.getGender()),
                student == null ? "Chưa cập nhật" : DisplayTextUtil.formatDate(student.getDateOfBirth()),
                student == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(student.getEmail()),
                student == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(student.getPhone()),
                student == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(student.getAddress()),
                student == null || student.getFaculty() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(student.getFaculty().getFacultyName()),
                student == null || student.getClassRoom() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(student.getClassRoom().getClassName()),
                student == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(AcademicFormatUtil.formatAcademicYear(student.getAcademicYear())),
                student == null ? "Chưa cập nhật" : DisplayTextUtil.formatStatus(student.getStatus()),
                student == null ? "Chưa cập nhật" : DisplayTextUtil.formatUserReference(student.getUserId())
        );
    }

    public static StudentProfileDto toStudentProfileDto(Student student) {
        StudentDisplayDto displayDto = toStudentDisplayDto(student);
        return new StudentProfileDto(
                displayDto.studentCode(),
                displayDto.fullName(),
                displayDto.genderText(),
                displayDto.dateOfBirthText(),
                displayDto.email(),
                displayDto.phone(),
                displayDto.address(),
                displayDto.classRoomName(),
                displayDto.facultyName(),
                displayDto.academicYear(),
                displayDto.statusText()
        );
    }

    public static CourseSectionDisplayDto toCourseSectionDisplayDto(CourseSection courseSection) {
        return toCourseSectionDisplayDto(courseSection, null);
    }

    public static CourseSectionDisplayDto toCourseSectionDisplayDto(CourseSection courseSection, String slotsText) {
        return new CourseSectionDisplayDto(
                courseSection == null ? null : courseSection.getId(),
                courseSection == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(courseSection.getSectionCode()),
                courseSection == null || courseSection.getSubject() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(courseSection.getSubject().getSubjectName()),
                courseSection == null || courseSection.getSubject() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(courseSection.getSubject().getCredits()),
                courseSection == null || courseSection.getLecturer() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(courseSection.getLecturer().getFullName()),
                courseSection == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(AcademicFormatUtil.formatSemester(courseSection.getSemester())),
                courseSection == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(AcademicFormatUtil.formatAcademicYear(courseSection.getSchoolYear())),
                courseSection == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(courseSection.getScheduleText()),
                courseSection == null || courseSection.getRoom() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(courseSection.getRoom().getRoomName()),
                courseSection == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(courseSection.getMaxStudents()),
                DisplayTextUtil.defaultText(slotsText)
        );
    }

    public static EnrollmentDisplayDto toEnrollmentDisplayDto(Enrollment enrollment) {
        CourseSection courseSection = enrollment == null ? null : enrollment.getCourseSection();
        Student student = enrollment == null ? null : enrollment.getStudent();
        CourseSectionDisplayDto courseSectionDto = toCourseSectionDisplayDto(courseSection);

        return new EnrollmentDisplayDto(
                enrollment == null ? null : enrollment.getId(),
                student == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(student.getStudentCode()),
                student == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(student.getFullName()),
                student == null || student.getClassRoom() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(student.getClassRoom().getClassName()),
                courseSectionDto.sectionCode(),
                courseSectionDto.subjectName(),
                courseSectionDto.lecturerName(),
                courseSectionDto.roomName(),
                courseSectionDto.scheduleText(),
                enrollment == null ? "Chưa cập nhật" : DisplayTextUtil.formatStatus(enrollment.getStatus()),
                enrollment == null ? "Chưa cập nhật" : DisplayTextUtil.formatDateTime(enrollment.getEnrolledAt())
        );
    }

    public static ScoreDisplayDto toScoreDisplayDto(Score score) {
        Enrollment enrollment = score == null ? null : score.getEnrollment();
        EnrollmentDisplayDto enrollmentDto = toEnrollmentDisplayDto(enrollment);

        return new ScoreDisplayDto(
                score == null ? null : score.getId(),
                enrollmentDto.studentCode(),
                enrollmentDto.studentName(),
                enrollmentDto.classRoomName(),
                enrollmentDto.sectionCode(),
                enrollmentDto.subjectName(),
                enrollmentDto.lecturerName(),
                enrollmentDto.roomName(),
                score == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(score.getProcessScore()),
                score == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(score.getMidtermScore()),
                score == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(score.getFinalScore()),
                score == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(score.getTotalScore()),
                score == null ? "Chưa cập nhật" : DisplayTextUtil.formatStatus(score.getResult())
        );
    }

    public static ScheduleDisplayDto toScheduleDisplayDto(Schedule schedule) {
        CourseSection courseSection = schedule == null ? null : schedule.getCourseSection();
        boolean unscheduled = schedule != null && schedule.getId() == null;
        return new ScheduleDisplayDto(
                schedule == null ? null : schedule.getId(),
                courseSection == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(courseSection.getSectionCode()),
                courseSection == null || courseSection.getSubject() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(courseSection.getSubject().getSubjectName()),
                courseSection == null || courseSection.getLecturer() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(courseSection.getLecturer().getFullName()),
                schedule == null ? "Chưa cập nhật" : unscheduled ? "Chưa có lịch" : DisplayTextUtil.defaultText(schedule.getDayOfWeek()),
                schedule == null || unscheduled ? "" : DisplayTextUtil.formatPeriod(schedule.getStartPeriod(), schedule.getEndPeriod()),
                schedule == null || schedule.getRoom() == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(schedule.getRoom().getRoomName()),
                schedule == null ? "Chưa cập nhật" : DisplayTextUtil.defaultText(schedule.getNote()),
                unscheduled
        );
    }
}
