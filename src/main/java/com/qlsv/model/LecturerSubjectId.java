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

    public LecturerSubjectId() {
    }

    public LecturerSubjectId(Long lecturerId, Long subjectId) {
        this.lecturerId = lecturerId;
        this.subjectId = subjectId;
    }

    public Long getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(Long lecturerId) {
        this.lecturerId = lecturerId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(lecturerId, subjectId);
    }
}
