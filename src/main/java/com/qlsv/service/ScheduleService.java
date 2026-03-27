package com.qlsv.service;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.config.SessionManager;
import com.qlsv.dao.CourseSectionDAO;
import com.qlsv.dao.LecturerDAO;
import com.qlsv.dao.ScheduleDAO;
import com.qlsv.dao.StudentDAO;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.Lecturer;
import com.qlsv.model.Schedule;
import com.qlsv.model.Student;
import com.qlsv.security.RolePermission;
import com.qlsv.utils.ValidationUtil;

import java.util.List;

public class ScheduleService {

    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final CourseSectionDAO courseSectionDAO = new CourseSectionDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final LecturerDAO lecturerDAO = new LecturerDAO();
    private final PermissionService permissionService = new PermissionService();

    public List<Schedule> findAll() {
        permissionService.requirePermission(RolePermission.MANAGE_SCHEDULES);
        return scheduleDAO.findAll();
    }

    public List<Schedule> findByCurrentStudent() {
        permissionService.requirePermission(RolePermission.VIEW_OWN_SCHEDULE);
        Student student = studentDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("KhÃ´ng tÃ¬m tháº¥y sinh viÃªn Ä‘ang Ä‘Äƒng nháº­p."));
        return scheduleDAO.findByStudentId(student.getId());
    }

    public List<Schedule> findByCurrentLecturer() {
        permissionService.requirePermission(RolePermission.VIEW_OWN_SCHEDULE);
        Lecturer lecturer = lecturerDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("KhÃ´ng tÃ¬m tháº¥y giáº£ng viÃªn Ä‘ang Ä‘Äƒng nháº­p."));
        return scheduleDAO.findByLecturerId(lecturer.getId());
    }

    public List<Schedule> findByCourseSectionId(Long courseSectionId) {
        permissionService.requirePermission(RolePermission.MANAGE_SCHEDULES);
        return scheduleDAO.findByCourseSectionId(courseSectionId);
    }

    public List<Schedule> findByRoom(Long roomId) {
        permissionService.requirePermission(RolePermission.MANAGE_SCHEDULES);
        return scheduleDAO.findByRoom(roomId);
    }

    public List<Schedule> findByFacultyId(Long facultyId) {
        permissionService.requirePermission(RolePermission.MANAGE_SCHEDULES);
        return scheduleDAO.findByFacultyId(facultyId);
    }

    public Schedule save(Schedule schedule) {
        permissionService.requirePermission(RolePermission.MANAGE_SCHEDULES);
        return JpaBootstrap.executeInTransaction(
                "KhÃ´ng thá»ƒ lÆ°u lá»‹ch há»c.",
                ignored -> {
                    validate(schedule);

                    if (scheduleDAO.hasLecturerScheduleConflict(schedule, schedule.getId())) {
                        throw new ValidationException("Lá»‹ch há»c bá»‹ trÃ¹ng vá»›i lá»‹ch dáº¡y khÃ¡c cá»§a giáº£ng viÃªn.");
                    }
                    if (scheduleDAO.hasRoomScheduleConflict(schedule, schedule.getId())) {
                        throw new ValidationException("Lá»‹ch há»c bá»‹ trÃ¹ng vá»›i lá»‹ch khÃ¡c cá»§a phÃ²ng há»c.");
                    }

                    return schedule.getId() == null ? scheduleDAO.insert(schedule) : updateAndReturn(schedule);
                }
        );
    }

    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_SCHEDULES);
        return JpaBootstrap.executeInTransaction(
                "KhÃ´ng thá»ƒ xÃ³a lá»‹ch há»c.",
                ignored -> {
                    scheduleDAO.findById(id)
                            .orElseThrow(() -> new ValidationException("KhÃ´ng tÃ¬m tháº¥y lá»‹ch há»c cáº§n xÃ³a."));
                    return scheduleDAO.delete(id);
                }
        );
    }

    private Schedule updateAndReturn(Schedule schedule) {
        scheduleDAO.update(schedule);
        return schedule;
    }

    private void validate(Schedule schedule) {
        if (schedule.getCourseSection() == null || schedule.getCourseSection().getId() == null) {
            throw new ValidationException("Lá»‹ch há»c pháº£i gáº¯n vá»›i má»™t há»c pháº§n.");
        }
        ValidationUtil.requireNotBlank(schedule.getDayOfWeek(), "Thá»© há»c khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.");
        ValidationUtil.requirePositive(schedule.getStartPeriod(), "Tiáº¿t báº¯t Ä‘áº§u pháº£i lá»›n hÆ¡n 0.");
        ValidationUtil.requirePositive(schedule.getEndPeriod(), "Tiáº¿t káº¿t thÃºc pháº£i lá»›n hÆ¡n 0.");
        if (schedule.getRoom() == null || schedule.getRoom().getId() == null) {
            throw new ValidationException("PhÃ²ng há»c khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.");
        }
        if (schedule.getStartPeriod() >= schedule.getEndPeriod()) {
            throw new ValidationException("Tiáº¿t báº¯t Ä‘áº§u pháº£i nhá» hÆ¡n tiáº¿t káº¿t thÃºc.");
        }
        courseSectionDAO.findById(schedule.getCourseSection().getId())
                .orElseThrow(() -> new ValidationException("Há»c pháº§n cá»§a lá»‹ch há»c khÃ´ng tá»“n táº¡i."));
    }
}
