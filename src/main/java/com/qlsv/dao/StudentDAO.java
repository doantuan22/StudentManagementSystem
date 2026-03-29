/**
 * Truy vấn dữ liệu sinh viên bằng JPA.
 */
package com.qlsv.dao;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.exception.AppException;
import com.qlsv.model.ClassRoom;
import com.qlsv.model.Faculty;
import com.qlsv.model.Student;
import com.qlsv.model.User;
import com.qlsv.utils.AcademicFormatUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Service-facing DAO boundary for Student persistence.
 * This class owns all JPA access for the Student module.
 */
public class StudentDAO {

    private static final String FETCH_BASE = """
            SELECT s
            FROM Student s
            LEFT JOIN FETCH s.user
            JOIN FETCH s.faculty
            JOIN FETCH s.classRoom
            """;

    /**
     * Lấy danh sách tất cả sinh viên trong hệ thống.
     */
    public List<Student> findAll() {
        return executeRead("Không thể tải danh sách sinh viên bằng JPA.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " ORDER BY s.id", Student.class)
                        .getResultList());
    }

    /**
     * Tìm kiếm sinh viên theo mã định danh.
     */
    public Optional<Student> findById(Long id) {
        return executeRead("Không thể tìm sinh viên theo mã định danh bằng JPA.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " WHERE s.id = :id", Student.class)
                        .setParameter("id", id)
                        .getResultStream()
                        .findFirst());
    }

    /**
     * Tìm kiếm sinh viên theo mã số sinh viên (ví dụ: SV001).
     */
    public Optional<Student> findByStudentCode(String studentCode) {
        return executeRead("Không thể tìm sinh viên theo mã sinh viên bằng JPA.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " WHERE LOWER(s.studentCode) = LOWER(:studentCode)", Student.class)
                        .setParameter("studentCode", normalize(studentCode))
                        .getResultStream()
                        .findFirst());
    }

    /**
     * Tìm sinh viên dựa trên mã tài khoản người dùng liên kết.
     */
    public Optional<Student> findByUserId(Long userId) {
        return executeRead("Không thể tìm sinh viên theo tài khoản người dùng bằng JPA.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " WHERE s.user.id = :userId", Student.class)
                        .setParameter("userId", userId)
                        .getResultStream()
                        .findFirst());
    }

    /**
     * Lọc danh sách sinh viên theo khoa.
     */
    public List<Student> findByFacultyId(Long facultyId) {
        return executeRead("Không thể lọc sinh viên theo khoa bằng JPA.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " WHERE s.faculty.id = :facultyId ORDER BY s.id", Student.class)
                        .setParameter("facultyId", facultyId)
                        .getResultList());
    }

    /**
     * Lọc danh sách sinh viên theo lớp học hành chính.
     */
    public List<Student> findByClassRoomId(Long classRoomId) {
        return executeRead("Không thể lọc sinh viên theo lớp bằng JPA.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " WHERE s.classRoom.id = :classRoomId ORDER BY s.id", Student.class)
                        .setParameter("classRoomId", classRoomId)
                        .getResultList());
    }

    /**
     * Lọc danh sách sinh viên theo niên khóa.
     */
    public List<Student> findByAcademicYear(String academicYear) {
        String normalizedAcademicYear = AcademicFormatUtil.normalizeAcademicYear(academicYear, "Niên khóa");
        return executeRead("Không thể lọc sinh viên theo niên khóa bằng JPA.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " ORDER BY s.id", Student.class)
                        .getResultList()
                        .stream()
                        .filter(student -> AcademicFormatUtil.academicYearsEqual(student.getAcademicYear(), normalizedAcademicYear))
                        .toList());
    }

    /**
     * Lấy danh sách tất cả các niên khóa hiện có của sinh viên.
     */
    public List<String> findAcademicYears() {
        return executeRead("Không thể tải danh sách niên khóa sinh viên bằng JPA.", entityManager -> {
            List<String> rawAcademicYears = entityManager.createQuery("""
                            SELECT DISTINCT s.academicYear
                            FROM Student s
                            /**
                             * Xử lý trim.
                             */
                            WHERE s.academicYear IS NOT NULL AND TRIM(s.academicYear) <> ''
                            ORDER BY s.academicYear
                            """, String.class)
                    .getResultList();

            List<String> normalizedAcademicYears = new ArrayList<>();
            for (String rawAcademicYear : rawAcademicYears) {
                String displayValue = AcademicFormatUtil.formatAcademicYear(rawAcademicYear);
                if (!displayValue.isBlank() && normalizedAcademicYears.stream().noneMatch(existing -> existing.equalsIgnoreCase(displayValue))) {
                    normalizedAcademicYears.add(displayValue);
                }
            }
            return normalizedAcademicYears;
        });
    }

    /**
     * Tìm kiếm theo keyword.
     */
    public List<Student> searchByKeyword(String keyword) {
        return searchByCriteria(keyword, null, null, null);
    }

    /**
     * Tìm kiếm sinh viên theo từ khóa và các tiêu chí lọc (khoa, lớp, niên khóa).
     */
    public List<Student> searchByCriteria(String keyword, Long facultyId, Long classRoomId, String academicYear) {
        String normalizedAcademicYear = academicYear == null || academicYear.isBlank()
                ? ""
                : AcademicFormatUtil.normalizeAcademicYear(academicYear, "Niên khóa");
        return executeRead("Không thể tìm kiếm sinh viên bằng JPA.", entityManager -> {
            List<Student> students = buildSearchQuery(entityManager, keyword, facultyId, classRoomId).getResultList();
            if (normalizedAcademicYear.isBlank()) {
                return students;
            }
            return students.stream()
                    .filter(student -> AcademicFormatUtil.academicYearsEqual(student.getAcademicYear(), normalizedAcademicYear))
                    .toList();
        });
    }

    /**
     * Thêm mới một sinh viên vào hệ thống.
     */
    public Student insert(Student student) {
        Long studentId = executeWrite("Không thể thêm sinh viên bằng JPA.", entityManager -> {
            Student entity = new Student();
            copyState(entityManager, student, entity);
            entityManager.persist(entity);
            entityManager.flush();
            student.setId(entity.getId());
            student.setUserId(entity.getUserId());
            return entity.getId();
        });
        return findById(studentId)
                .orElseThrow(() -> new AppException("Không thể tải lại sinh viên sau khi thêm bằng JPA."));
    }

    /**
     * Cập nhật thông tin sinh viên hiện có.
     */
    public boolean update(Student student) {
        executeWrite("Không thể cập nhật sinh viên bằng JPA.", entityManager -> {
            Student entity = entityManager.find(Student.class, student.getId());
            if (entity == null) {
                throw new AppException("Không tìm thấy sinh viên để cập nhật bằng JPA.");
            }
            copyState(entityManager, student, entity);
            entityManager.flush();
            return entity.getId();
        });
        return true;
    }

    /**
     * Cập nhật thông tin liên hệ (email, số điện thoại, địa chỉ) của sinh viên.
     */
    public boolean updateContactInfo(Long studentId, String email, String phone, String address) {
        return executeWrite("Không thể cập nhật thông tin liên hệ sinh viên bằng JPA.", entityManager -> {
            Student student = entityManager.find(Student.class, studentId);
            if (student == null) {
                return false;
            }
            student.setEmail(email);
            student.setPhone(phone);
            student.setAddress(address);
            entityManager.flush();
            return true;
        });
    }

    /**
     * Xóa sinh viên khỏi hệ thống theo mã định danh.
     */
    public boolean delete(Long id) {
        return executeWrite("Không thể xóa sinh viên bằng JPA.", entityManager -> {
            Student entity = entityManager.find(Student.class, id);
            if (entity == null) {
                return false;
            }
            entityManager.remove(entity);
            entityManager.flush();
            return true;
        });
    }

    /**
     * Bắt buộc theo id.
     */
    public Student requireById(Long id) {
        return findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy sinh viên theo mã định danh."));
    }

    /**
     * Thực thi read.
     */
    private <T> T executeRead(String errorMessage, Function<EntityManager, T> action) {
        try {
            return JpaBootstrap.executeWithEntityManager(action);
        } catch (RuntimeException exception) {
            throw new AppException(errorMessage, exception);
        }
    }

    /**
     * Thực thi write.
     */
    private <T> T executeWrite(String errorMessage, Function<EntityManager, T> action) {
        try {
            return JpaBootstrap.executeInCurrentTransaction(action);
        } catch (RuntimeException exception) {
            throw new AppException(errorMessage, exception);
        }
    }

    /**
     * Sao chép state.
     */
    private void copyState(EntityManager entityManager, Student source, Student target) {
        target.setUser(resolveUserReference(entityManager, source.getUserId()));
        target.setStudentCode(source.getStudentCode());
        target.setFullName(source.getFullName());
        target.setGender(source.getGender());
        target.setDateOfBirth(source.getDateOfBirth());
        target.setEmail(source.getEmail());
        target.setPhone(source.getPhone());
        target.setAddress(source.getAddress());
        target.setAcademicYear(source.getAcademicYear());
        target.setStatus(source.getStatus());
        target.setFaculty(resolveFacultyReference(entityManager, source.getFaculty()));
        target.setClassRoom(resolveClassRoomReference(entityManager, source.getClassRoom()));
    }

    /**
     * Xác định khoa reference.
     */
    private Faculty resolveFacultyReference(EntityManager entityManager, Faculty faculty) {
        if (faculty == null || faculty.getId() == null) {
            return null;
        }
        return entityManager.getReference(Faculty.class, faculty.getId());
    }

    /**
     * Xác định lớp reference.
     */
    private ClassRoom resolveClassRoomReference(EntityManager entityManager, ClassRoom classRoom) {
        if (classRoom == null || classRoom.getId() == null) {
            return null;
        }
        return entityManager.getReference(ClassRoom.class, classRoom.getId());
    }

    /**
     * Xác định người dùng reference.
     */
    private User resolveUserReference(EntityManager entityManager, Long userId) {
        if (userId == null) {
            return null;
        }
        return entityManager.getReference(User.class, userId);
    }

    /**
     * Tạo tìm kiếm query.
     */
    private TypedQuery<Student> buildSearchQuery(EntityManager entityManager,
                                                 String keyword,
                                                 Long facultyId,
                                                 Long classRoomId) {
        StringBuilder jpql = new StringBuilder(FETCH_BASE).append(" WHERE 1 = 1");
        String normalizedKeyword = normalize(keyword).toLowerCase();

        if (facultyId != null) {
            jpql.append(" AND s.faculty.id = :facultyId");
        }
        if (classRoomId != null) {
            jpql.append(" AND s.classRoom.id = :classRoomId");
        }
        if (!normalizedKeyword.isBlank()) {
            jpql.append("""
                     AND (
                            LOWER(s.studentCode) LIKE :keyword
                         /**
                          * Xử lý lower.
                          */
                         OR LOWER(s.fullName) LIKE :keyword
                         OR LOWER(s.email) LIKE :keyword
                     )
                    """);
        }

        jpql.append(" ORDER BY s.id");

        TypedQuery<Student> query = entityManager.createQuery(jpql.toString(), Student.class);
        if (facultyId != null) {
            query.setParameter("facultyId", facultyId);
        }
        if (classRoomId != null) {
            query.setParameter("classRoomId", classRoomId);
        }
        if (!normalizedKeyword.isBlank()) {
            query.setParameter("keyword", "%" + normalizedKeyword + "%");
        }
        return query;
    }

    /**
     * Chuẩn hóa dữ liệu hiện tại.
     */
    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
