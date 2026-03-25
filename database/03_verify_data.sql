-- ============================================================
-- FILE: 03_verify_data.sql
-- MUC DICH: Kiem tra du lieu sau khi chay 01 + 02
-- CHAY SAU: 02_seed_full_data.sql
-- ============================================================

USE student_management;

SET NAMES utf8mb4;

-- Kiem tra so luong ban ghi moi bang
SELECT 'roles'           AS bang, COUNT(*) AS so_luong FROM roles
UNION ALL
SELECT 'users',         COUNT(*) FROM users
UNION ALL
SELECT 'faculties',     COUNT(*) FROM faculties
UNION ALL
SELECT 'class_rooms',   COUNT(*) FROM class_rooms
UNION ALL
SELECT 'rooms',         COUNT(*) FROM rooms
UNION ALL
SELECT 'lecturers',     COUNT(*) FROM lecturers
UNION ALL
SELECT 'students',      COUNT(*) FROM students
UNION ALL
SELECT 'subjects',      COUNT(*) FROM subjects
UNION ALL
SELECT 'lecturer_subjects', COUNT(*) FROM lecturer_subjects
UNION ALL
SELECT 'course_sections',   COUNT(*) FROM course_sections
UNION ALL
SELECT 'schedules',     COUNT(*) FROM schedules
UNION ALL
SELECT 'enrollments',   COUNT(*) FROM enrollments
UNION ALL
SELECT 'scores',        COUNT(*) FROM scores;

-- Kiem tra xem tat ca hoc phan co room_id hop le khong
SELECT cs.id, cs.section_code, r.room_code, r.room_name
FROM course_sections cs
JOIN rooms r ON r.id = cs.room_id
ORDER BY cs.id;

-- Kiem tra tat ca lich hoc co room_id hop le khong
SELECT sc.id, sc.day_of_week, sc.start_period, sc.end_period, r.room_code
FROM schedules sc
JOIN rooms r ON r.id = sc.room_id
ORDER BY sc.id;

-- Kiem tra view lich hoc sinh vien
SELECT * FROM vw_student_schedules LIMIT 10;

-- Kiem tra view diem hoc phan
SELECT * FROM vw_section_scores LIMIT 10;
