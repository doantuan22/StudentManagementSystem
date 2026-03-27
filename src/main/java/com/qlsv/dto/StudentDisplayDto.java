package com.qlsv.dto;

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
