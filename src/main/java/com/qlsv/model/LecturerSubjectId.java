/**
 * Mô tả thực thể giảng viên môn học id của hệ thống.
 */
package com.qlsv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class LecturerSubjectId implements Serializable {

    @Column(name = "lecturer_id", nullable = false)
    private Long lecturerId;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    /**
     * Khởi tạo giảng viên môn học id.
     */
    public LecturerSubjectId() {
    }

    /**
     * Khởi tạo giảng viên môn học id.
     */
    public LecturerSubjectId(Long lecturerId, Long subjectId) {
        this.lecturerId = lecturerId;
        this.subjectId = subjectId;
    }

    /**
     * Trả về giảng viên id.
     */
    public Long getLecturerId() {
        return lecturerId;
    }

    /**
     * Cập nhật giảng viên id.
     */
    public void setLecturerId(Long lecturerId) {
        this.lecturerId = lecturerId;
    }

    /**
     * Trả về môn học id.
     */
    public Long getSubjectId() {
        return subjectId;
    }

    /**
     * Cập nhật môn học id.
     */
    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    /**
     * So sánh đối tượng theo định danh phù hợp.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof LecturerSubjectId that)) {
            return false;
        }
        return Objects.equals(lecturerId, that.lecturerId)
                && Objects.equals(subjectId, that.subjectId);
    }

    /**
     * Tạo mã băm cho đối tượng.
     */
    @Override
    public int hashCode() {
        return Objects.hash(lecturerId, subjectId);
    }
}
