# Database Scripts

Thư mục `database/` chứa các file SQL phục vụ việc tạo schema và chèn dữ liệu demo/test cho đồ án (MySQL 8).

## Các file script hiện tại

1. `00_drop_old_database.sql`
   - Xóa database cũ `student_management` (nếu tồn tại). Dùng khi muốn reset sạch dữ liệu.
2. `01_create_schema.sql`
   - Tạo database `student_management` (nếu chưa có).
   - Tạo toàn bộ bảng, khóa ngoại, view (`vw_student_schedules`, `vw_section_scores`) và index phục vụ tra cứu.
3. `02_seed_full_data.sql`
   - Chèn dữ liệu mẫu đầy đủ: roles/users, faculties/classes/rooms, lecturers/students, subjects/course_sections, schedules, enrollments và scores.
   - Mật khẩu demo mặc định cho các tài khoản: `123456` (đã được lưu dạng SHA-256 trong script).
4. `03_verify_data.sql`
   - Chạy các câu lệnh kiểm tra nhanh: số lượng bản ghi các bảng, kiểm tra view trả dữ liệu.

## Thứ tự chạy script (khuyến nghị)

Nếu setup lần đầu (hoặc muốn reset sạch dữ liệu):

1. (Tùy chọn) `00_drop_old_database.sql`
2. `01_create_schema.sql`
3. `02_seed_full_data.sql`
4. `03_verify_data.sql` (để kiểm tra nhanh)

Lưu ý: `02_seed_full_data.sql` dùng lệnh `INSERT INTO` (không xử lý trùng). Vì vậy nếu DB đã có dữ liệu và bạn chạy lại seed, bạn nên reset bằng `00_drop_old_database.sql` trước để tránh lỗi khóa/không trùng dữ liệu.

## Cấu hình MySQL mặc định của đồ án (demo)

Mặc định trong project:
- `db.username=root`
- `db.password=123456`
- `db.url=jdbc:mysql://localhost:3306/student_management`

Thành viên nhóm có thể cần chỉnh `src/main/resources/application.properties` theo MySQL của máy mình.

## Tài khoản demo để test

- `admin` / `123456`
- `lecturer01` / `123456`
- `lecturer02` / `123456`
- `student01` / `123456`
- `student02` / `123456`
- `student03` / `123456`
