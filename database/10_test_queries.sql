-- 10_test_queries.sql
-- Truy van mau de test nhanh du lieu sau khi import database.

USE student_management;

SELECT * FROM roles ORDER BY id;
SELECT username, full_name, active FROM users ORDER BY id;
SELECT student_code, full_name, email FROM students ORDER BY student_code;
SELECT lecturer_code, full_name, email FROM lecturers ORDER BY lecturer_code;
SELECT section_code, semester, school_year, schedule_text FROM course_sections ORDER BY section_code;
SELECT * FROM vw_student_schedules ORDER BY student_code, day_of_week, start_period;
SELECT * FROM vw_section_scores ORDER BY section_code, student_code;
