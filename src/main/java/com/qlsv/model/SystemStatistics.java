package com.qlsv.model;

public class SystemStatistics {

    private long totalStudents;
    private long totalLecturers;
    private long totalSubjects;
    private long totalCourseSections;
    private long totalEnrollments;

    public SystemStatistics() {
    }

    public SystemStatistics(long totalStudents, long totalLecturers, long totalSubjects,
                            long totalCourseSections, long totalEnrollments) {
        this.totalStudents = totalStudents;
        this.totalLecturers = totalLecturers;
        this.totalSubjects = totalSubjects;
        this.totalCourseSections = totalCourseSections;
        this.totalEnrollments = totalEnrollments;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getTotalLecturers() {
        return totalLecturers;
    }

    public void setTotalLecturers(long totalLecturers) {
        this.totalLecturers = totalLecturers;
    }

    public long getTotalSubjects() {
        return totalSubjects;
    }

    public void setTotalSubjects(long totalSubjects) {
        this.totalSubjects = totalSubjects;
    }

    public long getTotalCourseSections() {
        return totalCourseSections;
    }

    public void setTotalCourseSections(long totalCourseSections) {
        this.totalCourseSections = totalCourseSections;
    }

    public long getTotalEnrollments() {
        return totalEnrollments;
    }

    public void setTotalEnrollments(long totalEnrollments) {
        this.totalEnrollments = totalEnrollments;
    }
}
