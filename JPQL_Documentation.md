# TỔNG HỢP TẤT CẢ JPQL TRONG HỆ THỐNG QUẢN LÝ SINH VIÊN

> Tài liệu này tổng hợp tất cả các câu truy vấn JPQL được sử dụng trong các file DAO, bao gồm chức năng và luồng hoạt động chi tiết.

---

## MỤC LỤC

1. [ClassRoomDAO](#1-classroomdao)
2. [CourseSectionDAO](#2-coursesectiondao)
3. [EnrollmentDAO](#3-enrollmentdao)
4. [FacultyDAO](#4-facultydao)
5. [LecturerDAO](#5-lecturerdao)
6. [LecturerSubjectDAO](#6-lecturersubjectdao)
7. [ReportDAO](#7-reportdao)
8. [RoomDAO](#8-roomdao)
9. [ScheduleDAO](#9-scheduledao)
10. [ScoreDAO](#10-scoredao)
11. [StudentDAO](#11-studentdao)
12. [SubjectDAO](#12-subjectdao)
13. [UserDAO](#13-userdao)

---

## 1. ClassRoomDAO

### 1.1. findAll()
**Chức năng:** Lấy danh sách tất cả các lớp học kèm thông tin khoa

**JPQL:**
```jpql
SELECT c FROM ClassRoom c 
JOIN FETCH c.faculty 
ORDER BY c.id
```

**Luồng hoạt động:**
1. SELECT tất cả ClassRoom
2. JOIN FETCH để eager load Faculty (tránh lazy loading exception)
3. Sắp xếp theo ID tăng dần
4. Trả về List<ClassRoom>

---

### 1.2. findById()
**Chức năng:** Tìm lớp học theo mã định danh

**JPQL:**
```jpql
SELECT c FROM ClassRoom c 
JOIN FETCH c.faculty 
WHERE c.id = :id
```

**Luồng hoạt động:**
1. SELECT ClassRoom với ID cụ thể
2. JOIN FETCH Faculty
3. Lọc theo tham số :id
4. Trả về Optional<ClassRoom>

---

### 1.3. findByFacultyId()
**Chức năng:** Lấy danh sách lớp học thuộc một khoa cụ thể

**JPQL:**
```jpql
SELECT c FROM ClassRoom c 
JOIN FETCH c.faculty 
WHERE c.faculty.id = :facultyId 
ORDER BY c.id
```

**Luồng hoạt động:**
1. SELECT ClassRoom
2. JOIN FETCH Faculty
3. Lọc theo facultyId
4. Sắp xếp theo ID
5. Trả về List<ClassRoom>

---

### 1.4. searchByKeyword()
**Chức năng:** Tìm kiếm lớp học theo từ khóa (mã lớp, tên lớp, niên khóa)

**JPQL:**
```jpql
SELECT c FROM ClassRoom c 
JOIN FETCH c.faculty
WHERE LOWER(c.classCode) LIKE :keyword
   OR LOWER(c.className) LIKE :keyword
   OR LOWER(c.academicYear) LIKE :keyword
ORDER BY c.id
```

**Luồng hoạt động:**
1. SELECT ClassRoom
2. JOIN FETCH Faculty
3. Chuyển keyword về lowercase và thêm % ở đầu/cuối
4. Tìm kiếm không phân biệt hoa thường trên 3 trường
5. Sắp xếp theo ID
6. Trả về List<ClassRoom>

---

## 2. CourseSectionDAO

### 2.1. findAll()
**Chức năng:** Lấy tất cả học phần kèm đầy đủ thông tin môn học, khoa, giảng viên

**JPQL:**
```jpql
SELECT DISTINCT cs FROM CourseSection cs
JOIN FETCH cs.subject s
JOIN FETCH s.faculty
JOIN FETCH cs.lecturer l
ORDER BY cs.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT CourseSection (loại bỏ trùng lặp do multiple JOIN FETCH)
2. JOIN FETCH subject, faculty của subject, lecturer
3. Sắp xếp theo ID
4. Gọi hydrateScheduleCompatibility() để load thông tin lịch học và phòng
5. Trả về List<CourseSection>

---

### 2.2. findById()
**Chức năng:** Tìm học phần theo mã định danh

**JPQL:**
```jpql
SELECT DISTINCT cs FROM CourseSection cs
JOIN FETCH cs.subject s
JOIN FETCH s.faculty
JOIN FETCH cs.lecturer l
WHERE cs.id = :id
```

**Luồng hoạt động:**
1. SELECT DISTINCT CourseSection
2. JOIN FETCH subject, faculty, lecturer
3. Lọc theo :id
4. Gọi hydrateScheduleCompatibility()
5. Trả về Optional<CourseSection>

---

### 2.3. findByLecturerId()
**Chức năng:** Lấy danh sách học phần do một giảng viên phụ trách

**JPQL:**
```jpql
SELECT DISTINCT cs FROM CourseSection cs
JOIN FETCH cs.subject s
JOIN FETCH s.faculty
JOIN FETCH cs.lecturer l
WHERE cs.lecturer.id = :lecturerId 
ORDER BY cs.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT CourseSection
2. JOIN FETCH subject, faculty, lecturer
3. Lọc theo lecturerId
4. Sắp xếp theo ID
5. Gọi hydrateScheduleCompatibility()
6. Trả về List<CourseSection>

---

### 2.4. findByFacultyId()
**Chức năng:** Lấy danh sách học phần thuộc một khoa

**JPQL:**
```jpql
SELECT DISTINCT cs FROM CourseSection cs
JOIN FETCH cs.subject s
JOIN FETCH s.faculty
JOIN FETCH cs.lecturer l
WHERE s.faculty.id = :facultyId 
ORDER BY cs.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT CourseSection
2. JOIN FETCH subject, faculty, lecturer
3. Lọc theo facultyId của môn học
4. Sắp xếp theo ID
5. Gọi hydrateScheduleCompatibility()
6. Trả về List<CourseSection>

---

### 2.5. findBySectionCode()
**Chức năng:** Tìm học phần theo mã học phần

**JPQL:**
```jpql
SELECT DISTINCT cs FROM CourseSection cs
JOIN FETCH cs.subject s
JOIN FETCH s.faculty
JOIN FETCH cs.lecturer l
WHERE LOWER(cs.sectionCode) = LOWER(:sectionCode) 
ORDER BY cs.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT CourseSection
2. JOIN FETCH subject, faculty, lecturer
3. So sánh sectionCode không phân biệt hoa thường
4. Sắp xếp theo ID
5. Gọi hydrateScheduleCompatibility()
6. Trả về List<CourseSection>

---

### 2.6. findByRoomId()
**Chức năng:** Lấy danh sách học phần được xếp lịch tại một phòng học

**JPQL:**
```jpql
SELECT DISTINCT cs FROM CourseSection cs
JOIN FETCH cs.subject s
JOIN FETCH s.faculty
JOIN FETCH cs.lecturer l
WHERE EXISTS (
    SELECT 1 FROM Schedule sc
    WHERE sc.courseSection = cs 
      AND sc.room.id = :roomId
)
ORDER BY cs.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT CourseSection
2. JOIN FETCH subject, faculty, lecturer
3. Sử dụng EXISTS để kiểm tra có Schedule với roomId
4. Sắp xếp theo ID
5. Gọi hydrateScheduleCompatibility()
6. Trả về List<CourseSection>

---

### 2.7. searchByKeyword()
**Chức năng:** Tìm kiếm học phần theo từ khóa (mã học phần, học kỳ, năm học, môn học, giảng viên, phòng)

**JPQL:**
```jpql
SELECT DISTINCT cs FROM CourseSection cs
JOIN FETCH cs.subject s
JOIN FETCH s.faculty
JOIN FETCH cs.lecturer l
WHERE LOWER(cs.sectionCode) LIKE :keyword
   OR LOWER(cs.semester) LIKE :keyword
   OR LOWER(cs.schoolYear) LIKE :keyword
   OR LOWER(s.subjectCode) LIKE :keyword
   OR LOWER(s.subjectName) LIKE :keyword
   OR LOWER(l.lecturerCode) LIKE :keyword
   OR LOWER(l.fullName) LIKE :keyword
   OR EXISTS (
        SELECT 1 FROM Schedule sc
        JOIN sc.room r
        WHERE sc.courseSection = cs
          AND (LOWER(r.roomCode) LIKE :keyword 
               OR LOWER(r.roomName) LIKE :keyword)
   )
ORDER BY cs.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT CourseSection
2. JOIN FETCH subject, faculty, lecturer
3. Tìm kiếm không phân biệt hoa thường trên nhiều trường
4. Sử dụng EXISTS để tìm trong Schedule và Room
5. Sắp xếp theo ID
6. Gọi hydrateScheduleCompatibility()
7. Trả về List<CourseSection>

---

### 2.8. countEnrollments()
**Chức năng:** Đếm số lượng sinh viên đã đăng ký vào học phần

**JPQL:**
```jpql
SELECT COUNT(e) FROM Enrollment e 
WHERE e.courseSection.id = :courseSectionId
```

**Luồng hoạt động:**
1. COUNT số lượng Enrollment
2. Lọc theo courseSectionId
3. Trả về int (số lượng)

---

### 2.9. hydrateScheduleCompatibility() - Query phụ
**Chức năng:** Load thông tin lịch học và phòng để tương thích với UI cũ

**JPQL:**
```jpql
SELECT sc.courseSection.id,
       sc.dayOfWeek,
       sc.startPeriod,
       sc.endPeriod,
       r.id,
       r.roomCode,
       r.roomName
FROM Schedule sc
JOIN sc.room r
WHERE sc.courseSection.id IN :courseSectionIds
ORDER BY sc.courseSection.id, sc.dayOfWeek, sc.startPeriod, sc.id
```

**Luồng hoạt động:**
1. SELECT các trường cần thiết từ Schedule và Room
2. JOIN room
3. Lọc theo danh sách courseSectionIds
4. Sắp xếp theo courseSection, thứ, tiết bắt đầu
5. Xây dựng chuỗi lịch học dạng "Thứ 2 tiết 1-3 phòng A101; Thứ 4 tiết 4-6 phòng B202"
6. Gán vào CourseSection.scheduleText và CourseSection.room (compatibility fields)

---

## 3. EnrollmentDAO

**Lưu ý:** EnrollmentDAO sử dụng FETCH_BASE với 8 bảng JOIN FETCH để load đầy đủ thông tin:
- Enrollment → Student → Faculty, ClassRoom → Faculty
- Enrollment → CourseSection → Subject → Faculty, Lecturer

### 3.1. findAll()
**Chức năng:** Lấy tất cả đăng ký học phần với đầy đủ thông tin liên quan

**JPQL:**
```jpql
SELECT DISTINCT e FROM Enrollment e
JOIN FETCH e.student student
JOIN FETCH student.faculty studentFaculty
JOIN FETCH student.classRoom classRoom
JOIN FETCH classRoom.faculty classFaculty
JOIN FETCH e.courseSection courseSection
JOIN FETCH courseSection.subject subject
JOIN FETCH subject.faculty subjectFaculty
JOIN FETCH courseSection.lecturer lecturer
ORDER BY e.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Enrollment
2. JOIN FETCH 8 bảng liên quan
3. Sắp xếp theo ID
4. Gọi hydrateCourseSectionCompatibility() để load lịch học
5. Trả về List<Enrollment>

---

### 3.2. findById()
**Chức năng:** Tìm đăng ký học phần theo mã định danh

**JPQL:**
```jpql
SELECT DISTINCT e FROM Enrollment e
[... FETCH_BASE ...]
WHERE e.id = :id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Enrollment với FETCH_BASE
2. Lọc theo :id
3. Gọi hydrateCourseSectionCompatibility()
4. Trả về Optional<Enrollment>

---

### 3.3. findByStudentId()
**Chức năng:** Lấy danh sách học phần đã đăng ký của một sinh viên

**JPQL:**
```jpql
SELECT DISTINCT e FROM Enrollment e
[... FETCH_BASE ...]
WHERE student.id = :studentId 
ORDER BY e.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Enrollment với FETCH_BASE
2. Lọc theo studentId
3. Sắp xếp theo ID
4. Gọi hydrateCourseSectionCompatibility()
5. Trả về List<Enrollment>

---

### 3.4. findByStudentIdWithoutScore()
**Chức năng:** Lấy danh sách học phần đã đăng ký nhưng chưa có điểm của sinh viên

**JPQL:**
```jpql
SELECT DISTINCT e FROM Enrollment e
[... FETCH_BASE ...]
WHERE student.id = :studentId
  AND NOT EXISTS (
      SELECT 1 FROM Score s 
      WHERE s.enrollment.id = e.id
  )
ORDER BY e.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Enrollment với FETCH_BASE
2. Lọc theo studentId
3. Sử dụng NOT EXISTS để loại bỏ các enrollment đã có điểm
4. Sắp xếp theo ID
5. Gọi hydrateCourseSectionCompatibility()
6. Trả về List<Enrollment>

---

### 3.5. findByLecturerId()
**Chức năng:** Lấy danh sách đăng ký của các lớp do giảng viên phụ trách

**JPQL:**
```jpql
SELECT DISTINCT e FROM Enrollment e
[... FETCH_BASE ...]
WHERE lecturer.id = :lecturerId 
ORDER BY e.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Enrollment với FETCH_BASE
2. Lọc theo lecturerId
3. Sắp xếp theo ID
4. Gọi hydrateCourseSectionCompatibility()
5. Trả về List<Enrollment>

---

### 3.6. findEffectiveByLecturerId()
**Chức năng:** Lấy danh sách đăng ký còn hiệu lực (status = 'REGISTERED') của giảng viên

**JPQL:**
```jpql
SELECT DISTINCT e FROM Enrollment e
[... FETCH_BASE ...]
WHERE lecturer.id = :lecturerId
  AND UPPER(COALESCE(e.status, 'REGISTERED')) = :effectiveStatus
ORDER BY e.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Enrollment với FETCH_BASE
2. Lọc theo lecturerId
3. Kiểm tra status = 'REGISTERED' (mặc định nếu NULL)
4. Sắp xếp theo ID
5. Gọi hydrateCourseSectionCompatibility()
6. Trả về List<Enrollment>

---

### 3.7. findByClassRoomId()
**Chức năng:** Lấy danh sách đăng ký của sinh viên thuộc lớp hành chính

**JPQL:**
```jpql
SELECT DISTINCT e FROM Enrollment e
[... FETCH_BASE ...]
WHERE classRoom.id = :classRoomId 
ORDER BY e.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Enrollment với FETCH_BASE
2. Lọc theo classRoomId
3. Sắp xếp theo ID
4. Gọi hydrateCourseSectionCompatibility()
5. Trả về List<Enrollment>

---

### 3.8. findByFacultyId()
**Chức năng:** Lấy danh sách đăng ký theo khoa của sinh viên

**JPQL:**
```jpql
SELECT DISTINCT e FROM Enrollment e
[... FETCH_BASE ...]
WHERE studentFaculty.id = :facultyId 
ORDER BY e.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Enrollment với FETCH_BASE
2. Lọc theo facultyId của sinh viên
3. Sắp xếp theo ID
4. Gọi hydrateCourseSectionCompatibility()
5. Trả về List<Enrollment>

---

### 3.9. findByCourseSectionId()
**Chức năng:** Lấy danh sách sinh viên đã đăng ký vào một học phần

**JPQL:**
```jpql
SELECT DISTINCT e FROM Enrollment e
[... FETCH_BASE ...]
WHERE courseSection.id = :courseSectionId 
ORDER BY e.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Enrollment với FETCH_BASE
2. Lọc theo courseSectionId
3. Sắp xếp theo ID
4. Gọi hydrateCourseSectionCompatibility()
5. Trả về List<Enrollment>

---

### 3.10. findEffectiveByCourseSectionId()
**Chức năng:** Lấy danh sách đăng ký còn hiệu lực của một học phần

**JPQL:**
```jpql
SELECT DISTINCT e FROM Enrollment e
[... FETCH_BASE ...]
WHERE courseSection.id = :courseSectionId
  AND UPPER(COALESCE(e.status, 'REGISTERED')) = :effectiveStatus
ORDER BY e.enrolledAt, e.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Enrollment với FETCH_BASE
2. Lọc theo courseSectionId và status = 'REGISTERED'
3. Sắp xếp theo thời gian đăng ký, sau đó theo ID
4. Gọi hydrateCourseSectionCompatibility()
5. Trả về List<Enrollment>

---

### 3.11. findByStudentAndCourseSection()
**Chức năng:** Tìm đăng ký dựa trên sinh viên và học phần

**JPQL:**
```jpql
SELECT DISTINCT e FROM Enrollment e
[... FETCH_BASE ...]
WHERE student.id = :studentId
  AND courseSection.id = :courseSectionId
```

**Luồng hoạt động:**
1. SELECT DISTINCT Enrollment với FETCH_BASE
2. Lọc theo studentId và courseSectionId
3. Gọi hydrateCourseSectionCompatibility()
4. Trả về Optional<Enrollment>

---

### 3.12. searchByKeyword()
**Chức năng:** Tìm kiếm đăng ký theo từ khóa (sinh viên, học phần, môn học, giảng viên, trạng thái)

**JPQL:**
```jpql
SELECT DISTINCT e FROM Enrollment e
[... FETCH_BASE ...]
WHERE LOWER(student.studentCode) LIKE :keyword
   OR LOWER(student.fullName) LIKE :keyword
   OR LOWER(COALESCE(student.email, '')) LIKE :keyword
   OR LOWER(courseSection.sectionCode) LIKE :keyword
   OR LOWER(subject.subjectCode) LIKE :keyword
   OR LOWER(subject.subjectName) LIKE :keyword
   OR LOWER(lecturer.fullName) LIKE :keyword
   OR LOWER(COALESCE(e.status, '')) LIKE :keyword
ORDER BY e.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Enrollment với FETCH_BASE
2. Tìm kiếm không phân biệt hoa thường trên 8 trường
3. Sử dụng COALESCE để xử lý NULL
4. Sắp xếp theo ID
5. Gọi hydrateCourseSectionCompatibility()
6. Trả về List<Enrollment>

---

### 3.13. existsByStudentAndSubject()
**Chức năng:** Kiểm tra sinh viên đã đăng ký môn học trong học kỳ/năm học chưa (phát hiện trùng môn)

**JPQL:**
```jpql
SELECT COUNT(e) FROM Enrollment e
JOIN e.courseSection courseSection
JOIN courseSection.subject subject
WHERE e.student.id = :studentId
  AND LOWER(TRIM(subject.subjectName)) = :subjectName
  AND UPPER(REPLACE(courseSection.semester, ' ', '')) = UPPER(REPLACE(:semester, ' ', ''))
  AND REPLACE(courseSection.schoolYear, ' ', '') = REPLACE(:schoolYear, ' ', '')
  AND (:excludeEnrollmentId IS NULL OR e.id <> :excludeEnrollmentId)
  AND UPPER(COALESCE(e.status, 'REGISTERED')) = :effectiveStatus
```

**Luồng hoạt động:**
1. JOIN courseSection và subject
2. Lọc theo studentId
3. So sánh tên môn học (chuẩn hóa: lowercase, trim)
4. So sánh học kỳ và năm học (chuẩn hóa: loại bỏ khoảng trắng)
5. Loại trừ enrollmentId nếu có (dùng khi update)
6. Kiểm tra status = 'REGISTERED'
7. COUNT và trả về boolean (> 0 = true)

---

### 3.14. existsByStudentAndCourseSection()
**Chức năng:** Kiểm tra sinh viên đã đăng ký học phần cụ thể chưa

**JPQL:**
```jpql
SELECT COUNT(e) FROM Enrollment e
WHERE e.student.id = :studentId
  AND e.courseSection.id = :courseSectionId
  AND (:excludeEnrollmentId IS NULL OR e.id <> :excludeEnrollmentId)
  AND UPPER(COALESCE(e.status, 'REGISTERED')) = :effectiveStatus
```

**Luồng hoạt động:**
1. Lọc theo studentId và courseSectionId
2. Loại trừ enrollmentId nếu có
3. Kiểm tra status = 'REGISTERED'
4. COUNT và trả về boolean

---

### 3.15. countByCourseSectionId()
**Chức năng:** Đếm số sinh viên đã đăng ký học phần (chỉ tính status = 'REGISTERED')

**JPQL:**
```jpql
SELECT COUNT(e) FROM Enrollment e
WHERE e.courseSection.id = :courseSectionId
  AND (:excludeEnrollmentId IS NULL OR e.id <> :excludeEnrollmentId)
  AND UPPER(COALESCE(e.status, 'REGISTERED')) = :effectiveStatus
```

**Luồng hoạt động:**
1. Lọc theo courseSectionId
2. Loại trừ enrollmentId nếu có
3. Kiểm tra status = 'REGISTERED'
4. COUNT và trả về int

---

### 3.16. hasEffectiveStudentSubjectConflictForCourseSection()
**Chức năng:** Kiểm tra xung đột môn học khi cập nhật học phần (có sinh viên nào trong học phần đã đăng ký môn trùng không)

**JPQL:**
```jpql
SELECT COUNT(currentEnrollment) 
FROM Enrollment currentEnrollment
JOIN currentEnrollment.courseSection currentSection
WHERE currentSection.id = :courseSectionId
  AND UPPER(COALESCE(currentEnrollment.status, 'REGISTERED')) = :effectiveStatus
  AND EXISTS (
        SELECT otherEnrollment.id 
        FROM Enrollment otherEnrollment
        JOIN otherEnrollment.courseSection otherSection
        JOIN otherSection.subject otherSubject
        WHERE otherEnrollment.student.id = currentEnrollment.student.id
          AND otherSection.id <> currentSection.id
          AND LOWER(TRIM(otherSubject.subjectName)) = :subjectName
          AND UPPER(REPLACE(otherSection.semester, ' ', '')) = UPPER(REPLACE(:semester, ' ', ''))
          AND REPLACE(otherSection.schoolYear, ' ', '') = REPLACE(:schoolYear, ' ', '')
          AND UPPER(COALESCE(otherEnrollment.status, 'REGISTERED')) = :effectiveStatus
  )
```

**Luồng hoạt động:**
1. Lấy tất cả enrollment hiệu lực trong học phần hiện tại
2. Với mỗi enrollment, kiểm tra EXISTS:
   - Sinh viên đó có đăng ký học phần khác không?
   - Học phần khác đó có cùng môn học không? (so sánh tên môn)
   - Có cùng học kỳ và năm học không?
   - Status có hiệu lực không?
3. COUNT số lượng sinh viên bị xung đột
4. Trả về boolean (> 0 = có xung đột)

**Ứng dụng:** Khi admin muốn đổi môn học của một CourseSection, cần kiểm tra xem có sinh viên nào đã đăng ký môn mới trong cùng kỳ chưa.

---

### 3.17. hasEffectiveStudentScheduleConflictForCourseSection()
**Chức năng:** Kiểm tra xung đột lịch học khi cập nhật học phần (có sinh viên nào bị trùng lịch không)

**JPQL:**
```jpql
SELECT COUNT(currentEnrollment) 
FROM Enrollment currentEnrollment
JOIN currentEnrollment.courseSection currentSection
WHERE currentSection.id = :courseSectionId
  AND UPPER(COALESCE(currentEnrollment.status, 'REGISTERED')) = :effectiveStatus
  AND EXISTS (
        SELECT otherEnrollment.id 
        FROM Enrollment otherEnrollment
        JOIN otherEnrollment.courseSection otherSection
        WHERE otherEnrollment.student.id = currentEnrollment.student.id
          AND otherSection.id <> currentSection.id
          AND UPPER(REPLACE(otherSection.semester, ' ', '')) = UPPER(REPLACE(:semester, ' ', ''))
          AND REPLACE(otherSection.schoolYear, ' ', '') = REPLACE(:schoolYear, ' ', '')
          AND UPPER(COALESCE(otherEnrollment.status, 'REGISTERED')) = :effectiveStatus
          AND EXISTS (
                SELECT s1.id FROM Schedule s1
                WHERE s1.courseSection.id = otherSection.id
                  AND EXISTS (
                        SELECT s2.id FROM Schedule s2
                        WHERE s2.courseSection.id = currentSection.id
                          AND s1.dayOfWeek = s2.dayOfWeek
                          AND NOT (s1.endPeriod < s2.startPeriod OR s1.startPeriod > s2.endPeriod)
                  )
          )
  )
```

**Luồng hoạt động:**
1. Lấy tất cả enrollment hiệu lực trong học phần hiện tại
2. Với mỗi enrollment, kiểm tra EXISTS:
   - Sinh viên đó có đăng ký học phần khác trong cùng kỳ/năm không?
   - Học phần khác đó có lịch học không?
   - Lịch học có trùng với lịch của học phần hiện tại không?
     - Cùng thứ (dayOfWeek)
     - Tiết học giao nhau: NOT (end1 < start2 OR start1 > end2)
3. COUNT số lượng sinh viên bị xung đột
4. Trả về boolean (> 0 = có xung đột)

**Ứng dụng:** Khi admin muốn thay đổi lịch học của CourseSection, cần kiểm tra xem có sinh viên nào bị trùng lịch với các môn khác không.

---

### 3.18. findEffectiveConflictsByStudentAndSubject()
**Chức năng:** Lấy danh sách đăng ký trùng môn học của sinh viên trong cùng học kỳ/năm

**JPQL:**
```jpql
SELECT DISTINCT e FROM Enrollment e
[... FETCH_BASE ...]
WHERE student.id = :studentId
  AND LOWER(TRIM(subject.subjectName)) = :subjectName
  AND UPPER(REPLACE(courseSection.semester, ' ', '')) = UPPER(REPLACE(:semester, ' ', ''))
  AND REPLACE(courseSection.schoolYear, ' ', '') = REPLACE(:schoolYear, ' ', '')
  AND (:excludeCourseSectionId IS NULL OR courseSection.id <> :excludeCourseSectionId)
  AND UPPER(COALESCE(e.status, 'REGISTERED')) = :effectiveStatus
ORDER BY e.enrolledAt, e.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Enrollment với FETCH_BASE
2. Lọc theo studentId, tên môn, học kỳ, năm học
3. Loại trừ courseSectionId nếu có
4. Kiểm tra status = 'REGISTERED'
5. Sắp xếp theo thời gian đăng ký
6. Gọi hydrateCourseSectionCompatibility()
7. Trả về List<Enrollment> (danh sách các môn trùng)

**Ứng dụng:** Hiển thị cho người dùng biết sinh viên đã đăng ký môn nào trùng để quyết định xử lý.

---

### 3.19. findEffectiveScheduleConflictsByStudent()
**Chức năng:** Lấy danh sách đăng ký trùng lịch học của sinh viên

**JPQL:**
```jpql
SELECT DISTINCT e FROM Enrollment e
[... FETCH_BASE ...]
WHERE student.id = :studentId
  AND UPPER(REPLACE(courseSection.semester, ' ', '')) = UPPER(REPLACE(:semester, ' ', ''))
  AND REPLACE(courseSection.schoolYear, ' ', '') = REPLACE(:schoolYear, ' ', '')
  AND (:excludeCourseSectionId IS NULL OR courseSection.id <> :excludeCourseSectionId)
  AND UPPER(COALESCE(e.status, 'REGISTERED')) = :effectiveStatus
  AND EXISTS (
        SELECT s1.id FROM Schedule s1
        WHERE s1.courseSection.id = courseSection.id
          AND EXISTS (
                SELECT s2.id FROM Schedule s2
                WHERE s2.courseSection.id = :courseSectionId
                  AND s1.dayOfWeek = s2.dayOfWeek
                  AND NOT (s1.endPeriod < s2.startPeriod OR s1.startPeriod > s2.endPeriod)
          )
  )
ORDER BY e.enrolledAt, e.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Enrollment với FETCH_BASE
2. Lọc theo studentId, học kỳ, năm học
3. Loại trừ courseSectionId nếu có
4. Kiểm tra status = 'REGISTERED'
5. EXISTS kiểm tra lịch học có trùng không (cùng thứ và tiết giao nhau)
6. Sắp xếp theo thời gian đăng ký
7. Gọi hydrateCourseSectionCompatibility()
8. Trả về List<Enrollment> (danh sách các môn trùng lịch)

**Ứng dụng:** Hiển thị cho người dùng biết sinh viên đã đăng ký môn nào trùng lịch để quyết định xử lý.

---

## 4. FacultyDAO

### 4.1. findAll()
**Chức năng:** Lấy danh sách tất cả các khoa

**JPQL:**
```jpql
SELECT f FROM Faculty f 
ORDER BY f.id
```

**Luồng hoạt động:**
1. SELECT tất cả Faculty
2. Sắp xếp theo ID
3. Trả về List<Faculty>

---

### 4.2. findById()
**Chức năng:** Tìm khoa theo mã định danh

**JPQL:**
```jpql
SELECT f FROM Faculty f 
WHERE f.id = :id
```

**Luồng hoạt động:**
1. SELECT Faculty
2. Lọc theo :id
3. Trả về Optional<Faculty>

---

### 4.3. findByCode()
**Chức năng:** Tìm khoa theo mã khoa (ví dụ: CNTT)

**JPQL:**
```jpql
SELECT f FROM Faculty f 
WHERE LOWER(f.facultyCode) = LOWER(:facultyCode)
```

**Luồng hoạt động:**
1. SELECT Faculty
2. So sánh facultyCode không phân biệt hoa thường
3. Trả về Optional<Faculty>

---

### 4.4. searchByKeyword()
**Chức năng:** Tìm kiếm khoa theo từ khóa (mã khoa hoặc tên khoa)

**JPQL:**
```jpql
SELECT f FROM Faculty f
WHERE LOWER(f.facultyCode) LIKE :keyword
   OR LOWER(f.facultyName) LIKE :keyword
ORDER BY f.id
```

**Luồng hoạt động:**
1. SELECT Faculty
2. Tìm kiếm không phân biệt hoa thường trên 2 trường
3. Sắp xếp theo ID
4. Trả về List<Faculty>

---

## 5. LecturerDAO

### 5.1. findAll()
**Chức năng:** Lấy danh sách tất cả giảng viên kèm thông tin user và khoa

**JPQL:**
```jpql
SELECT l FROM Lecturer l
LEFT JOIN FETCH l.user
JOIN FETCH l.faculty
ORDER BY l.id
```

**Luồng hoạt động:**
1. SELECT Lecturer
2. LEFT JOIN FETCH user (có thể NULL)
3. JOIN FETCH faculty
4. Sắp xếp theo ID
5. Trả về List<Lecturer>

---

### 5.2. findById()
**Chức năng:** Tìm giảng viên theo mã định danh

**JPQL:**
```jpql
SELECT l FROM Lecturer l
LEFT JOIN FETCH l.user
JOIN FETCH l.faculty
WHERE l.id = :id
```

**Luồng hoạt động:**
1. SELECT Lecturer
2. LEFT JOIN FETCH user, JOIN FETCH faculty
3. Lọc theo :id
4. Trả về Optional<Lecturer>

---

### 5.3. findByUserId()
**Chức năng:** Tìm giảng viên dựa trên tài khoản người dùng liên kết

**JPQL:**
```jpql
SELECT l FROM Lecturer l
LEFT JOIN FETCH l.user
JOIN FETCH l.faculty
WHERE l.user.id = :userId
```

**Luồng hoạt động:**
1. SELECT Lecturer
2. LEFT JOIN FETCH user, JOIN FETCH faculty
3. Lọc theo userId
4. Trả về Optional<Lecturer>

---

### 5.4. findByFacultyId()
**Chức năng:** Lấy danh sách giảng viên thuộc một khoa

**JPQL:**
```jpql
SELECT l FROM Lecturer l
LEFT JOIN FETCH l.user
JOIN FETCH l.faculty
WHERE l.faculty.id = :facultyId 
ORDER BY l.id
```

**Luồng hoạt động:**
1. SELECT Lecturer
2. LEFT JOIN FETCH user, JOIN FETCH faculty
3. Lọc theo facultyId
4. Sắp xếp theo ID
5. Trả về List<Lecturer>

---

### 5.5. searchByKeyword()
**Chức năng:** Tìm kiếm giảng viên theo từ khóa (mã giảng viên, họ tên, email)

**JPQL:**
```jpql
SELECT l FROM Lecturer l
LEFT JOIN FETCH l.user
JOIN FETCH l.faculty
WHERE LOWER(l.lecturerCode) LIKE :keyword
   OR LOWER(l.fullName) LIKE :keyword
   OR LOWER(COALESCE(l.email, '')) LIKE :keyword
ORDER BY l.id
```

**Luồng hoạt động:**
1. SELECT Lecturer
2. LEFT JOIN FETCH user, JOIN FETCH faculty
3. Tìm kiếm không phân biệt hoa thường trên 3 trường
4. Sử dụng COALESCE để xử lý email NULL
5. Sắp xếp theo ID
6. Trả về List<Lecturer>

---

## 6. LecturerSubjectDAO

### 6.1. findByLecturerId()
**Chức năng:** Lấy danh sách môn học mà giảng viên được phép giảng dạy (whitelist)

**JPQL:**
```jpql
SELECT ls FROM LecturerSubject ls
JOIN FETCH ls.subject subject
JOIN FETCH subject.faculty
WHERE ls.lecturer.id = :lecturerId
ORDER BY subject.subjectCode, subject.subjectName
```

**Luồng hoạt động:**
1. SELECT LecturerSubject
2. JOIN FETCH subject và faculty
3. Lọc theo lecturerId
4. Sắp xếp theo mã môn và tên môn
5. Trả về List<LecturerSubject>

---

### 6.2. findBySubjectId()
**Chức năng:** Lấy danh sách giảng viên có đủ điều kiện giảng dạy một môn học

**JPQL:**
```jpql
SELECT ls FROM LecturerSubject ls
JOIN FETCH ls.lecturer lecturer
JOIN FETCH lecturer.faculty
LEFT JOIN FETCH lecturer.user
WHERE ls.subject.id = :subjectId
ORDER BY lecturer.lecturerCode, lecturer.fullName
```

**Luồng hoạt động:**
1. SELECT LecturerSubject
2. JOIN FETCH lecturer, faculty, user
3. Lọc theo subjectId
4. Sắp xếp theo mã GV và họ tên
5. Trả về List<LecturerSubject>

---

### 6.3. exists()
**Chức năng:** Kiểm tra giảng viên có nằm trong whitelist được phép dạy môn học không

**JPQL:**
```jpql
SELECT COUNT(ls) FROM LecturerSubject ls
WHERE ls.lecturer.id = :lecturerId 
  AND ls.subject.id = :subjectId
```

**Luồng hoạt động:**
1. COUNT LecturerSubject
2. Lọc theo lecturerId và subjectId
3. Trả về boolean (count > 0)

---

### 6.4. saveAll() - DELETE Query
**Chức năng:** Xóa tất cả môn học của giảng viên (trước khi cập nhật whitelist mới)

**JPQL:**
```jpql
DELETE FROM LecturerSubject ls 
WHERE ls.lecturer.id = :lecturerId
```

**Luồng hoạt động:**
1. DELETE tất cả LecturerSubject của giảng viên
2. Sau đó INSERT các môn mới từ danh sách subjectIds
3. Flush theo batch (mỗi 25 records)

---

### 6.5. backfillFromCourseSectionsIfEmpty() - Query 1
**Chức năng:** Kiểm tra bảng LecturerSubject có rỗng không

**JPQL:**
```jpql
SELECT COUNT(ls) FROM LecturerSubject ls
```

**Luồng hoạt động:**
1. COUNT tất cả LecturerSubject
2. Nếu > 0 thì return 0 (không cần backfill)
3. Nếu = 0 thì tiếp tục query 2

---

### 6.6. backfillFromCourseSectionsIfEmpty() - Query 2
**Chức năng:** Lấy danh sách giảng viên-môn học từ CourseSection để tự động khởi tạo whitelist

**JPQL:**
```jpql
SELECT DISTINCT cs.lecturer.id, cs.subject.id
FROM CourseSection cs
WHERE cs.lecturer.id IS NOT NULL 
  AND cs.subject.id IS NOT NULL
ORDER BY cs.lecturer.id, cs.subject.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT cặp (lecturerId, subjectId) từ CourseSection
2. Lọc NOT NULL
3. Sắp xếp
4. INSERT vào LecturerSubject nếu chưa tồn tại
5. Trả về số lượng records đã insert

**Ứng dụng:** Khi hệ thống mới triển khai tính năng whitelist, tự động tạo dữ liệu từ các học phần đã có.

---

## 7. ReportDAO

**Lưu ý:** ReportDAO chỉ đọc dữ liệu để tạo báo cáo, sử dụng JPQL projection (SELECT các trường cụ thể thay vì entity).

### 7.1. findStudentsByClassRoom()
**Chức năng:** Truy xuất thông tin cơ bản của sinh viên theo lớp học để lập báo cáo

**JPQL:**
```jpql
SELECT s.studentCode, s.fullName, s.email, s.phone, s.status
FROM Student s
WHERE s.classRoom.id = :classRoomId
ORDER BY s.studentCode
```

**Luồng hoạt động:**
1. SELECT 5 trường cần thiết (không load toàn bộ entity)
2. Lọc theo classRoomId
3. Sắp xếp theo mã sinh viên
4. Trả về List<Object[]>

---

### 7.2. findLecturersByFaculty()
**Chức năng:** Truy xuất thông tin cơ bản của giảng viên theo khoa để lập báo cáo

**JPQL:**
```jpql
SELECT l.lecturerCode, l.fullName, l.email, l.phone, l.status
FROM Lecturer l
WHERE l.faculty.id = :facultyId
ORDER BY l.lecturerCode
```

**Luồng hoạt động:**
1. SELECT 5 trường cần thiết
2. Lọc theo facultyId
3. Sắp xếp theo mã giảng viên
4. Trả về List<Object[]>

---

### 7.3. findStudentsByCourseSection()
**Chức năng:** Truy xuất danh sách sinh viên đã đăng ký vào một học phần

**JPQL:**
```jpql
SELECT s.studentCode, s.fullName, s.email, e.status, e.enrolledAt
FROM Enrollment e
JOIN e.student s
WHERE e.courseSection.id = :courseSectionId
ORDER BY s.studentCode
```

**Luồng hoạt động:**
1. SELECT thông tin sinh viên và trạng thái đăng ký
2. JOIN student
3. Lọc theo courseSectionId
4. Sắp xếp theo mã sinh viên
5. Trả về List<Object[]>

---

### 7.4. findScoresByCourseSection()
**Chức năng:** Truy xuất bảng điểm chi tiết của tất cả sinh viên trong một học phần

**JPQL:**
```jpql
SELECT s.studentCode,
       s.fullName,
       COALESCE(sc.processScore, 0),
       COALESCE(sc.midtermScore, 0),
       COALESCE(sc.finalScore, 0),
       COALESCE(sc.totalScore, 0),
       COALESCE(sc.result, 'FAIL')
FROM Enrollment e
JOIN e.student s
LEFT JOIN Score sc ON sc.enrollment = e
WHERE e.courseSection.id = :courseSectionId
ORDER BY s.studentCode
```

**Luồng hoạt động:**
1. SELECT thông tin sinh viên và điểm số
2. JOIN student, LEFT JOIN score (có thể chưa có điểm)
3. Sử dụng COALESCE để xử lý NULL (điểm = 0, kết quả = 'FAIL')
4. Lọc theo courseSectionId
5. Sắp xếp theo mã sinh viên
6. Trả về List<Object[]>

---

### 7.5. getSystemStatistics()
**Chức năng:** Tổng hợp các số liệu thống kê cơ bản của toàn bộ hệ thống

**JPQL (5 queries):**
```jpql
SELECT COUNT(s) FROM Student s
SELECT COUNT(l) FROM Lecturer l
SELECT COUNT(su) FROM Subject su
SELECT COUNT(cs) FROM CourseSection cs
SELECT COUNT(e) FROM Enrollment e
```

**Luồng hoạt động:**
1. Thực hiện 5 COUNT queries riêng biệt
2. Tạo đối tượng SystemStatistics với 5 giá trị
3. Trả về SystemStatistics

---

## 8. RoomDAO

### 8.1. findAll()
**Chức năng:** Lấy danh sách tất cả các phòng học

**JPQL:**
```jpql
SELECT r FROM Room r 
ORDER BY r.id
```

**Luồng hoạt động:**
1. SELECT tất cả Room
2. Sắp xếp theo ID
3. Trả về List<Room>

---

### 8.2. findById()
**Chức năng:** Tìm phòng học theo mã định danh

**JPQL:**
```jpql
SELECT r FROM Room r 
WHERE r.id = :id
```

**Luồng hoạt động:**
1. SELECT Room
2. Lọc theo :id
3. Trả về Optional<Room>

---

### 8.3. searchByKeyword()
**Chức năng:** Tìm kiếm phòng học theo từ khóa (mã phòng hoặc tên phòng)

**JPQL:**
```jpql
SELECT r FROM Room r
WHERE LOWER(r.roomCode) LIKE :keyword
   OR LOWER(r.roomName) LIKE :keyword
ORDER BY r.id
```

**Luồng hoạt động:**
1. SELECT Room
2. Tìm kiếm không phân biệt hoa thường trên 2 trường
3. Sắp xếp theo ID
4. Trả về List<Room>

---

## 9. ScheduleDAO

### 9.1. findAll()
**Chức năng:** Lấy tất cả lịch học trong hệ thống, sắp xếp theo thứ và tiết học

**JPQL:**
```jpql
SELECT DISTINCT s FROM Schedule s
JOIN FETCH s.courseSection cs
JOIN FETCH cs.subject subject
JOIN FETCH subject.faculty
JOIN FETCH cs.lecturer lecturer
JOIN FETCH s.room room
ORDER BY s.dayOfWeek, s.startPeriod, cs.id, s.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Schedule
2. JOIN FETCH courseSection, subject, faculty, lecturer, room
3. Sắp xếp theo thứ, tiết bắt đầu, courseSection, schedule ID
4. Trả về List<Schedule>

---

### 9.2. findById()
**Chức năng:** Tìm lịch học theo mã định danh

**JPQL:**
```jpql
SELECT DISTINCT s FROM Schedule s
JOIN FETCH s.courseSection cs
JOIN FETCH cs.subject subject
JOIN FETCH subject.faculty
JOIN FETCH cs.lecturer lecturer
JOIN FETCH s.room room
WHERE s.id = :id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Schedule với đầy đủ JOIN FETCH
2. Lọc theo :id
3. Trả về Optional<Schedule>

---

### 9.3. findByStudentId()
**Chức năng:** Truy xuất thời khóa biểu của một sinh viên dựa trên danh sách học phần đã đăng ký

**JPQL:**
```jpql
SELECT DISTINCT s FROM Schedule s
JOIN FETCH s.courseSection cs
JOIN FETCH cs.subject subject
JOIN FETCH subject.faculty
JOIN FETCH cs.lecturer lecturer
JOIN FETCH s.room room
JOIN Enrollment enrollment ON enrollment.courseSection = cs
WHERE enrollment.student.id = :studentId
ORDER BY s.dayOfWeek, s.startPeriod, cs.id, s.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Schedule với đầy đủ JOIN FETCH
2. JOIN Enrollment để lọc theo sinh viên
3. Lọc theo studentId
4. Sắp xếp theo thứ và tiết
5. Trả về List<Schedule> (thời khóa biểu của sinh viên)

---

### 9.4. findByLecturerId()
**Chức năng:** Truy xuất lịch giảng dạy của một giảng viên

**JPQL:**
```jpql
SELECT DISTINCT s FROM Schedule s
JOIN FETCH s.courseSection cs
JOIN FETCH cs.subject subject
JOIN FETCH subject.faculty
JOIN FETCH cs.lecturer lecturer
JOIN FETCH s.room room
WHERE cs.lecturer.id = :lecturerId 
ORDER BY s.dayOfWeek, s.startPeriod, s.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Schedule với đầy đủ JOIN FETCH
2. Lọc theo lecturerId
3. Sắp xếp theo thứ và tiết
4. Trả về List<Schedule>

---

### 9.5. findByCourseSectionId()
**Chức năng:** Lấy danh sách các buổi học/lịch học của một học phần

**JPQL:**
```jpql
SELECT DISTINCT s FROM Schedule s
JOIN FETCH s.courseSection cs
JOIN FETCH cs.subject subject
JOIN FETCH subject.faculty
JOIN FETCH cs.lecturer lecturer
JOIN FETCH s.room room
WHERE cs.id = :courseSectionId 
ORDER BY s.dayOfWeek, s.startPeriod, s.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Schedule với đầy đủ JOIN FETCH
2. Lọc theo courseSectionId
3. Sắp xếp theo thứ và tiết
4. Trả về List<Schedule>

---

### 9.6. findByRoom()
**Chức năng:** Lọc danh sách lịch học theo phòng học cụ thể

**JPQL:**
```jpql
SELECT DISTINCT s FROM Schedule s
JOIN FETCH s.courseSection cs
JOIN FETCH cs.subject subject
JOIN FETCH subject.faculty
JOIN FETCH cs.lecturer lecturer
JOIN FETCH s.room room
WHERE room.id = :roomId 
ORDER BY s.dayOfWeek, s.startPeriod, s.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Schedule với đầy đủ JOIN FETCH
2. Lọc theo roomId
3. Sắp xếp theo thứ và tiết
4. Trả về List<Schedule>

---

### 9.7. findByFacultyId()
**Chức năng:** Lọc danh sách lịch học theo khoa quản lý

**JPQL:**
```jpql
SELECT DISTINCT s FROM Schedule s
JOIN FETCH s.courseSection cs
JOIN FETCH cs.subject subject
JOIN FETCH subject.faculty
JOIN FETCH cs.lecturer lecturer
JOIN FETCH s.room room
WHERE subject.faculty.id = :facultyId 
ORDER BY s.dayOfWeek, s.startPeriod, s.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Schedule với đầy đủ JOIN FETCH
2. Lọc theo facultyId của môn học
3. Sắp xếp theo thứ và tiết
4. Trả về List<Schedule>

---

### 9.8. searchByKeyword()
**Chức năng:** Tìm kiếm lịch học theo từ khóa (mã học phần, môn học, giảng viên, phòng, thứ, tiết, ghi chú)

**JPQL:**
```jpql
SELECT DISTINCT s FROM Schedule s
JOIN FETCH s.courseSection cs
JOIN FETCH cs.subject subject
JOIN FETCH subject.faculty
JOIN FETCH cs.lecturer lecturer
JOIN FETCH s.room room
WHERE LOWER(cs.sectionCode) LIKE :keyword
   OR LOWER(subject.subjectCode) LIKE :keyword
   OR LOWER(subject.subjectName) LIKE :keyword
   OR LOWER(lecturer.lecturerCode) LIKE :keyword
   OR LOWER(lecturer.fullName) LIKE :keyword
   OR LOWER(room.roomCode) LIKE :keyword
   OR LOWER(room.roomName) LIKE :keyword
   OR LOWER(s.dayOfWeek) LIKE :keyword
   OR LOWER(COALESCE(s.note, '')) LIKE :keyword
   OR STR(s.startPeriod) LIKE :keyword
   OR STR(s.endPeriod) LIKE :keyword
ORDER BY s.dayOfWeek, s.startPeriod, s.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Schedule với đầy đủ JOIN FETCH
2. Tìm kiếm không phân biệt hoa thường trên 11 trường
3. Sử dụng STR() để chuyển số thành chuỗi cho startPeriod/endPeriod
4. Sắp xếp theo thứ và tiết
5. Trả về List<Schedule>

---

### 9.9. hasLecturerScheduleConflict()
**Chức năng:** Kiểm tra giảng viên có bị trùng lịch dạy vào thời gian đã chọn không

**JPQL:**
```jpql
SELECT COUNT(s) FROM Schedule s
JOIN s.courseSection otherSection
WHERE otherSection.lecturer.id = :lecturerId
  AND s.dayOfWeek = :dayOfWeek
  AND NOT (s.endPeriod < :startPeriod OR s.startPeriod > :endPeriod)
  AND (:excludeScheduleId IS NULL OR s.id <> :excludeScheduleId)
```

**Luồng hoạt động:**
1. JOIN courseSection để lấy lecturer
2. Lọc theo lecturerId và dayOfWeek
3. Kiểm tra tiết học có giao nhau không: NOT (end < start OR start > end)
4. Loại trừ scheduleId nếu có (dùng khi update)
5. COUNT và trả về boolean (> 0 = có xung đột)

**Ứng dụng:** Khi tạo/sửa lịch học, đảm bảo giảng viên không dạy 2 lớp cùng lúc.

---

### 9.10. hasRoomScheduleConflict()
**Chức năng:** Kiểm tra phòng học có bị trùng lịch sử dụng vào thời gian đã chọn không

**JPQL:**
```jpql
SELECT COUNT(s) FROM Schedule s
WHERE s.room.id = :roomId
  AND s.dayOfWeek = :dayOfWeek
  AND NOT (s.endPeriod < :startPeriod OR s.startPeriod > :endPeriod)
  AND (:excludeScheduleId IS NULL OR s.id <> :excludeScheduleId)
```

**Luồng hoạt động:**
1. Lọc theo roomId và dayOfWeek
2. Kiểm tra tiết học có giao nhau không
3. Loại trừ scheduleId nếu có
4. COUNT và trả về boolean (> 0 = có xung đột)

**Ứng dụng:** Khi tạo/sửa lịch học, đảm bảo phòng không bị đặt trùng.

---

### 9.11. hasStudentScheduleConflict()
**Chức năng:** Kiểm tra sinh viên có bị trùng thời khóa biểu khi đăng ký thêm học phần mới không

**JPQL:**
```jpql
SELECT COUNT(existingSchedule)
FROM Schedule existingSchedule
JOIN Enrollment enrollment ON enrollment.courseSection = existingSchedule.courseSection
JOIN enrollment.courseSection existingSection
WHERE enrollment.student.id = :studentId
  AND (:excludeEnrollmentId IS NULL OR enrollment.id <> :excludeEnrollmentId)
  AND UPPER(COALESCE(enrollment.status, 'REGISTERED')) = :effectiveStatus
  AND EXISTS (
        SELECT newSchedule.id FROM Schedule newSchedule
        JOIN newSchedule.courseSection newSection
        WHERE newSection.id = :courseSectionId
          AND UPPER(REPLACE(existingSection.semester, ' ', '')) = UPPER(REPLACE(newSection.semester, ' ', ''))
          AND REPLACE(existingSection.schoolYear, ' ', '') = REPLACE(newSection.schoolYear, ' ', '')
          AND existingSchedule.dayOfWeek = newSchedule.dayOfWeek
          AND NOT (existingSchedule.endPeriod < newSchedule.startPeriod 
                   OR existingSchedule.startPeriod > newSchedule.endPeriod)
  )
```

**Luồng hoạt động:**
1. Lấy tất cả lịch học của sinh viên (qua Enrollment)
2. Kiểm tra status = 'REGISTERED'
3. Với mỗi lịch hiện tại, EXISTS kiểm tra:
   - Lịch của học phần mới (courseSectionId)
   - Cùng học kỳ và năm học
   - Cùng thứ
   - Tiết học giao nhau
4. COUNT và trả về boolean (> 0 = có xung đột)

**Ứng dụng:** Khi sinh viên đăng ký học phần mới, kiểm tra có trùng lịch với các môn đã đăng ký không.

---

## 10. ScoreDAO

### 10.1. findAll()
**Chức năng:** Lấy danh sách điểm của tất cả sinh viên trong hệ thống

**JPQL:**
```jpql
SELECT DISTINCT score FROM Score score
JOIN FETCH score.enrollment enrollment
JOIN FETCH enrollment.student student
JOIN FETCH student.faculty studentFaculty
JOIN FETCH student.classRoom classRoom
JOIN FETCH classRoom.faculty classFaculty
JOIN FETCH enrollment.courseSection courseSection
JOIN FETCH courseSection.subject subject
JOIN FETCH subject.faculty subjectFaculty
JOIN FETCH courseSection.lecturer lecturer
ORDER BY score.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Score
2. JOIN FETCH 9 bảng liên quan (enrollment → student, courseSection → subject, lecturer)
3. Sắp xếp theo ID
4. Gọi hydrateCourseSectionCompatibility() để load lịch học
5. Trả về List<Score>

---

### 10.2. findById()
**Chức năng:** Tìm thông tin điểm theo mã định danh

**JPQL:**
```jpql
SELECT DISTINCT score FROM Score score
[... FETCH_BASE ...]
WHERE score.id = :id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Score với FETCH_BASE
2. Lọc theo :id
3. Gọi hydrateCourseSectionCompatibility()
4. Trả về Optional<Score>

---

### 10.3. findByEnrollmentId()
**Chức năng:** Tìm điểm của một lượt đăng ký học phần cụ thể

**JPQL:**
```jpql
SELECT DISTINCT score FROM Score score
[... FETCH_BASE ...]
WHERE enrollment.id = :enrollmentId
```

**Luồng hoạt động:**
1. SELECT DISTINCT Score với FETCH_BASE
2. Lọc theo enrollmentId
3. Gọi hydrateCourseSectionCompatibility()
4. Trả về Optional<Score>

---

### 10.4. findByStudentId()
**Chức năng:** Lấy danh sách bảng điểm của một sinh viên

**JPQL:**
```jpql
SELECT DISTINCT score FROM Score score
[... FETCH_BASE ...]
WHERE student.id = :studentId 
ORDER BY score.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Score với FETCH_BASE
2. Lọc theo studentId
3. Sắp xếp theo ID
4. Gọi hydrateCourseSectionCompatibility()
5. Trả về List<Score>

---

### 10.5. findByLecturerId()
**Chức năng:** Lấy danh sách điểm của các học phần do một giảng viên phụ trách

**JPQL:**
```jpql
SELECT DISTINCT score FROM Score score
[... FETCH_BASE ...]
WHERE lecturer.id = :lecturerId 
ORDER BY score.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Score với FETCH_BASE
2. Lọc theo lecturerId
3. Sắp xếp theo ID
4. Gọi hydrateCourseSectionCompatibility()
5. Trả về List<Score>

---

### 10.6. findByCourseSectionId()
**Chức năng:** Lấy bảng điểm của tất cả sinh viên trong một học phần

**JPQL:**
```jpql
SELECT DISTINCT score FROM Score score
[... FETCH_BASE ...]
WHERE courseSection.id = :courseSectionId 
ORDER BY score.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Score với FETCH_BASE
2. Lọc theo courseSectionId
3. Sắp xếp theo ID
4. Gọi hydrateCourseSectionCompatibility()
5. Trả về List<Score>

---

### 10.7. findByClassRoomId()
**Chức năng:** Lấy danh sách điểm của sinh viên thuộc một lớp hành chính

**JPQL:**
```jpql
SELECT DISTINCT score FROM Score score
[... FETCH_BASE ...]
WHERE classRoom.id = :classRoomId 
ORDER BY score.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Score với FETCH_BASE
2. Lọc theo classRoomId
3. Sắp xếp theo ID
4. Gọi hydrateCourseSectionCompatibility()
5. Trả về List<Score>

---

### 10.8. searchByKeyword()
**Chức năng:** Tìm kiếm điểm theo từ khóa (sinh viên, học phần, môn học, giảng viên, kết quả)

**JPQL:**
```jpql
SELECT DISTINCT score FROM Score score
[... FETCH_BASE ...]
WHERE LOWER(student.studentCode) LIKE :keyword
   OR LOWER(student.fullName) LIKE :keyword
   OR LOWER(COALESCE(student.email, '')) LIKE :keyword
   OR LOWER(courseSection.sectionCode) LIKE :keyword
   OR LOWER(subject.subjectCode) LIKE :keyword
   OR LOWER(subject.subjectName) LIKE :keyword
   OR LOWER(lecturer.lecturerCode) LIKE :keyword
   OR LOWER(lecturer.fullName) LIKE :keyword
   OR LOWER(COALESCE(score.result, '')) LIKE :keyword
ORDER BY score.id
```

**Luồng hoạt động:**
1. SELECT DISTINCT Score với FETCH_BASE
2. Tìm kiếm không phân biệt hoa thường trên 9 trường
3. Sử dụng COALESCE để xử lý NULL
4. Sắp xếp theo ID
5. Gọi hydrateCourseSectionCompatibility()
6. Trả về List<Score>

---

### 10.9. hydrateCourseSectionCompatibility() - Query phụ
**Chức năng:** Load thông tin lịch học và phòng cho CourseSection trong Score

**JPQL:**
```jpql
SELECT schedule.courseSection.id,
       schedule.dayOfWeek,
       schedule.startPeriod,
       schedule.endPeriod,
       room.id,
       room.roomCode,
       room.roomName
FROM Schedule schedule
JOIN schedule.room room
WHERE schedule.courseSection.id IN :courseSectionIds
ORDER BY schedule.courseSection.id, schedule.dayOfWeek, schedule.startPeriod, schedule.id
```

**Luồng hoạt động:**
1. SELECT các trường cần thiết từ Schedule và Room
2. JOIN room
3. Lọc theo danh sách courseSectionIds
4. Sắp xếp theo courseSection, thứ, tiết
5. Xây dựng chuỗi lịch học và gán vào CourseSection

---

## 11. StudentDAO

### 11.1. findAll()
**Chức năng:** Lấy danh sách tất cả sinh viên trong hệ thống

**JPQL:**
```jpql
SELECT s FROM Student s
LEFT JOIN FETCH s.user
JOIN FETCH s.faculty
JOIN FETCH s.classRoom
ORDER BY s.id
```

**Luồng hoạt động:**
1. SELECT Student
2. LEFT JOIN FETCH user (có thể NULL)
3. JOIN FETCH faculty và classRoom
4. Sắp xếp theo ID
5. Trả về List<Student>

---

### 11.2. findById()
**Chức năng:** Tìm sinh viên theo mã định danh

**JPQL:**
```jpql
SELECT s FROM Student s
LEFT JOIN FETCH s.user
JOIN FETCH s.faculty
JOIN FETCH s.classRoom
WHERE s.id = :id
```

**Luồng hoạt động:**
1. SELECT Student với đầy đủ JOIN FETCH
2. Lọc theo :id
3. Trả về Optional<Student>

---

### 11.3. findByStudentCode()
**Chức năng:** Tìm sinh viên theo mã số sinh viên (ví dụ: SV001)

**JPQL:**
```jpql
SELECT s FROM Student s
LEFT JOIN FETCH s.user
JOIN FETCH s.faculty
JOIN FETCH s.classRoom
WHERE LOWER(s.studentCode) = LOWER(:studentCode)
```

**Luồng hoạt động:**
1. SELECT Student với đầy đủ JOIN FETCH
2. So sánh studentCode không phân biệt hoa thường
3. Trả về Optional<Student>

---

### 11.4. findByUserId()
**Chức năng:** Tìm sinh viên dựa trên tài khoản người dùng liên kết

**JPQL:**
```jpql
SELECT s FROM Student s
LEFT JOIN FETCH s.user
JOIN FETCH s.faculty
JOIN FETCH s.classRoom
WHERE s.user.id = :userId
```

**Luồng hoạt động:**
1. SELECT Student với đầy đủ JOIN FETCH
2. Lọc theo userId
3. Trả về Optional<Student>

---

### 11.5. findByFacultyId()
**Chức năng:** Lọc danh sách sinh viên theo khoa

**JPQL:**
```jpql
SELECT s FROM Student s
LEFT JOIN FETCH s.user
JOIN FETCH s.faculty
JOIN FETCH s.classRoom
WHERE s.faculty.id = :facultyId 
ORDER BY s.id
```

**Luồng hoạt động:**
1. SELECT Student với đầy đủ JOIN FETCH
2. Lọc theo facultyId
3. Sắp xếp theo ID
4. Trả về List<Student>

---

### 11.6. findByClassRoomId()
**Chức năng:** Lọc danh sách sinh viên theo lớp học hành chính

**JPQL:**
```jpql
SELECT s FROM Student s
LEFT JOIN FETCH s.user
JOIN FETCH s.faculty
JOIN FETCH s.classRoom
WHERE s.classRoom.id = :classRoomId 
ORDER BY s.id
```

**Luồng hoạt động:**
1. SELECT Student với đầy đủ JOIN FETCH
2. Lọc theo classRoomId
3. Sắp xếp theo ID
4. Trả về List<Student>

---

### 11.7. findAcademicYears()
**Chức năng:** Lấy danh sách tất cả các niên khóa hiện có của sinh viên

**JPQL:**
```jpql
SELECT DISTINCT s.academicYear FROM Student s
WHERE s.academicYear IS NOT NULL 
  AND TRIM(s.academicYear) <> ''
ORDER BY s.academicYear
```

**Luồng hoạt động:**
1. SELECT DISTINCT academicYear
2. Lọc NOT NULL và không rỗng
3. Sắp xếp
4. Chuẩn hóa format (2020-2024 hoặc 2020 - 2024)
5. Loại bỏ trùng lặp sau khi chuẩn hóa
6. Trả về List<String>

---

### 11.8. searchByCriteria()
**Chức năng:** Tìm kiếm sinh viên theo từ khóa và các tiêu chí lọc (khoa, lớp, niên khóa)

**JPQL (động):**
```jpql
SELECT s FROM Student s
LEFT JOIN FETCH s.user
JOIN FETCH s.faculty
JOIN FETCH s.classRoom
WHERE 1 = 1
  [AND s.faculty.id = :facultyId]
  [AND s.classRoom.id = :classRoomId]
  [AND (LOWER(s.studentCode) LIKE :keyword
        OR LOWER(s.fullName) LIKE :keyword
        OR LOWER(s.email) LIKE :keyword)]
ORDER BY s.id
```

**Luồng hoạt động:**
1. Xây dựng JPQL động dựa trên các tham số không NULL
2. SELECT Student với đầy đủ JOIN FETCH
3. Thêm điều kiện WHERE theo facultyId, classRoomId, keyword
4. Sắp xếp theo ID
5. Lọc theo academicYear trong Java (do cần chuẩn hóa format)
6. Trả về List<Student>

---

## 12. SubjectDAO

### 12.1. findAll()
**Chức năng:** Lấy danh sách toàn bộ các môn học trong hệ thống

**JPQL:**
```jpql
SELECT s FROM Subject s
JOIN FETCH s.faculty
ORDER BY s.id
```

**Luồng hoạt động:**
1. SELECT Subject
2. JOIN FETCH faculty
3. Sắp xếp theo ID
4. Trả về List<Subject>

---

### 12.2. findById()
**Chức năng:** Tìm môn học theo mã định danh

**JPQL:**
```jpql
SELECT s FROM Subject s
JOIN FETCH s.faculty
WHERE s.id = :id
```

**Luồng hoạt động:**
1. SELECT Subject
2. JOIN FETCH faculty
3. Lọc theo :id
4. Trả về Optional<Subject>

---

### 12.3. findByFacultyId()
**Chức năng:** Lọc danh sách môn học theo khoa quản lý

**JPQL:**
```jpql
SELECT s FROM Subject s
JOIN FETCH s.faculty
WHERE s.faculty.id = :facultyId 
ORDER BY s.id
```

**Luồng hoạt động:**
1. SELECT Subject
2. JOIN FETCH faculty
3. Lọc theo facultyId
4. Sắp xếp theo ID
5. Trả về List<Subject>

---

### 12.4. searchByKeyword()
**Chức năng:** Tìm kiếm môn học theo từ khóa (mã môn hoặc tên môn)

**JPQL:**
```jpql
SELECT s FROM Subject s
JOIN FETCH s.faculty
WHERE LOWER(s.subjectCode) LIKE :keyword
   OR LOWER(s.subjectName) LIKE :keyword
ORDER BY s.id
```

**Luồng hoạt động:**
1. SELECT Subject
2. JOIN FETCH faculty
3. Tìm kiếm không phân biệt hoa thường trên 2 trường
4. Sắp xếp theo ID
5. Trả về List<Subject>

---

## 13. UserDAO

### 13.1. findAll()
**Chức năng:** Lấy danh sách tất cả người dùng trong hệ thống

**JPQL:**
```jpql
SELECT u FROM User u
JOIN FETCH u.roleEntity
ORDER BY u.id
```

**Luồng hoạt động:**
1. SELECT User
2. JOIN FETCH roleEntity (để detached User vẫn có thể gọi getRole())
3. Sắp xếp theo ID
4. Trả về List<User>

---

### 13.2. findById()
**Chức năng:** Tìm người dùng theo mã định danh

**JPQL:**
```jpql
SELECT u FROM User u
JOIN FETCH u.roleEntity
WHERE u.id = :id
```

**Luồng hoạt động:**
1. SELECT User
2. JOIN FETCH roleEntity
3. Lọc theo :id
4. Trả về Optional<User>

---

### 13.3. findByUsername()
**Chức năng:** Tìm người dùng theo tên đăng nhập (không phân biệt hoa thường)

**JPQL:**
```jpql
SELECT u FROM User u
JOIN FETCH u.roleEntity
WHERE LOWER(u.username) = LOWER(:username)
```

**Luồng hoạt động:**
1. SELECT User
2. JOIN FETCH roleEntity
3. So sánh username không phân biệt hoa thường
4. Trả về Optional<User>

**Ứng dụng:** Dùng cho đăng nhập, kiểm tra username tồn tại.

---

### 13.4. searchByKeyword()
**Chức năng:** Tìm kiếm người dùng theo từ khóa (tên đăng nhập, họ tên, email)

**JPQL:**
```jpql
SELECT u FROM User u
JOIN FETCH u.roleEntity
WHERE LOWER(u.username) LIKE :keyword
   OR LOWER(u.fullName) LIKE :keyword
   OR LOWER(COALESCE(u.email, '')) LIKE :keyword
ORDER BY u.id
```

**Luồng hoạt động:**
1. SELECT User
2. JOIN FETCH roleEntity
3. Tìm kiếm không phân biệt hoa thường trên 3 trường
4. Sử dụng COALESCE để xử lý email NULL
5. Sắp xếp theo ID
6. Trả về List<User>

---

### 13.5. resolveRoleEntity() - Query phụ
**Chức năng:** Xác định RoleEntity khi lưu User (chuyển từ Role enum sang RoleEntity)

**JPQL:**
```jpql
SELECT r FROM RoleEntity r
WHERE UPPER(r.roleCode) = UPPER(:roleCode)
```

**Luồng hoạt động:**
1. SELECT RoleEntity
2. So sánh roleCode không phân biệt hoa thường
3. Trả về RoleEntity hoặc throw exception nếu không tìm thấy

**Ứng dụng:** Khi insert/update User, cần chuyển Role enum (ADMIN, LECTURER, STUDENT) thành RoleEntity để lưu vào database.

---

## PHỤ LỤC

### A. Các kỹ thuật JPQL được sử dụng

#### A.1. JOIN FETCH
**Mục đích:** Eager loading để tránh lazy loading exception khi EntityManager đã đóng.

**Ví dụ:**
```jpql
SELECT s FROM Student s
JOIN FETCH s.faculty
JOIN FETCH s.classRoom
```

**Lưu ý:** 
- Sử dụng `LEFT JOIN FETCH` khi quan hệ có thể NULL
- Sử dụng `DISTINCT` khi có nhiều JOIN FETCH để tránh duplicate

---

#### A.2. COALESCE
**Mục đích:** Xử lý giá trị NULL, cung cấp giá trị mặc định.

**Ví dụ:**
```jpql
COALESCE(e.status, 'REGISTERED')
COALESCE(student.email, '')
```

---

#### A.3. LOWER / UPPER
**Mục đích:** Tìm kiếm không phân biệt hoa thường.

**Ví dụ:**
```jpql
WHERE LOWER(s.studentCode) LIKE :keyword
WHERE UPPER(e.status) = 'REGISTERED'
```

---

#### A.4. REPLACE
**Mục đích:** Chuẩn hóa dữ liệu trước khi so sánh (loại bỏ khoảng trắng).

**Ví dụ:**
```jpql
REPLACE(courseSection.semester, ' ', '')
REPLACE(courseSection.schoolYear, ' ', '')
```

---

#### A.5. TRIM
**Mục đích:** Loại bỏ khoảng trắng đầu/cuối.

**Ví dụ:**
```jpql
WHERE TRIM(s.academicYear) <> ''
LOWER(TRIM(subject.subjectName))
```

---

#### A.6. EXISTS / NOT EXISTS
**Mục đích:** Kiểm tra sự tồn tại của dữ liệu liên quan.

**Ví dụ:**
```jpql
WHERE NOT EXISTS (
    SELECT 1 FROM Score s 
    WHERE s.enrollment.id = e.id
)
```

---

#### A.7. COUNT
**Mục đích:** Đếm số lượng records, thường dùng để kiểm tra tồn tại hoặc xung đột.

**Ví dụ:**
```jpql
SELECT COUNT(e) FROM Enrollment e
WHERE e.student.id = :studentId
```

---

#### A.8. STR
**Mục đích:** Chuyển số thành chuỗi để tìm kiếm.

**Ví dụ:**
```jpql
WHERE STR(s.startPeriod) LIKE :keyword
```

---

#### A.9. Kiểm tra giao nhau của khoảng thời gian
**Mục đích:** Kiểm tra 2 khoảng thời gian có giao nhau không.

**Công thức:**
```jpql
NOT (end1 < start2 OR start1 > end2)
```

**Ví dụ:**
```jpql
WHERE NOT (s1.endPeriod < s2.startPeriod 
           OR s1.startPeriod > s2.endPeriod)
```

**Giải thích:** 
- Hai khoảng KHÔNG giao nhau khi: end1 < start2 HOẶC start1 > end2
- Hai khoảng GIAO NHAU khi: NOT (điều kiện trên)

---

### B. Các pattern thường gặp

#### B.1. Pattern: Tìm kiếm với nhiều điều kiện
```jpql
WHERE LOWER(field1) LIKE :keyword
   OR LOWER(field2) LIKE :keyword
   OR LOWER(COALESCE(field3, '')) LIKE :keyword
```

#### B.2. Pattern: Kiểm tra xung đột
```jpql
SELECT COUNT(entity) FROM Entity entity
WHERE entity.field = :value
  AND EXISTS (
      SELECT 1 FROM RelatedEntity re
      WHERE re.id = entity.relatedId
        AND [điều kiện xung đột]
  )
```

#### B.3. Pattern: Loại trừ khi update
```jpql
WHERE entity.field = :value
  AND (:excludeId IS NULL OR entity.id <> :excludeId)
```

#### B.4. Pattern: Chuẩn hóa và so sánh
```jpql
WHERE UPPER(REPLACE(field, ' ', '')) = UPPER(REPLACE(:value, ' ', ''))
```

---

### C. Tổng kết số lượng JPQL

| DAO | Số lượng JPQL | Ghi chú |
|-----|---------------|---------|
| ClassRoomDAO | 4 | Cơ bản |
| CourseSectionDAO | 9 | Có hydrate schedule |
| EnrollmentDAO | 19 | Phức tạp nhất, nhiều conflict check |
| FacultyDAO | 4 | Cơ bản |
| LecturerDAO | 5 | Cơ bản |
| LecturerSubjectDAO | 6 | Có backfill logic |
| ReportDAO | 6 | Projection queries |
| RoomDAO | 3 | Đơn giản nhất |
| ScheduleDAO | 11 | Nhiều conflict check |
| ScoreDAO | 9 | Có hydrate schedule |
| StudentDAO | 8 | Có dynamic query |
| SubjectDAO | 4 | Cơ bản |
| UserDAO | 5 | Có resolve role |
| **TỔNG** | **93** | |

---

## KẾT LUẬN

Hệ thống sử dụng 93 câu JPQL trải rộng trên 13 DAO classes, bao gồm:

1. **Queries cơ bản:** findAll, findById, search (40%)
2. **Queries phức tạp:** conflict detection, schedule hydration (35%)
3. **Queries thống kê:** count, exists, report (15%)
4. **Queries đặc biệt:** backfill, dynamic query, projection (10%)

**Điểm mạnh:**
- Sử dụng JOIN FETCH hiệu quả để tránh N+1 problem
- Xử lý NULL và chuẩn hóa dữ liệu tốt
- Logic conflict detection chi tiết và chính xác

**Lưu ý khi maintain:**
- Các query có nhiều JOIN FETCH cần chú ý performance
- Conflict detection queries phức tạp, cần test kỹ
- Hydrate compatibility là workaround tạm thời, nên refactor sau

---

**Tài liệu được tạo tự động từ source code**  
**Ngày tạo:** 2024  
**Phiên bản:** 1.0
