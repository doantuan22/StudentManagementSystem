package com.qlsv.dto;

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
