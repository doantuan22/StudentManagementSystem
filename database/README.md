# Database Scripts

Thư mục `database/` chứa các file SQL chính thức để thiết lập hệ thống cơ sở dữ liệu cho đồ án (MySQL 8).

## Bộ file SQL chính thức (Thứ tự chạy)

Hãy chạy các file theo đúng thứ tự đánh số dưới đây để đảm bảo tính toàn vẹn dữ liệu:

1. **`00_drop_old_database.sql`**
   - Xóa database cũ `student_management`. Dùng khi muốn reset sạch toàn bộ hệ thống.
   - **CẢNH BÁO**: Sẽ mất toàn bộ dữ liệu hiện tại.

2. **`01_create_schema.sql`**
   - Tạo database, toàn bộ bảng, khóa ngoại, index và view.
   - **Đặc biệt**: Đã tích hợp các Trigger tự động tạo tài khoản User khi thêm Sinh viên/Giảng viên và tự động đồng bộ họ tên giữa các bảng.

3. **`02_seed_full_data.sql`**
   - Chèn dữ liệu mẫu đầy đủ (Roles, Users, Faculties, Classes, Rooms, Students, Lecturers, Subjects, Course Sections, Enrollments, Scores).
   - Mật khẩu mặc định cho tất cả tài khoản: `123456`.

4. **`03_verify_data.sql`**
   - Các câu lệnh truy vấn để kiểm tra nhanh tình trạng database sau khi dựng.

## Lưu ý về tài khoản Demo

Sau khi chạy xong bộ script trên, bạn có thể dùng các tài khoản sau để test:
- **Quản trị viên**: `admin` / `123456`
- **Giảng viên**: `gv001`, `gv002`, `gv003` / `123456`
- **Sinh viên**: `sv2200001`, `sv2200002`, `sv2200003`, ... / `123456`

*(Lưu ý: Username của Sinh viên/Giảng viên được chuẩn hóa theo mã số viết thường)*

## Cấu hình Kết nối (application.properties)

Mặc định project sử dụng:
- **URL**: `jdbc:mysql://localhost:3306/student_management`
- **User**: `root`
- **Pass**: `123456` (Hoặc chỉnh lại theo máy cá nhân trong file config).

---
*Các file có tiền tố `obsolete_` là các bản vá cũ đã được hợp nhất vào bộ script chính thức, không nên sử dụng đơn lẻ.*
