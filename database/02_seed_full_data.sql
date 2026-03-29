-- ============================================================
-- FILE: 02_seed_full_data.sql
-- MUC DICH: Chen toan bo du lieu mau bang tieng Viet co dau.
-- Du lieu da bao gom cac truong dia chi va nhat quan username.
-- CHAY SAU: 01_create_schema.sql
-- MAT KHAU: Tat ca tai khoan dung mat khau "123456"
--           SHA-256: 8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92
-- ============================================================

SET NAMES utf8mb4;
-- Nạp dữ liệu mẫu cho các vai trò và nghiệp vụ chính của hệ thống.
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
    (1,  'admin',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Lê Văn Lân', 'admin@sms.edu.vn',   1, TRUE),
    (2,  'gv001',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Nguyễn Văn Minh',  'nv.minh@sms.edu.vn', 2, TRUE),
    (3,  'gv002',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Lê Thị Lan',       'lt.lan@sms.edu.vn',  2, TRUE),
    (4,  'gv003',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Trần Văn Hùng',    'tv.hung@sms.edu.vn', 2, TRUE),
    (5,  'sv2200001', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Trần Thị Mai',     'tt.mai@sms.edu.vn',  3, TRUE),
    (6,  'sv2200002', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Nguyễn Văn Nam',   'pv.nam@sms.edu.vn',  3, TRUE),
    (7,  'sv2200003', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Nguyễn Đặng Minh Khôi', 'nm.khoi@sms.edu.vn', 3, TRUE),
    (8,  'sv2200004', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Đỗ Thị Linh',      'dt.linh@sms.edu.vn', 3, TRUE),
    (9,  'sv2200005', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Đoàn Anh Tuấn',    'lm.tuan@sms.edu.vn', 3, TRUE),
    (10, 'sv2200006', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Ngô Minh Khánh',   'bt.hoa@sms.edu.vn',  3, TRUE),
    (11, 'gv004',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Phạm Quốc Bảo',    'pq.bao@sms.edu.vn',      2, TRUE),
    (12, 'gv005',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Võ Thị Hương',     'vt.huong@sms.edu.vn',    2, TRUE),
    (13, 'gv006',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Bùi Đức Long',     'bd.long@sms.edu.vn',     2, TRUE),
    (14, 'gv007',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Nguyễn Hoàng Anh', 'nh.anh@sms.edu.vn',      2, TRUE),
    (15, 'gv008',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Phạm Thị Thu',     'pt.thu@sms.edu.vn',      2, TRUE),
    (16, 'gv009',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Đỗ Quang Huy',     'dq.huy@sms.edu.vn',      2, TRUE),
    (17, 'gv010',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Lê Minh Tâm',      'lm.tam@sms.edu.vn',      2, TRUE),
    (18, 'gv011',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Trần Thị Bích',    'tt.bich@sms.edu.vn',     2, TRUE),
    (19, 'gv012',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Phan Ngọc Sơn',    'pn.son@sms.edu.vn',      2, TRUE),
    (20, 'gv013',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Hoàng Tuấn Kiệt',  'ht.kiet@sms.edu.vn',     2, TRUE),
    (21, 'gv014',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Nguyễn Thị Thanh', 'nt.thanh@sms.edu.vn',    2, TRUE),
    (22, 'gv015',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Đặng Gia Bảo',     'dg.bao@sms.edu.vn',      2, TRUE),
    (23, 'sv2200007', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Phạm Gia Huy',     'sv2200007@sms.edu.vn',   3, TRUE),
    (24, 'sv2200008', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Vũ Thị Ngọc Anh',  'sv2200008@sms.edu.vn',   3, TRUE),
    (25, 'sv2200009', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Lê Quốc Cường',    'sv2200009@sms.edu.vn',   3, TRUE),
    (26, 'sv2200010', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Trịnh Khánh Linh', 'sv2200010@sms.edu.vn',   3, TRUE),
    (27, 'sv2200011', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Phan Minh Đức',    'sv2200011@sms.edu.vn',   3, TRUE),
    (28, 'sv2200012', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Bùi Hải Yến',      'sv2200012@sms.edu.vn',   3, TRUE),
    (29, 'sv2200013', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Nguyễn Trung Hiếu','sv2200013@sms.edu.vn',   3, TRUE),
    (30, 'sv2200014', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Đặng Thu Hà',      'sv2200014@sms.edu.vn',   3, TRUE),
    (31, 'sv2200015', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Trần Đức Mạnh',    'sv2200015@sms.edu.vn',   3, TRUE),
    (32, 'sv2200016', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Hồ Ngọc Mai',      'sv2200016@sms.edu.vn',   3, TRUE),
    (33, 'sv2200017', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Võ Thành Công',    'sv2200017@sms.edu.vn',   3, TRUE),
    (34, 'sv2200018', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Lý Thu Trang',     'sv2200018@sms.edu.vn',   3, TRUE),
    (35, 'sv2200019', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Phạm Nhật Quang',  'sv2200019@sms.edu.vn',   3, TRUE),
    (36, 'sv2200020', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Nguyễn Diễm My',   'sv2200020@sms.edu.vn',   3, TRUE),
    (37, 'sv2200021', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Đỗ Hoài Nam',      'sv2200021@sms.edu.vn',   3, TRUE),
    (38, 'sv2200022', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Trương Khánh Vy',  'sv2200022@sms.edu.vn',   3, TRUE),
    (39, 'sv2200023', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Phùng Anh Dũng',   'sv2200023@sms.edu.vn',   3, TRUE),
    (40, 'sv2200024', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Mai Quỳnh Chi',    'sv2200024@sms.edu.vn',   3, TRUE),
    (41, 'sv2200025', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Nguyễn Đức Tài',   'sv2200025@sms.edu.vn',   3, TRUE),
    (42, 'sv2200026', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Lâm Bảo Trân',     'sv2200026@sms.edu.vn',   3, TRUE),
    (43, 'sv2200027', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Cao Minh Tiến',    'sv2200027@sms.edu.vn',   3, TRUE),
    (44, 'sv2200028', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Dương Ngọc Ánh',   'sv2200028@sms.edu.vn',   3, TRUE),
    (45, 'sv2200029', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Trần Gia Bảo',     'sv2200029@sms.edu.vn',   3, TRUE),
    (46, 'sv2200030', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Phạm Thảo Nhi',    'sv2200030@sms.edu.vn',   3, TRUE);

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
    (3, 4, 'GV003', 'Trần Văn Hùng',   'Nam', 'tv.hung@sms.edu.vn', '1984-11-21', '0901234569', '45 Lê Lợi, Hà Đông, Hà Nội', 1, 'ACTIVE'),
    (4, 11, 'GV004', 'Phạm Quốc Bảo',   'Nam', 'pq.bao@sms.edu.vn',   '1987-01-17', '0901234570', '18 Trần Phú, Ba Đình, Hà Nội', 1, 'ACTIVE'),
    (5, 12, 'GV005', 'Võ Thị Hương',    'Nữ',  'vt.huong@sms.edu.vn', '1989-06-24', '0901234571', '27 Hồ Tùng Mậu, Cầu Giấy, Hà Nội', 1, 'ACTIVE'),
    (6, 13, 'GV006', 'Bùi Đức Long',    'Nam', 'bd.long@sms.edu.vn',  '1985-12-05', '0901234572', '3 Lê Văn Lương, Thanh Xuân, Hà Nội', 1, 'ACTIVE'),
    (7, 14, 'GV007', 'Nguyễn Hoàng Anh','Nam', 'nh.anh@sms.edu.vn',   '1988-09-09', '0901234573', '52 Nguyễn Chí Thanh, Đống Đa, Hà Nội', 2, 'ACTIVE'),
    (8, 15, 'GV008', 'Phạm Thị Thu',    'Nữ',  'pt.thu@sms.edu.vn',   '1990-02-14', '0901234574', '91 Xuân Thủy, Cầu Giấy, Hà Nội', 2, 'ACTIVE'),
    (9, 16, 'GV009', 'Đỗ Quang Huy',    'Nam', 'dq.huy@sms.edu.vn',   '1987-07-30', '0901234575', '16 Phạm Hùng, Nam Từ Liêm, Hà Nội', 2, 'ACTIVE'),
    (10, 17, 'GV010', 'Lê Minh Tâm',    'Nam', 'lm.tam@sms.edu.vn',   '1986-04-18', '0901234576', '210 Trần Phú, Hà Đông, Hà Nội', 3, 'ACTIVE'),
    (11, 18, 'GV011', 'Trần Thị Bích',  'Nữ',  'tt.bich@sms.edu.vn',  '1991-10-02', '0901234577', '44 Quang Trung, Hà Đông, Hà Nội', 3, 'ACTIVE'),
    (12, 19, 'GV012', 'Phan Ngọc Sơn',  'Nam', 'pn.son@sms.edu.vn',   '1985-05-11', '0901234578', '8 Nguyễn Xiển, Thanh Xuân, Hà Nội', 3, 'ACTIVE'),
    (13, 20, 'GV013', 'Hoàng Tuấn Kiệt','Nam', 'ht.kiet@sms.edu.vn',  '1984-08-26', '0901234579', '15 Phố Vọng, Hai Bà Trưng, Hà Nội', 4, 'ACTIVE'),
    (14, 21, 'GV014', 'Nguyễn Thị Thanh','Nữ', 'nt.thanh@sms.edu.vn', '1989-01-05', '0901234580', '102 Tây Sơn, Đống Đa, Hà Nội', 4, 'ACTIVE'),
    (15, 22, 'GV015', 'Đặng Gia Bảo',   'Nam', 'dg.bao@sms.edu.vn',   '1987-03-19', '0901234581', '67 Lạc Long Quân, Tây Hồ, Hà Nội', 4, 'ACTIVE');

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
    (6, 10, 'SV2200006', 'Ngô Minh Khánh',            'Nữ',  '2004-02-14', 'bt.hoa@sms.edu.vn',      '0912000006', 'Phố Huế, Hai Bà Trưng, Hà Nội', 1, 1, '2022 - 2026', 'ACTIVE'),
    (7, 23, 'SV2200007', 'Phạm Gia Huy',              'Nam', '2004-04-11', 'sv2200007@sms.edu.vn',   '0912000007', 'Số 25 Trần Duy Hưng, Cầu Giấy, Hà Nội', 1, 1, '2022 - 2026', 'ACTIVE'),
    (8, 24, 'SV2200008', 'Vũ Thị Ngọc Anh',           'Nữ',  '2004-06-03', 'sv2200008@sms.edu.vn',   '0912000008', 'Số 9 Phạm Ngọc Thạch, Đống Đa, Hà Nội', 1, 1, '2022 - 2026', 'ACTIVE'),
    (9, 25, 'SV2200009', 'Lê Quốc Cường',             'Nam', '2004-01-19', 'sv2200009@sms.edu.vn',   '0912000009', 'Số 63 Nguyễn Chí Thanh, Đống Đa, Hà Nội', 1, 1, '2022 - 2026', 'ACTIVE'),
    (10, 26, 'SV2200010', 'Trịnh Khánh Linh',         'Nữ',  '2004-09-08', 'sv2200010@sms.edu.vn',   '0912000010', 'Số 18 Hồ Tùng Mậu, Cầu Giấy, Hà Nội', 1, 1, '2022 - 2026', 'ACTIVE'),
    (11, 27, 'SV2200011', 'Phan Minh Đức',            'Nam', '2004-12-12', 'sv2200011@sms.edu.vn',   '0912000011', 'Số 7 Nguyễn Khang, Cầu Giấy, Hà Nội', 1, 1, '2022 - 2026', 'ACTIVE'),
    (12, 28, 'SV2200012', 'Bùi Hải Yến',              'Nữ',  '2004-03-27', 'sv2200012@sms.edu.vn',   '0912000012', 'Số 42 Lê Văn Lương, Thanh Xuân, Hà Nội', 1, 1, '2022 - 2026', 'ACTIVE'),
    (13, 29, 'SV2200013', 'Nguyễn Trung Hiếu',        'Nam', '2004-05-14', 'sv2200013@sms.edu.vn',   '0912000013', 'Số 21 Khuất Duy Tiến, Thanh Xuân, Hà Nội', 1, 2, '2022 - 2026', 'ACTIVE'),
    (14, 30, 'SV2200014', 'Đặng Thu Hà',              'Nữ',  '2004-10-01', 'sv2200014@sms.edu.vn',   '0912000014', 'Số 11 Nguyễn Tuân, Thanh Xuân, Hà Nội', 1, 2, '2022 - 2026', 'ACTIVE'),
    (15, 31, 'SV2200015', 'Trần Đức Mạnh',            'Nam', '2004-07-22', 'sv2200015@sms.edu.vn',   '0912000015', 'Số 30 Mạc Thái Tổ, Cầu Giấy, Hà Nội', 1, 2, '2022 - 2026', 'ACTIVE'),
    (16, 32, 'SV2200016', 'Hồ Ngọc Mai',              'Nữ',  '2004-02-06', 'sv2200016@sms.edu.vn',   '0912000016', 'Số 4 Dương Đình Nghệ, Cầu Giấy, Hà Nội', 1, 2, '2022 - 2026', 'ACTIVE'),
    (17, 33, 'SV2200017', 'Võ Thành Công',            'Nam', '2004-11-17', 'sv2200017@sms.edu.vn',   '0912000017', 'Số 88 Trung Kính, Cầu Giấy, Hà Nội', 1, 2, '2022 - 2026', 'ACTIVE'),
    (18, 34, 'SV2200018', 'Lý Thu Trang',             'Nữ',  '2004-08-29', 'sv2200018@sms.edu.vn',   '0912000018', 'Số 14 Nguyễn Hữu Thọ, Hoàng Mai, Hà Nội', 1, 2, '2022 - 2026', 'ACTIVE'),
    (19, 35, 'SV2200019', 'Phạm Nhật Quang',          'Nam', '2004-04-25', 'sv2200019@sms.edu.vn',   '0912000019', 'Số 6 Trần Phú, Hà Đông, Hà Nội', 2, 3, '2022 - 2026', 'ACTIVE'),
    (20, 36, 'SV2200020', 'Nguyễn Diễm My',           'Nữ',  '2004-01-31', 'sv2200020@sms.edu.vn',   '0912000020', 'Số 10 Quang Trung, Hà Đông, Hà Nội', 2, 3, '2022 - 2026', 'ACTIVE'),
    (21, 37, 'SV2200021', 'Đỗ Hoài Nam',              'Nam', '2004-06-18', 'sv2200021@sms.edu.vn',   '0912000021', 'Số 55 Nguyễn Trãi, Hà Đông, Hà Nội', 2, 3, '2022 - 2026', 'ACTIVE'),
    (22, 38, 'SV2200022', 'Trương Khánh Vy',          'Nữ',  '2004-09-12', 'sv2200022@sms.edu.vn',   '0912000022', 'Số 39 Tô Hiệu, Hà Đông, Hà Nội', 2, 3, '2022 - 2026', 'ACTIVE'),
    (23, 39, 'SV2200023', 'Phùng Anh Dũng',           'Nam', '2004-12-02', 'sv2200023@sms.edu.vn',   '0912000023', 'Số 5 Lê Lợi, Hà Đông, Hà Nội', 2, 3, '2022 - 2026', 'ACTIVE'),
    (24, 40, 'SV2200024', 'Mai Quỳnh Chi',            'Nữ',  '2004-03-09', 'sv2200024@sms.edu.vn',   '0912000024', 'Số 28 Thanh Bình, Hà Đông, Hà Nội', 2, 3, '2022 - 2026', 'ACTIVE'),
    (25, 41, 'SV2200025', 'Nguyễn Đức Tài',           'Nam', '2004-05-06', 'sv2200025@sms.edu.vn',   '0912000025', 'Số 13 Ngô Quyền, Hà Đông, Hà Nội', 3, 4, '2022 - 2026', 'ACTIVE'),
    (26, 42, 'SV2200026', 'Lâm Bảo Trân',             'Nữ',  '2004-10-20', 'sv2200026@sms.edu.vn',   '0912000026', 'Số 73 Phùng Hưng, Hà Đông, Hà Nội', 3, 4, '2022 - 2026', 'ACTIVE'),
    (27, 43, 'SV2200027', 'Cao Minh Tiến',            'Nam', '2004-07-03', 'sv2200027@sms.edu.vn',   '0912000027', 'Số 16 Tân Triều, Thanh Trì, Hà Nội', 3, 4, '2022 - 2026', 'ACTIVE'),
    (28, 44, 'SV2200028', 'Dương Ngọc Ánh',           'Nữ',  '2004-02-28', 'sv2200028@sms.edu.vn',   '0912000028', 'Số 101 Chiến Thắng, Thanh Trì, Hà Nội', 3, 4, '2022 - 2026', 'ACTIVE'),
    (29, 45, 'SV2200029', 'Trần Gia Bảo',             'Nam', '2004-08-15', 'sv2200029@sms.edu.vn',   '0912000029', 'Số 22 Phùng Khoang, Nam Từ Liêm, Hà Nội', 3, 4, '2022 - 2026', 'ACTIVE'),
    (30, 46, 'SV2200030', 'Phạm Thảo Nhi',            'Nữ',  '2004-11-06', 'sv2200030@sms.edu.vn',   '0912000030', 'Số 58 Nguyễn Xiển, Thanh Xuân, Hà Nội', 3, 4, '2022 - 2026', 'ACTIVE');

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
    (1, 'INT101-01', 1, 1, 'HK1', '2024-2025', 40),
    (2, 'INT102-01', 2, 4, 'HK1', '2024-2025', 40),
    (3, 'INT103-01', 3, 5, 'HK1', '2024-2025', 40),
    (4, 'INT104-01', 4, 6, 'HK1', '2024-2025', 40),
    (5, 'BUS101-01', 5, 7, 'HK1', '2024-2025', 40),
    (6, 'BUS102-01', 6, 8, 'HK1', '2024-2025', 40);

-- ------------------------------------------------------------
-- 10. LICH HOC (schedules)
-- ------------------------------------------------------------
INSERT INTO schedules (id, course_section_id, day_of_week, start_period, end_period, room_id, note)
VALUES
    (1, 1, 'Thứ Hai', 1, 3, 1, 'Lịch chính thức học phần Nhập môn Lập trình Java'),
    (2, 2, 'Thứ Ba', 1, 3, 2, 'Lịch chính thức học phần Cơ sở Dữ liệu'),
    (3, 3, 'Thứ Tư', 1, 3, 3, 'Lịch chính thức học phần Lập trình Hướng đối tượng'),
    (4, 4, 'Thứ Năm', 1, 3, 4, 'Lịch chính thức học phần Mạng Máy tính'),
    (5, 5, 'Thứ Sáu', 1, 3, 6, 'Lịch chính thức học phần Nguyên lý Quản trị'),
    (6, 6, 'Thứ Bảy', 1, 3, 7, 'Lịch chính thức học phần Marketing Căn bản');

-- ------------------------------------------------------------
-- 11. DANG KY HOC PHAN (enrollments)
-- ------------------------------------------------------------
INSERT INTO enrollments (id, student_id, course_section_id, status)
SELECT 1 + ((s.id - 1) * 3), s.id, MOD(s.id - 1, 6) + 1, 'REGISTERED'
FROM students s
WHERE s.id BETWEEN 1 AND 30
UNION ALL
SELECT 2 + ((s.id - 1) * 3), s.id, MOD(s.id, 6) + 1, 'REGISTERED'
FROM students s
WHERE s.id BETWEEN 1 AND 30
UNION ALL
SELECT 3 + ((s.id - 1) * 3), s.id, MOD(s.id + 1, 6) + 1, 'REGISTERED'
FROM students s
WHERE s.id BETWEEN 1 AND 30;

-- ------------------------------------------------------------
-- 12. DIEM SO (scores)
-- ------------------------------------------------------------
INSERT INTO scores (id, enrollment_id, process_score, midterm_score, final_score, total_score, result)
SELECT
    t.enrollment_id,
    t.enrollment_id,
    t.process_score,
    t.midterm_score,
    t.final_score,
    ROUND(t.process_score * 0.3 + t.midterm_score * 0.2 + t.final_score * 0.5, 2) AS total_score,
    CASE
        WHEN ROUND(t.process_score * 0.3 + t.midterm_score * 0.2 + t.final_score * 0.5, 2) >= 5.0 THEN 'PASS'
        ELSE 'FAIL'
    END AS result
FROM (
    SELECT
        e.id AS enrollment_id,
        ROUND(3.5 + MOD(e.id * 3, 8) * 0.70, 2) AS process_score,
        ROUND(3.0 + MOD(e.id * 5, 8) * 0.75, 2) AS midterm_score,
        ROUND(3.5 + MOD(e.id * 7, 8) * 0.80, 2) AS final_score
    FROM enrollments e
    WHERE e.id BETWEEN 1 AND 90
) t;
