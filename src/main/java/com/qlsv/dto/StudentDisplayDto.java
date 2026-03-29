/**
 * Gói dữ liệu hiển thị cho sinh viên hiển thị.
 */
package com.qlsv.dto;

/**
 * Xử lý sinh viên hiển thị dto.
 */
public record StudentDisplayDto(
        Long id,
        String studentCode,
        String fullName,
        String genderText,
        String dateOfBirthText,
        String email,
        String phone,
        String address,
        String facultyName,
        String classRoomName,
        String academicYear,
        String statusText,
        String userReference
) {
}
