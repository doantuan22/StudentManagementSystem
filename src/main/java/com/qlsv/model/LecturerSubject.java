/**
 * Mô tả thực thể giảng viên môn học của hệ thống.
 */
package com.qlsv.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "lecturer_subjects")
public class LecturerSubject {

    @EmbeddedId
    private LecturerSubjectId id = new LecturerSubjectId();

    @MapsId("lecturerId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;

    @MapsId("subjectId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    /**
     * Khởi tạo giảng viên môn học.
     */
    public LecturerSubject() {
    }

    /**
     * Khởi tạo giảng viên môn học.
     */
    public LecturerSubject(Lecturer lecturer, Subject subject) {
        setLecturer(lecturer);
        setSubject(subject);
    }

    /**
     * Trả về id.
     */
    public LecturerSubjectId getId() {
        return id;
    }

    /**
     * Cập nhật id.
     */
    public void setId(LecturerSubjectId id) {
        this.id = id == null ? new LecturerSubjectId() : id;
    }

    /**
     * Trả về giảng viên.
     */
    public Lecturer getLecturer() {
        return lecturer;
    }

    /**
     * Cập nhật giảng viên.
     */
    public void setLecturer(Lecturer lecturer) {
        this.lecturer = lecturer;
        if (id == null) {
            id = new LecturerSubjectId();
        }
        id.setLecturerId(lecturer == null ? null : lecturer.getId());
    }

    /**
     * Trả về môn học.
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     * Cập nhật môn học.
     */
    public void setSubject(Subject subject) {
        this.subject = subject;
        if (id == null) {
            id = new LecturerSubjectId();
        }
        id.setSubjectId(subject == null ? null : subject.getId());
    }

    /**
     * So sánh đối tượng theo định danh phù hợp.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof LecturerSubject that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    /**
     * Tạo mã băm cho đối tượng.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
