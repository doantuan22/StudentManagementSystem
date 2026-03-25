-- ============================================================
-- FILE: 02_seed_full_data.sql
-- MUC DICH: Chèn toàn bộ dữ liệu mẫu bằng tiếng Việt có dấu
--           cho He thong Quan ly Sinh vien.
-- CHAY SAU: 01_create_schema.sql
-- MAT KHAU: Tất cả tài khoản dùng mật khẩu "123456"
--           SHA-256: 8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92
-- ============================================================

SET NAMES utf8mb4;

USE student_management;

-- ============================================================
-- 1. VAI TRO (roles)
-- ============================================================

INSERT INTO roles (id, role_code, role_name)
VALUES (1, 'ADMIN',    'Quản trị viên'),
       (2, 'LECTURER', 'Giảng viên'),
       (3, 'STUDENT',  'Sinh viên');

-- ============================================================
-- 2. NGƯỜI DÙNG (users)
-- Mật khẩu mặc định: 123456 (SHA-256)
-- ============================================================

INSERT INTO users (id, username, password_hash, full_name, email, role_id, active, created_at)
VALUES
    (1,  'admin',       '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Nguyễn Hoàng Anh',   'admin@sms.edu.vn',      1, TRUE, '2024-08-01 08:00:00'),
    (2,  'gv.minh',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Nguyễn Văn Minh',    'nv.minh@sms.edu.vn',    2, TRUE, '2024-08-01 08:05:00'),
    (3,  'gv.lan',      '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Lê Thị Lan',         'lt.lan@sms.edu.vn',     2, TRUE, '2024-08-01 08:10:00'),
    (4,  'gv.hung',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Trần Văn Hùng',      'tv.hung@sms.edu.vn',    2, TRUE, '2024-08-01 08:15:00'),
    (5,  'sv.mai',      '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Trần Thị Mai',       'tt.mai@sms.edu.vn',     3, TRUE, '2024-08-05 09:00:00'),
    (6,  'sv.nam',      '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Phạm Văn Nam',       'pv.nam@sms.edu.vn',     3, TRUE, '2024-08-05 09:05:00'),
    (7,  'sv.khoi',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Ngô Minh Khôi',      'nm.khoi@sms.edu.vn',    3, TRUE, '2024-08-05 09:10:00'),
    (8,  'sv.linh',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Đỗ Thị Linh',        'dt.linh@sms.edu.vn',    3, TRUE, '2024-08-05 09:15:00'),
    (9,  'sv.tuan',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Lý Minh Tuấn',       'lm.tuan@sms.edu.vn',    3, TRUE, '2024-08-05 09:20:00'),
    (10, 'sv.hoa',      '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Bùi Thị Hoa',        'bt.hoa@sms.edu.vn',     3, TRUE, '2024-08-05 09:25:00');

-- ============================================================
-- 3. KHOA (faculties)
-- ============================================================

INSERT INTO faculties (id, faculty_code, faculty_name, description, created_at)
VALUES
    (1, 'CNTT',  'Công nghệ Thông tin',       'Khoa đào tạo kỹ sư phần mềm, mạng máy tính và an toàn thông tin', '2024-07-01 08:00:00'),
    (2, 'QTKD',  'Quản trị Kinh doanh',       'Khoa đào tạo cử nhân quản trị kinh doanh và marketing',           '2024-07-01 08:05:00'),
    (3, 'DTVT',  'Điện tử Viễn thông',        'Khoa đào tạo kỹ sư điện tử và viễn thông',                        '2024-07-01 08:10:00'),
    (4, 'KTCK',  'Kỹ thuật Cơ khí',           'Khoa đào tạo kỹ sư cơ khí và tự động hóa',                        '2024-07-01 08:15:00');

-- ============================================================
-- 4. PHÒNG HỌC (rooms)
-- ============================================================

INSERT INTO rooms (id, room_code, room_name, created_at)
VALUES
    (1,  'A101',    'Phòng máy tính A101',           '2024-07-01 09:00:00'),
    (2,  'A102',    'Phòng học lý thuyết A102',      '2024-07-01 09:05:00'),
    (3,  'A103',    'Phòng học lý thuyết A103',      '2024-07-01 09:10:00'),
    (4,  'B201',    'Phòng thực hành B201',           '2024-07-01 09:15:00'),
    (5,  'B202',    'Phòng thực hành B202',           '2024-07-01 09:20:00'),
    (6,  'C301',    'Hội trường đa năng C301',        '2024-07-01 09:25:00'),
    (7,  'C302',    'Phòng hội thảo C302',            '2024-07-01 09:30:00'),
    (8,  'D401',    'Phòng học cao học D401',         '2024-07-01 09:35:00');

-- ============================================================
-- 5. LỚP HÀNH CHÍNH (class_rooms)
-- ============================================================

INSERT INTO class_rooms (id, class_code, class_name, academic_year, faculty_id, created_at)
VALUES
    (1, 'D17CQCN01-N', 'Lớp Công nghệ Thông tin 01',       '2022 - 2026', 1, '2024-07-02 08:00:00'),
    (2, 'D17CQCN02-N', 'Lớp Công nghệ Thông tin 02',       '2022 - 2026', 1, '2024-07-02 08:05:00'),
    (3, 'D17QTKD01-N', 'Lớp Quản trị Kinh doanh 01',       '2022 - 2026', 2, '2024-07-02 08:10:00'),
    (4, 'D17DTVT01-N', 'Lớp Điện tử Viễn thông 01',        '2022 - 2026', 3, '2024-07-02 08:15:00'),
    (5, 'D18CQCN01-N', 'Lớp Công nghệ Thông tin 03',       '2023 - 2027', 1, '2024-07-02 08:20:00'),
    (6, 'D18QTKD01-N', 'Lớp Quản trị Kinh doanh 02',       '2023 - 2027', 2, '2024-07-02 08:25:00');

-- ============================================================
-- 6. GIẢNG VIÊN (lecturers)
-- ============================================================

INSERT INTO lecturers (id, user_id, lecturer_code, full_name, email, phone, faculty_id, status, created_at)
VALUES
    (1, 2, 'GV001', 'Nguyễn Văn Minh',  'nv.minh@sms.edu.vn',  '0901 234 567', 1, 'ACTIVE', '2024-07-10 08:00:00'),
    (2, 3, 'GV002', 'Lê Thị Lan',        'lt.lan@sms.edu.vn',   '0901 234 568', 2, 'ACTIVE', '2024-07-10 08:05:00'),
    (3, 4, 'GV003', 'Trần Văn Hùng',    'tv.hung@sms.edu.vn',   '0901 234 569', 1, 'ACTIVE', '2024-07-10 08:10:00');

-- ============================================================
-- 7. SINH VIÊN (students)
-- ============================================================

INSERT INTO students (id, user_id, student_code, full_name, gender, date_of_birth, email, phone, faculty_id, class_room_id, academic_year, status, created_at)
VALUES
    (1, 5,  'SV2200001', 'Trần Thị Mai',       'Nữ',  '2004-05-20', 'tt.mai@sms.edu.vn',    '0912 000 001', 1, 1, '2022 - 2026', 'ACTIVE', '2024-08-20 09:00:00'),
    (2, 6,  'SV2200002', 'Phạm Văn Nam',       'Nam', '2004-03-15', 'pv.nam@sms.edu.vn',    '0912 000 002', 1, 1, '2022 - 2026', 'ACTIVE', '2024-08-20 09:05:00'),
    (3, 7,  'SV2200003', 'Ngô Minh Khôi',      'Nam', '2004-08-01', 'nm.khoi@sms.edu.vn',   '0912 000 003', 2, 3, '2022 - 2026', 'ACTIVE', '2024-08-20 09:10:00'),
    (4, 8,  'SV2200004', 'Đỗ Thị Linh',        'Nữ',  '2004-11-25', 'dt.linh@sms.edu.vn',   '0912 000 004', 1, 2, '2022 - 2026', 'ACTIVE', '2024-08-20 09:15:00'),
    (5, 9,  'SV2200005', 'Lý Minh Tuấn',       'Nam', '2004-07-10', 'lm.tuan@sms.edu.vn',   '0912 000 005', 3, 4, '2022 - 2026', 'ACTIVE', '2024-08-20 09:20:00'),
    (6, 10, 'SV2200006', 'Bùi Thị Hoa',        'Nữ',  '2004-02-14', 'bt.hoa@sms.edu.vn',    '0912 000 006', 1, 1, '2022 - 2026', 'ACTIVE', '2024-08-20 09:25:00');

-- ============================================================
-- 8. MÔN HỌC (subjects)
-- ============================================================

INSERT INTO subjects (id, subject_code, subject_name, credits, faculty_id, description, created_at)
VALUES
    (1, 'INT101', 'Nhập môn Lập trình Java',         3, 1, 'Môn học cơ bản về ngôn ngữ lập trình Java',              '2024-07-15 08:00:00'),
    (2, 'INT102', 'Cơ sở Dữ liệu',                   3, 1, 'Lý thuyết và thực hành thiết kế cơ sở dữ liệu',          '2024-07-15 08:05:00'),
    (3, 'INT103', 'Lập trình Hướng đối tượng',        3, 1, 'Các nguyên lý OOP với Java',                             '2024-07-15 08:10:00'),
    (4, 'INT104', 'Mạng Máy tính',                    3, 1, 'Kiến trúc và giao thức mạng máy tính',                   '2024-07-15 08:15:00'),
    (5, 'BUS101', 'Nguyên lý Quản trị',               2, 2, 'Các nguyên lý cơ bản trong quản trị doanh nghiệp',       '2024-07-15 08:20:00'),
    (6, 'BUS102', 'Marketing Căn bản',                 2, 2, 'Tổng quan về marketing và hành vi tiêu dùng',            '2024-07-15 08:25:00'),
    (7, 'ETE101', 'Điện tử Cơ bản',                   3, 3, 'Lý thuyết mạch điện và linh kiện điện tử cơ bản',        '2024-07-15 08:30:00');

-- ============================================================
-- 9. PHÂN CÔNG GIẢNG VIÊN - MÔN HỌC (lecturer_subjects)
-- ============================================================

INSERT INTO lecturer_subjects (lecturer_id, subject_id)
VALUES
    (1, 1),  -- GV Nguyễn Văn Minh dạy INT101
    (1, 2),  -- GV Nguyễn Văn Minh dạy INT102
    (1, 3),  -- GV Nguyễn Văn Minh dạy INT103
    (2, 5),  -- GV Lê Thị Lan dạy BUS101
    (2, 6),  -- GV Lê Thị Lan dạy BUS102
    (3, 1),  -- GV Trần Văn Hùng dạy INT101
    (3, 4);  -- GV Trần Văn Hùng dạy INT104

-- ============================================================
-- 10. HỌC PHẦN MỞ (course_sections)
-- room_id tham chiếu bảng rooms
-- ============================================================

INSERT INTO course_sections (id, section_code, subject_id, lecturer_id, room_id, semester, school_year, schedule_text, max_students, created_at)
VALUES
    (1, 'INT101-01', 1, 1, 1, 'HK1', '2024-2025', 'Thứ Hai - Tiết 1-3 - Phòng A101', 60, '2024-08-25 08:00:00'),
    (2, 'INT102-01', 2, 1, 4, 'HK1', '2024-2025', 'Thứ Tư - Tiết 4-6 - Phòng B201',  40, '2024-08-25 08:05:00'),
    (3, 'INT102-02', 2, 1, 5, 'HK1', '2024-2025', 'Thứ Hai - Tiết 2-4 - Phòng B202', 35, '2024-08-25 08:10:00'),
    (4, 'BUS101-01', 5, 2, 6, 'HK1', '2024-2025', 'Thứ Ba - Tiết 1-3 - Phòng C301',  50, '2024-08-25 08:15:00'),
    (5, 'INT101-02', 1, 3, 2, 'HK1', '2024-2025', 'Thứ Năm - Tiết 1-3 - Phòng A102', 55, '2024-08-25 08:20:00'),
    (6, 'INT103-01', 3, 1, 1, 'HK1', '2024-2025', 'Thứ Sáu - Tiết 2-4 - Phòng A101', 45, '2024-08-25 08:25:00'),
    (7, 'BUS102-01', 6, 2, 3, 'HK1', '2024-2025', 'Thứ Năm - Tiết 4-6 - Phòng A103', 50, '2024-08-25 08:30:00'),
    (8, 'INT104-01', 4, 3, 7, 'HK1', '2024-2025', 'Thứ Hai - Tiết 7-9 - Phòng C302', 40, '2024-08-25 08:35:00');

-- ============================================================
-- 11. LỊCH HỌC (schedules)
-- room_id tham chiếu bảng rooms
-- ============================================================

INSERT INTO schedules (id, course_section_id, day_of_week, start_period, end_period, room_id, note, created_at)
VALUES
    (1, 1, 'Thứ Hai',  1, 3, 1, 'Lịch chính thức học phần Nhập môn Java - Nhóm 01',      '2024-08-26 08:00:00'),
    (2, 2, 'Thứ Tư',   4, 6, 4, 'Lịch chính thức học phần Cơ sở Dữ liệu - Nhóm 01',     '2024-08-26 08:05:00'),
    (3, 3, 'Thứ Hai',  2, 4, 5, 'Lịch chính thức học phần Cơ sở Dữ liệu - Nhóm 02',     '2024-08-26 08:10:00'),
    (4, 4, 'Thứ Ba',   1, 3, 6, 'Lịch chính thức học phần Nguyên lý Quản trị',            '2024-08-26 08:15:00'),
    (5, 5, 'Thứ Năm',  1, 3, 2, 'Lịch chính thức học phần Nhập môn Java - Nhóm 02',      '2024-08-26 08:20:00'),
    (6, 6, 'Thứ Sáu',  2, 4, 1, 'Lịch chính thức học phần Lập trình Hướng đối tượng',   '2024-08-26 08:25:00'),
    (7, 7, 'Thứ Năm',  4, 6, 3, 'Lịch chính thức học phần Marketing Căn bản',             '2024-08-26 08:30:00'),
    (8, 8, 'Thứ Hai',  7, 9, 7, 'Lịch chính thức học phần Mạng Máy tính',                 '2024-08-26 08:35:00');

-- ============================================================
-- 12. ĐĂNG KÝ HỌC PHẦN (enrollments)
-- ============================================================

INSERT INTO enrollments (id, student_id, course_section_id, status, enrolled_at)
VALUES
    -- Trần Thị Mai (SV1)
    (1,  1, 1, 'REGISTERED', '2024-09-01 09:00:00'),  -- INT101-01
    (2,  1, 2, 'REGISTERED', '2024-09-01 09:05:00'),  -- INT102-01
    (3,  1, 6, 'REGISTERED', '2024-09-01 09:10:00'),  -- INT103-01

    -- Phạm Văn Nam (SV2)
    (4,  2, 1, 'REGISTERED', '2024-09-01 09:15:00'),  -- INT101-01
    (5,  2, 2, 'REGISTERED', '2024-09-01 09:20:00'),  -- INT102-01

    -- Ngô Minh Khôi (SV3)
    (6,  3, 4, 'REGISTERED', '2024-09-01 09:25:00'),  -- BUS101-01
    (7,  3, 7, 'REGISTERED', '2024-09-01 09:30:00'),  -- BUS102-01

    -- Đỗ Thị Linh (SV4)
    (8,  4, 5, 'REGISTERED', '2024-09-01 09:35:00'),  -- INT101-02
    (9,  4, 3, 'REGISTERED', '2024-09-01 09:40:00'),  -- INT102-02

    -- Lý Minh Tuấn (SV5)
    (10, 5, 8, 'REGISTERED', '2024-09-01 09:45:00'),  -- INT104-01

    -- Bùi Thị Hoa (SV6)
    (11, 6, 1, 'REGISTERED', '2024-09-01 09:50:00'),  -- INT101-01
    (12, 6, 6, 'REGISTERED', '2024-09-01 09:55:00');  -- INT103-01

-- ============================================================
-- 13. ĐIỂM SỐ (scores)
-- Công thức: total = process*0.3 + midterm*0.2 + final*0.5
-- PASS khi total >= 5.0
-- ============================================================

INSERT INTO scores (id, enrollment_id, process_score, midterm_score, final_score, total_score, result, updated_at)
VALUES
    -- Trần Thị Mai: INT101-01 - Điểm tốt
    (1,  1,  8.00, 7.50, 9.00, 8.40, 'PASS', '2024-12-20 10:00:00'),
    -- Trần Thị Mai: INT102-01 - Điểm khá
    (2,  2,  7.00, 6.50, 7.50, 7.15, 'PASS', '2024-12-20 10:05:00'),
    -- Trần Thị Mai: INT103-01 - Điểm trung bình
    (3,  3,  6.00, 5.50, 6.00, 5.90, 'PASS', '2024-12-20 10:10:00'),

    -- Phạm Văn Nam: INT101-01 - Điểm khá
    (4,  4,  7.50, 6.00, 7.00, 6.95, 'PASS', '2024-12-20 10:15:00'),
    -- Phạm Văn Nam: INT102-01 - Điểm trung bình
    (5,  5,  5.00, 5.50, 5.00, 5.10, 'PASS', '2024-12-20 10:20:00'),

    -- Ngô Minh Khôi: BUS101-01 - Điểm yếu (trượt)
    (6,  6,  4.00, 4.50, 4.00, 4.10, 'FAIL', '2024-12-20 10:25:00'),
    -- Ngô Minh Khôi: BUS102-01 - Điểm trung bình
    (7,  7,  6.50, 5.50, 6.00, 6.05, 'PASS', '2024-12-20 10:30:00'),

    -- Đỗ Thị Linh: INT101-02 - Điểm giỏi
    (8,  8,  9.00, 8.50, 9.50, 9.15, 'PASS', '2024-12-20 10:35:00'),
    -- Đỗ Thị Linh: INT102-02 - Điểm khá
    (9,  9,  8.00, 7.00, 7.50, 7.55, 'PASS', '2024-12-20 10:40:00');
    -- Lý Minh Tuấn và Bùi Thị Hoa chưa có điểm (vẫn đang học)
