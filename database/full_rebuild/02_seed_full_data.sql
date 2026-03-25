-- Bo du lieu mau day du va lien ket cho schema hien tai.
-- Chi doi ten nguoi sang tieng Viet co dau, cac ma va gia tri nghiep vu khac duoc giu nguyen.

SET NAMES utf8mb4;

USE student_management;

-- Mat khau test chung cho admin / lecturer01 / lecturer02 / student01 / student02 / student03 la 123456
-- SHA-256(123456) = 8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92

INSERT INTO roles (id, role_code, role_name)
VALUES (1, 'ADMIN', 'Quan tri vien'),
       (2, 'LECTURER', 'Giang vien'),
       (3, 'STUDENT', 'Sinh vien');

INSERT INTO users (id, username, password_hash, full_name, email, role_id, active, created_at)
VALUES (1, 'admin', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Nguyễn Hoàng Anh', 'admin@sms.local', 1, TRUE, '2025-07-01 08:00:00'),
       (2, 'lecturer01', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Nguyễn Văn Minh', 'lecturer01@sms.local', 2, TRUE, '2025-07-01 08:05:00'),
       (3, 'lecturer02', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Lê Thị Lan', 'lecturer02@sms.local', 2, TRUE, '2025-07-01 08:10:00'),
       (4, 'student01', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Trần Thị Mai', 'student01@sms.local', 3, TRUE, '2025-07-01 08:15:00'),
       (5, 'student02', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Phạm Văn Nam', 'student02@sms.local', 3, TRUE, '2025-07-01 08:20:00'),
       (6, 'student03', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Ngô Minh Khôi', 'student03@sms.local', 3, TRUE, '2025-07-01 08:25:00');

INSERT INTO faculties (id, faculty_code, faculty_name, description, created_at)
VALUES (1, 'CNTT', 'Cong nghe thong tin', 'Khoa mau de test luong chuc nang chinh', '2025-07-01 09:00:00'),
       (2, 'QTKD', 'Quan tri kinh doanh', 'Khoa mau phuc vu bao cao va thong ke', '2025-07-01 09:05:00');

INSERT INTO class_rooms (id, class_code, class_name, academic_year, faculty_id, created_at)
VALUES (1, 'D17CQCN01-N', 'Lop Cong nghe thong tin 1', '2022 - 2026', 1, '2025-07-02 08:00:00'),
       (2, 'D17CQCN02-N', 'Lop Cong nghe thong tin 2', '2022 - 2026', 1, '2025-07-02 08:05:00'),
       (3, 'D17QTKD01-N', 'Lop Quan tri kinh doanh 1', '2022 - 2026', 2, '2025-07-02 08:10:00');

INSERT INTO lecturers (id, user_id, lecturer_code, full_name, email, phone, faculty_id, status, created_at)
VALUES (1, 2, 'GV001', 'Nguyễn Văn Minh', 'lecturer01@sms.local', '0900000001', 1, 'ACTIVE', '2025-07-03 08:00:00'),
       (2, 3, 'GV002', 'Lê Thị Lan', 'lecturer02@sms.local', '0900000003', 2, 'ACTIVE', '2025-07-03 08:05:00');

INSERT INTO students (id, user_id, student_code, full_name, gender, date_of_birth, email, phone, faculty_id, class_room_id, academic_year, status, created_at)
VALUES (1, 4, 'SV001', 'Trần Thị Mai', 'Nữ', '2005-05-20', 'student01@sms.local', '0900000002', 1, 1, '2022 - 2026', 'ACTIVE', '2025-07-03 09:00:00'),
       (2, 5, 'SV002', 'Phạm Văn Nam', 'Nam', '2005-03-15', 'student02@sms.local', '0900000004', 1, 1, '2022 - 2026', 'ACTIVE', '2025-07-03 09:05:00'),
       (3, 6, 'SV003', 'Ngô Minh Khôi', 'Nam', '2005-08-01', 'student03@sms.local', '0900000005', 2, 3, '2022 - 2026', 'ACTIVE', '2025-07-03 09:10:00');

INSERT INTO subjects (id, subject_code, subject_name, credits, faculty_id, description, created_at)
VALUES (1, 'INT101', 'Nhap mon lap trinh Java', 3, 1, 'Mon hoc mau de test quan ly mon hoc va hoc phan', '2025-07-04 08:00:00'),
       (2, 'INT102', 'Co so du lieu', 3, 1, 'Mon hoc mau de test dang ky hoc phan va trung lich', '2025-07-04 08:05:00'),
       (3, 'BUS101', 'Nguyen ly quan tri', 2, 2, 'Mon hoc mau cua khoa QTKD', '2025-07-04 08:10:00');

INSERT INTO lecturer_subjects (lecturer_id, subject_id)
VALUES (1, 1),
       (1, 2),
       (2, 3);

INSERT INTO course_sections (id, section_code, subject_id, lecturer_id, room, semester, school_year, schedule_text, max_students, created_at)
VALUES (1, 'INT101-01', 1, 1, 'A101', 'HK1', '2025-2026', 'Thu 2 tiet 1-3 phong A101', 60, '2025-08-15 08:00:00'),
       (2, 'INT102-01', 2, 1, 'B201', 'HK1', '2025-2026', 'Thu 4 tiet 4-6 phong B201', 40, '2025-08-15 08:05:00'),
       (3, 'INT102-02', 2, 1, 'B202', 'HK1', '2025-2026', 'Thu 2 tiet 2-4 phong B202', 30, '2025-08-15 08:10:00'),
       (4, 'BUS101-01', 3, 2, 'C301', 'HK1', '2025-2026', 'Thu 3 tiet 1-3 phong C301', 50, '2025-08-15 08:15:00');

INSERT INTO schedules (id, course_section_id, day_of_week, start_period, end_period, room, note, created_at)
VALUES (1, 1, 'Thu 2', 1, 3, 'A101', 'Lich hoc chinh cua hoc phan Java', '2025-08-16 08:00:00'),
       (2, 2, 'Thu 4', 4, 6, 'B201', 'Hoc phan CSDL khong trung lich voi INT101-01', '2025-08-16 08:05:00'),
       (3, 3, 'Thu 2', 2, 4, 'B202', 'Hoc phan nay co chu y de test chan trung lich', '2025-08-16 08:10:00'),
       (4, 4, 'Thu 3', 1, 3, 'C301', 'Hoc phan khoa QTKD', '2025-08-16 08:15:00');

INSERT INTO enrollments (id, student_id, course_section_id, status, enrolled_at)
VALUES (1, 1, 1, 'REGISTERED', '2025-09-01 09:00:00'),
       (2, 1, 2, 'REGISTERED', '2025-09-01 09:05:00'),
       (3, 2, 1, 'REGISTERED', '2025-09-01 09:10:00'),
       (4, 3, 4, 'REGISTERED', '2025-09-01 09:15:00');

INSERT INTO scores (id, enrollment_id, process_score, midterm_score, final_score, total_score, result, updated_at)
VALUES (1, 1, 8.00, 7.00, 9.00, 8.30, 'PASS', '2025-11-20 10:00:00'),
       (2, 3, 6.00, 6.50, 7.00, 6.60, 'PASS', '2025-11-20 10:05:00'),
       (3, 4, 5.00, 5.50, 4.50, 4.85, 'FAIL', '2025-11-20 10:10:00');
