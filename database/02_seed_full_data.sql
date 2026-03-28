-- ============================================================
-- FILE: 02_seed_full_data.sql
-- MUC DICH: Chen toan bo du lieu mau bang tieng Viet co dau.
-- Du lieu da bao gom cac truong dia chi va nhat quan username.
-- CHAY SAU: 01_create_schema.sql
-- MAT KHAU: Tat ca tai khoan dung mat khau "123456"
--           SHA-256: 8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92
-- ============================================================

SET NAMES utf8mb4;
USE student_management;

-- ------------------------------------------------------------
-- 1. VAI TRO (roles)
-- ------------------------------------------------------------
INSERT INTO roles (id, role_code, role_name)
VALUES (1, 'ADMIN',    'Quản trị viên'),
       (2, 'LECTURER', 'Giảng viên'),
       (3, 'STUDENT',  'Sinh viên');

-- ------------------------------------------------------------
-- 2. NGUOI DUNG (users)
-- Mat khau mac dinh: 123456
-- ------------------------------------------------------------
INSERT INTO users (id, username, password_hash, full_name, email, role_id, active)
VALUES
    (1,  'admin',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Nguyễn Hoàng Anh', 'admin@sms.edu.vn',   1, TRUE),
    (2,  'gv001',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Nguyễn Văn Minh',  'nv.minh@sms.edu.vn', 2, TRUE),
    (3,  'gv002',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Lê Thị Lan',       'lt.lan@sms.edu.vn',  2, TRUE),
    (4,  'gv003',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Trần Văn Hùng',    'tv.hung@sms.edu.vn', 2, TRUE),
    (5,  'sv2200001', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Trần Thị Mai',     'tt.mai@sms.edu.vn',  3, TRUE),
    (6,  'sv2200002', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Nguyễn Văn Nam',   'pv.nam@sms.edu.vn',  3, TRUE),
    (7,  'sv2200003', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Nguyễn Đặng Minh Khôi', 'nm.khoi@sms.edu.vn', 3, TRUE),
    (8,  'sv2200004', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Đỗ Thị Linh',      'dt.linh@sms.edu.vn', 3, TRUE),
    (9,  'sv2200005', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Đoàn Anh Tuấn',    'lm.tuan@sms.edu.vn', 3, TRUE),
    (10, 'sv2200006', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Ngô Minh Khánh',   'bt.hoa@sms.edu.vn',  3, TRUE);

-- ------------------------------------------------------------
-- 3. KHOA (faculties)
-- ------------------------------------------------------------
INSERT INTO faculties (id, faculty_code, faculty_name, description)
VALUES
    (1, 'CNTT', 'Công nghệ Thông tin', 'Khoa đào tạo kỹ sư phần mềm, mạng máy tính và an toàn thông tin'),
    (2, 'QTKD', 'Quản trị Kinh doanh', 'Khoa đào tạo cử nhân quản trị kinh doanh và marketing'),
    (3, 'DTVT', 'Điện tử Viễn thông',  'Khoa đào tạo kỹ sư điện tử và viễn thông'),
    (4, 'KTCK', 'Kỹ thuật Cơ khí',     'Khoa đào tạo kỹ sư cơ khí và tự động hóa');

-- ------------------------------------------------------------
-- 4. PHONG HOC (rooms)
-- ------------------------------------------------------------
INSERT INTO rooms (id, room_code, room_name)
VALUES
    (1, 'A101', 'Phòng máy tính A101'),
    (2, 'A102', 'Phòng học lý thuyết A102'),
    (3, 'A103', 'Phòng học lý thuyết A103'),
    (4, 'B201', 'Phòng thực hành B201'),
    (5, 'B202', 'Phòng thực hành B202'),
    (6, 'C301', 'Hội trường đa năng C301'),
    (7, 'C302', 'Phòng hội thảo C302'),
    (8, 'D401', 'Phòng học cao học D401');

-- ------------------------------------------------------------
-- 5. LOP HANH CHINH (class_rooms)
-- ------------------------------------------------------------
INSERT INTO class_rooms (id, class_code, class_name, academic_year, faculty_id)
VALUES
    (1, 'D17CQCN01-N', 'Lớp Công nghệ Thông tin 01', '2022 - 2026', 1),
    (2, 'D17CQCN02-N', 'Lớp Công nghệ Thông tin 02', '2022 - 2026', 1),
    (3, 'D17QTKD01-N', 'Lớp Quản trị Kinh doanh 01', '2022 - 2026', 2),
    (4, 'D17DTVT01-N', 'Lớp Điện tử Viễn thông 01',  '2022 - 2026', 3);

-- ------------------------------------------------------------
-- 6. GIANG VIEN (lecturers)
-- ------------------------------------------------------------
INSERT INTO lecturers (id, user_id, lecturer_code, full_name, gender, email, date_of_birth, phone, address, faculty_id, status)
VALUES
    (1, 2, 'GV001', 'Nguyễn Văn Minh', 'Nam', 'nv.minh@sms.edu.vn', '1986-09-12', '0901234567', '99 Nguyễn Trãi, Thanh Xuân, Hà Nội', 1, 'ACTIVE'),
    (2, 3, 'GV002', 'Lê Thị Lan',      'Nữ',  'lt.lan@sms.edu.vn',  '1988-03-08', '0901234568', '12 Cầu Giấy, Hà Nội', 2, 'ACTIVE'),
    (3, 4, 'GV003', 'Trần Văn Hùng',   'Nam', 'tv.hung@sms.edu.vn', '1984-11-21', '0901234569', '45 Lê Lợi, Hà Đông, Hà Nội', 1, 'ACTIVE');

-- ------------------------------------------------------------
-- 7. SINH VIEN (students)
-- ------------------------------------------------------------
INSERT INTO students (id, user_id, student_code, full_name, gender, date_of_birth, email, phone, address, faculty_id, class_room_id, academic_year, status)
VALUES
    (1, 5,  'SV2200001', 'Trần Thị Mai',              'Nữ',  '2004-05-20', 'tt.mai@sms.edu.vn',  '0912000001', 'Số 1 Đại Cồ Việt, Hai Bà Trưng, Hà Nội', 1, 1, '2022 - 2026', 'ACTIVE'),
    (2, 6,  'SV2200002', 'Nguyễn Văn Nam',            'Nam', '2004-03-15', 'pv.nam@sms.edu.vn',  '0912000002', 'Ngõ 10 Tôn Thất Tùng, Đống Đa, Hà Nội', 1, 1, '2022 - 2026', 'ACTIVE'),
    (3, 7,  'SV2200003', 'Nguyễn Đặng Minh Khôi',     'Nam', '2004-08-01', 'nm.khoi@sms.edu.vn', '0912000003', 'Thôn 5, Đông Anh, Hà Nội', 2, 3, '2022 - 2026', 'ACTIVE'),
    (4, 8,  'SV2200004', 'Đỗ Thị Linh',               'Nữ',  '2004-11-25', 'dt.linh@sms.edu.vn', '0912000004', 'Mỹ Đình, Nam Từ Liêm, Hà Nội', 1, 2, '2022 - 2026', 'ACTIVE'),
    (5, 9,  'SV2200005', 'Đoàn Anh Tuấn',             'Nam', '2004-07-10', 'lm.tuan@sms.edu.vn', '0912000005', 'Phường Mộ Lao, Hà Đông, Hà Nội', 3, 4, '2022 - 2026', 'ACTIVE'),
    (6, 10, 'SV2200006', 'Ngô Minh Khánh',            'Nữ',  '2004-02-14', 'bt.hoa@sms.edu.vn',  '0912000006', 'Phố Huế, Hai Bà Trưng, Hà Nội', 1, 1, '2022 - 2026', 'ACTIVE');

-- ------------------------------------------------------------
-- 8. MON HOC (subjects)
-- ------------------------------------------------------------
INSERT INTO subjects (id, subject_code, subject_name, credits, faculty_id, description)
VALUES
    (1, 'INT101', 'Nhập môn Lập trình Java', 3, 1, 'Môn học cơ bản về ngôn ngữ lập trình Java'),
    (2, 'INT102', 'Cơ sở Dữ liệu',           3, 1, 'Lý thuyết và thực hành thiết kế cơ sở dữ liệu'),
    (3, 'INT103', 'Lập trình Hướng đối tượng', 3, 1, 'Các nguyên lý OOP với Java'),
    (4, 'INT104', 'Mạng Máy tính',           3, 1, 'Kiến trúc và giao thức mạng máy tính'),
    (5, 'BUS101', 'Nguyên lý Quản trị',      2, 2, 'Các nguyên lý cơ bản trong quản trị doanh nghiệp'),
    (6, 'BUS102', 'Marketing Căn bản',       2, 2, 'Tổng quan về marketing và hành vi tiêu dùng');

-- ------------------------------------------------------------
-- 9. HOC PHAN MO (course_sections)
-- ------------------------------------------------------------
INSERT INTO course_sections (id, section_code, subject_id, lecturer_id, semester, school_year, max_students)
VALUES
    (1, 'INT101-01', 1, 1, 'HK1', '2024-2025', 60),
    (2, 'INT102-01', 2, 1, 'HK1', '2024-2025', 40),
    (3, 'INT102-02', 2, 1, 'HK1', '2024-2025', 35),
    (4, 'BUS101-01', 5, 2, 'HK1', '2024-2025', 50),
    (5, 'INT101-02', 1, 3, 'HK1', '2024-2025', 55);

-- ------------------------------------------------------------
-- 10. LICH HOC (schedules)
-- ------------------------------------------------------------
INSERT INTO schedules (id, course_section_id, day_of_week, start_period, end_period, room_id, note)
VALUES
    (1, 1, 'Thứ Hai', 1, 3, 1, 'Lịch chính thức học phần Nhập môn Java - Nhóm 01'),
    (2, 2, 'Thứ Tư',  4, 6, 4, 'Lịch chính thức học phần Cơ sở Dữ liệu - Nhóm 01'),
    (3, 3, 'Thứ Hai', 4, 6, 5, 'Lịch chính thức học phần Cơ sở Dữ liệu - Nhóm 02'),
    (4, 4, 'Thứ Ba',  1, 3, 6, 'Lịch chính thức học phần Nguyên lý Quản trị'),
    (5, 5, 'Thứ Năm', 1, 3, 2, 'Lịch chính thức học phần Nhập môn Java - Nhóm 02');

-- ------------------------------------------------------------
-- 11. DANG KY HOC PHAN (enrollments)
-- ------------------------------------------------------------
INSERT INTO enrollments (id, student_id, course_section_id, status)
VALUES
    (1, 1, 1, 'REGISTERED'),
    (2, 1, 2, 'REGISTERED'),
    (3, 2, 1, 'REGISTERED'),
    (4, 3, 4, 'REGISTERED'),
    (5, 4, 5, 'REGISTERED');

-- ------------------------------------------------------------
-- 12. DIEM SO (scores)
-- ------------------------------------------------------------
INSERT INTO scores (id, enrollment_id, process_score, midterm_score, final_score, total_score, result)
VALUES
    (1, 1, 8.0, 7.5, 9.0, 8.4, 'PASS'),
    (2, 2, 7.0, 6.5, 7.5, 7.15, 'PASS'),
    (3, 3, 7.5, 6.0, 7.0, 6.95, 'PASS'),
    (4, 4, 4.0, 4.5, 4.0, 4.10, 'FAIL'),
    (5, 5, 9.0, 8.5, 9.5, 9.15, 'PASS');
