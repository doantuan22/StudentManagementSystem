package com.qlsv.security;

import com.qlsv.model.Role;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public final class RolePermission {

    public static final String MANAGE_STUDENTS = "MANAGE_STUDENTS";
    public static final String MANAGE_LECTURERS = "MANAGE_LECTURERS";
    public static final String MANAGE_FACULTIES = "MANAGE_FACULTIES";
    public static final String MANAGE_CLASSES = "MANAGE_CLASSES";
    public static final String MANAGE_SUBJECTS = "MANAGE_SUBJECTS";
    public static final String MANAGE_COURSE_SECTIONS = "MANAGE_COURSE_SECTIONS";
    public static final String MANAGE_ENROLLMENTS = "MANAGE_ENROLLMENTS";
    public static final String MANAGE_SCORES = "MANAGE_SCORES";
    public static final String MANAGE_SCHEDULES = "MANAGE_SCHEDULES";
    public static final String VIEW_REPORTS = "VIEW_REPORTS";
    public static final String VIEW_SYSTEM_STATISTICS = "VIEW_SYSTEM_STATISTICS";
    public static final String VIEW_OWN_PROFILE = "VIEW_OWN_PROFILE";
    public static final String EDIT_OWN_PROFILE = "EDIT_OWN_PROFILE";
    public static final String VIEW_ASSIGNED_CLASSES = "VIEW_ASSIGNED_CLASSES";
    public static final String VIEW_ASSIGNED_STUDENTS = "VIEW_ASSIGNED_STUDENTS";
    public static final String REGISTER_ENROLLMENT = "REGISTER_ENROLLMENT";
    public static final String VIEW_OWN_SCORES = "VIEW_OWN_SCORES";
    public static final String VIEW_OWN_SCHEDULE = "VIEW_OWN_SCHEDULE";

    private static final Map<Role, Set<String>> PERMISSION_MAP = buildPermissionMap();

    private RolePermission() {
    }

    private static Map<Role, Set<String>> buildPermissionMap() {
        Map<Role, Set<String>> permissionMap = new EnumMap<>(Role.class);
        permissionMap.put(Role.ADMIN, Set.of(
                MANAGE_STUDENTS,
                MANAGE_LECTURERS,
                MANAGE_FACULTIES,
                MANAGE_CLASSES,
                MANAGE_SUBJECTS,
                MANAGE_COURSE_SECTIONS,
                MANAGE_ENROLLMENTS,
                MANAGE_SCORES,
                MANAGE_SCHEDULES,
                VIEW_REPORTS,
                VIEW_SYSTEM_STATISTICS,
                VIEW_OWN_PROFILE,
                EDIT_OWN_PROFILE,
                VIEW_OWN_SCHEDULE
        ));
        permissionMap.put(Role.LECTURER, Set.of(
                VIEW_OWN_PROFILE,
                EDIT_OWN_PROFILE,
                VIEW_ASSIGNED_CLASSES,
                VIEW_ASSIGNED_STUDENTS,
                MANAGE_SCORES,
                VIEW_OWN_SCHEDULE
        ));
        permissionMap.put(Role.STUDENT, Set.of(
                VIEW_OWN_PROFILE,
                EDIT_OWN_PROFILE,
                REGISTER_ENROLLMENT,
                VIEW_OWN_SCORES,
                VIEW_OWN_SCHEDULE
        ));
        return permissionMap;
    }

    public static boolean hasPermission(Role role, String permission) {
        if (role == null || permission == null) {
            return false;
        }
        return PERMISSION_MAP.getOrDefault(role, Set.of()).contains(permission);
    }

    public static Set<String> getPermissions(Role role) {
        return PERMISSION_MAP.getOrDefault(role, Set.of());
    }
}
