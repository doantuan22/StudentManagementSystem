/**
 * Mô tả thực thể sinh viên của hệ thống.
 */
package com.qlsv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "student_code", nullable = false, unique = true, length = 50)
    private String studentCode;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_room_id", nullable = false)
    private ClassRoom classRoom;

    @Column(name = "academic_year", nullable = false, length = 50)
    private String academicYear;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    /**
     * Khởi tạo sinh viên.
     */
    public Student() {
    }

    /**
     * Khởi tạo sinh viên.
     */
    public Student(Long id, Long userId, String studentCode, String fullName, String gender, LocalDate dateOfBirth,
                   String email, String phone, String address, Faculty faculty, ClassRoom classRoom, String academicYear, String status) {
        this.id = id;
        setUserId(userId);
        this.studentCode = studentCode;
        this.fullName = fullName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.faculty = faculty;
        this.classRoom = classRoom;
        this.academicYear = academicYear;
        this.status = status;
    }

    /**
     * Trả về id.
     */
    public Long getId() {
        return id;
    }

    /**
     * Cập nhật id.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Trả về người dùng id.
     */
    public Long getUserId() {
        return user == null ? null : user.getId();
    }

    /**
     * Cập nhật người dùng id.
     */
    public void setUserId(Long userId) {
        if (userId == null) {
            this.user = null;
            return;
        }
        if (this.user == null) {
            this.user = new User();
        }
        this.user.setId(userId);
    }

    /**
     * Trả về người dùng.
     */
    public User getUser() {
        return user;
    }

    /**
     * Cập nhật người dùng.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Trả về sinh viên mã.
     */
    public String getStudentCode() {
        return studentCode;
    }

    /**
     * Cập nhật sinh viên mã.
     */
    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    /**
     * Trả về họ tên.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Cập nhật họ tên.
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Trả về gender.
     */
    public String getGender() {
        return gender;
    }

    /**
     * Cập nhật gender.
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * Trả về ngày sinh.
     */
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Cập nhật ngày sinh.
     */
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Trả về email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Cập nhật email.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Trả về điện thoại.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Cập nhật điện thoại.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Trả về địa chỉ.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Cập nhật địa chỉ.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Trả về khoa.
     */
    public Faculty getFaculty() {
        return faculty;
    }

    /**
     * Cập nhật khoa.
     */
    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    /**
     * Trả về lớp.
     */
    public ClassRoom getClassRoom() {
        return classRoom;
    }

    /**
     * Cập nhật lớp.
     */
    public void setClassRoom(ClassRoom classRoom) {
        this.classRoom = classRoom;
    }

    /**
     * Trả về niên khóa.
     */
    public String getAcademicYear() {
        return academicYear;
    }

    /**
     * Cập nhật niên khóa.
     */
    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    /**
     * Trả về trạng thái.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Cập nhật trạng thái.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Trả về chuỗi hiển thị của đối tượng.
     */
    @Override
    public String toString() {
        return studentCode + " - " + fullName;
    }

    /**
     * So sánh đối tượng theo định danh phù hợp.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Student student)) {
            return false;
        }
        if (id != null && student.id != null) {
            return Objects.equals(id, student.id);
        }
        return Objects.equals(studentCode, student.studentCode);
    }

    /**
     * Tạo mã băm cho đối tượng.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : studentCode);
    }
}
