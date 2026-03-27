package com.qlsv.dao;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.exception.AppException;
import com.qlsv.model.ClassRoom;
import com.qlsv.model.Faculty;
import com.qlsv.model.Student;
import com.qlsv.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

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

    public List<Student> findAll() {
        return executeRead("Khong the tai danh sach sinh vien bang JPA.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " ORDER BY s.id", Student.class)
                        .getResultList());
    }

    public Optional<Student> findById(Long id) {
        return executeRead("Khong the tim sinh vien theo ma dinh danh bang JPA.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " WHERE s.id = :id", Student.class)
                        .setParameter("id", id)
                        .getResultStream()
                        .findFirst());
    }

    public Optional<Student> findByStudentCode(String studentCode) {
        return executeRead("Khong the tim sinh vien theo ma sinh vien bang JPA.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " WHERE LOWER(s.studentCode) = LOWER(:studentCode)", Student.class)
                        .setParameter("studentCode", normalize(studentCode))
                        .getResultStream()
                        .findFirst());
    }

    public Optional<Student> findByUserId(Long userId) {
        return executeRead("Khong the tim sinh vien theo tai khoan nguoi dung bang JPA.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " WHERE s.user.id = :userId", Student.class)
                        .setParameter("userId", userId)
                        .getResultStream()
                        .findFirst());
    }

    public List<Student> findByFacultyId(Long facultyId) {
        return executeRead("Khong the loc sinh vien theo khoa bang JPA.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " WHERE s.faculty.id = :facultyId ORDER BY s.id", Student.class)
                        .setParameter("facultyId", facultyId)
                        .getResultList());
    }

    public List<Student> findByClassRoomId(Long classRoomId) {
        return executeRead("Khong the loc sinh vien theo lop bang JPA.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " WHERE s.classRoom.id = :classRoomId ORDER BY s.id", Student.class)
                        .setParameter("classRoomId", classRoomId)
                        .getResultList());
    }

    public List<Student> findByAcademicYear(String academicYear) {
        return executeRead("Khong the loc sinh vien theo nien khoa bang JPA.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " WHERE LOWER(s.academicYear) = LOWER(:academicYear) ORDER BY s.id", Student.class)
                        .setParameter("academicYear", normalize(academicYear))
                        .getResultList());
    }

    public List<String> findAcademicYears() {
        return executeRead("Khong the tai danh sach nien khoa sinh vien bang JPA.", entityManager ->
                entityManager.createQuery("""
                                SELECT DISTINCT s.academicYear
                                FROM Student s
                                WHERE s.academicYear IS NOT NULL AND TRIM(s.academicYear) <> ''
                                ORDER BY s.academicYear
                                """, String.class)
                        .getResultList());
    }

    public List<Student> searchByKeyword(String keyword) {
        return searchByCriteria(keyword, null, null, null);
    }

    public List<Student> searchByCriteria(String keyword, Long facultyId, Long classRoomId, String academicYear) {
        return executeRead("Khong the tim kiem sinh vien bang JPA.", entityManager ->
                buildSearchQuery(entityManager, keyword, facultyId, classRoomId, academicYear).getResultList());
    }

    public Student insert(Student student) {
        Long studentId = executeWrite("Khong the them sinh vien bang JPA.", entityManager -> {
            Student entity = new Student();
            copyState(entityManager, student, entity);
            entityManager.persist(entity);
            entityManager.flush();
            student.setId(entity.getId());
            student.setUserId(entity.getUserId());
            return entity.getId();
        });
        return findById(studentId)
                .orElseThrow(() -> new AppException("Khong the tai lai sinh vien sau khi them bang JPA."));
    }

    public boolean update(Student student) {
        executeWrite("Khong the cap nhat sinh vien bang JPA.", entityManager -> {
            Student entity = entityManager.find(Student.class, student.getId());
            if (entity == null) {
                throw new AppException("Khong tim thay sinh vien de cap nhat bang JPA.");
            }
            copyState(entityManager, student, entity);
            entityManager.flush();
            return entity.getId();
        });
        return true;
    }

    public boolean updateContactInfo(Long studentId, String email, String phone, String address) {
        return executeWrite("Khong the cap nhat thong tin lien he sinh vien bang JPA.", entityManager -> {
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

    public boolean delete(Long id) {
        return executeWrite("Khong the xoa sinh vien bang JPA.", entityManager -> {
            Student entity = entityManager.find(Student.class, id);
            if (entity == null) {
                return false;
            }
            entityManager.remove(entity);
            entityManager.flush();
            return true;
        });
    }

    public Student requireById(Long id) {
        return findById(id)
                .orElseThrow(() -> new AppException("Khong tim thay sinh vien theo ma dinh danh."));
    }

    private <T> T executeRead(String errorMessage, Function<EntityManager, T> action) {
        try {
            return JpaBootstrap.executeWithEntityManager(action);
        } catch (RuntimeException exception) {
            throw new AppException(errorMessage, exception);
        }
    }

    private <T> T executeWrite(String errorMessage, Function<EntityManager, T> action) {
        try {
            return JpaBootstrap.executeInCurrentTransaction(action);
        } catch (RuntimeException exception) {
            throw new AppException(errorMessage, exception);
        }
    }

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

    private Faculty resolveFacultyReference(EntityManager entityManager, Faculty faculty) {
        if (faculty == null || faculty.getId() == null) {
            return null;
        }
        return entityManager.getReference(Faculty.class, faculty.getId());
    }

    private ClassRoom resolveClassRoomReference(EntityManager entityManager, ClassRoom classRoom) {
        if (classRoom == null || classRoom.getId() == null) {
            return null;
        }
        return entityManager.getReference(ClassRoom.class, classRoom.getId());
    }

    private User resolveUserReference(EntityManager entityManager, Long userId) {
        if (userId == null) {
            return null;
        }
        return entityManager.getReference(User.class, userId);
    }

    private TypedQuery<Student> buildSearchQuery(EntityManager entityManager,
                                                 String keyword,
                                                 Long facultyId,
                                                 Long classRoomId,
                                                 String academicYear) {
        StringBuilder jpql = new StringBuilder(FETCH_BASE).append(" WHERE 1 = 1");
        String normalizedKeyword = normalize(keyword).toLowerCase();
        String normalizedAcademicYear = normalize(academicYear).toLowerCase();

        if (facultyId != null) {
            jpql.append(" AND s.faculty.id = :facultyId");
        }
        if (classRoomId != null) {
            jpql.append(" AND s.classRoom.id = :classRoomId");
        }
        if (!normalizedAcademicYear.isBlank()) {
            jpql.append(" AND LOWER(s.academicYear) = :academicYear");
        }
        if (!normalizedKeyword.isBlank()) {
            jpql.append("""
                     AND (
                            LOWER(s.studentCode) LIKE :keyword
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
        if (!normalizedAcademicYear.isBlank()) {
            query.setParameter("academicYear", normalizedAcademicYear);
        }
        if (!normalizedKeyword.isBlank()) {
            query.setParameter("keyword", "%" + normalizedKeyword + "%");
        }
        return query;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
