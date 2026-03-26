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
                .orElseThrow(() -> new ValidationException("Không tìm thấy sinh viên đang đăng nhập."));
        return scheduleDAO.findByStudentId(student.getId());
    }

    public List<Schedule> findByCurrentLecturer() {
        permissionService.requirePermission(RolePermission.VIEW_OWN_SCHEDULE);
        Lecturer lecturer = lecturerDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Không tìm thấy giảng viên đang đăng nhập."));
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
        return findAll().stream()
                .filter(schedule -> schedule.getCourseSection() != null
                        && schedule.getCourseSection().getSubject() != null
                        && schedule.getCourseSection().getSubject().getFaculty() != null
                        && schedule.getCourseSection().getSubject().getFaculty().getId() != null
                        && schedule.getCourseSection().getSubject().getFaculty().getId().equals(facultyId))
                .toList();
    }

    public Schedule save(Schedule schedule) {
        permissionService.requirePermission(RolePermission.MANAGE_SCHEDULES);
        validate(schedule);

        if (scheduleDAO.hasLecturerScheduleConflict(schedule, schedule.getId())) {
            throw new ValidationException("Lịch học bị trùng với lịch dạy khác của giảng viên.");
        }
        if (scheduleDAO.hasRoomScheduleConflict(schedule, schedule.getId())) {
            throw new ValidationException("Lịch học bị trùng với lịch khác của phòng học.");
        }

        return schedule.getId() == null ? scheduleDAO.insert(schedule) : updateAndReturn(schedule);
    }

    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_SCHEDULES);
        scheduleDAO.findById(id)
                .orElseThrow(() -> new ValidationException("Không tìm thấy lịch học cần xóa."));
        return scheduleDAO.delete(id);
    }

    private Schedule updateAndReturn(Schedule schedule) {
        scheduleDAO.update(schedule);
        return schedule;
    }

    private void validate(Schedule schedule) {
        if (schedule.getCourseSection() == null || schedule.getCourseSection().getId() == null) {
            throw new ValidationException("Lịch học phải gắn với một học phần.");
        }
        ValidationUtil.requireNotBlank(schedule.getDayOfWeek(), "Thứ học không được để trống.");
        ValidationUtil.requirePositive(schedule.getStartPeriod(), "Tiết bắt đầu phải lớn hơn 0.");
        ValidationUtil.requirePositive(schedule.getEndPeriod(), "Tiết kết thúc phải lớn hơn 0.");
        if (schedule.getRoom() == null || schedule.getRoom().getId() == null) {
            throw new ValidationException("Phòng học không được để trống.");
        }
        if (schedule.getStartPeriod() >= schedule.getEndPeriod()) {
            throw new ValidationException("Tiết bắt đầu phải nhỏ hơn tiết kết thúc.");
        }
        courseSectionDAO.findById(schedule.getCourseSection().getId())
                .orElseThrow(() -> new ValidationException("Học phần của lịch học không tồn tại."));
    }
}
