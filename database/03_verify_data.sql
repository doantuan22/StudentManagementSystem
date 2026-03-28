-- ============================================================
-- FILE: 03_verify_data.sql
-- MUC DICH: Kiem tra nhanh sau khi da dung xong database va seed.
-- ============================================================

USE student_management;

-- 1. Kiem tra so luong bang (Ky vong: 13-14 bang chinh)
SELECT 'Table Count' AS Metric, COUNT(*) AS Value
FROM information_schema.tables
WHERE table_schema = 'student_management' AND table_type = 'BASE TABLE';

-- 2. Kiem tra so luong Model quan trong
SELECT 'Roles' AS Entity, COUNT(*) AS Count FROM roles
UNION ALL
SELECT 'Users', COUNT(*) FROM users
UNION ALL
SELECT 'Faculties', COUNT(*) FROM faculties
UNION ALL
SELECT 'Students', COUNT(*) FROM students
UNION ALL
SELECT 'Lecturers', COUNT(*) FROM lecturers
UNION ALL
SELECT 'ClassRooms', COUNT(*) FROM class_rooms
UNION ALL
SELECT 'Subjects', COUNT(*) FROM subjects
UNION ALL
SELECT 'CourseSections', COUNT(*) FROM course_sections
UNION ALL
SELECT 'Schedules', COUNT(*) FROM schedules
UNION ALL
SELECT 'Enrollments', COUNT(*) FROM enrollments
UNION ALL
SELECT 'Scores', COUNT(*) FROM scores;

-- 3. Kiem tra tinh nhat quan User (Tat ca sinh vien/giang vien phai co User ID)
SELECT 'Lecturers without UserID' AS Issue, COUNT(*) AS Count FROM lecturers WHERE user_id IS NULL
UNION ALL
SELECT 'Students without UserID', COUNT(*) FROM students WHERE user_id IS NULL;

-- 4. Kiem tra su trung khop Full Name giua Users va Students/Lecturers
SELECT 'Mismatch Name (Lecturer)' AS Issue, COUNT(*) AS Count
FROM lecturers l JOIN users u ON l.user_id = u.id
WHERE l.full_name <> u.full_name
UNION ALL
SELECT 'Mismatch Name (Student)', COUNT(*)
FROM students s JOIN users u ON s.user_id = u.id
WHERE s.full_name <> u.full_name;

-- 4b. Kiem tra giang vien da co ngay sinh
SELECT 'Lecturers missing DOB' AS Issue, COUNT(*) AS Count
FROM lecturers
WHERE date_of_birth IS NULL;

-- 5. Kiem tra course_sections khong con luu phong/lich truc tiep
SELECT column_name
FROM information_schema.columns
WHERE table_schema = 'student_management'
  AND table_name = 'course_sections'
  AND column_name IN ('room_id', 'schedule_text');

-- 6. Kiem tra xung dot phong hoc (Ky vong: 0)
SELECT 'Room schedule conflicts' AS Issue, COUNT(*) AS Count
FROM (
    SELECT s1.id
    FROM schedules s1
    JOIN schedules s2
      ON s1.id < s2.id
     AND s1.room_id = s2.room_id
     AND s1.day_of_week = s2.day_of_week
     AND NOT (s1.end_period < s2.start_period OR s1.start_period > s2.end_period)
) room_conflicts;

-- 7. Kiem tra xung dot giang vien (Ky vong: 0)
SELECT 'Lecturer schedule conflicts' AS Issue, COUNT(*) AS Count
FROM (
    SELECT s1.id
    FROM schedules s1
    JOIN course_sections cs1 ON cs1.id = s1.course_section_id
    JOIN schedules s2
      ON s1.id < s2.id
     AND s1.day_of_week = s2.day_of_week
     AND NOT (s1.end_period < s2.start_period OR s1.start_period > s2.end_period)
    JOIN course_sections cs2 ON cs2.id = s2.course_section_id
    WHERE cs1.lecturer_id = cs2.lecturer_id
) lecturer_conflicts;

-- 8. Thu nghiem View
SELECT * FROM vw_student_schedules LIMIT 5;
SELECT * FROM vw_section_scores LIMIT 5;

-- 9. Kiem tra Trigger tu tao User
INSERT INTO lecturers (lecturer_code, full_name, faculty_id) VALUES ('GV_TEST', 'Test Trigger Lecturer', 1);

SELECT u.username, u.full_name, l.lecturer_code
FROM users u
JOIN lecturers l ON u.id = l.user_id
WHERE l.lecturer_code = 'GV_TEST';

-- Don dep test
DELETE FROM lecturers WHERE lecturer_code = 'GV_TEST';
DELETE FROM users WHERE username = 'gv_test';
