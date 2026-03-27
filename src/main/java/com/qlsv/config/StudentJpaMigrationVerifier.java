package com.qlsv.config;

import com.qlsv.controller.StudentController;
import com.qlsv.dao.StudentDAO;
import com.qlsv.dao.UserDAO;
import com.qlsv.exception.AppException;
import com.qlsv.model.ClassRoom;
import com.qlsv.model.Faculty;
import com.qlsv.model.Role;
import com.qlsv.model.Student;
import com.qlsv.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class StudentJpaMigrationVerifier {

    private static final DateTimeFormatter CODE_SUFFIX_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final StudentDAO studentDAO = new StudentDAO();
    private final StudentController studentController = new StudentController();
    private final UserDAO userDAO = new UserDAO();

    public static void main(String[] args) {
        StudentJpaMigrationVerifier verifier = new StudentJpaMigrationVerifier();
        verifier.run();
    }

    public void run() {
        verifyStudentDaoReadPaths();
        verifyControllerAndServiceFlow();
        verifyJpaCrudWithRollback();
        System.out.println("STUDENT_JPA_MIGRATION_OK");
    }

    private void verifyStudentDaoReadPaths() {
        List<Student> students = studentDAO.findAll();

        assertCondition(!students.isEmpty(), "Khong co du lieu Student de verify DAO read path.");

        Student sampleStudent = students.get(0);
        compareStudent(
                sampleStudent,
                studentDAO.findById(sampleStudent.getId()).orElseThrow(() ->
                        new AppException("DAO khong tim thay Student mau theo id.")),
                "findById"
        );

        students.stream()
                .filter(student -> student.getUserId() != null)
                .findFirst()
                .ifPresent(userLinkedStudent -> compareStudent(
                        userLinkedStudent,
                        studentDAO.findByUserId(userLinkedStudent.getUserId()).orElseThrow(() ->
                                new AppException("DAO khong tim thay Student mau theo userId.")),
                        "findByUserId"
                ));

        assertEquals(
                extractIds(filterByFaculty(students, sampleStudent.getFaculty().getId())),
                extractIds(studentDAO.findByFacultyId(sampleStudent.getFaculty().getId())),
                "Lech ket qua loc Student theo Faculty trong StudentDAO."
        );
        assertEquals(
                extractIds(filterByClassRoom(students, sampleStudent.getClassRoom().getId())),
                extractIds(studentDAO.findByClassRoomId(sampleStudent.getClassRoom().getId())),
                "Lech ket qua loc Student theo ClassRoom trong StudentDAO."
        );
        assertEquals(
                extractIds(filterByAcademicYear(students, sampleStudent.getAcademicYear())),
                extractIds(studentDAO.findByAcademicYear(sampleStudent.getAcademicYear())),
                "Lech ket qua loc Student theo nien khoa trong StudentDAO."
        );
        assertEquals(
                normalizeAcademicYears(students),
                studentDAO.findAcademicYears(),
                "Lech danh sach nien khoa trong StudentDAO."
        );

        String searchKeyword = deriveSearchKeyword(sampleStudent);
        assertEquals(
                extractIds(studentDAO.searchByKeyword(searchKeyword)),
                extractIds(studentDAO.searchByCriteria(searchKeyword, null, null, null)),
                "Lech ket qua tim kiem Student trong StudentDAO."
        );
        assertEquals(
                extractIds(filterByFaculty(studentDAO.searchByKeyword(searchKeyword), sampleStudent.getFaculty().getId())),
                extractIds(studentDAO.searchByCriteria(
                        searchKeyword,
                        sampleStudent.getFaculty().getId(),
                        null,
                        null
                )),
                "Lech ket qua tim kiem Student theo keyword + Faculty trong StudentDAO."
        );

        System.out.println("COMPARE_OK studentCount=" + students.size() + ", keyword=" + searchKeyword);
    }

    private void verifyControllerAndServiceFlow() {
        User originalUser = SessionManager.getCurrentUser();

        try {
            User admin = findActiveUserByRole(Role.ADMIN)
                    .orElseThrow(() -> new AppException("Khong tim thay tai khoan ADMIN de verify Student controller/service."));

            SessionManager.setCurrentUser(admin);
            List<Student> jpaStudentsViaController = studentController.getAllStudents();
            assertEquals(
                    extractIds(studentDAO.findAll()),
                    extractIds(jpaStudentsViaController),
                    "Controller/service tra ve danh sach Student khong dung."
            );

            Student updateCandidate = jpaStudentsViaController.stream()
                    .filter(student -> student.getUserId() != null)
                    .findFirst()
                    .orElseThrow(() -> new AppException("Khong tim thay Student co userId de smoke test save/update JPA."));

            compareStudent(
                    studentDAO.findById(updateCandidate.getId()).orElseThrow(() ->
                            new AppException("DAO khong tim thay Student updateCandidate.")),
                    studentController.getStudentById(updateCandidate.getId()),
                    "controller.getStudentById"
            );

            assertEquals(
                    extractIds(studentDAO.findByFacultyId(updateCandidate.getFaculty().getId())),
                    extractIds(studentController.getStudentsByFaculty(updateCandidate.getFaculty().getId())),
                    "Controller/service loc Student theo Faculty khong dung."
            );
            assertEquals(
                    extractIds(studentDAO.findByClassRoomId(updateCandidate.getClassRoom().getId())),
                    extractIds(studentController.getStudentsByClassRoom(updateCandidate.getClassRoom().getId())),
                    "Controller/service loc Student theo ClassRoom khong dung."
            );
            assertEquals(
                    extractIds(studentDAO.findByAcademicYear(updateCandidate.getAcademicYear())),
                    extractIds(studentController.getStudentsByAcademicYear(updateCandidate.getAcademicYear())),
                    "Controller/service loc Student theo nien khoa khong dung."
            );

            String searchKeyword = deriveSearchKeyword(updateCandidate);
            assertEquals(
                    extractIds(studentDAO.searchByKeyword(searchKeyword)),
                    extractIds(studentController.searchStudents(searchKeyword, null, null, null)),
                    "Controller/service tim kiem Student theo keyword khong dung."
            );
            assertEquals(
                    extractIds(studentDAO.searchByCriteria(
                            searchKeyword,
                            updateCandidate.getFaculty().getId(),
                            null,
                            null
                    )),
                    extractIds(studentController.searchStudents(
                            searchKeyword,
                            updateCandidate.getFaculty().getId(),
                            null,
                            null
                    )),
                    "Controller/service tim kiem Student theo keyword + Faculty khong dung."
            );

            Student savedStudent = studentController.saveStudent(copyStudent(updateCandidate));
            compareStudent(updateCandidate, savedStudent, "controller.saveStudent(update)");

            Optional<User> studentUser = userDAO.findById(updateCandidate.getUserId());
            if (studentUser.isPresent() && updateCandidate.getAddress() != null) {
                SessionManager.setCurrentUser(studentUser.get());
                Student currentStudent = studentController.getCurrentStudent();
                assertEquals(updateCandidate.getId(), currentStudent.getId(), "controller.getCurrentStudent tra ve sai Student.");

                Student updatedStudent = studentController.updateCurrentStudentContactInfo(
                        currentStudent.getEmail(),
                        currentStudent.getPhone(),
                        currentStudent.getAddress()
                );
                compareStudent(currentStudent, updatedStudent, "controller.updateCurrentStudentContactInfo");
            }

            SessionManager.setCurrentUser(admin);
            System.out.println("SERVICE_OK jpaCount=" + jpaStudentsViaController.size());
        } finally {
            SessionManager.setCurrentUser(originalUser);
        }
    }

    private void verifyJpaCrudWithRollback() {
        Student template = studentDAO.findAll().stream()
                .filter(student -> student.getFaculty() != null && student.getClassRoom() != null)
                .findFirst()
                .orElseThrow(() -> new AppException("Khong tim thay Student mau de smoke test CRUD JPA."));

        String suffix = CODE_SUFFIX_FORMAT.format(LocalDateTime.now());
        String temporaryStudentCode = "JPASTU" + suffix;
        String temporaryUsername = temporaryStudentCode.toLowerCase();
        String temporaryEmail = "jpa.student." + suffix + "@example.com";

        EntityTransaction transaction = null;
        try (EntityManager entityManager = JpaBootstrap.createEntityManager()) {
            transaction = entityManager.getTransaction();
            transaction.begin();

            Student temporaryStudent = new Student();
            temporaryStudent.setStudentCode(temporaryStudentCode);
            temporaryStudent.setFullName("JPA Smoke Student " + suffix);
            temporaryStudent.setGender("Nam");
            temporaryStudent.setDateOfBirth(LocalDate.of(2004, 1, 1));
            temporaryStudent.setEmail(temporaryEmail);
            temporaryStudent.setPhone("0912345678");
            temporaryStudent.setAddress("Smoke test rollback");
            temporaryStudent.setAcademicYear(template.getAcademicYear());
            temporaryStudent.setStatus(template.getStatus());
            temporaryStudent.setFaculty(entityManager.getReference(Faculty.class, template.getFaculty().getId()));
            temporaryStudent.setClassRoom(entityManager.getReference(ClassRoom.class, template.getClassRoom().getId()));

            entityManager.persist(temporaryStudent);
            entityManager.flush();
            assertCondition(temporaryStudent.getId() != null, "Persist Student bang JPA khong sinh id.");

            temporaryStudent.setPhone("0987654321");
            temporaryStudent.setAddress("Smoke test rollback updated");
            entityManager.flush();

            entityManager.clear();
            Student updatedStudent = entityManager.find(Student.class, temporaryStudent.getId());
            assertCondition(updatedStudent != null, "Khong tim thay Student vua persist trong transaction JPA.");
            assertEquals("0987654321", updatedStudent.getPhone(), "Cap nhat Student bang JPA trong transaction khong dung.");
            assertEquals("Smoke test rollback updated", updatedStudent.getAddress(),
                    "Cap nhat dia chi Student bang JPA trong transaction khong dung.");

            entityManager.remove(updatedStudent);
            entityManager.flush();
            entityManager.clear();

            Student deletedStudent = entityManager.find(Student.class, temporaryStudent.getId());
            assertCondition(deletedStudent == null, "Xoa Student bang JPA trong transaction khong dung.");

            transaction.rollback();
        } catch (Exception exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new AppException("Smoke test CRUD rollback cho Student JPA that bai.", exception);
        }

        assertCondition(studentDAO.findByStudentCode(temporaryStudentCode).isEmpty(),
                "Rollback CRUD JPA van con de lai Student tam.");
        assertCondition(userDAO.findByUsername(temporaryUsername).isEmpty(),
                "Rollback CRUD JPA van con de lai User tam do trigger tao.");

        System.out.println("CRUD_ROLLBACK_OK tempStudentCode=" + temporaryStudentCode);
    }

    private List<Long> extractIds(List<Student> students) {
        return students.stream()
                .map(Student::getId)
                .toList();
    }

    private List<Student> filterByFaculty(List<Student> students, Long facultyId) {
        return students.stream()
                .filter(student -> student.getFaculty() != null
                        && Objects.equals(student.getFaculty().getId(), facultyId))
                .toList();
    }

    private List<Student> filterByClassRoom(List<Student> students, Long classRoomId) {
        return students.stream()
                .filter(student -> student.getClassRoom() != null
                        && Objects.equals(student.getClassRoom().getId(), classRoomId))
                .toList();
    }

    private List<Student> filterByAcademicYear(List<Student> students, String academicYear) {
        String normalizedAcademicYear = normalize(academicYear);
        return students.stream()
                .filter(student -> normalize(student.getAcademicYear()).equalsIgnoreCase(normalizedAcademicYear))
                .toList();
    }

    private List<String> normalizeAcademicYears(List<Student> students) {
        return students.stream()
                .map(Student::getAcademicYear)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .toList();
    }

    private Optional<User> findActiveUserByRole(Role role) {
        return userDAO.findAll().stream()
                .filter(User::isActive)
                .filter(user -> user.getRole() == role)
                .findFirst();
    }

    private Student copyStudent(Student source) {
        return new Student(
                source.getId(),
                source.getUserId(),
                source.getStudentCode(),
                source.getFullName(),
                source.getGender(),
                source.getDateOfBirth(),
                source.getEmail(),
                source.getPhone(),
                source.getAddress(),
                source.getFaculty(),
                source.getClassRoom(),
                source.getAcademicYear(),
                source.getStatus()
        );
    }

    private void compareStudent(Student expected, Student actual, String context) {
        assertEquals(expected.getId(), actual.getId(), context + " sai id.");
        assertEquals(normalize(expected.getUserId()), normalize(actual.getUserId()), context + " sai userId.");
        assertEquals(normalize(expected.getStudentCode()), normalize(actual.getStudentCode()), context + " sai studentCode.");
        assertEquals(normalize(expected.getFullName()), normalize(actual.getFullName()), context + " sai fullName.");
        assertEquals(normalize(expected.getGender()), normalize(actual.getGender()), context + " sai gender.");
        assertEquals(expected.getDateOfBirth(), actual.getDateOfBirth(), context + " sai dateOfBirth.");
        assertEquals(normalize(expected.getEmail()), normalize(actual.getEmail()), context + " sai email.");
        assertEquals(normalize(expected.getPhone()), normalize(actual.getPhone()), context + " sai phone.");
        assertEquals(normalize(expected.getAddress()), normalize(actual.getAddress()), context + " sai address.");
        assertEquals(normalize(expected.getAcademicYear()), normalize(actual.getAcademicYear()), context + " sai academicYear.");
        assertEquals(normalize(expected.getStatus()), normalize(actual.getStatus()), context + " sai status.");
        assertEquals(
                expected.getFaculty() == null ? null : expected.getFaculty().getId(),
                actual.getFaculty() == null ? null : actual.getFaculty().getId(),
                context + " sai faculty."
        );
        assertEquals(
                expected.getClassRoom() == null ? null : expected.getClassRoom().getId(),
                actual.getClassRoom() == null ? null : actual.getClassRoom().getId(),
                context + " sai classRoom."
        );
    }

    private String deriveSearchKeyword(Student sampleStudent) {
        String studentCode = normalize(sampleStudent.getStudentCode());
        if (!studentCode.isBlank()) {
            return studentCode.length() > 4 ? studentCode.substring(0, 4) : studentCode;
        }
        String fullName = normalize(sampleStudent.getFullName());
        if (!fullName.isBlank()) {
            return fullName.length() > 4 ? fullName.substring(0, 4) : fullName;
        }
        return normalize(sampleStudent.getEmail());
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private Long normalize(Long value) {
        return value;
    }

    private void assertCondition(boolean condition, String message) {
        if (!condition) {
            throw new AppException(message);
        }
    }

    private void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AppException(message + " Expected=" + expected + ", actual=" + actual);
        }
    }
}
