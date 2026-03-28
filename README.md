# HỆ THỐNG QUẢN LÝ SINH VIÊN

## 1. Tổng Quan Project

### 1.1. Mục tiêu phần mềm

Đây là đồ án xây dựng hệ thống quản lý sinh viên dạng ứng dụng desktop, phục vụ 3 nhóm người dùng chính:

- `ADMIN`: quản trị dữ liệu hệ thống, danh mục, học phần, lịch học, điểm, báo cáo.
- `GIẢNG VIÊN`: xem học phần phụ trách, xem sinh viên đăng ký, nhập/sửa điểm, xem lịch dạy, cập nhật hồ sơ cá nhân.
- `SINH VIÊN`: xem hồ sơ, đăng ký học phần, xem học phần đã đăng ký, xem điểm, xem lịch học.

Phần mềm tập trung quản lý các nghiệp vụ cốt lõi trong môi trường đào tạo:

- Quản lý khoa, lớp hành chính, phòng học, môn học.
- Quản lý giảng viên, sinh viên và tài khoản đăng nhập.
- Mở học phần theo học kỳ, năm học.
- Xếp lịch học theo phòng, thứ, tiết.
- Đăng ký học phần cho sinh viên.
- Nhập, cập nhật và theo dõi điểm.
- Tổng hợp báo cáo và thống kê.

### 1.2. Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java 17 |
| Giao diện desktop | Java Swing |
| Quản lý phụ thuộc | Maven |
| ORM / Persistence | JPA (Jakarta Persistence 3.1) |
| ORM Provider | Hibernate 6.4.10.Final |
| CSDL | MySQL 8 |
| Driver CSDL | mysql-connector-j 8.3.0 |
| Logging runtime | slf4j-simple 2.0.13 |
| Xuất PDF | iTextPDF 5.5.13.3 |
| Chọn ngày | JCalendar 1.4 |
| API ngoài tùy chọn | Groq API để phân tích điểm bằng AI |

### 1.3. Kiến trúc tổng thể

Project tổ chức theo hướng phân lớp rõ ràng:

`View -> Controller -> Service -> DAO -> JPA/Hibernate -> MySQL`

Mô tả ngắn:

- `View`: các màn hình Swing, dialog, bảng dữ liệu, form nhập liệu.
- `Controller`: lớp trung gian giữa UI và service, điều phối dữ liệu cho từng màn hình.
- `Service`: xử lý nghiệp vụ, phân quyền, validate dữ liệu, transaction.
- `DAO`: truy vấn dữ liệu bằng JPA/JPQL, insert/update/delete.
- `DB`: MySQL, script schema/seed/verify nằm trong thư mục `database/`.

Ngoài ra còn có các lớp hỗ trợ:

- `config`: nạp cấu hình, bootstrap JPA, session người dùng.
- `security`: phân quyền theo role, băm mật khẩu, quản lý đăng nhập.
- `dto`: DTO phục vụ hiển thị dữ liệu trên UI.
- `utils`: validate, format, dialog, export PDF, hỗ trợ bảng.

## 2. Cấu Trúc Project

### 2.1. Cấu trúc thư mục chính

```text
StudentManagementSystem/
├─ database/                  # Script tạo, seed, verify database
├─ docs/                      # Tài liệu mô tả/use case/wireframe
├─ lib/                       # Một số file jar đính kèm
├─ src/main/java/com/qlsv/
│  ├─ config/                 # Cấu hình app, JPA, session, DB bootstrap
│  ├─ controller/             # Controller nghiệp vụ và controller cho màn hình
│  ├─ dao/                    # Truy cập dữ liệu bằng JPA/JPQL
│  ├─ dto/                    # DTO hiển thị
│  ├─ exception/              # Exception tùy biến
│  ├─ model/                  # Entity/domain model
│  ├─ navigation/             # Điều hướng giữa login/dashboard
│  ├─ security/               # Role, permission, hash mật khẩu
│  ├─ service/                # Nghiệp vụ
│  ├─ utils/                  # Hàm tiện ích
│  └─ view/                   # Toàn bộ giao diện Swing
│     ├─ admin/
│     ├─ auth/
│     ├─ common/
│     ├─ dialog/
│     ├─ lecturer/
│     └─ student/
├─ src/main/resources/
│  ├─ META-INF/persistence.xml
│  └─ application.properties
└─ pom.xml
```

### 2.2. Vai trò từng package/layer

#### `com.qlsv`

- Chứa `Main.java`, là entry point khởi động ứng dụng.

#### `com.qlsv.config`

- `AppConfig`: đọc `application.properties`.
- `JpaBootstrap`: khởi tạo `EntityManagerFactory`, cung cấp helper cho read/write transaction.
- `SessionManager`: lưu user đang đăng nhập.
- `DBConnection`: lớp JDBC cũ, hiện giữ lại cho mục đích diagnostic/bootstrap.
- `StudentJpaMigrationVerifier`: class smoke test cho luồng Student khi migrate sang JPA.

#### `com.qlsv.model`

- Chứa entity/domain model chính: `User`, `Student`, `Lecturer`, `Faculty`, `Subject`, `CourseSection`, `Schedule`, `Enrollment`, `Score`, `Room`, `ClassRoom`, `RoleEntity`.
- `Role` là enum vai trò.
- `SystemStatistics` là model tổng hợp thống kê.
- `Assignment` hiện mới là placeholder, chưa hoàn thiện.

#### `com.qlsv.dao`

- Tầng truy cập dữ liệu.
- Dùng `JpaBootstrap.executeWithEntityManager(...)` cho thao tác đọc.
- Dùng `JpaBootstrap.executeInCurrentTransaction(...)` cho thao tác ghi trong transaction đang mở từ service.
- Viết JPQL và `JOIN FETCH` để nạp dữ liệu phục vụ màn hình Swing.

#### `com.qlsv.service`

- Chứa logic nghiệp vụ:
  - kiểm tra đăng nhập,
  - kiểm tra quyền,
  - validate dữ liệu,
  - xử lý trùng lịch,
  - xử lý trùng đăng ký,
  - tính điểm tổng kết,
  - đồng bộ tài khoản `users` khi thêm/sửa sinh viên hoặc giảng viên.

#### `com.qlsv.controller`

- Controller nghiệp vụ đơn giản gọi service.
- Một số controller chuyên cho màn hình như:
  - `StudentHomeScreenController`
  - `StudentEnrollmentScreenController`
  - `StudentProfileScreenController`
  - `StudentManagementScreenController`
  - `ScheduleManagementScreenController`
  - `ScoreManagementScreenController`
  - `CourseSectionManagementScreenController`
  - `EnrollmentManagementScreenController`

#### `com.qlsv.dto`

- DTO phục vụ hiển thị UI, tránh buộc view phụ thuộc hoàn toàn vào entity.
- Ví dụ: `StudentHomeDto`, `StudentProfileDto`, `CourseSectionDisplayDto`, `EnrollmentDisplayDto`, `ScoreDisplayDto`, `ScheduleDisplayDto`.

#### `com.qlsv.security`

- `RolePermission`: map quyền theo `Role`.
- `AuthManager`: thao tác đăng nhập/đăng xuất/check role/check permission.
- `PasswordHasher`: băm mật khẩu SHA-256.

#### `com.qlsv.view`

- `auth`: màn hình đăng nhập, đổi mật khẩu.
- `admin`: dashboard quản trị và các màn CRUD/báo cáo.
- `lecturer`: màn giảng viên.
- `student`: màn sinh viên.
- `common`: component dùng chung như `BaseFrame`, `BasePanel`, `SidebarMenu`, `DashboardCard`, theme, button, input...
- `dialog`: form nhập liệu, popup xem chi tiết, confirm dialog...

### 2.3. Entry point và luồng khởi động

#### Entry point

- File chạy chính: `src/main/java/com/qlsv/Main.java`

#### Luồng khởi động

1. `Main.main()` chạy trên `SwingUtilities.invokeLater(...)`.
2. Cài `LookAndFeel` hệ thống.
3. Gọi `AppTheme.install()` để áp dụng style chung cho Swing.
4. Gọi `startApplication(...)`.
5. `JpaBootstrap.canBootstrap()` kiểm tra có khởi tạo được JPA/Hibernate không.
6. `JpaBootstrap.hasRequiredSchema()` kiểm tra schema tối thiểu trong database.
7. Nếu DB ổn, `SwingAppNavigator.showLogin()` mở `LoginFrame`.
8. Sau khi đăng nhập thành công:
   - `DashboardController` xác định role.
   - `SwingAppNavigator` mở dashboard tương ứng:
     - `AdminDashboardFrame`
     - `LecturerDashboardFrame`
     - `StudentDashboardFrame`

### 2.4. Luồng xử lý khi đăng nhập

`LoginFrame -> LoginController -> AuthService -> UserDAO -> PasswordHasher/AuthManager -> SessionManager -> DashboardController -> DashboardFrame`

Chi tiết:

- `LoginFrame` lấy username/password từ form.
- `LoginController.login(...)` gọi `AuthService.login(...)`.
- `AuthService`:
  - validate rỗng,
  - tìm user theo username,
  - kiểm tra `active`,
  - so khớp mật khẩu đã băm,
  - gọi `AuthManager.login(user)`.
- `AuthManager` lưu user vào `SessionManager`.
- `SwingAppNavigator.showDashboard(user)` điều hướng sang dashboard theo role.

## 3. Database Chi Tiết

### 3.1. Tổng quan database

- Tên database: `student_management`
- Hệ quản trị: MySQL
- Character set: `utf8mb4`
- Collation: `utf8mb4_unicode_ci`

Schema SQL hiện có:

- `13` bảng dữ liệu chính
- `2` view
- `4` trigger
- nhiều index hỗ trợ truy vấn

### 3.2. Danh sách bảng

1. `roles`
2. `faculties`
3. `rooms`
4. `users`
5. `class_rooms`
6. `subjects`
7. `lecturers`
8. `students`
9. `lecturer_subjects`
10. `course_sections`
11. `schedules`
12. `enrollments`
13. `scores`

### 3.3. Quan hệ tổng quát giữa các bảng

- `roles (1) -> (n) users`
- `faculties (1) -> (n) class_rooms`
- `faculties (1) -> (n) subjects`
- `faculties (1) -> (n) lecturers`
- `faculties (1) -> (n) students`
- `class_rooms (1) -> (n) students`
- `users (1) -> (0..1) lecturers`
- `users (1) -> (0..1) students`
- `lecturers (n) <-> (n) subjects` qua bảng `lecturer_subjects`
- `subjects (1) -> (n) course_sections`
- `lecturers (1) -> (n) course_sections`
- `course_sections (1) -> (n) schedules`
- `rooms (1) -> (n) schedules`
- `students (1) -> (n) enrollments`
- `course_sections (1) -> (n) enrollments`
- `enrollments (1) -> (0..1) scores`

### 3.4. Chi tiết từng bảng

#### Bảng `roles`

Chức năng: lưu danh mục vai trò đăng nhập.

| Cột | Kiểu | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| `id` | `BIGINT` | Khóa định danh vai trò | PK, AUTO_INCREMENT |
| `role_code` | `VARCHAR(50)` | Mã vai trò (`ADMIN`, `LECTURER`, `STUDENT`) | UNIQUE, NOT NULL |
| `role_name` | `VARCHAR(100)` | Tên vai trò hiển thị | NOT NULL |

Khóa chính:

- `id`

Khóa ngoại:

- Không có

Quan hệ:

- Một role có nhiều user.

#### Bảng `faculties`

Chức năng: lưu thông tin khoa.

| Cột | Kiểu | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| `id` | `BIGINT` | Mã định danh khoa | PK, AUTO_INCREMENT |
| `faculty_code` | `VARCHAR(50)` | Mã khoa | UNIQUE, NOT NULL |
| `faculty_name` | `VARCHAR(150)` | Tên khoa | NOT NULL |
| `description` | `VARCHAR(255)` | Mô tả khoa | NULL |
| `created_at` | `TIMESTAMP` | Thời điểm tạo | DEFAULT CURRENT_TIMESTAMP |

Khóa chính:

- `id`

Khóa ngoại:

- Không có

Quan hệ:

- Một khoa có nhiều lớp, môn học, giảng viên, sinh viên.

#### Bảng `rooms`

Chức năng: lưu danh mục phòng học.

| Cột | Kiểu | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| `id` | `BIGINT` | Mã định danh phòng | PK, AUTO_INCREMENT |
| `room_code` | `VARCHAR(50)` | Mã phòng | UNIQUE, NOT NULL |
| `room_name` | `VARCHAR(150)` | Tên phòng | NOT NULL |
| `created_at` | `TIMESTAMP` | Thời điểm tạo | DEFAULT CURRENT_TIMESTAMP |

Khóa chính:

- `id`

Khóa ngoại:

- Không có

Quan hệ:

- Một phòng có thể được dùng trong nhiều dòng lịch học.

#### Bảng `users`

Chức năng: tài khoản đăng nhập của hệ thống.

| Cột | Kiểu | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| `id` | `BIGINT` | Mã định danh tài khoản | PK, AUTO_INCREMENT |
| `username` | `VARCHAR(50)` | Tên đăng nhập | UNIQUE, NOT NULL |
| `password_hash` | `VARCHAR(255)` | Mật khẩu đã băm SHA-256 | NOT NULL |
| `full_name` | `VARCHAR(150)` | Họ tên tài khoản | NOT NULL |
| `email` | `VARCHAR(150)` | Email tài khoản | NULL |
| `role_id` | `BIGINT` | Vai trò của tài khoản | FK, NOT NULL |
| `active` | `BOOLEAN` | Trạng thái hoạt động | DEFAULT TRUE |
| `created_at` | `TIMESTAMP` | Thời điểm tạo | DEFAULT CURRENT_TIMESTAMP |

Khóa chính:

- `id`

Khóa ngoại:

- `role_id -> roles(id)`

Quan hệ:

- Mỗi user thuộc một role.
- Một user có thể liên kết 1 sinh viên hoặc 1 giảng viên.

#### Bảng `class_rooms`

Chức năng: lưu lớp hành chính của sinh viên.

| Cột | Kiểu | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| `id` | `BIGINT` | Mã định danh lớp | PK, AUTO_INCREMENT |
| `class_code` | `VARCHAR(50)` | Mã lớp | UNIQUE, NOT NULL |
| `class_name` | `VARCHAR(150)` | Tên lớp | NOT NULL |
| `academic_year` | `VARCHAR(50)` | Niên khóa của lớp | NOT NULL |
| `faculty_id` | `BIGINT` | Khoa quản lý lớp | FK, NOT NULL |
| `created_at` | `TIMESTAMP` | Thời điểm tạo | DEFAULT CURRENT_TIMESTAMP |

Khóa chính:

- `id`

Khóa ngoại:

- `faculty_id -> faculties(id)`

Quan hệ:

- Một lớp thuộc một khoa.
- Một lớp có nhiều sinh viên.

#### Bảng `subjects`

Chức năng: lưu danh mục môn học.

| Cột | Kiểu | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| `id` | `BIGINT` | Mã định danh môn học | PK, AUTO_INCREMENT |
| `subject_code` | `VARCHAR(50)` | Mã môn học | UNIQUE, NOT NULL |
| `subject_name` | `VARCHAR(150)` | Tên môn học | NOT NULL |
| `credits` | `INT` | Số tín chỉ | NOT NULL, CHECK `> 0` |
| `faculty_id` | `BIGINT` | Khoa phụ trách môn học | FK, NOT NULL |
| `description` | `VARCHAR(255)` | Mô tả môn học | NULL |
| `created_at` | `TIMESTAMP` | Thời điểm tạo | DEFAULT CURRENT_TIMESTAMP |

Khóa chính:

- `id`

Khóa ngoại:

- `faculty_id -> faculties(id)`

Quan hệ:

- Một môn học thuộc một khoa.
- Một môn học có thể mở nhiều học phần.
- Một môn học có thể liên kết nhiều giảng viên qua `lecturer_subjects`.

#### Bảng `lecturers`

Chức năng: lưu hồ sơ giảng viên.

| Cột | Kiểu | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| `id` | `BIGINT` | Mã định danh giảng viên | PK, AUTO_INCREMENT |
| `user_id` | `BIGINT` | Tài khoản đăng nhập liên kết | UNIQUE, FK |
| `lecturer_code` | `VARCHAR(50)` | Mã giảng viên | UNIQUE, NOT NULL |
| `full_name` | `VARCHAR(150)` | Họ tên giảng viên | NOT NULL |
| `gender` | `VARCHAR(20)` | Giới tính | NULL |
| `email` | `VARCHAR(150)` | Email | NULL |
| `date_of_birth` | `DATE` | Ngày sinh | NULL trong schema, nhưng service yêu cầu có |
| `phone` | `VARCHAR(30)` | Số điện thoại | NULL |
| `address` | `VARCHAR(255)` | Địa chỉ | NULL |
| `faculty_id` | `BIGINT` | Khoa công tác | FK, NOT NULL |
| `status` | `VARCHAR(30)` | Trạng thái | DEFAULT `'ACTIVE'` |
| `created_at` | `TIMESTAMP` | Thời điểm tạo | DEFAULT CURRENT_TIMESTAMP |

Khóa chính:

- `id`

Khóa ngoại:

- `user_id -> users(id)`
- `faculty_id -> faculties(id)`

Quan hệ:

- Một giảng viên thuộc một khoa.
- Một giảng viên có thể phụ trách nhiều học phần.
- Một giảng viên có thể dạy nhiều môn qua bảng `lecturer_subjects`.

#### Bảng `students`

Chức năng: lưu hồ sơ sinh viên.

| Cột | Kiểu | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| `id` | `BIGINT` | Mã định danh sinh viên | PK, AUTO_INCREMENT |
| `user_id` | `BIGINT` | Tài khoản đăng nhập liên kết | UNIQUE, FK |
| `student_code` | `VARCHAR(50)` | Mã sinh viên | UNIQUE, NOT NULL |
| `full_name` | `VARCHAR(150)` | Họ tên sinh viên | NOT NULL |
| `gender` | `VARCHAR(20)` | Giới tính | NULL |
| `date_of_birth` | `DATE` | Ngày sinh | NULL |
| `email` | `VARCHAR(150)` | Email | NULL |
| `phone` | `VARCHAR(30)` | Số điện thoại | NULL |
| `address` | `VARCHAR(255)` | Địa chỉ | NULL |
| `faculty_id` | `BIGINT` | Khoa quản lý | FK, NOT NULL |
| `class_room_id` | `BIGINT` | Lớp hành chính | FK, NOT NULL |
| `academic_year` | `VARCHAR(50)` | Niên khóa | NOT NULL |
| `status` | `VARCHAR(30)` | Trạng thái | DEFAULT `'ACTIVE'` |
| `created_at` | `TIMESTAMP` | Thời điểm tạo | DEFAULT CURRENT_TIMESTAMP |

Khóa chính:

- `id`

Khóa ngoại:

- `user_id -> users(id)`
- `faculty_id -> faculties(id)`
- `class_room_id -> class_rooms(id)`

Quan hệ:

- Một sinh viên thuộc một khoa, một lớp hành chính.
- Một sinh viên có nhiều đăng ký học phần.

#### Bảng `lecturer_subjects`

Chức năng: bảng trung gian giảng viên - môn học.

| Cột | Kiểu | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| `lecturer_id` | `BIGINT` | Giảng viên | PK kép, FK |
| `subject_id` | `BIGINT` | Môn học | PK kép, FK |

Khóa chính:

- `(lecturer_id, subject_id)`

Khóa ngoại:

- `lecturer_id -> lecturers(id)`
- `subject_id -> subjects(id)`

Quan hệ:

- Tạo quan hệ nhiều-nhiều giữa giảng viên và môn học.

Ghi chú hiện trạng:

- Bảng này có trong schema SQL.
- Tuy nhiên runtime JPA hiện tại chưa map bảng này thành entity hoàn chỉnh.
- Phân công giảng dạy đang được hệ thống sử dụng chủ yếu qua `course_sections.lecturer_id`.

#### Bảng `course_sections`

Chức năng: lưu học phần mở theo học kỳ/năm học.

| Cột | Kiểu | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| `id` | `BIGINT` | Mã định danh học phần | PK, AUTO_INCREMENT |
| `section_code` | `VARCHAR(50)` | Mã học phần mở | UNIQUE, NOT NULL |
| `subject_id` | `BIGINT` | Môn học gốc | FK, NOT NULL |
| `lecturer_id` | `BIGINT` | Giảng viên phụ trách | FK, NOT NULL |
| `semester` | `VARCHAR(30)` | Học kỳ | NOT NULL |
| `school_year` | `VARCHAR(30)` | Năm học | NOT NULL |
| `max_students` | `INT` | Sĩ số tối đa | DEFAULT 50, CHECK `> 0` |
| `created_at` | `TIMESTAMP` | Thời điểm tạo | DEFAULT CURRENT_TIMESTAMP |

Khóa chính:

- `id`

Khóa ngoại:

- `subject_id -> subjects(id)`
- `lecturer_id -> lecturers(id)`

Quan hệ:

- Một học phần thuộc một môn học.
- Một học phần do một giảng viên phụ trách.
- Một học phần có nhiều lịch học.
- Một học phần có nhiều đăng ký.

Ghi chú hiện trạng code:

- Entity `CourseSection` có thêm 2 field `room` và `scheduleText` dạng `@Transient`.
- Hai field này chỉ dùng để tương thích giao diện cũ và được hydrate từ bảng `schedules`.

#### Bảng `schedules`

Chức năng: lưu lịch học/lịch dạy của từng học phần.

| Cột | Kiểu | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| `id` | `BIGINT` | Mã định danh lịch học | PK, AUTO_INCREMENT |
| `course_section_id` | `BIGINT` | Học phần được xếp lịch | FK, NOT NULL |
| `day_of_week` | `VARCHAR(20)` | Thứ học | NOT NULL |
| `start_period` | `INT` | Tiết bắt đầu | NOT NULL |
| `end_period` | `INT` | Tiết kết thúc | NOT NULL |
| `room_id` | `BIGINT` | Phòng học | FK, NOT NULL |
| `note` | `VARCHAR(255)` | Ghi chú | NULL |
| `created_at` | `TIMESTAMP` | Thời điểm tạo | DEFAULT CURRENT_TIMESTAMP |

Khóa chính:

- `id`

Khóa ngoại:

- `course_section_id -> course_sections(id)`
- `room_id -> rooms(id)`

Ràng buộc:

- UNIQUE `(course_section_id, day_of_week, start_period, room_id)`
- CHECK `start_period > 0`
- CHECK `end_period > 0`
- CHECK `start_period <= end_period`

Quan hệ:

- Một lịch thuộc một học phần.
- Một phòng có thể xuất hiện trong nhiều lịch khác nhau.

Ghi chú nghiệp vụ:

- Xung đột lịch phòng và lịch giảng viên không chỉ dựa vào UNIQUE, mà còn được kiểm tra thêm ở service/DAO theo khoảng tiết giao nhau.

#### Bảng `enrollments`

Chức năng: lưu đăng ký học phần của sinh viên.

| Cột | Kiểu | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| `id` | `BIGINT` | Mã định danh đăng ký | PK, AUTO_INCREMENT |
| `student_id` | `BIGINT` | Sinh viên đăng ký | FK, NOT NULL |
| `course_section_id` | `BIGINT` | Học phần đăng ký | FK, NOT NULL |
| `status` | `VARCHAR(30)` | Trạng thái đăng ký | DEFAULT `'REGISTERED'` |
| `enrolled_at` | `TIMESTAMP` | Thời điểm đăng ký | DEFAULT CURRENT_TIMESTAMP |

Khóa chính:

- `id`

Khóa ngoại:

- `student_id -> students(id)`
- `course_section_id -> course_sections(id)`

Ràng buộc:

- UNIQUE `(student_id, course_section_id)`

Quan hệ:

- Một sinh viên có nhiều đăng ký.
- Một học phần có nhiều sinh viên đăng ký.
- Một đăng ký có thể có một bản ghi điểm.

#### Bảng `scores`

Chức năng: lưu điểm của từng đăng ký học phần.

| Cột | Kiểu | Ý nghĩa | Ràng buộc |
|---|---|---|---|
| `id` | `BIGINT` | Mã định danh điểm | PK, AUTO_INCREMENT |
| `enrollment_id` | `BIGINT` | Đăng ký tương ứng | UNIQUE, FK, NOT NULL |
| `process_score` | `DECIMAL(4,2)` | Điểm quá trình | DEFAULT 0, CHECK `0..10` |
| `midterm_score` | `DECIMAL(4,2)` | Điểm giữa kỳ | DEFAULT 0, CHECK `0..10` |
| `final_score` | `DECIMAL(4,2)` | Điểm cuối kỳ | DEFAULT 0, CHECK `0..10` |
| `total_score` | `DECIMAL(4,2)` | Điểm tổng kết | DEFAULT 0, CHECK `0..10` |
| `result` | `VARCHAR(20)` | Kết quả (`PASS`/`FAIL`) | DEFAULT `'FAIL'` |
| `updated_at` | `TIMESTAMP` | Thời điểm cập nhật cuối | DEFAULT CURRENT_TIMESTAMP, ON UPDATE CURRENT_TIMESTAMP |

Khóa chính:

- `id`

Khóa ngoại:

- `enrollment_id -> enrollments(id)`

Quan hệ:

- Một đăng ký tương ứng tối đa một bản ghi điểm.

Ghi chú nghiệp vụ:

- Service tự tính `total_score = process*0.3 + midterm*0.2 + final*0.5`.
- Nếu `total_score >= 5.0` thì `result = PASS`, ngược lại `FAIL`.

### 3.5. View trong database

#### `vw_student_schedules`

Mục đích:

- Gom lịch học theo sinh viên từ các bảng `enrollments`, `students`, `course_sections`, `subjects`, `schedules`, `rooms`.

Các trường trả ra:

- `student_code`
- `student_name`
- `section_code`
- `subject_code`
- `subject_name`
- `day_of_week`
- `start_period`
- `end_period`
- `room_code`
- `room_name`

#### `vw_section_scores`

Mục đích:

- Gom bảng điểm theo học phần từ `enrollments`, `students`, `course_sections`, `scores`.

Các trường trả ra:

- `section_code`
- `student_code`
- `student_name`
- `process_score`
- `midterm_score`
- `final_score`
- `total_score`
- `result`

### 3.6. Trigger trong database

#### Trigger tự tạo tài khoản

- `trg_lecturers_create_user`
- `trg_students_create_user`

Chức năng:

- Khi thêm giảng viên/sinh viên mới mà `user_id` đang `NULL`, trigger tự tạo user tương ứng trong bảng `users`.
- `username` được lấy từ mã giảng viên/mã sinh viên viết thường.
- Mật khẩu mặc định là `123456` đã băm SHA-256.

#### Trigger đồng bộ user khi cập nhật hồ sơ

- `trg_lecturers_update_user`
- `trg_students_update_user`

Chức năng:

- Khi sửa mã giảng viên/sinh viên hoặc họ tên, trigger đồng bộ lại `users.username` và `users.full_name`.

### 3.7. Index đáng chú ý

Script có tạo các index quan trọng:

- `idx_students_class_room`
- `idx_students_faculty`
- `idx_lecturers_faculty`
- `idx_subjects_faculty`
- `idx_course_sections_lecturer`
- `idx_enrollments_section`
- `idx_enrollments_student`
- `idx_scores_enrollment`
- `idx_schedules_section_day`
- `idx_schedules_room_day_period`

## 4. Chức Năng Chính

### 4.1. Theo role `ADMIN`

#### Quản lý sinh viên

- Xem danh sách sinh viên.
- Tìm kiếm theo từ khóa.
- Lọc theo khoa, lớp, niên khóa.
- Thêm mới sinh viên.
- Cập nhật thông tin sinh viên.
- Xóa sinh viên.
- Đổi mật khẩu tài khoản sinh viên.
- Đồng bộ thông tin với bảng `users`.

#### Quản lý giảng viên

- Xem danh sách giảng viên.
- Lọc theo khoa.
- Thêm mới giảng viên.
- Cập nhật giảng viên.
- Xóa giảng viên.
- Đổi mật khẩu tài khoản giảng viên.
- Đồng bộ thông tin với bảng `users`.

#### Quản lý khoa

- CRUD khoa.

#### Quản lý lớp hành chính

- CRUD lớp.
- Lọc theo khoa và niên khóa.

#### Quản lý phòng học

- CRUD phòng học.
- Tìm kiếm theo mã phòng/tên phòng.

#### Quản lý môn học

- CRUD môn học.
- Lọc theo khoa.

#### Quản lý học phần

- CRUD học phần.
- Gán môn học, giảng viên, học kỳ, năm học, sĩ số tối đa.
- Kiểm tra trùng lịch giảng viên khi đổi giảng viên cho học phần đã có lịch.

#### Quản lý lịch học

- CRUD lịch học.
- Lọc theo học phần, phòng, khoa.
- Kiểm tra:
  - trùng phòng,
  - trùng lịch giảng viên,
  - ràng buộc tiết bắt đầu/tiết kết thúc.

#### Quản lý đăng ký học phần

- CRUD đăng ký.
- Xem theo sinh viên, lớp, khoa, học phần.

#### Quản lý điểm

- CRUD điểm.
- Xem theo học phần, lớp.
- Tính tổng kết và kết quả tự động.

#### Báo cáo và thống kê

- Thống kê tổng sinh viên, giảng viên, môn học, học phần.
- Báo cáo sinh viên theo lớp.
- Báo cáo giảng viên theo khoa.
- Báo cáo sinh viên trong học phần.
- Báo cáo điểm theo học phần.
- Xuất PDF báo cáo hiện tại.

#### Quản trị tài khoản

- Đổi mật khẩu chính tài khoản admin.
- Reset mật khẩu cho sinh viên/giảng viên thông qua màn hình quản lý tương ứng.

### 4.2. Theo role `GIẢNG VIÊN`

#### Hồ sơ cá nhân

- Xem thông tin cá nhân.
- Sửa email, số điện thoại, địa chỉ.
- Đổi mật khẩu.

#### Học phần phụ trách

- Xem danh sách học phần đang phụ trách.
- Xem chi tiết học phần: môn học, số tín chỉ, học kỳ, năm học, lịch học, sĩ số tối đa.

#### Danh sách sinh viên

- Xem danh sách sinh viên đã đăng ký theo học phần.
- Lọc theo học phần phụ trách.
- Xem chi tiết sinh viên.
- Xuất danh sách ra PDF.

#### Nhập/sửa điểm

- Xem danh sách điểm của sinh viên trong các học phần mình phụ trách.
- Lọc theo học phần.
- Tìm kiếm theo mã sinh viên/họ tên.
- Nhập điểm quá trình, giữa kỳ, cuối kỳ.
- Cập nhật lại điểm cho sinh viên.
- Chỉ được nhập điểm cho học phần được phân công.

#### Lịch dạy

- Xem toàn bộ lịch dạy theo các học phần mình phụ trách.

### 4.3. Theo role `SINH VIÊN`

#### Trang tổng quan

- Xem thông tin tóm tắt:
  - số học phần đã đăng ký,
  - tổng số tín chỉ,
  - điểm trung bình hiện tại,
  - số lịch học,
  - tóm tắt kết quả học tập.

#### Hồ sơ cá nhân

- Xem thông tin cá nhân.
- Cập nhật email, số điện thoại, địa chỉ.
- Đổi mật khẩu.

#### Đăng ký học phần

- Xem danh sách học phần đang mở.
- Tìm kiếm học phần theo mã/tên môn/giảng viên.
- Lọc theo học kỳ.
- Đăng ký học phần.
- Hủy đăng ký học phần của chính mình.

#### Các ràng buộc khi đăng ký học phần

- Không được đăng ký trùng đúng học phần.
- Không được đăng ký học phần khác của cùng môn học.
- Không vượt quá sĩ số tối đa.
- Không được đăng ký nếu trùng lịch với học phần đã có.

#### Học phần đã đăng ký

- Xem danh sách học phần đã đăng ký.

#### Xem điểm

- Xem bảng điểm cá nhân.
- Xem số môn đạt/chưa đạt.
- Xem điểm trung bình tổng kết.
- Có chức năng phân tích điểm bằng AI nếu cấu hình `groq.api.key`.

#### Xem lịch học

- Xem toàn bộ lịch học cá nhân lấy từ các học phần đã đăng ký.

### 4.4. Phân quyền

Phân quyền hiện tại được hard-code trong `RolePermission`, không đọc động từ bảng `roles`.

#### `ADMIN`

- `MANAGE_STUDENTS`
- `MANAGE_LECTURERS`
- `MANAGE_FACULTIES`
- `MANAGE_CLASSES`
- `MANAGE_SUBJECTS`
- `MANAGE_COURSE_SECTIONS`
- `MANAGE_ENROLLMENTS`
- `MANAGE_SCORES`
- `MANAGE_SCHEDULES`
- `VIEW_REPORTS`
- `VIEW_SYSTEM_STATISTICS`
- `VIEW_OWN_PROFILE`
- `EDIT_OWN_PROFILE`
- `VIEW_OWN_SCHEDULE`

#### `LECTURER`

- `VIEW_OWN_PROFILE`
- `EDIT_OWN_PROFILE`
- `VIEW_ASSIGNED_CLASSES`
- `VIEW_ASSIGNED_STUDENTS`
- `MANAGE_SCORES`
- `VIEW_OWN_SCHEDULE`

#### `STUDENT`

- `VIEW_OWN_PROFILE`
- `EDIT_OWN_PROFILE`
- `REGISTER_ENROLLMENT`
- `VIEW_OWN_SCORES`
- `VIEW_OWN_SCHEDULE`

## 5. Luồng Hoạt Động

### 5.1. Luồng tổng quát từ UI tới DB

Luồng chuẩn của project:

`Swing Panel/Dialog -> Controller -> Service -> DAO -> EntityManager/JPA -> MySQL`

Ví dụ khi admin thêm sinh viên:

1. Form trên `StudentManagementPanel` nhận dữ liệu.
2. `StudentManagementScreenController` tạo object `Student`.
3. `StudentController.saveStudent(...)` gọi `StudentService.save(...)`.
4. `StudentService`:
   - kiểm tra quyền,
   - validate dữ liệu,
   - mở transaction,
   - tạo/liên kết `User`,
   - gọi `StudentDAO.insert(...)` hoặc `update(...)`,
   - đồng bộ email/họ tên sang bảng `users`.
5. `StudentDAO` ghi dữ liệu bằng JPA.
6. Hibernate sinh SQL thao tác với MySQL.

### 5.2. Luồng đăng nhập

1. Người dùng nhập username/password tại `LoginFrame`.
2. `AuthService` kiểm tra thông tin.
3. Nếu hợp lệ:
   - lưu user vào `SessionManager`,
   - xác định role,
   - mở dashboard tương ứng.
4. Nếu sai:
   - hiển thị dialog lỗi.

### 5.3. Luồng xử lý dữ liệu trong tầng nghiệp vụ

#### Luồng đọc dữ liệu

- `Service -> DAO.find...() -> JpaBootstrap.executeWithEntityManager(...) -> JPQL -> Entity -> trả về UI`

#### Luồng ghi dữ liệu

- `Service -> JpaBootstrap.executeInTransaction(...) -> DAO.insert/update/delete -> flush -> commit`

#### Kiểm tra chéo trong service

- `EnrollmentService`: chống trùng đăng ký, chống trùng môn, chống quá sĩ số, chống trùng lịch sinh viên.
- `ScheduleService`: chống trùng phòng, chống trùng lịch giảng viên.
- `ScoreService`: chỉ giảng viên phụ trách mới được nhập điểm; tự tính điểm tổng kết và kết quả.
- `StudentService`/`LecturerService`: đồng bộ hồ sơ với bảng `users`.

### 5.4. Luồng script database

Thư mục `database/` đi theo luồng:

1. `00_drop_old_database.sql`
2. `01_create_schema.sql`
3. `02_seed_full_data.sql`
4. `03_verify_data.sql`

Ý nghĩa:

- `00`: xóa DB cũ nếu muốn reset.
- `01`: tạo schema, bảng, khóa ngoại, index, view, trigger.
- `02`: nạp dữ liệu mẫu cho admin, giảng viên, sinh viên.
- `03`: kiểm tra số lượng bảng, dữ liệu, view, trigger và thử test nhanh.

## 6. Luồng Cài Đặt Và Chạy

### 6.1. Yêu cầu môi trường

- Java 17
- Maven
- MySQL 8
- IDE Java như IntelliJ IDEA / Eclipse / NetBeans

### 6.2. Cấu hình database

File cấu hình hiện tại:

- `src/main/resources/application.properties`

Giá trị mặc định:

```properties
db.url=jdbc:mysql://localhost:3306/student_management
db.username=root
db.password=123456
jpa.persistence.unit=student-management-jpa
jpa.hibernate.ddl-auto=none
jpa.show-sql=false
student.persistence.mode=jpa
groq.api.key=YOUR_GROQ_API_KEY
```

### 6.3. Thứ tự setup database

Chạy theo đúng thứ tự:

1. `database/00_drop_old_database.sql` (tùy chọn nếu muốn reset sạch)
2. `database/01_create_schema.sql`
3. `database/02_seed_full_data.sql`
4. `database/03_verify_data.sql`

### 6.4. Tài khoản demo sau khi seed

- Admin: `admin / 123456`
- Giảng viên: `gv001 / 123456`, `gv002 / 123456`, `gv003 / 123456`
- Sinh viên: `sv2200001 / 123456`, `sv2200002 / 123456`, ...

### 6.5. Cách chạy project

#### Cách đơn giản nhất

- Mở project dưới dạng Maven project trong IDE.
- Chờ IDE tải dependency.
- Chạy file `Main.java`.

#### Có thể build trước bằng Maven

```bash
mvn clean compile
```

Sau đó chạy `Main.java` trong IDE.

### 6.6. Dependency cần thiết

Theo `pom.xml`, project cần các dependency chính:

- `hibernate-core`
- `jakarta.persistence-api`
- `mysql-connector-j`
- `slf4j-simple`
- `itextpdf`
- `jcalendar`

Ghi chú:

- Thư mục `lib/` cũng đang chứa một số `.jar` tương ứng, nhưng nguồn quản lý chính vẫn là Maven.

## 7. Ưu Điểm

### 7.1. Về kiến trúc

- Chia layer tương đối rõ: `view`, `controller`, `service`, `dao`, `model`.
- Nghiệp vụ quan trọng nằm ở `service`, không nhét toàn bộ xuống UI.
- Dùng `JpaBootstrap` để gom logic transaction/read/write khá gọn.

### 7.2. Về tách lớp dữ liệu

- Dùng JPA/Hibernate thay vì viết toàn bộ SQL thủ công trong runtime.
- Entity được map rõ ràng với bảng chính.
- DAO sử dụng `JOIN FETCH` để giảm lỗi lazy loading khi đưa dữ liệu lên Swing.

### 7.3. Về nghiệp vụ

- Có kiểm tra quyền theo role.
- Có validate dữ liệu ở service.
- Có kiểm tra:
  - trùng lịch phòng,
  - trùng lịch giảng viên,
  - trùng lịch sinh viên,
  - trùng đăng ký môn/học phần,
  - quá sĩ số tối đa.

### 7.4. Về database

- Có bộ script khá đầy đủ: tạo schema, seed data, verify data.
- Có view hỗ trợ truy vấn tổng hợp.
- Có trigger tự tạo/đồng bộ tài khoản user.
- Có index cho các quan hệ truy vấn nhiều.

### 7.5. Về UI/UX

- Có theme Swing chung (`AppTheme`, `AppColors`, `SidebarMenu`, `DashboardCard`).
- Dashboard riêng cho từng role.
- Nhiều màn hình có dialog chi tiết, lọc dữ liệu, tìm kiếm, thống kê nhanh.
- Có chức năng xuất PDF cho báo cáo và danh sách sinh viên.

## 8. Nhược Điểm / Rủi Ro

### 8.1. Chức năng chưa hoàn thiện đồng đều

- `UserManagementPanel` đang rỗng.
- `AssignmentManagementPanel` đang rỗng.
- `RoleManagementPanel` chỉ là màn hình thông báo, chưa có CRUD thực tế.
- `AssignmentDAO` mới là placeholder.
- `LecturerAssignedSubjectsPanel` đang rỗng.

=> Nghĩa là project có sẵn khung mở rộng, nhưng chưa hoàn thiện toàn bộ module.

### 8.2. Chưa đồng nhất hoàn toàn giữa schema và runtime

- Bảng `lecturer_subjects` tồn tại trong SQL nhưng JPA runtime hiện chưa map/use đầy đủ.
- Phân công giảng dạy thực tế đang bám chủ yếu vào `course_sections.lecturer_id`.

### 8.3. Phân quyền chưa động theo database

- Bảng `roles` có trong DB.
- Nhưng quyền thao tác hiện đang hard-code trong `RolePermission`.
- Muốn đổi permission phải sửa code, chưa có quản trị quyền động theo DB.

### 8.4. Bảo mật còn cơ bản

- Mật khẩu mặc định seed là `123456`.
- Mật khẩu được băm SHA-256 nhưng chưa có salt.
- Thông tin DB đang để trực tiếp trong `application.properties`.

### 8.5. Chưa có test tự động chuẩn

- Không thấy thư mục `src/test`.
- Chỉ có `StudentJpaMigrationVerifier` dạng smoke test thủ công cho một phần hệ thống.

### 8.6. Rủi ro bảo trì

- Project đang tồn tại song song:
  - luồng JPA mới,
  - `DBConnection` JDBC cũ để diagnostic.
- Một số field `@Transient` trong `CourseSection` dùng cho tương thích UI cũ, dễ gây khó theo dõi nếu mở rộng tiếp.

### 8.7. Hiển thị tiếng Việt trong source chưa sạch hoàn toàn

- Trong nhiều file source đang xuất hiện dấu hiệu lỗi encoding ký tự tiếng Việt khi đọc raw text.
- Dù logic vẫn rõ, nhưng phần hiển thị/chỉnh sửa source có thể khó bảo trì nếu không thống nhất UTF-8.

## 9. Hướng Phát Triển

### 9.1. Hoàn thiện module còn dở

- Hoàn thiện `RoleManagementPanel`, `UserManagementPanel`, `AssignmentManagementPanel`.
- Nếu cần quản lý phân công môn - giảng viên riêng, nên map đầy đủ bảng `lecturer_subjects`.

### 9.2. Nâng cấp phân quyền

- Đưa permission xuống database thay vì hard-code hoàn toàn trong `RolePermission`.
- Cho phép admin cấu hình quyền theo role trên UI.

### 9.3. Cải tiến bảo mật

- Dùng password encoder mạnh hơn có salt.
- Đưa cấu hình DB/API key sang biến môi trường hoặc file cấu hình tách riêng.
- Bắt buộc đổi mật khẩu ở lần đăng nhập đầu tiên với tài khoản mặc định.

### 9.4. Tối ưu kiểm thử và chất lượng mã nguồn

- Bổ sung unit test/integration test cho service và DAO.
- Thêm test cho luồng đăng ký học phần, xếp lịch, nhập điểm.
- Tách rõ hơn logic UI và logic nghiệp vụ ở một số panel dài.

### 9.5. Cải tiến UI/UX

- Đồng bộ thêm các màn hình đang còn placeholder.
- Bổ sung phân trang nếu dữ liệu lớn.
- Tối ưu trải nghiệm lọc/tìm kiếm trên các bảng quản lý.

### 9.6. Cải tiến database

- Rà soát thêm index theo các truy vấn thực tế khi dữ liệu tăng.
- Chuẩn hóa hơn phần phân công giảng dạy nếu muốn tách riêng khỏi `course_sections`.

### 9.7. Mở rộng tính năng

- Quản lý học phí.
- Quản lý học kỳ/niên khóa độc lập.
- Import/export Excel.
- Gửi thông báo/email cho sinh viên, giảng viên.
- Mở API hoặc web/mobile client nếu muốn tách frontend khỏi desktop app.

## 10. Kết Luận

Project đã xây dựng được một hệ thống quản lý sinh viên desktop khá đầy đủ cho 3 role chính, với bộ chức năng cốt lõi gồm quản lý danh mục, học phần, lịch học, đăng ký, điểm và báo cáo. Điểm mạnh lớn nhất là kiến trúc phân lớp rõ, có JPA/Hibernate, có script database đầy đủ và có kiểm tra nghiệp vụ tương đối chặt ở các luồng quan trọng.

Tuy nhiên, project vẫn còn một số phần đang ở mức khung mở rộng hoặc chưa đồng bộ hoàn toàn giữa code và schema, đặc biệt là các module user/role/assignment. Nếu tiếp tục hoàn thiện các điểm này, đây sẽ là một đồ án có tính thực tiễn và khả năng mở rộng tốt hơn.
