/**
 * Mô tả thực thể thống kê hệ thống của hệ thống.
 */
package com.qlsv.model;

public class SystemStatistics {

    private long totalStudents;
    private long totalLecturers;
    private long totalSubjects;
    private long totalCourseSections;
    private long totalEnrollments;

    /**
     * Khởi tạo thống kê hệ thống.
     */
    public SystemStatistics() {
    }

    /**
     * Khởi tạo thống kê hệ thống.
     */
    public SystemStatistics(long totalStudents, long totalLecturers, long totalSubjects,
                            long totalCourseSections, long totalEnrollments) {
        this.totalStudents = totalStudents;
        this.totalLecturers = totalLecturers;
        this.totalSubjects = totalSubjects;
        this.totalCourseSections = totalCourseSections;
        this.totalEnrollments = totalEnrollments;
    }

    /**
     * Trả về tổng sinh viên.
     */
    public long getTotalStudents() {
        return totalStudents;
    }

    /**
     * Cập nhật tổng sinh viên.
     */
    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    /**
     * Trả về tổng giảng viên.
     */
    public long getTotalLecturers() {
        return totalLecturers;
    }

    /**
     * Cập nhật tổng giảng viên.
     */
    public void setTotalLecturers(long totalLecturers) {
        this.totalLecturers = totalLecturers;
    }

    /**
     * Trả về tổng môn học.
     */
    public long getTotalSubjects() {
        return totalSubjects;
    }

    /**
     * Cập nhật tổng môn học.
     */
    public void setTotalSubjects(long totalSubjects) {
        this.totalSubjects = totalSubjects;
    }

    /**
     * Trả về tổng học phần.
     */
    public long getTotalCourseSections() {
        return totalCourseSections;
    }

    /**
     * Cập nhật tổng học phần.
     */
    public void setTotalCourseSections(long totalCourseSections) {
        this.totalCourseSections = totalCourseSections;
    }

    /**
     * Trả về tổng đăng ký.
     */
    public long getTotalEnrollments() {
        return totalEnrollments;
    }

    /**
     * Cập nhật tổng đăng ký.
     */
    public void setTotalEnrollments(long totalEnrollments) {
        this.totalEnrollments = totalEnrollments;
    }
}
