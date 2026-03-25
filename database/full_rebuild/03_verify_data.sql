-- Truy van kiem tra nhanh bo schema va du lieu sau khi import.

USE student_management;

SELECT DATABASE() AS current_database;

SELECT 'roles' AS object_name, COUNT(*) AS total_rows FROM roles
UNION ALL
SELECT 'users', COUNT(*) FROM users
UNION ALL
SELECT 'faculties', COUNT(*) FROM faculties
UNION ALL
SELECT 'class_rooms', COUNT(*) FROM class_rooms
UNION ALL
SELECT 'students', COUNT(*) FROM students
UNION ALL
SELECT 'lecturers', COUNT(*) FROM lecturers
UNION ALL
SELECT 'subjects', COUNT(*) FROM subjects
UNION ALL
SELECT 'lecturer_subjects', COUNT(*) FROM lecturer_subjects
UNION ALL
SELECT 'course_sections', COUNT(*) FROM course_sections
UNION ALL
SELECT 'schedules', COUNT(*) FROM schedules
UNION ALL
SELECT 'enrollments', COUNT(*) FROM enrollments
UNION ALL
SELECT 'scores', COUNT(*) FROM scores
UNION ALL
SELECT 'vw_student_schedules', COUNT(*) FROM vw_student_schedules
UNION ALL
SELECT 'vw_section_scores', COUNT(*) FROM vw_section_scores;

SELECT username, full_name, email, active
FROM users
ORDER BY id;

SELECT student_code, full_name, gender, academic_year, status
FROM students
ORDER BY student_code;

SELECT lecturer_code, full_name, faculty_id, status
FROM lecturers
ORDER BY lecturer_code;

SELECT section_code, room, semester, school_year, schedule_text
FROM course_sections
ORDER BY section_code;

SELECT *
FROM vw_student_schedules
ORDER BY student_code, day_of_week, start_period;

SELECT *
FROM vw_section_scores
ORDER BY section_code, student_code;
