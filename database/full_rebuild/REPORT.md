# Báo cáo bộ SQL rút gọn

## Mục tiêu

Tạo một bộ file SQL mới, độc lập với các file hiện có, để:

- xóa database cũ
- tạo lại đầy đủ schema ở trạng thái cuối cùng
- nạp bộ dữ liệu mẫu liên kết đầy đủ
- kiểm tra nhanh dữ liệu sau khi import

Toàn bộ thay đổi chỉ nằm trong thư mục `database/full_rebuild`. Không chỉnh sửa bất kỳ file SQL cũ nào.

## Bộ file mới

- `00_drop_old_database.sql`: xóa database `student_management` cũ.
- `01_create_schema.sql`: tạo database, toàn bộ bảng, khóa, view và index của schema hiện hành.
- `02_seed_full_data.sql`: nạp dữ liệu mẫu đầy đủ, liên kết xuyên suốt giữa các bảng.
- `03_verify_data.sql`: chạy các truy vấn kiểm tra nhanh sau khi import.

## Nguồn gốc hợp nhất

### 1. Schema

`01_create_schema.sql` đã gộp logic hiệu lực từ các file sau:

- `01_create_database.sql`
- `02_create_tables.sql`
- `06_create_views.sql`
- `07_create_indexes.sql`
- trạng thái cuối cùng của `11_add_student_academic_year.sql`
- trạng thái cuối cùng của `12_rename_course_sections_class_room_to_room.sql`

Hai file migration `11` và `12` không được giữ riêng trong bộ mới vì schema hiện tại đã ở trạng thái sau nâng cấp. Việc hấp thụ trực tiếp vào file tạo schema giúp bộ mới ngắn gọn hơn và không làm thay đổi cấu trúc so với trạng thái cuối cùng đang dùng.

### 2. Dữ liệu

`02_seed_full_data.sql` lấy đầy đủ dữ liệu liên kết từ bộ hiện có, chủ yếu kế thừa từ:

- `03_insert_sample_data.sql`
- `04_insert_roles.sql`
- `05_seed_sample_data.sql`

Phần dữ liệu mới vẫn giữ nguyên:

- mã vai trò
- username
- email
- mã khoa, lớp, môn học, học phần
- phòng học, học kỳ, niên khóa
- các giá trị điểm và trạng thái nghiệp vụ

Chỉ thay đổi có chủ đích:

- tên người được đổi sang tiếng Việt có dấu
- giá trị giới tính `Nữ` được ghi đúng UTF-8 để tránh lỗi mã hóa cũ

### 3. File placeholder

`08_create_triggers.sql` và `09_create_procedures.sql` hiện không chứa logic thực thi, nên không tách thành file riêng trong bộ rút gọn. Điều này không làm mất cấu trúc hay chức năng thực tế nào của hệ thống hiện tại.

## Cam kết phạm vi

- Không đổi tên bảng.
- Không thêm bảng mới.
- Không bớt cột nào của schema hiện hành.
- Không thay đổi khóa chính, khóa ngoại, unique key, check constraint.
- Không sửa các file cũ trong thư mục `database`.

## Thành phần schema được giữ nguyên

### Bảng

- `roles`
- `users`
- `faculties`
- `class_rooms`
- `students`
- `lecturers`
- `subjects`
- `lecturer_subjects`
- `course_sections`
- `schedules`
- `enrollments`
- `scores`

### View

- `vw_student_schedules`
- `vw_section_scores`

### Index tạo riêng

- `idx_students_class_room`
- `idx_students_faculty`
- `idx_lecturers_faculty`
- `idx_subjects_faculty`
- `idx_course_sections_lecturer`
- `idx_course_sections_room`
- `idx_enrollments_section`
- `idx_enrollments_student`
- `idx_scores_enrollment`
- `idx_schedules_section_day`

## Dữ liệu liên kết trong bộ seed mới

Sau khi chạy `02_seed_full_data.sql`, bộ dữ liệu có:

- 3 role
- 6 user
- 2 faculty
- 3 class room
- 2 lecturer
- 3 student
- 3 subject
- 3 phân công giảng dạy trong `lecturer_subjects`
- 4 course section
- 4 schedule
- 4 enrollment
- 3 score

Liên kết nghiệp vụ đã có đủ các luồng chính:

- user liên kết với lecturer và student
- student liên kết faculty và class room
- subject liên kết faculty
- course section liên kết subject và lecturer
- schedule liên kết course section
- enrollment liên kết student và course section
- score liên kết enrollment
- view lịch học và view bảng điểm đều đọc được từ dữ liệu mẫu

## Thứ tự chạy khuyến nghị

1. Chạy `00_drop_old_database.sql`
2. Chạy `01_create_schema.sql`
3. Chạy `02_seed_full_data.sql`
4. Chạy `03_verify_data.sql`

## Tài khoản mẫu

Mật khẩu chung: `123456`

- `admin`
- `lecturer01`
- `lecturer02`
- `student01`
- `student02`
- `student03`

## Ghi chú kiểm soát rủi ro

- Bộ mới được thiết kế cho kịch bản tạo lại database từ đầu.
- File xóa database cũ đã được tách riêng để thao tác có chủ đích.
- Vì dùng schema cuối cùng, bộ mới không cần chạy thêm các file migration nâng cấp từ bản cũ.
