package com.qlsv.service;

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
                .orElseThrow(() -> new ValidationException("Khong tim thay sinh vien dang dang nhap."));
        return scheduleDAO.findByStudentId(student.getId());
    }

    public List<Schedule> findByCurrentLecturer() {
        permissionService.requirePermission(RolePermission.VIEW_OWN_SCHEDULE);
        Lecturer lecturer = lecturerDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Khong tim thay giang vien dang dang nhap."));
        return scheduleDAO.findByLecturerId(lecturer.getId());
    }

    public Schedule save(Schedule schedule) {
        permissionService.requirePermission(RolePermission.MANAGE_SCHEDULES);
        validate(schedule);

        // Chot nghiep vu tranh trung lich ngay tai service de moi man hinh dung chung mot quy tac.
        if (scheduleDAO.hasLecturerScheduleConflict(schedule, schedule.getId())) {
            throw new ValidationException("Lich hoc bi trung voi lich day khac cua giang vien.");
        }
        if (scheduleDAO.hasClassRoomScheduleConflict(schedule, schedule.getId())) {
            throw new ValidationException("Lich hoc bi trung voi lich khac cua lop.");
        }

        Schedule savedSchedule = schedule.getId() == null ? scheduleDAO.insert(schedule) : updateAndReturn(schedule);
        courseSectionDAO.updateScheduleText(savedSchedule.getCourseSection().getId(), savedSchedule.toDisplayText());
        return savedSchedule;
    }

    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_SCHEDULES);
        return scheduleDAO.delete(id);
    }

    private Schedule updateAndReturn(Schedule schedule) {
        scheduleDAO.update(schedule);
        return schedule;
    }

    private void validate(Schedule schedule) {
        if (schedule.getCourseSection() == null || schedule.getCourseSection().getId() == null) {
            throw new ValidationException("Lich hoc phai gan voi mot hoc phan.");
        }
        ValidationUtil.requireNotBlank(schedule.getDayOfWeek(), "Thu hoc khong duoc de trong.");
        ValidationUtil.requirePositive(schedule.getStartPeriod(), "Tiet bat dau phai lon hon 0.");
        ValidationUtil.requirePositive(schedule.getEndPeriod(), "Tiet ket thuc phai lon hon 0.");
        ValidationUtil.requireNotBlank(schedule.getRoom(), "Phong hoc khong duoc de trong.");
        if (schedule.getStartPeriod() > schedule.getEndPeriod()) {
            throw new ValidationException("Tiet bat dau khong duoc lon hon tiet ket thuc.");
        }
        courseSectionDAO.findById(schedule.getCourseSection().getId())
                .orElseThrow(() -> new ValidationException("Hoc phan cua lich hoc khong ton tai."));
    }
}
