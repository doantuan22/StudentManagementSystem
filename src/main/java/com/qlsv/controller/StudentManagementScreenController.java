package com.qlsv.controller;

import com.qlsv.dto.DisplayDtoMapper;
import com.qlsv.dto.StudentDisplayDto;
import com.qlsv.model.ClassRoom;
import com.qlsv.model.Faculty;
import com.qlsv.model.Student;
import com.qlsv.utils.AcademicFormatUtil;

import java.time.LocalDate;
import java.util.List;

public class StudentManagementScreenController {

    private final StudentController studentController = new StudentController();
    private final FacultyController facultyController = new FacultyController();
    private final ClassRoomController classRoomController = new ClassRoomController();

    public List<Student> loadItems(boolean filterReady, String filterType, Object filterValue,
                                   String filterAll, String filterFaculty, String filterClassRoom, String filterAcademicYear) {
        if (!filterReady) {
            return List.of();
        }
        String normalizedFilterType = filterType == null ? "" : filterType;
        if (normalizedFilterType.equals(filterAll)) {
            return studentController.getAllStudents();
        }
        if (normalizedFilterType.equals(filterFaculty) && filterValue instanceof Faculty faculty) {
            return studentController.getStudentsByFaculty(faculty.getId());
        }
        if (normalizedFilterType.equals(filterClassRoom) && filterValue instanceof ClassRoom classRoom) {
            return studentController.getStudentsByClassRoom(classRoom.getId());
        }
        if (normalizedFilterType.equals(filterAcademicYear) && filterValue instanceof String academicYear) {
            return studentController.getStudentsByAcademicYear(academicYear);
        }
        return List.of();
    }

    public List<Student> searchItems(boolean filterReady, String filterType, Object filterValue,
                                     String filterAll, String filterFaculty, String filterClassRoom,
                                     String filterAcademicYear, String keyword) {
        if (!filterReady) {
            return List.of();
        }

        String normalizedFilterType = filterType == null ? "" : filterType;
        Long facultyId = null;
        Long classRoomId = null;
        String academicYear = null;

        if (normalizedFilterType.equals(filterFaculty) && filterValue instanceof Faculty faculty) {
            facultyId = faculty.getId();
        } else if (normalizedFilterType.equals(filterClassRoom) && filterValue instanceof ClassRoom classRoom) {
            classRoomId = classRoom.getId();
        } else if (normalizedFilterType.equals(filterAcademicYear) && filterValue instanceof String selectedAcademicYear) {
            academicYear = selectedAcademicYear;
        } else if (!normalizedFilterType.equals(filterAll)) {
            return List.of();
        }

        return studentController.searchStudents(keyword, facultyId, classRoomId, academicYear);
    }

    public Student resolveSelection(Student selectedItem) {
        if (selectedItem == null || selectedItem.getId() == null) {
            return selectedItem;
        }
        return studentController.getStudentById(selectedItem.getId());
    }

    public StudentDisplayDto toDisplayDto(Student student) {
        return DisplayDtoMapper.toStudentDisplayDto(student);
    }

    public List<DisplayField> buildDetailFields(Student student) {
        if (student == null) {
            return List.of();
        }
        StudentDisplayDto displayDto = toDisplayDto(student);
        return List.of(
                new DisplayField("Mã sinh viên", displayDto.studentCode()),
                new DisplayField("Họ và tên", displayDto.fullName()),
                new DisplayField("Giới tính", displayDto.genderText()),
                new DisplayField("Ngày sinh", displayDto.dateOfBirthText()),
                new DisplayField("Số điện thoại", displayDto.phone()),
                new DisplayField("Email", displayDto.email()),
                new DisplayField("Khoa", displayDto.facultyName()),
                new DisplayField("Lớp", displayDto.classRoomName()),
                new DisplayField("Niên khóa", displayDto.academicYear()),
                new DisplayField("Trạng thái", displayDto.statusText()),
                new DisplayField("Địa chỉ", displayDto.address()),
                new DisplayField("ID người dùng", displayDto.userReference())
        );
    }

    public List<Faculty> loadFaculties() {
        return facultyController.getFacultiesForSelection();
    }

    public List<ClassRoom> loadClassRooms() {
        return classRoomController.getClassRoomsForSelection();
    }

    public List<String> loadAcademicYears() {
        return studentController.getAcademicYearsForSelection();
    }

    public List<ClassRoom> filterClassRooms(List<ClassRoom> allClassRooms, Faculty selectedFaculty) {
        if (selectedFaculty == null || selectedFaculty.getId() == null) {
            return allClassRooms;
        }
        return allClassRooms.stream()
                .filter(classRoom -> classRoom.getFaculty() == null
                        || classRoom.getFaculty().getId() == null
                        || selectedFaculty.getId().equals(classRoom.getFaculty().getId()))
                .toList();
    }

    public Student applyFormData(Student existingItem, StudentFormData formData) {
        Student student = existingItem == null ? new Student() : existingItem;
        student.setStudentCode(formData.studentCode().trim());
        student.setFullName(formData.fullName().trim());
        student.setGender(formData.gender());
        student.setDateOfBirth(formData.dateOfBirth().isBlank() ? null : LocalDate.parse(formData.dateOfBirth().trim()));
        student.setEmail(formData.email().trim());
        student.setPhone(formData.phone().trim());
        student.setAddress(formData.address().trim());
        student.setFaculty(formData.faculty());
        student.setClassRoom(formData.classRoom());
        student.setAcademicYear(AcademicFormatUtil.normalizeAcademicYear(formData.academicYear(), "Niên khóa"));
        student.setStatus(formData.status());
        return student;
    }

    public void saveStudent(Student student) {
        studentController.saveStudent(student);
    }

    public void deleteStudent(Student student) {
        studentController.deleteStudent(student.getId());
    }

    public record StudentFormData(
            String studentCode,
            String fullName,
            String gender,
            String dateOfBirth,
            String email,
            String phone,
            String address,
            Faculty faculty,
            ClassRoom classRoom,
            String academicYear,
            String status
    ) {
    }
}
