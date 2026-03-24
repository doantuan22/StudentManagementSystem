-- 07_create_indexes.sql
-- Cac index co ban cho truy van CRUD va bao cao.

USE student_management;

CREATE INDEX idx_students_class_room ON students(class_room_id);
CREATE INDEX idx_students_faculty ON students(faculty_id);
CREATE INDEX idx_lecturers_faculty ON lecturers(faculty_id);
CREATE INDEX idx_subjects_faculty ON subjects(faculty_id);
CREATE INDEX idx_course_sections_lecturer ON course_sections(lecturer_id);
CREATE INDEX idx_course_sections_class_room ON course_sections(class_room_id);
CREATE INDEX idx_enrollments_section ON enrollments(course_section_id);
CREATE INDEX idx_enrollments_student ON enrollments(student_id);
CREATE INDEX idx_scores_enrollment ON scores(enrollment_id);
CREATE INDEX idx_schedules_section_day ON schedules(course_section_id, day_of_week, start_period, end_period);
