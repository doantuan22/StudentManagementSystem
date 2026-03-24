-- 06_create_views.sql
-- Cac view ho tro tra cuu nhanh va bao cao co ban.

USE student_management;

CREATE OR REPLACE VIEW vw_student_schedules AS
SELECT st.student_code,
       st.full_name AS student_name,
       cs.section_code,
       sb.subject_code,
       sb.subject_name,
       sc.day_of_week,
       sc.start_period,
       sc.end_period,
       sc.room
FROM enrollments e
JOIN students st ON st.id = e.student_id
JOIN course_sections cs ON cs.id = e.course_section_id
JOIN subjects sb ON sb.id = cs.subject_id
JOIN schedules sc ON sc.course_section_id = cs.id;

CREATE OR REPLACE VIEW vw_section_scores AS
SELECT cs.section_code,
       st.student_code,
       st.full_name AS student_name,
       COALESCE(s.process_score, 0) AS process_score,
       COALESCE(s.midterm_score, 0) AS midterm_score,
       COALESCE(s.final_score, 0) AS final_score,
       COALESCE(s.total_score, 0) AS total_score,
       COALESCE(s.result, 'FAIL') AS result
FROM enrollments e
JOIN students st ON st.id = e.student_id
JOIN course_sections cs ON cs.id = e.course_section_id
LEFT JOIN scores s ON s.enrollment_id = e.id;
