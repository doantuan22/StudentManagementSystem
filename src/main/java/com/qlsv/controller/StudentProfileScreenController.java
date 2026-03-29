/**
 * Điều phối dữ liệu cho hồ sơ sinh viên màn hình.
 */
package com.qlsv.controller;

import com.qlsv.dto.DisplayDtoMapper;
import com.qlsv.dto.StudentProfileDto;
import com.qlsv.model.Student;

public class StudentProfileScreenController {

    private final StudentController studentController = new StudentController();

    /**
     * Nạp sinh viên hiện tại.
     */
    public StudentProfileDto loadCurrentStudent() {
        return DisplayDtoMapper.toStudentProfileDto(studentController.getCurrentStudent());
    }

    /**
     * Cập nhật sinh viên contact thông tin hiện tại.
     */
    public StudentProfileDto updateCurrentStudentContactInfo(String email, String phone, String address) {
        String normalizedEmail = email == null ? "" : email.trim();
        String normalizedPhone = phone == null ? "" : phone.trim();
        String normalizedAddress = address == null ? "" : address.trim();
        Student updatedStudent = studentController.updateCurrentStudentContactInfo(normalizedEmail, normalizedPhone, normalizedAddress);
        return DisplayDtoMapper.toStudentProfileDto(updatedStudent);
    }
}
