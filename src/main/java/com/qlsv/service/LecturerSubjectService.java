/**
 * Xử lý nghiệp vụ giảng viên môn học.
 */
package com.qlsv.service;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.dao.LecturerSubjectDAO;
import com.qlsv.model.Lecturer;
import com.qlsv.model.LecturerSubject;
import com.qlsv.model.Subject;
import com.qlsv.security.RolePermission;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class LecturerSubjectService {

    private static volatile boolean backfillAttempted;

    private final LecturerSubjectDAO lecturerSubjectDAO = new LecturerSubjectDAO();
    private final PermissionService permissionService = new PermissionService();

    /**
     * Trả về môn học theo giảng viên.
     */
    public List<Subject> getSubjectsByLecturer(Long lecturerId) {
        permissionService.requireLogin();
        List<LecturerSubject> rows = lecturerSubjectDAO.findByLecturerId(lecturerId);
        List<Subject> subjects = new ArrayList<>();
        for (LecturerSubject row : rows) {
            if (row.getSubject() != null) {
                subjects.add(row.getSubject());
            }
        }
        return subjects;
    }

    /**
     * Trả về giảng viên theo môn học.
     */
    public List<Lecturer> getLecturersBySubject(Long subjectId) {
        permissionService.requireLogin();
        List<LecturerSubject> rows = lecturerSubjectDAO.findBySubjectId(subjectId);
        List<Lecturer> lecturers = new ArrayList<>();
        for (LecturerSubject row : rows) {
            if (row.getLecturer() != null) {
                lecturers.add(row.getLecturer());
            }
        }
        return lecturers;
    }

    /**
     * Xử lý exists.
     */
    public boolean exists(Long lecturerId, Long subjectId) {
        permissionService.requireLogin();
        return lecturerSubjectDAO.exists(lecturerId, subjectId);
    }

    /**
     * Lưu môn học for giảng viên.
     */
    public void saveSubjectsForLecturer(Long lecturerId, List<Subject> subjects) {
        permissionService.requirePermission(RolePermission.MANAGE_LECTURERS);
        JpaBootstrap.executeInTransaction(
                "KhÃ´ng thá»ƒ lÆ°u whitelist mÃ´n giáº£ng dáº¡y.",
                ignored -> {
                    lecturerSubjectDAO.saveAll(lecturerId, extractSubjectIds(subjects));
                    backfillAttempted = true;
                    return null;
                }
        );
    }

    /**
     * Xử lý backfill from học phần if needed.
     */
    public int backfillFromCourseSectionsIfNeeded() {
        permissionService.requirePermission(RolePermission.MANAGE_LECTURERS);
        if (backfillAttempted) {
            return 0;
        }
        synchronized (LecturerSubjectService.class) {
            if (backfillAttempted) {
                return 0;
            }
            int inserted = JpaBootstrap.executeInTransaction(
                    "KhÃ´ng thá»ƒ Ä‘á»“ng bá»™ whitelist giÃ¡ng viÃªn - mÃ´n há»c tá»« dá»¯ liá»‡u há»c pháº§n hiá»‡n cÃ³.",
                    ignored -> lecturerSubjectDAO.backfillFromCourseSectionsIfEmpty()
            );
            backfillAttempted = true;
            return inserted;
        }
    }

    /**
     * Tách môn học ids.
     */
    private List<Long> extractSubjectIds(List<Subject> subjects) {
        Set<Long> subjectIds = new LinkedHashSet<>();
        if (subjects != null) {
            for (Subject subject : subjects) {
                if (subject != null && subject.getId() != null) {
                    subjectIds.add(subject.getId());
                }
            }
        }
        return new ArrayList<>(subjectIds);
    }
}
