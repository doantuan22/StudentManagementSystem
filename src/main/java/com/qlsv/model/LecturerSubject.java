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

    public LecturerSubject() {
    }

    public LecturerSubject(Lecturer lecturer, Subject subject) {
        setLecturer(lecturer);
        setSubject(subject);
    }

    public LecturerSubjectId getId() {
        return id;
    }

    public void setId(LecturerSubjectId id) {
        this.id = id == null ? new LecturerSubjectId() : id;
    }

    public Lecturer getLecturer() {
        return lecturer;
    }

    public void setLecturer(Lecturer lecturer) {
        this.lecturer = lecturer;
        if (id == null) {
            id = new LecturerSubjectId();
        }
        id.setLecturerId(lecturer == null ? null : lecturer.getId());
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
        if (id == null) {
            id = new LecturerSubjectId();
        }
        id.setSubjectId(subject == null ? null : subject.getId());
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
