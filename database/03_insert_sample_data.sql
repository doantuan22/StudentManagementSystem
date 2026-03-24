-- 03_insert_sample_data.sql
-- Du lieu mau de test dang nhap, phan quyen, CRUD, lich hoc, dang ky hoc phan, diem va bao cao.

USE student_management;

-- Mat khau test chung cho admin / lecturer01 / lecturer02 / student01 / student02 / student03 la 123456
-- SHA-256(123456) = 8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92

INSERT INTO roles(role_code, role_name)
VALUES ('ADMIN', 'Quan tri vien'),
       ('LECTURER', 'Giang vien'),
       ('STUDENT', 'Sinh vien')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

INSERT INTO users(username, password_hash, full_name, email, role_id, active)
SELECT 'admin',
       '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',
       'Quan tri he thong',
       'admin@sms.local',
       r.id,
       TRUE
FROM roles r
WHERE r.role_code = 'ADMIN'
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    full_name = VALUES(full_name),
    email = VALUES(email),
    role_id = VALUES(role_id),
    active = VALUES(active);

INSERT INTO users(username, password_hash, full_name, email, role_id, active)
SELECT 'lecturer01',
       '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',
       'Nguyen Van Lecturer',
       'lecturer01@sms.local',
       r.id,
       TRUE
FROM roles r
WHERE r.role_code = 'LECTURER'
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    full_name = VALUES(full_name),
    email = VALUES(email),
    role_id = VALUES(role_id),
    active = VALUES(active);

INSERT INTO users(username, password_hash, full_name, email, role_id, active)
SELECT 'lecturer02',
       '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',
       'Le Thi Lecturer',
       'lecturer02@sms.local',
       r.id,
       TRUE
FROM roles r
WHERE r.role_code = 'LECTURER'
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    full_name = VALUES(full_name),
    email = VALUES(email),
    role_id = VALUES(role_id),
    active = VALUES(active);

INSERT INTO users(username, password_hash, full_name, email, role_id, active)
SELECT 'student01',
       '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',
       'Tran Thi Student',
       'student01@sms.local',
       r.id,
       TRUE
FROM roles r
WHERE r.role_code = 'STUDENT'
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    full_name = VALUES(full_name),
    email = VALUES(email),
    role_id = VALUES(role_id),
    active = VALUES(active);

INSERT INTO users(username, password_hash, full_name, email, role_id, active)
SELECT 'student02',
       '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',
       'Pham Van Student',
       'student02@sms.local',
       r.id,
       TRUE
FROM roles r
WHERE r.role_code = 'STUDENT'
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    full_name = VALUES(full_name),
    email = VALUES(email),
    role_id = VALUES(role_id),
    active = VALUES(active);

INSERT INTO users(username, password_hash, full_name, email, role_id, active)
SELECT 'student03',
       '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',
       'Ngo Minh Student',
       'student03@sms.local',
       r.id,
       TRUE
FROM roles r
WHERE r.role_code = 'STUDENT'
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    full_name = VALUES(full_name),
    email = VALUES(email),
    role_id = VALUES(role_id),
    active = VALUES(active);

INSERT INTO faculties(faculty_code, faculty_name, description)
VALUES ('CNTT', 'Cong nghe thong tin', 'Khoa mau de test luong chuc nang chinh'),
       ('QTKD', 'Quan tri kinh doanh', 'Khoa mau phuc vu bao cao va thong ke')
ON DUPLICATE KEY UPDATE
    faculty_name = VALUES(faculty_name),
    description = VALUES(description);

INSERT INTO class_rooms(class_code, class_name, academic_year, faculty_id)
SELECT 'D17CQCN01-N', 'Lop Cong nghe thong tin 1', '2025-2026', f.id
FROM faculties f
WHERE f.faculty_code = 'CNTT'
ON DUPLICATE KEY UPDATE
    class_name = VALUES(class_name),
    academic_year = VALUES(academic_year),
    faculty_id = VALUES(faculty_id);

INSERT INTO class_rooms(class_code, class_name, academic_year, faculty_id)
SELECT 'D17CQCN02-N', 'Lop Cong nghe thong tin 2', '2025-2026', f.id
FROM faculties f
WHERE f.faculty_code = 'CNTT'
ON DUPLICATE KEY UPDATE
    class_name = VALUES(class_name),
    academic_year = VALUES(academic_year),
    faculty_id = VALUES(faculty_id);

INSERT INTO class_rooms(class_code, class_name, academic_year, faculty_id)
SELECT 'D17QTKD01-N', 'Lop Quan tri kinh doanh 1', '2025-2026', f.id
FROM faculties f
WHERE f.faculty_code = 'QTKD'
ON DUPLICATE KEY UPDATE
    class_name = VALUES(class_name),
    academic_year = VALUES(academic_year),
    faculty_id = VALUES(faculty_id);

INSERT INTO lecturers(user_id, lecturer_code, full_name, email, phone, faculty_id, status)
SELECT u.id, 'GV001', 'Nguyen Van Lecturer', 'lecturer01@sms.local', '0900000001', f.id, 'ACTIVE'
FROM users u
JOIN faculties f ON f.faculty_code = 'CNTT'
WHERE u.username = 'lecturer01'
ON DUPLICATE KEY UPDATE
    full_name = VALUES(full_name),
    email = VALUES(email),
    phone = VALUES(phone),
    faculty_id = VALUES(faculty_id),
    status = VALUES(status);

INSERT INTO lecturers(user_id, lecturer_code, full_name, email, phone, faculty_id, status)
SELECT u.id, 'GV002', 'Le Thi Lecturer', 'lecturer02@sms.local', '0900000003', f.id, 'ACTIVE'
FROM users u
JOIN faculties f ON f.faculty_code = 'QTKD'
WHERE u.username = 'lecturer02'
ON DUPLICATE KEY UPDATE
    full_name = VALUES(full_name),
    email = VALUES(email),
    phone = VALUES(phone),
    faculty_id = VALUES(faculty_id),
    status = VALUES(status);

INSERT INTO students(user_id, student_code, full_name, gender, date_of_birth, email, phone, faculty_id, class_room_id, status)
SELECT u.id, 'SV001', 'Tran Thi Student', 'Nu', '2005-05-20', 'student01@sms.local', '0900000002', f.id, c.id, 'ACTIVE'
FROM users u
JOIN faculties f ON f.faculty_code = 'CNTT'
JOIN class_rooms c ON c.class_code = 'D17CQCN01-N'
WHERE u.username = 'student01'
ON DUPLICATE KEY UPDATE
    full_name = VALUES(full_name),
    gender = VALUES(gender),
    date_of_birth = VALUES(date_of_birth),
    email = VALUES(email),
    phone = VALUES(phone),
    faculty_id = VALUES(faculty_id),
    class_room_id = VALUES(class_room_id),
    status = VALUES(status);

INSERT INTO students(user_id, student_code, full_name, gender, date_of_birth, email, phone, faculty_id, class_room_id, status)
SELECT u.id, 'SV002', 'Pham Van Student', 'Nam', '2005-03-15', 'student02@sms.local', '0900000004', f.id, c.id, 'ACTIVE'
FROM users u
JOIN faculties f ON f.faculty_code = 'CNTT'
JOIN class_rooms c ON c.class_code = 'D17CQCN01-N'
WHERE u.username = 'student02'
ON DUPLICATE KEY UPDATE
    full_name = VALUES(full_name),
    gender = VALUES(gender),
    date_of_birth = VALUES(date_of_birth),
    email = VALUES(email),
    phone = VALUES(phone),
    faculty_id = VALUES(faculty_id),
    class_room_id = VALUES(class_room_id),
    status = VALUES(status);

INSERT INTO students(user_id, student_code, full_name, gender, date_of_birth, email, phone, faculty_id, class_room_id, status)
SELECT u.id, 'SV003', 'Ngo Minh Student', 'Nam', '2005-08-01', 'student03@sms.local', '0900000005', f.id, c.id, 'ACTIVE'
FROM users u
JOIN faculties f ON f.faculty_code = 'QTKD'
JOIN class_rooms c ON c.class_code = 'D17QTKD01-N'
WHERE u.username = 'student03'
ON DUPLICATE KEY UPDATE
    full_name = VALUES(full_name),
    gender = VALUES(gender),
    date_of_birth = VALUES(date_of_birth),
    email = VALUES(email),
    phone = VALUES(phone),
    faculty_id = VALUES(faculty_id),
    class_room_id = VALUES(class_room_id),
    status = VALUES(status);

INSERT INTO subjects(subject_code, subject_name, credits, faculty_id, description)
SELECT 'INT101', 'Nhap mon lap trinh Java', 3, f.id, 'Mon hoc mau de test quan ly mon hoc va hoc phan'
FROM faculties f
WHERE f.faculty_code = 'CNTT'
ON DUPLICATE KEY UPDATE
    subject_name = VALUES(subject_name),
    credits = VALUES(credits),
    faculty_id = VALUES(faculty_id),
    description = VALUES(description);

INSERT INTO subjects(subject_code, subject_name, credits, faculty_id, description)
SELECT 'INT102', 'Co so du lieu', 3, f.id, 'Mon hoc mau de test dang ky hoc phan va trung lich'
FROM faculties f
WHERE f.faculty_code = 'CNTT'
ON DUPLICATE KEY UPDATE
    subject_name = VALUES(subject_name),
    credits = VALUES(credits),
    faculty_id = VALUES(faculty_id),
    description = VALUES(description);

INSERT INTO subjects(subject_code, subject_name, credits, faculty_id, description)
SELECT 'BUS101', 'Nguyen ly quan tri', 2, f.id, 'Mon hoc mau cua khoa QTKD'
FROM faculties f
WHERE f.faculty_code = 'QTKD'
ON DUPLICATE KEY UPDATE
    subject_name = VALUES(subject_name),
    credits = VALUES(credits),
    faculty_id = VALUES(faculty_id),
    description = VALUES(description);

INSERT IGNORE INTO lecturer_subjects(lecturer_id, subject_id)
SELECT l.id, s.id
FROM lecturers l
JOIN subjects s ON s.subject_code = 'INT101'
WHERE l.lecturer_code = 'GV001';

INSERT IGNORE INTO lecturer_subjects(lecturer_id, subject_id)
SELECT l.id, s.id
FROM lecturers l
JOIN subjects s ON s.subject_code = 'INT102'
WHERE l.lecturer_code = 'GV001';

INSERT IGNORE INTO lecturer_subjects(lecturer_id, subject_id)
SELECT l.id, s.id
FROM lecturers l
JOIN subjects s ON s.subject_code = 'BUS101'
WHERE l.lecturer_code = 'GV002';

INSERT INTO course_sections(section_code, subject_id, lecturer_id, class_room_id, semester, school_year, schedule_text, max_students)
SELECT 'INT101-01', s.id, l.id, c.id, 'HK1', '2025-2026', 'Thu 2 tiet 1-3 phong A101', 60
FROM subjects s
JOIN lecturers l ON l.lecturer_code = 'GV001'
JOIN class_rooms c ON c.class_code = 'D17CQCN01-N'
WHERE s.subject_code = 'INT101'
ON DUPLICATE KEY UPDATE
    subject_id = VALUES(subject_id),
    lecturer_id = VALUES(lecturer_id),
    class_room_id = VALUES(class_room_id),
    semester = VALUES(semester),
    school_year = VALUES(school_year),
    schedule_text = VALUES(schedule_text),
    max_students = VALUES(max_students);

INSERT INTO course_sections(section_code, subject_id, lecturer_id, class_room_id, semester, school_year, schedule_text, max_students)
SELECT 'INT102-01', s.id, l.id, c.id, 'HK1', '2025-2026', 'Thu 4 tiet 4-6 phong B201', 40
FROM subjects s
JOIN lecturers l ON l.lecturer_code = 'GV001'
JOIN class_rooms c ON c.class_code = 'D17CQCN01-N'
WHERE s.subject_code = 'INT102'
ON DUPLICATE KEY UPDATE
    subject_id = VALUES(subject_id),
    lecturer_id = VALUES(lecturer_id),
    class_room_id = VALUES(class_room_id),
    semester = VALUES(semester),
    school_year = VALUES(school_year),
    schedule_text = VALUES(schedule_text),
    max_students = VALUES(max_students);

INSERT INTO course_sections(section_code, subject_id, lecturer_id, class_room_id, semester, school_year, schedule_text, max_students)
SELECT 'INT102-02', s.id, l.id, c.id, 'HK1', '2025-2026', 'Thu 2 tiet 2-4 phong B202', 30
FROM subjects s
JOIN lecturers l ON l.lecturer_code = 'GV001'
JOIN class_rooms c ON c.class_code = 'D17CQCN02-N'
WHERE s.subject_code = 'INT102'
ON DUPLICATE KEY UPDATE
    subject_id = VALUES(subject_id),
    lecturer_id = VALUES(lecturer_id),
    class_room_id = VALUES(class_room_id),
    semester = VALUES(semester),
    school_year = VALUES(school_year),
    schedule_text = VALUES(schedule_text),
    max_students = VALUES(max_students);

INSERT INTO course_sections(section_code, subject_id, lecturer_id, class_room_id, semester, school_year, schedule_text, max_students)
SELECT 'BUS101-01', s.id, l.id, c.id, 'HK1', '2025-2026', 'Thu 3 tiet 1-3 phong C301', 50
FROM subjects s
JOIN lecturers l ON l.lecturer_code = 'GV002'
JOIN class_rooms c ON c.class_code = 'D17QTKD01-N'
WHERE s.subject_code = 'BUS101'
ON DUPLICATE KEY UPDATE
    subject_id = VALUES(subject_id),
    lecturer_id = VALUES(lecturer_id),
    class_room_id = VALUES(class_room_id),
    semester = VALUES(semester),
    school_year = VALUES(school_year),
    schedule_text = VALUES(schedule_text),
    max_students = VALUES(max_students);

INSERT INTO schedules(course_section_id, day_of_week, start_period, end_period, room, note)
SELECT cs.id, 'Thu 2', 1, 3, 'A101', 'Lich hoc chinh cua hoc phan Java'
FROM course_sections cs
WHERE cs.section_code = 'INT101-01'
ON DUPLICATE KEY UPDATE
    end_period = VALUES(end_period),
    room = VALUES(room),
    note = VALUES(note);

INSERT INTO schedules(course_section_id, day_of_week, start_period, end_period, room, note)
SELECT cs.id, 'Thu 4', 4, 6, 'B201', 'Hoc phan CSDL khong trung lich voi INT101-01'
FROM course_sections cs
WHERE cs.section_code = 'INT102-01'
ON DUPLICATE KEY UPDATE
    end_period = VALUES(end_period),
    room = VALUES(room),
    note = VALUES(note);

INSERT INTO schedules(course_section_id, day_of_week, start_period, end_period, room, note)
SELECT cs.id, 'Thu 2', 2, 4, 'B202', 'Hoc phan nay co chu y de test chan trung lich'
FROM course_sections cs
WHERE cs.section_code = 'INT102-02'
ON DUPLICATE KEY UPDATE
    end_period = VALUES(end_period),
    room = VALUES(room),
    note = VALUES(note);

INSERT INTO schedules(course_section_id, day_of_week, start_period, end_period, room, note)
SELECT cs.id, 'Thu 3', 1, 3, 'C301', 'Hoc phan khoa QTKD'
FROM course_sections cs
WHERE cs.section_code = 'BUS101-01'
ON DUPLICATE KEY UPDATE
    end_period = VALUES(end_period),
    room = VALUES(room),
    note = VALUES(note);

INSERT INTO enrollments(student_id, course_section_id, status, enrolled_at)
SELECT st.id, cs.id, 'REGISTERED', CURRENT_TIMESTAMP
FROM students st
JOIN course_sections cs ON cs.section_code = 'INT101-01'
WHERE st.student_code = 'SV001'
ON DUPLICATE KEY UPDATE
    status = VALUES(status);

INSERT INTO enrollments(student_id, course_section_id, status, enrolled_at)
SELECT st.id, cs.id, 'REGISTERED', CURRENT_TIMESTAMP
FROM students st
JOIN course_sections cs ON cs.section_code = 'INT102-01'
WHERE st.student_code = 'SV001'
ON DUPLICATE KEY UPDATE
    status = VALUES(status);

INSERT INTO enrollments(student_id, course_section_id, status, enrolled_at)
SELECT st.id, cs.id, 'REGISTERED', CURRENT_TIMESTAMP
FROM students st
JOIN course_sections cs ON cs.section_code = 'INT101-01'
WHERE st.student_code = 'SV002'
ON DUPLICATE KEY UPDATE
    status = VALUES(status);

INSERT INTO enrollments(student_id, course_section_id, status, enrolled_at)
SELECT st.id, cs.id, 'REGISTERED', CURRENT_TIMESTAMP
FROM students st
JOIN course_sections cs ON cs.section_code = 'BUS101-01'
WHERE st.student_code = 'SV003'
ON DUPLICATE KEY UPDATE
    status = VALUES(status);

INSERT INTO scores(enrollment_id, process_score, midterm_score, final_score, total_score, result)
SELECT e.id,
       8.0,
       7.0,
       9.0,
       ROUND(8.0 * 0.3 + 7.0 * 0.2 + 9.0 * 0.5, 2),
       CASE WHEN ROUND(8.0 * 0.3 + 7.0 * 0.2 + 9.0 * 0.5, 2) >= 5.0 THEN 'PASS' ELSE 'FAIL' END
FROM enrollments e
JOIN students st ON st.id = e.student_id
JOIN course_sections cs ON cs.id = e.course_section_id
WHERE st.student_code = 'SV001'
  AND cs.section_code = 'INT101-01'
ON DUPLICATE KEY UPDATE
    process_score = VALUES(process_score),
    midterm_score = VALUES(midterm_score),
    final_score = VALUES(final_score),
    total_score = VALUES(total_score),
    result = VALUES(result);

INSERT INTO scores(enrollment_id, process_score, midterm_score, final_score, total_score, result)
SELECT e.id,
       6.0,
       6.5,
       7.0,
       ROUND(6.0 * 0.3 + 6.5 * 0.2 + 7.0 * 0.5, 2),
       CASE WHEN ROUND(6.0 * 0.3 + 6.5 * 0.2 + 7.0 * 0.5, 2) >= 5.0 THEN 'PASS' ELSE 'FAIL' END
FROM enrollments e
JOIN students st ON st.id = e.student_id
JOIN course_sections cs ON cs.id = e.course_section_id
WHERE st.student_code = 'SV002'
  AND cs.section_code = 'INT101-01'
ON DUPLICATE KEY UPDATE
    process_score = VALUES(process_score),
    midterm_score = VALUES(midterm_score),
    final_score = VALUES(final_score),
    total_score = VALUES(total_score),
    result = VALUES(result);

INSERT INTO scores(enrollment_id, process_score, midterm_score, final_score, total_score, result)
SELECT e.id,
       5.0,
       5.5,
       4.5,
       ROUND(5.0 * 0.3 + 5.5 * 0.2 + 4.5 * 0.5, 2),
       CASE WHEN ROUND(5.0 * 0.3 + 5.5 * 0.2 + 4.5 * 0.5, 2) >= 5.0 THEN 'PASS' ELSE 'FAIL' END
FROM enrollments e
JOIN students st ON st.id = e.student_id
JOIN course_sections cs ON cs.id = e.course_section_id
WHERE st.student_code = 'SV003'
  AND cs.section_code = 'BUS101-01'
ON DUPLICATE KEY UPDATE
    process_score = VALUES(process_score),
    midterm_score = VALUES(midterm_score),
    final_score = VALUES(final_score),
    total_score = VALUES(total_score),
    result = VALUES(result);
