/**
 * Gói dữ liệu hiển thị cho hồ sơ sinh viên.
 */
package com.qlsv.dto;

/**
 * Xử lý hồ sơ sinh viên dto.
 */
public record StudentProfileDto(
        String studentCode,
        String fullName,
        String genderText,
        String dateOfBirthText,
        String email,
        String phone,
        String address,
        String classRoomName,
        String facultyName,
        String academicYear,
        String statusText
) {
}
