# Hệ thống quản lý sinh viên kết hợp AI phân tích dữ liệu

## Mục lục
1. Giới thiệu đề tài
2. Mục tiêu đề tài
3. Phạm vi đề tài
4. Chức năng chính của phần mềm
5. Phân quyền người dùng
6. Kiến trúc, mô hình và công nghệ sử dụng
7. Cấu trúc project
8. Cấu trúc database
9. Chi tiết từng bảng trong database
10. Thuật toán và chính sách xử lý chính
11. Các luồng hoạt động chính của chương trình
12. Hướng dẫn sử dụng phần mềm
13. Tài khoản test đã xác minh từ project
14. Đánh giá đồ án dưới góc độ đồ án cuối kì
15. Ghi chú xác minh và hiện trạng

## 1. Giới thiệu đề tài
Đây là đồ án desktop viết bằng Java Swing, phục vụ quản lý đào tạo trong phạm vi trường/khoa với ba vai trò chính: quản trị viên, giảng viên và sinh viên. Hệ thống quản lý dữ liệu học thuật cốt lõi như sinh viên, giảng viên, khoa, lớp hành chính, môn học, học phần, lịch học, đăng ký học phần và điểm số.

Điểm nổi bật của project là ngoài các nghiệp vụ quản lý học vụ truyền thống, hệ thống còn có nền tảng tích hợp AI để phân tích dữ liệu điểm học tập thông qua dịch vụ Groq. AI đang được dùng ở hai hướng:

- Phân tích kết quả học tập cá nhân cho sinh viên.
- Phân tích xu hướng điểm của cả danh sách/lớp học phần cho giảng viên.

README này được viết bám theo source code, cấu hình và SQL đang có trong working tree hiện tại của project. Phần nào chưa thấy được nối vào luồng chạy thực tế sẽ được ghi chú rõ, không suy diễn thêm ngoài code và database.

## 2. Mục tiêu đề tài
Mục tiêu thực tế có thể xác minh từ project hiện tại:

- Tin học hóa quản lý thông tin sinh viên, giảng viên, khoa, lớp, môn học và học phần.
- Tổ chức lịch học và kiểm soát các xung đột cơ bản về phòng học, lịch dạy và lịch đăng ký.
- Hỗ trợ sinh viên đăng ký học phần, xem lịch học, xem điểm và theo dõi kết quả học tập.
- Hỗ trợ giảng viên theo dõi học phần phụ trách, danh sách sinh viên, nhập điểm và phân tích điểm.
- Hỗ trợ quản trị viên quản lý dữ liệu đào tạo tập trung và xuất báo cáo PDF.
- Tạo nền tảng tích hợp AI để đưa ra nhận xét và gợi ý dựa trên dữ liệu điểm.

## 3. Phạm vi đề tài
### 3.1. Phạm vi đã có trong project
- Ứng dụng desktop Java Swing, không phải ứng dụng web.
- Dữ liệu lưu trên MySQL schema `student_management`.
- Truy cập dữ liệu chính bằng JPA/Hibernate, có dùng JPQL trong DAO.
- Có phân quyền theo 3 vai trò: `ADMIN`, `LECTURER`, `STUDENT`.
- Có dữ liệu seed mẫu để chạy thử và đăng nhập ngay.
- Có báo cáo thống kê và xuất PDF ở một số màn hình.
- Có tích hợp Groq API để phân tích điểm.

### 3.2. Phạm vi chưa thấy triển khai đầy đủ trong working tree hiện tại
- Chưa thấy màn hình quản lý trực tiếp bảng `users` và `roles`.
- Bảng `lecturer_subjects` có trong database nhưng chưa thấy entity/DAO/service/UI sử dụng trực tiếp, cũng chưa thấy dữ liệu seed cho bảng này.
- `LecturerAssignedSubjectsPanel.java` hiện chỉ là khung rỗng và chưa được gắn vào dashboard.
- Thư mục `docs/` hiện có file tiêu đề nhưng nội dung tài liệu thiết kế gần như chưa được viết đầy đủ.
- Không thấy `src/test`, tức là chưa có bộ test tự động trong project hiện tại.

## 4. Chức năng chính của phần mềm
### 4.1. Nhóm chức năng quản trị viên
- Đăng nhập và vào dashboard quản trị.
- Xem tổng quan thống kê hệ thống.
- Quản lý sinh viên: thêm, sửa, xóa, tìm kiếm, lọc theo khoa, lớp, niên khóa, xem chi tiết, đổi mật khẩu tài khoản sinh viên.
- Quản lý giảng viên: thêm, sửa, xóa, lọc theo khoa, xem chi tiết, đổi mật khẩu tài khoản giảng viên.
- Quản lý khoa: thêm, sửa, xóa, lọc theo mã khoa.
- Quản lý lớp hành chính: thêm, sửa, xóa, lọc theo khoa hoặc niên khóa.
- Quản lý phòng học: thêm, sửa, xóa, tìm theo từ khóa.
- Quản lý môn học: thêm, sửa, xóa, lọc theo khoa.
- Quản lý học phần: thêm, sửa, xóa, lọc theo mã học phần, phòng học hoặc khoa.
- Quản lý đăng ký học phần: thêm, sửa, xóa, lọc theo học phần, lớp hoặc khoa.
- Quản lý điểm: thêm, sửa, xóa điểm; xem danh sách điểm theo sinh viên; lọc theo học phần/lớp; xem chi tiết từng môn.
- Quản lý lịch học: thêm, sửa, xóa lịch; lọc theo học phần, phòng học hoặc khoa.
- Xem báo cáo: sinh viên theo lớp, giảng viên theo khoa, sinh viên trong học phần, điểm theo học phần.
- Xuất PDF cho màn hình báo cáo.
- Đổi mật khẩu quản trị viên và đăng xuất.

### 4.2. Nhóm chức năng giảng viên
- Xem và cập nhật thông tin cá nhân.
- Đổi mật khẩu giảng viên.
- Xem học phần đang phụ trách.
- Xem danh sách sinh viên theo học phần phụ trách.
- Lọc danh sách sinh viên theo học phần.
- Xuất PDF danh sách sinh viên của học phần đang xem.
- Xem và nhập/sửa điểm cho sinh viên thuộc học phần được phân công.
- Tìm kiếm sinh viên theo mã hoặc họ tên trong danh sách điểm.
- Phân tích AI trên danh sách điểm đang lọc.
- Xem lịch dạy.
- Đăng xuất.

### 4.3. Nhóm chức năng sinh viên
- Xem dashboard cá nhân: số học phần, tổng tín chỉ, điểm trung bình, số buổi học.
- Xem thông tin cá nhân.
- Cập nhật thông tin liên hệ: email, số điện thoại, địa chỉ.
- Đổi mật khẩu sinh viên.
- Tìm kiếm và lọc học phần mở theo từ khóa và học kỳ.
- Đăng ký học phần.
- Hủy đăng ký học phần của chính mình.
- Xem danh sách học phần đã đăng ký.
- Xem bảng điểm cá nhân.
- Phân tích AI kết quả học tập cá nhân.
- Xem lịch học.
- Đăng xuất.

## 5. Phân quyền người dùng
Project dùng `Role`, `RoleEntity`, `SessionManager`, `AuthManager` và `RolePermission` để kiểm soát quyền.

| Vai trò | Quyền chính đã xác minh từ code |
|---|---|
| `ADMIN` | Quản lý sinh viên, giảng viên, khoa, lớp, môn học, học phần, đăng ký, điểm, lịch học; xem báo cáo; xem thống kê; xem/sửa hồ sơ cá nhân; xem lịch của mình |
| `LECTURER` | Xem/sửa hồ sơ cá nhân; xem lớp/học phần được phân công; xem sinh viên thuộc học phần phụ trách; nhập/xem điểm; xem lịch dạy |
| `STUDENT` | Xem/sửa hồ sơ cá nhân; đăng ký học phần; xem điểm; xem lịch học |

### 5.1. Ghi chú về xác thực và phiên đăng nhập
- Đăng nhập kiểm tra trên bảng `users`.
- Mật khẩu được băm bằng SHA-256 qua `PasswordHasher`.
- `AuthService` sẽ từ chối tài khoản có `users.active = false`.
- Phiên hiện tại được lưu trong `SessionManager`.
- `DashboardController` điều hướng tới màn hình Admin/Giảng viên/Sinh viên theo vai trò.

## 6. Kiến trúc, mô hình và công nghệ sử dụng
### 6.1. Kiến trúc tổng thể
Project đang theo mô hình phân lớp khá rõ:

`View (Swing)` -> `Controller` -> `Service` -> `DAO` -> `JPA/Hibernate` -> `MySQL`

Các thành phần hỗ trợ:

- `config`: tải cấu hình, khởi tạo JPA, quản lý session.
- `security`: xác thực, băm mật khẩu, bản đồ quyền.
- `dto`: chuẩn hóa dữ liệu hiển thị cho UI.
- `utils`: kiểm tra dữ liệu, định dạng năm học, xuất PDF, dialog, hiển thị văn bản.

### 6.2. Công nghệ đã dùng
- Ngôn ngữ: Java 17.
- Build tool: Maven.
- Giao diện: Java Swing.
- ORM: Hibernate 6.4.10.Final.
- JPA API: Jakarta Persistence 3.1.
- Database: MySQL 8.
- Cách truy vấn chính: JPQL trong các DAO.
- JDBC: có dùng trong `DBConnection.java` cho mục đích bootstrap/diagnostics legacy, không phải luồng runtime chính.
- Kết nối AI: `java.net.http.HttpClient` gọi Groq Chat Completions API.
- Xuất PDF: iText 5 (`com.itextpdf:itextpdf`).
- Logging runtime: `slf4j-simple`.

### 6.3. Ghi chú công nghệ quan trọng
- `application.properties` là file cấu hình tự đọc bằng `AppConfig`, không phải Spring Boot.
- `persistence.xml` dùng persistence unit `student-management-jpa`, transaction type `RESOURCE_LOCAL`.
- `JpaBootstrap` tự quản lý `EntityManagerFactory`, `EntityManager` và transaction.
- `jcalendar` có khai báo trong `pom.xml` và có file jar trong `lib/`, nhưng chưa thấy import/sử dụng trực tiếp trong source hiện tại.

## 7. Cấu trúc project
### 7.1. Cấu trúc thư mục mức cao

| Đường dẫn | Vai trò |
|---|---|
| `database/` | Chứa bộ script SQL tạo schema, seed dữ liệu và kiểm tra dữ liệu |
| `docs/` | Chứa tài liệu hỗ trợ; hiện tại đa số file mới ở mức tiêu đề |
| `lib/` | Chứa jar phụ trợ như MySQL Connector, iText, JCalendar |
| `src/main/java/com/qlsv/` | Toàn bộ mã nguồn Java chính |
| `src/main/resources/` | File cấu hình runtime |
| `target/` | Thư mục build artifact của Maven |
| `pom.xml` | Khai báo dependencies và cấu hình build Maven |
| `README.md` | Báo cáo tổng hợp project |

### 7.2. Cấu trúc package Java

| Package | Vai trò |
|---|---|
| `com.qlsv` | Chứa `Main.java`, entry point chính của ứng dụng |
| `com.qlsv.config` | Cấu hình ứng dụng, bootstrap JPA, session, utility kiểm tra migration |
| `com.qlsv.controller` | Lớp trung gian cho UI; điều phối service, tạo DTO hiển thị và xử lý dữ liệu form |
| `com.qlsv.dao` | Truy cập dữ liệu bằng JPA/JPQL |
| `com.qlsv.dto` | DTO/record để hiển thị ra bảng, dashboard, form |
| `com.qlsv.exception` | Ngoại lệ nghiệp vụ: xác thực, phân quyền, validation, lỗi chung |
| `com.qlsv.model` | Entity và model domain |
| `com.qlsv.navigation` | Điều hướng giữa màn hình login và dashboard |
| `com.qlsv.security` | Băm mật khẩu, kiểm tra vai trò/quyền |
| `com.qlsv.service` | Nghiệp vụ chính, validation và transaction |
| `com.qlsv.utils` | Hàm tiện ích định dạng, kiểm tra dữ liệu, dialog, PDF |
| `com.qlsv.view.admin` | Dashboard và màn hình nghiệp vụ quản trị |
| `com.qlsv.view.auth` | Màn hình đăng nhập và đổi mật khẩu |
| `com.qlsv.view.common` | Component UI dùng chung, theme, màu sắc, panel nền |
| `com.qlsv.view.dialog` | Form dialog và detail dialog cho CRUD |
| `com.qlsv.view.lecturer` | Dashboard và màn hình nghiệp vụ giảng viên |
| `com.qlsv.view.student` | Dashboard và màn hình nghiệp vụ sinh viên |

### 7.3. File và class quan trọng trong project

| File/Class | Vai trò |
|---|---|
| `src/main/java/com/qlsv/Main.java` | Entry point chính; kiểm tra khả năng bootstrap JPA và schema trước khi mở login |
| `src/main/java/com/qlsv/config/AppConfig.java` | Tải `application.properties` bằng UTF-8 |
| `src/main/java/com/qlsv/config/JpaBootstrap.java` | Khởi tạo Hibernate/JPA, thực thi transaction `RESOURCE_LOCAL` |
| `src/main/java/com/qlsv/config/SessionManager.java` | Lưu người dùng đang đăng nhập |
| `src/main/java/com/qlsv/config/DBConnection.java` | Utility JDBC legacy để kiểm tra kết nối/schema |
| `src/main/java/com/qlsv/config/StudentJpaMigrationVerifier.java` | Chương trình kiểm tra smoke test cho luồng JPA của Student |
| `src/main/java/com/qlsv/security/PasswordHasher.java` | Băm và đối chiếu mật khẩu SHA-256 |
| `src/main/java/com/qlsv/security/RolePermission.java` | Bản đồ quyền cho từng vai trò |
| `src/main/java/com/qlsv/service/GroqService.java` | Tích hợp Groq API |
| `src/main/java/com/qlsv/service/LecturerScoreAnalysisService.java` | Chuẩn bị snapshot và prompt AI cho giảng viên |
| `src/main/java/com/qlsv/utils/ValidationUtil.java` | Kiểm tra email, số điện thoại, độ dài, khoảng điểm |
| `src/main/java/com/qlsv/utils/AcademicFormatUtil.java` | Chuẩn hóa học kỳ và năm học |
| `src/main/java/com/qlsv/utils/PDFExportUtil.java` | Xuất dữ liệu bảng Swing ra PDF |
| `src/main/resources/application.properties` | Cấu hình DB, JPA, tên app và Groq API key |
| `src/main/resources/META-INF/persistence.xml` | Cấu hình persistence unit và danh sách entity |
| `database/00_drop_old_database.sql` | Xóa schema cũ |
| `database/01_create_schema.sql` | Tạo bảng, khóa, view, trigger, index |
| `database/02_seed_full_data.sql` | Seed dữ liệu mẫu |
| `database/03_verify_data.sql` | Script kiểm tra nhanh dữ liệu và schema |

### 7.4. Các màn hình/chức năng theo folder `view`
#### `view.admin`
- `AdminDashboardFrame`: khung dashboard quản trị.
- `AdminHomePanel`: màn hình chào và thống kê nhanh.
- `SystemStatisticsPanel`: 5 thẻ thống kê chính.
- `StudentManagementPanel`: CRUD sinh viên, lọc nhiều tiêu chí, đổi mật khẩu sinh viên.
- `LecturerManagementPanel`: CRUD giảng viên, lọc theo khoa, đổi mật khẩu giảng viên.
- `FacultyManagementPanel`: CRUD khoa.
- `ClassRoomManagementPanel`: CRUD lớp hành chính.
- `RoomManagementPanel`: CRUD phòng học.
- `SubjectManagementPanel`: CRUD môn học.
- `CourseSectionManagementPanel`: CRUD học phần.
- `EnrollmentManagementPanel`: CRUD đăng ký học phần.
- `ScoreManagementPanel`: quản lý điểm theo sinh viên và môn.
- `ScheduleManagementPanel`: quản lý lịch học.
- `ReportManagementPanel`: báo cáo và xuất PDF.

#### `view.lecturer`
- `LecturerDashboardFrame`: dashboard giảng viên.
- `LecturerProfilePanel`: xem/sửa hồ sơ, đổi mật khẩu.
- `LecturerCourseSectionPanel`: xem học phần phụ trách.
- `LecturerStudentListPanel`: xem/lọc sinh viên theo học phần, xuất PDF.
- `LecturerScorePanel`: nhập điểm, lọc theo học phần, tìm sinh viên, phân tích AI.
- `LecturerSchedulePanel`: xem lịch dạy.
- `LecturerAssignedSubjectsPanel`: hiện là lớp rỗng, chưa nối vào luồng chính.

#### `view.student`
- `StudentDashboardFrame`: dashboard sinh viên.
- `StudentHomePanel`: tổng quan học tập.
- `StudentProfilePanel`: xem/sửa liên hệ, đổi mật khẩu.
- `StudentEnrollmentPanel`: tìm kiếm, lọc, đăng ký/hủy học phần.
- `StudentRegisteredSubjectsPanel`: xem học phần đã đăng ký.
- `StudentScorePanel`: xem điểm và phân tích AI.
- `StudentSchedulePanel`: xem lịch học.

#### `view.common` và `view.dialog`
- `AppTheme`, `AppColors`, `DashboardCard`, `SidebarMenu`, `BaseFrame`, `BasePanel`: lớp nền giao diện dùng chung.
- `AbstractCrudPanel`: khung CRUD tái sử dụng cho nhiều màn hình quản trị.
- `*FormDialog`, `*DetailDialog`, `ChangePasswordDialog`, `AnalysisResultDialog`: dialog nhập liệu và xem chi tiết.

## 8. Cấu trúc database
### 8.1. Tổng quan schema
- Tên schema: `student_management`.
- Số bảng base đã xác minh từ `01_create_schema.sql`: 13.
- Số view: 2.
- Số trigger: 4.
- Số index tùy biến khai báo cuối script: 10.

### 8.2. Các nhóm bảng chính
- Nhóm phân quyền và tài khoản: `roles`, `users`.
- Nhóm danh mục đào tạo: `faculties`, `class_rooms`, `rooms`, `subjects`.
- Nhóm hồ sơ con người: `lecturers`, `students`.
- Nhóm tổ chức giảng dạy: `lecturer_subjects`, `course_sections`, `schedules`.
- Nhóm học vụ: `enrollments`, `scores`.

### 8.3. Mapping giữa bảng và entity/model

| Bảng | Entity/model trong code | Ghi chú |
|---|---|---|
| `roles` | `RoleEntity`, `Role` | `Role` là enum nghiệp vụ; `RoleEntity` là entity JPA |
| `users` | `User` | Tài khoản đăng nhập |
| `faculties` | `Faculty` | Khoa |
| `rooms` | `Room` | Phòng học |
| `class_rooms` | `ClassRoom` | Lớp hành chính |
| `subjects` | `Subject` | Môn học |
| `lecturers` | `Lecturer` | Giảng viên |
| `students` | `Student` | Sinh viên |
| `course_sections` | `CourseSection` | Học phần mở |
| `schedules` | `Schedule` | Lịch học/lịch dạy |
| `enrollments` | `Enrollment` | Đăng ký học phần |
| `scores` | `Score` | Điểm |
| `lecturer_subjects` | Không có entity riêng | Có trong schema nhưng chưa thấy luồng sử dụng trực tiếp |

### 8.4. View và trigger
#### View
- `vw_student_schedules`: tổng hợp lịch học theo sinh viên.
- `vw_section_scores`: tổng hợp điểm theo học phần và sinh viên.

#### Trigger
- `trg_lecturers_create_user`: tự tạo/liên kết `users` khi thêm giảng viên mới.
- `trg_students_create_user`: tự tạo/liên kết `users` khi thêm sinh viên mới.
- `trg_lecturers_update_user`: đồng bộ `username`, `full_name` của `users` khi sửa giảng viên.
- `trg_students_update_user`: đồng bộ `username`, `full_name` của `users` khi sửa sinh viên.

## 9. Chi tiết từng bảng trong database
### 9.1. Bảng `roles`
- Chức năng: lưu danh mục vai trò hệ thống.
- Cột chính:

| Cột | Ý nghĩa |
|---|---|
| `id` | Khóa chính tự tăng |
| `role_code` | Mã vai trò, ví dụ `ADMIN`, `LECTURER`, `STUDENT` |
| `role_name` | Tên hiển thị của vai trò |

- Khóa chính: `id`.
- Khóa ngoại: không có.
- Liên kết: `users.role_id` tham chiếu sang bảng này.
- Ghi chú logic: seed hiện có đúng 3 vai trò.

### 9.2. Bảng `users`
- Chức năng: tài khoản đăng nhập chung của toàn hệ thống.
- Cột chính:

| Cột | Ý nghĩa |
|---|---|
| `id` | Khóa chính tự tăng |
| `username` | Tên đăng nhập duy nhất |
| `password_hash` | Mật khẩu băm SHA-256 |
| `full_name` | Tên đầy đủ hiển thị |
| `email` | Email tài khoản |
| `role_id` | Vai trò của tài khoản |
| `active` | Trạng thái khóa/mở tài khoản |
| `created_at` | Thời gian tạo |

- Khóa chính: `id`.
- Khóa ngoại: `role_id -> roles.id`.
- Liên kết:
  - Một `users` có thể gắn 1 hồ sơ `lecturers`.
  - Một `users` có thể gắn 1 hồ sơ `students`.
- Ghi chú logic:
  - Đăng nhập luôn đi qua bảng này.
  - `AuthService` sẽ từ chối tài khoản có `active = false`.
  - Tài khoản sinh viên/giảng viên có thể được tạo tự động qua trigger SQL hoặc qua service khi thêm mới trong app.

### 9.3. Bảng `faculties`
- Chức năng: lưu thông tin khoa/viện.
- Cột chính:

| Cột | Ý nghĩa |
|---|---|
| `id` | Khóa chính |
| `faculty_code` | Mã khoa duy nhất |
| `faculty_name` | Tên khoa |
| `description` | Mô tả ngắn |
| `created_at` | Thời gian tạo |

- Khóa chính: `id`.
- Khóa ngoại: không có.
- Liên kết:
  - `class_rooms.faculty_id`
  - `subjects.faculty_id`
  - `lecturers.faculty_id`
  - `students.faculty_id`
- Ghi chú logic: là bảng danh mục trung tâm để lọc sinh viên, giảng viên, môn học, lớp, lịch và học phần.

### 9.4. Bảng `rooms`
- Chức năng: lưu phòng học/phòng thực hành/hội trường.
- Cột chính:

| Cột | Ý nghĩa |
|---|---|
| `id` | Khóa chính |
| `room_code` | Mã phòng duy nhất |
| `room_name` | Tên phòng |
| `created_at` | Thời gian tạo |

- Khóa chính: `id`.
- Khóa ngoại: không có.
- Liên kết: `schedules.room_id -> rooms.id`.
- Ghi chú logic: xung đột phòng được kiểm tra ở tầng service/DAO chứ không chỉ bằng unique constraint đơn giản.

### 9.5. Bảng `class_rooms`
- Chức năng: lưu lớp hành chính của sinh viên.
- Cột chính:

| Cột | Ý nghĩa |
|---|---|
| `id` | Khóa chính |
| `class_code` | Mã lớp duy nhất |
| `class_name` | Tên lớp |
| `academic_year` | Niên khóa lớp |
| `faculty_id` | Khoa quản lý lớp |
| `created_at` | Thời gian tạo |

- Khóa chính: `id`.
- Khóa ngoại: `faculty_id -> faculties.id`.
- Liên kết: `students.class_room_id -> class_rooms.id`.
- Ghi chú logic: được dùng mạnh trong lọc sinh viên, đăng ký học phần và báo cáo.

### 9.6. Bảng `subjects`
- Chức năng: lưu môn học trong chương trình đào tạo.
- Cột chính:

| Cột | Ý nghĩa |
|---|---|
| `id` | Khóa chính |
| `subject_code` | Mã môn duy nhất |
| `subject_name` | Tên môn |
| `credits` | Số tín chỉ |
| `faculty_id` | Khoa phụ trách |
| `description` | Mô tả môn học |
| `created_at` | Thời gian tạo |

- Khóa chính: `id`.
- Khóa ngoại: `faculty_id -> faculties.id`.
- Liên kết:
  - `course_sections.subject_id`
  - `lecturer_subjects.subject_id`
- Ghi chú logic:
  - Có check constraint `credits > 0`.
  - Khi sinh viên đăng ký, hệ thống chặn đăng ký 2 học phần khác nhau của cùng một môn.

### 9.7. Bảng `lecturers`
- Chức năng: hồ sơ giảng viên.
- Cột chính:

| Cột | Ý nghĩa |
|---|---|
| `id` | Khóa chính |
| `user_id` | Tài khoản đăng nhập liên kết |
| `lecturer_code` | Mã giảng viên duy nhất |
| `full_name` | Họ tên |
| `gender` | Giới tính |
| `email` | Email |
| `date_of_birth` | Ngày sinh |
| `phone` | Số điện thoại |
| `address` | Địa chỉ |
| `faculty_id` | Khoa phụ trách |
| `status` | Trạng thái hoạt động |
| `created_at` | Thời gian tạo |

- Khóa chính: `id`.
- Khóa ngoại:
  - `user_id -> users.id`
  - `faculty_id -> faculties.id`
- Liên kết:
  - `course_sections.lecturer_id`
  - `lecturer_subjects.lecturer_id`
- Ghi chú logic:
  - Nếu chưa có `user_id`, hệ thống có thể tự tạo tài khoản đăng nhập cho giảng viên.
  - Khi sửa giảng viên, tên và email tài khoản `users` cũng được đồng bộ.

### 9.8. Bảng `students`
- Chức năng: hồ sơ sinh viên.
- Cột chính:

| Cột | Ý nghĩa |
|---|---|
| `id` | Khóa chính |
| `user_id` | Tài khoản đăng nhập liên kết |
| `student_code` | Mã sinh viên duy nhất |
| `full_name` | Họ tên |
| `gender` | Giới tính |
| `date_of_birth` | Ngày sinh |
| `email` | Email |
| `phone` | Số điện thoại |
| `address` | Địa chỉ |
| `faculty_id` | Khoa |
| `class_room_id` | Lớp hành chính |
| `academic_year` | Niên khóa |
| `status` | Trạng thái |
| `created_at` | Thời gian tạo |

- Khóa chính: `id`.
- Khóa ngoại:
  - `user_id -> users.id`
  - `faculty_id -> faculties.id`
  - `class_room_id -> class_rooms.id`
- Liên kết:
  - `enrollments.student_id`
- Ghi chú logic:
  - Service chuẩn hóa prefix mã sinh viên về `SV`.
  - Có thể tự tạo tài khoản `users` nếu chưa có.
  - Cập nhật liên hệ của sinh viên sẽ đồng bộ email sang `users`.

### 9.9. Bảng `lecturer_subjects`
- Chức năng: bảng nối nhiều-nhiều giữa giảng viên và môn học.
- Cột chính:

| Cột | Ý nghĩa |
|---|---|
| `lecturer_id` | Giảng viên |
| `subject_id` | Môn học |

- Khóa chính: khóa ghép (`lecturer_id`, `subject_id`).
- Khóa ngoại:
  - `lecturer_id -> lecturers.id`
  - `subject_id -> subjects.id`
- Liên kết: mô tả giảng viên có thể dạy môn nào.
- Ghi chú logic:
  - Trong source code hiện tại chưa thấy entity riêng, DAO riêng, service riêng hay màn hình đang sử dụng trực tiếp bảng này.
  - Cũng chưa thấy dữ liệu seed chèn vào bảng này.
  - Suy ra đây là nền tảng mở rộng, chưa trở thành luồng nghiệp vụ chính ở working tree hiện tại.

### 9.10. Bảng `course_sections`
- Chức năng: lưu các lớp học phần mở cho từng môn theo học kỳ/năm học.
- Cột chính:

| Cột | Ý nghĩa |
|---|---|
| `id` | Khóa chính |
| `section_code` | Mã học phần duy nhất |
| `subject_id` | Môn học gốc |
| `lecturer_id` | Giảng viên phụ trách |
| `semester` | Học kỳ |
| `school_year` | Năm học |
| `max_students` | Sĩ số tối đa |
| `created_at` | Thời gian tạo |

- Khóa chính: `id`.
- Khóa ngoại:
  - `subject_id -> subjects.id`
  - `lecturer_id -> lecturers.id`
- Liên kết:
  - `schedules.course_section_id`
  - `enrollments.course_section_id`
- Ghi chú logic:
  - Có check constraint `max_students > 0`.
  - Trong mô hình JPA hiện tại, `CourseSection` có thêm `room` và `scheduleText` dạng `@Transient` để tương thích với các màn hình cũ; dữ liệu này được hydrate từ bảng `schedules`.
  - Việc đổi giảng viên của học phần sẽ bị chặn nếu lịch học hiện có gây xung đột với lịch dạy của giảng viên mới.

### 9.11. Bảng `schedules`
- Chức năng: lưu lịch học/lịch dạy chi tiết theo học phần.
- Cột chính:

| Cột | Ý nghĩa |
|---|---|
| `id` | Khóa chính |
| `course_section_id` | Học phần |
| `day_of_week` | Thứ học |
| `start_period` | Tiết bắt đầu |
| `end_period` | Tiết kết thúc |
| `room_id` | Phòng học |
| `note` | Ghi chú |
| `created_at` | Thời gian tạo |

- Khóa chính: `id`.
- Khóa ngoại:
  - `course_section_id -> course_sections.id`
  - `room_id -> rooms.id`
- Ràng buộc:
  - Unique: `uq_schedules_section_slot (course_section_id, day_of_week, start_period, room_id)`.
  - Check: `start_period > 0`, `end_period > 0`, `start_period <= end_period`.
- Liên kết: dùng để tạo lịch học sinh viên và lịch dạy giảng viên.
- Ghi chú logic:
  - Source code kiểm tra nghiêm hơn SQL: service yêu cầu `start_period < end_period`.
  - Trùng phòng và trùng lịch giảng viên được kiểm tra bằng truy vấn overlap trong `ScheduleDAO`, không chỉ dựa vào unique đơn giản.
  - `ScheduleManagementScreenController` còn chủ động thêm “schedule giả” cho học phần chưa có lịch để admin có thể nhìn thấy học phần chưa được xếp lịch.

### 9.12. Bảng `enrollments`
- Chức năng: lưu đăng ký học phần của sinh viên.
- Cột chính:

| Cột | Ý nghĩa |
|---|---|
| `id` | Khóa chính |
| `student_id` | Sinh viên đăng ký |
| `course_section_id` | Học phần đăng ký |
| `status` | Trạng thái đăng ký |
| `enrolled_at` | Thời điểm đăng ký |

- Khóa chính: `id`.
- Khóa ngoại:
  - `student_id -> students.id`
  - `course_section_id -> course_sections.id`
- Ràng buộc:
  - Unique: `uq_enrollment_student_section (student_id, course_section_id)`.
- Liên kết:
  - `scores.enrollment_id`
- Ghi chú logic:
  - Service chặn trùng cùng học phần.
  - Service chặn trùng môn học khác học phần.
  - Service chặn vượt sĩ số tối đa.
  - Service chặn trùng lịch với các học phần sinh viên đã đăng ký.
  - Sinh viên chỉ được hủy đăng ký của chính mình.

### 9.13. Bảng `scores`
- Chức năng: lưu điểm thành phần và kết quả học tập của từng đăng ký.
- Cột chính:

| Cột | Ý nghĩa |
|---|---|
| `id` | Khóa chính |
| `enrollment_id` | Đăng ký học phần tương ứng |
| `process_score` | Điểm quá trình |
| `midterm_score` | Điểm giữa kỳ |
| `final_score` | Điểm cuối kỳ |
| `total_score` | Điểm tổng kết |
| `result` | Kết quả `PASS`/`FAIL` |
| `updated_at` | Thời gian cập nhật cuối |

- Khóa chính: `id`.
- Khóa ngoại: `enrollment_id -> enrollments.id`.
- Ràng buộc:
  - Unique: `enrollment_id`.
  - Check điểm từ 0 đến 10 cho tất cả cột điểm.
- Liên kết: một đăng ký học phần có tối đa một bản ghi điểm.
- Ghi chú logic:
  - Tổng kết được tính ở tầng service theo công thức `0.3 * QT + 0.2 * GK + 0.5 * CK`.
  - `PASS` nếu `total_score >= 5.0`, ngược lại `FAIL`.
  - Với giảng viên, nếu sinh viên chưa có bản ghi điểm trong DB thì UI tạo placeholder tương thích để có thể nhập mới.

## 10. Thuật toán và chính sách xử lý chính
### 10.1. Đăng nhập và phân quyền
- `AuthService.login()` đọc `users` theo `username`.
- So khớp mật khẩu bằng `PasswordHasher.matches()` với SHA-256.
- Kiểm tra `users.active`; nếu bị khóa thì từ chối đăng nhập.
- `AuthManager.login()` lưu user vào `SessionManager`.
- `DashboardController.resolveDashboard()` điều hướng tới màn hình Admin/Giảng viên/Sinh viên.
- `PermissionService` kiểm tra quyền theo `RolePermission`.

### 10.2. Tìm kiếm và lọc
- Sinh viên: lọc theo khoa, lớp, niên khóa; tìm kiếm theo mã, tên, email.
- Giảng viên: lọc theo khoa.
- Lớp: lọc theo khoa, niên khóa.
- Môn học: lọc theo khoa.
- Học phần: lọc theo mã học phần, phòng học, khoa.
- Lịch học: lọc theo học phần, phòng học, khoa.
- Đăng ký: lọc theo học phần, lớp, khoa.
- Điểm:
  - Admin lọc theo học phần hoặc lớp.
  - Giảng viên lọc theo học phần, tìm thêm theo mã sinh viên/họ tên.
  - Admin `ScoreManagementPanel` còn chuẩn hóa text tìm kiếm bằng `Normalizer` để giảm ảnh hưởng dấu tiếng Việt.

### 10.3. Kiểm tra ràng buộc dữ liệu
- `ValidationUtil` kiểm tra:
  - Không để trống.
  - Độ dài tối đa.
  - Định dạng email.
  - Định dạng số điện thoại Việt Nam (`+84` hoặc `0`).
  - Khoảng điểm 0..10.
- `AcademicFormatUtil` chuẩn hóa:
  - Năm học dạng `yyyy - yyyy`.
  - Học kỳ chỉ nhận `HK1`, `HK2`, `HK3`.
- `DateUtil` kiểm tra ngày theo `yyyy-MM-dd`.

### 10.4. Kiểm tra trùng mã và đồng bộ tài khoản
- Mã sinh viên được chuẩn hóa prefix `SV`.
- Mã giảng viên được chuẩn hóa prefix `GV`.
- Khi thêm sinh viên/giảng viên mới:
  - Nếu chưa có `user_id`, service sẽ tìm `users.username` tương ứng.
  - Nếu chưa có tài khoản thì tự tạo tài khoản mới với mật khẩu mặc định `123456`.
- Trigger SQL cũng hỗ trợ tự tạo tài khoản ở mức database khi chèn trực tiếp vào bảng `students` hoặc `lecturers`.
- Khi cập nhật hồ sơ sinh viên/giảng viên, `users.full_name` và/hoặc `users.email` được đồng bộ lại.

### 10.5. Kiểm tra trùng lịch, trùng phòng, trùng đăng ký
- Trùng phòng:
  - `ScheduleDAO.hasRoomScheduleConflict()` kiểm tra overlap theo phòng, thứ và khoảng tiết.
- Trùng lịch giảng viên:
  - `ScheduleDAO.hasLecturerScheduleConflict()` kiểm tra overlap giữa các lịch dạy.
- Trùng lịch sinh viên khi đăng ký:
  - `ScheduleDAO.hasStudentScheduleConflict()` so sánh lịch của học phần mới với các học phần sinh viên đã đăng ký.
- Trùng đăng ký:
  - Không cho đăng ký cùng học phần hai lần.
  - Không cho đăng ký hai học phần khác nhau của cùng một môn.

### 10.6. Logic tính điểm và xếp loại
- Công thức tổng kết: `QT * 0.3 + GK * 0.2 + CK * 0.5`.
- Làm tròn 2 chữ số thập phân.
- Xếp loại:
  - `PASS` nếu `total_score >= 5.0`.
  - `FAIL` nếu `total_score < 5.0`.
- Nếu giảng viên đang xem danh sách có sinh viên chưa có bản ghi điểm, hệ thống hiển thị placeholder 0.0 để nhập mới.

### 10.7. Logic AI phân tích dữ liệu
#### Cho sinh viên
- `StudentScorePanel` gọi `GroqService.analyzeScores(currentScores)`.
- Dữ liệu gửi đi gồm danh sách JSON rút gọn theo môn, tổng kết và kết quả.
- Kết quả trả về là nhận xét/gợi ý học tập bằng tiếng Việt.

#### Cho giảng viên
- `LecturerScoreAnalysisService.prepareSnapshot()`:
  - Chỉ cho phép phân tích khi toàn bộ sinh viên trong danh sách hiện tại đã có điểm đầy đủ.
  - Tính số lượng sinh viên, điểm trung bình, min, max, số đạt, số chưa đạt.
  - Tạo prompt AI theo vai trò giảng viên.
- `analyzeSnapshot()` gọi `GroqService.requestAnalysis()`.
- Nếu thiếu điểm hoặc chưa có bản ghi điểm thật, hệ thống sẽ từ chối phân tích và báo rõ danh sách sinh viên còn thiếu.

## 11. Các luồng hoạt động chính của chương trình
### 11.1. Luồng khởi tạo từ SQL đến khi vào hệ thống
1. Chạy `00_drop_old_database.sql` nếu muốn reset hoàn toàn dữ liệu cũ.
2. Chạy `01_create_schema.sql` để tạo schema, bảng, view, trigger, index.
3. Chạy `02_seed_full_data.sql` để nạp dữ liệu mẫu.
4. Chạy `03_verify_data.sql` để kiểm tra nhanh số lượng bảng, dữ liệu, trigger, view và xung đột lịch.
5. Cấu hình lại `application.properties`.
6. Chạy `Main.java`.
7. `Main` gọi `JpaBootstrap.canBootstrap()` và `JpaBootstrap.hasRequiredSchema()`.
8. Nếu kết nối DB và schema hợp lệ, hệ thống mở `LoginFrame`.
9. Người dùng đăng nhập.
10. `DashboardController` điều hướng theo vai trò:
   - `ADMIN` -> `AdminDashboardFrame`
   - `LECTURER` -> `LecturerDashboardFrame`
   - `STUDENT` -> `StudentDashboardFrame`

### 11.2. Luồng quản lý sinh viên
1. Admin vào `StudentManagementPanel`.
2. Chọn điều kiện lọc: tất cả, theo khoa, theo lớp, theo niên khóa.
3. Tìm kiếm theo từ khóa nếu cần.
4. Thêm/sửa sinh viên qua `StudentFormDialog`.
5. `StudentManagementScreenController` chuẩn hóa dữ liệu form.
6. `StudentService` kiểm tra email, số điện thoại, niên khóa, mã SV, khoa, lớp.
7. Nếu sinh viên chưa có user, service tự tạo tài khoản login hoặc liên kết tài khoản có sẵn đúng vai trò.
8. Dữ liệu được ghi qua JPA transaction.
9. Admin có thể đổi mật khẩu tài khoản sinh viên từ chính màn hình này.

### 11.3. Luồng quản lý giảng viên
1. Admin vào `LecturerManagementPanel`.
2. Lọc theo khoa hoặc xem tất cả.
3. Thêm/sửa hồ sơ giảng viên qua `LecturerFormDialog`.
4. `LecturerService` kiểm tra mã GV, ngày sinh, email, số điện thoại, khoa.
5. Nếu chưa có user, service tạo tài khoản hoặc liên kết tài khoản sẵn có đúng vai trò giảng viên.
6. Admin có thể đổi mật khẩu tài khoản giảng viên.
7. Giảng viên tự đăng nhập để chỉnh sửa thông tin liên hệ của chính mình trong `LecturerProfilePanel`.

### 11.4. Luồng quản lý môn học, học phần, lịch học
1. Admin quản lý môn học trong `SubjectManagementPanel`.
2. Admin tạo học phần trong `CourseSectionManagementPanel` bằng cách chọn môn học, giảng viên, học kỳ, năm học, sĩ số.
3. `CourseSectionService` kiểm tra:
   - Môn học có tồn tại.
   - Giảng viên có tồn tại.
   - Học kỳ và năm học hợp lệ.
   - Sĩ số tối đa lớn hơn 0.
4. Sau đó admin gán lịch học trong `ScheduleManagementPanel`.
5. `ScheduleService` kiểm tra:
   - Học phần hợp lệ.
   - Phòng học hợp lệ.
   - `start_period < end_period`.
   - Không trùng phòng.
   - Không trùng lịch dạy của giảng viên.
6. Các màn hình học phần, đăng ký và điểm lấy `scheduleText`/`room` từ bảng `schedules` thông qua trường `@Transient` của `CourseSection`.

### 11.5. Luồng đăng ký học phần của sinh viên
1. Sinh viên vào `StudentEnrollmentPanel`.
2. Tìm kiếm học phần mở theo từ khóa và học kỳ.
3. Chọn học phần muốn đăng ký.
4. `StudentEnrollmentScreenController` gọi `EnrollmentController.registerCurrentStudent()`.
5. `EnrollmentService` kiểm tra:
   - Sinh viên tồn tại.
   - Học phần tồn tại.
   - Chưa đăng ký học phần này.
   - Chưa đăng ký học phần khác của cùng môn.
   - Chưa vượt sĩ số tối đa.
   - Không trùng lịch với các học phần hiện có.
6. Nếu hợp lệ, tạo bản ghi `enrollments`.
7. Sinh viên có thể hủy đăng ký của chính mình ở bảng đã đăng ký.

### 11.6. Luồng nhập và quản lý điểm
#### Phía giảng viên
1. Giảng viên vào `LecturerScorePanel`.
2. Hệ thống lấy tất cả enrollments của các học phần giảng viên phụ trách.
3. Hệ thống ghép với bảng `scores`.
4. Nếu enrollment chưa có điểm, tạo placeholder để giảng viên nhập mới.
5. Khi lưu:
   - Kiểm tra giảng viên có đúng quyền trên học phần đó.
   - Kiểm tra điểm thành phần nằm trong khoảng 0..10.
   - Tính tổng kết 30/20/50.
   - Xác định `PASS`/`FAIL`.

#### Phía quản trị viên
1. Admin vào `ScoreManagementPanel`.
2. Lọc theo học phần hoặc lớp.
3. Hệ thống gom điểm theo từng sinh viên để hiển thị bảng tổng quan.
4. Chọn sinh viên để xem danh sách điểm chi tiết theo môn.
5. Admin có thể thêm, sửa, xóa bản ghi điểm từ form riêng.

### 11.7. Luồng AI phân tích dữ liệu
#### AI cho sinh viên
1. Sinh viên mở `StudentScorePanel`.
2. Nhấn nút `Phân tích điểm (AI)`.
3. `GroqService` gửi danh sách điểm hiện có lên Groq.
4. Kết quả trả về hiển thị trong dialog dạng văn bản.

#### AI cho giảng viên
1. Giảng viên lọc dữ liệu trong `LecturerScorePanel`.
2. Nhấn `Phân tích điểm`.
3. `LecturerScoreAnalysisService` kiểm tra toàn bộ danh sách đã có điểm đầy đủ.
4. Tạo snapshot thống kê và prompt phân tích.
5. Gửi lên Groq.
6. Hiển thị kết quả trong `AnalysisResultDialog`.

## 12. Hướng dẫn sử dụng phần mềm
### 12.1. Yêu cầu môi trường
- Java 17.
- Maven.
- MySQL 8.
- Kết nối Internet nếu muốn dùng chức năng AI Groq.

### 12.2. Chạy SQL theo thứ tự
Thứ tự khuyến nghị đã được chính project ghi trong `database/README.md`:

1. `database/00_drop_old_database.sql`
2. `database/01_create_schema.sql`
3. `database/02_seed_full_data.sql`
4. `database/03_verify_data.sql`

Ghi chú:
- File `00_drop_old_database.sql` là bước reset, có thể bỏ qua nếu không muốn xóa toàn bộ dữ liệu cũ.
- Nếu chạy từ đầu cho môi trường mới thì nên chạy đủ `00 -> 03`.

### 12.3. Cấu hình database ở đâu
Chỉnh file:

`src/main/resources/application.properties`

Các khóa cấu hình quan trọng:

```properties
db.url=jdbc:mysql://localhost:3306/student_management
db.username=root
db.password=123456
app.name=Hệ Thống Quản Lý Sinh Viên
jpa.persistence.unit=student-management-jpa
jpa.hibernate.dialect=org.hibernate.dialect.MySQLDialect
jpa.hibernate.ddl-auto=none
jpa.show-sql=false
groq.api.key=...
```

Ghi chú:
- Nếu không dùng AI, hệ thống nghiệp vụ cốt lõi vẫn có thể chạy; riêng chức năng phân tích AI sẽ báo lỗi cấu hình khi gọi.
- `groq.api.key` hiện đang được đặt trực tiếp trong file cấu hình. Đây là cách phù hợp cho demo/đồ án, nhưng chưa tốt cho môi trường production.

### 12.4. Chạy chương trình
Cách bám sát project hiện tại nhất:

1. Import project như một Maven project trong IDE.
2. Bảo đảm MySQL đang chạy và database đã được tạo theo bộ SQL trên.
3. Mở file `src/main/java/com/qlsv/Main.java`.
4. Run `Main.java` như một Java Application.

Ghi chú:
- `pom.xml` hiện chưa cấu hình plugin chạy desktop app trực tiếp, nên cách thực tế nhất là chạy từ IDE.
- `Main.java` sẽ dừng ở bước khởi động nếu không kết nối được DB hoặc schema chưa đúng.

### 12.5. Tài khoản admin để test
Đã xác minh trực tiếp từ `database/02_seed_full_data.sql`:

- Admin: `admin` / `123456`

Ngoài ra seed còn có:

- Giảng viên: `gv001`, `gv002`, `gv003` / `123456`
- Sinh viên: `sv2200001` đến `sv2200006` / `123456`

Khi thêm sinh viên hoặc giảng viên mới từ ứng dụng:
- Nếu hệ thống tự tạo tài khoản login mới, mật khẩu mặc định cũng là `123456`.

## 13. Tài khoản test đã xác minh từ project
### 13.1. Theo SQL seed

| Vai trò | Tài khoản | Mật khẩu |
|---|---|---|
| Admin | `admin` | `123456` |
| Giảng viên | `gv001`, `gv002`, `gv003` | `123456` |
| Sinh viên | `sv2200001` đến `sv2200006` | `123456` |

### 13.2. Theo logic code
- Mật khẩu mặc định được băm SHA-256 qua `PasswordHasher`.
- Trigger SQL và service Java đều đang dùng cùng mật khẩu mặc định `123456` khi tự tạo user cho sinh viên/giảng viên.

## 14. Đánh giá đồ án dưới góc độ đồ án cuối kì
### 14.1. Ưu điểm
- Có phạm vi nghiệp vụ khá đầy đủ cho một hệ thống quản lý đào tạo cơ bản.
- Phân vai rõ ràng giữa Admin, Giảng viên và Sinh viên.
- Cấu trúc phân lớp tương đối tốt, dễ đọc và dễ bảo trì.
- Dùng JPA/Hibernate và JPQL thay vì viết toàn bộ SQL thủ công trong ứng dụng.
- Có kiểm tra ràng buộc nghiệp vụ quan trọng: trùng lịch, trùng phòng, trùng đăng ký, vượt sĩ số, định dạng dữ liệu.
- Có trigger SQL và service Java cùng hỗ trợ tự tạo tài khoản người dùng.
- Có báo cáo và xuất PDF.
- Có tích hợp AI thực tế, không chỉ dừng ở ý tưởng.

### 14.2. Hạn chế
- Chưa có test tự động.
- Một số file tài liệu trong `docs/` mới ở mức tiêu đề.
- Có vài thành phần mở rộng chưa nối vào luồng chính như `lecturer_subjects`, `LecturerAssignedSubjectsPanel`.
- `application.properties` đang chứa thông tin kết nối DB và Groq API key trực tiếp.
- Chưa thấy màn hình quản lý trực tiếp `users` và `roles`.
- Có dependency `jcalendar` nhưng chưa thấy sử dụng trực tiếp trong source hiện tại.
- Một số lớp vẫn mang dấu vết chuyển đổi từ mô hình cũ sang schema mới, ví dụ `CourseSection.room` và `scheduleText` đang là `@Transient` để tương thích.

### 14.3. Mức độ hoàn thiện
Xét theo phạm vi đồ án cuối kì, project đang ở mức khá hoàn thiện đối với các luồng nghiệp vụ cốt lõi:

- Có bộ dữ liệu seed và có thể đăng nhập thử ngay.
- Có đầy đủ các luồng chính của quản lý đào tạo cơ bản.
- Có cả phần quản trị, phần giảng viên và phần sinh viên.
- Có phần AI đã chạy được về mặt kiến trúc tích hợp.

Tuy nhiên, để đạt mức hoàn thiện cao hơn theo tiêu chuẩn sản phẩm thực tế, project vẫn cần bổ sung test, tài liệu và cơ chế quản lý cấu hình an toàn hơn.

### 14.4. Khả năng mở rộng
- Bổ sung màn hình quản lý người dùng và vai trò.
- Kích hoạt đầy đủ bảng `lecturer_subjects` để quản lý phân công dạy theo môn.
- Bổ sung import/export Excel.
- Thêm cảnh báo, thông báo lịch học, nhắc học vụ.
- Tách cấu hình bí mật sang biến môi trường.
- Thêm log hệ thống, audit thao tác và test tự động.
- Có thể mở rộng từ desktop sang web/API vì tầng service/DAO đã tương đối tách lớp.

### 14.5. Hướng phát triển
- Bổ sung dashboard phân tích sâu hơn cho Admin.
- Thêm xếp loại học lực, GPA, CPA, học vụ cảnh báo.
- Thêm quản lý học phí, cố vấn học tập, lớp cố định theo học kỳ.
- Tăng chất lượng AI bằng prompt chuẩn hóa, lưu lịch sử phân tích và cho phép phân tích theo từng môn/học kỳ.
- Chuẩn hóa tài liệu thiết kế trong `docs/`.

## 15. Ghi chú xác minh và hiện trạng
- Đã xác minh `Main.java` là entry point chính cho runtime desktop.
- Đã xác minh project hiện chạy theo Java Swing + JPA/Hibernate + MySQL, không phải Spring Boot.
- Đã xác minh có 3 vai trò thực sự đang dùng: Admin, Giảng viên, Sinh viên.
- Đã xác minh có 13 bảng base, 2 view và 4 trigger trong schema hiện tại.
- Đã xác minh có dữ liệu seed cho tài khoản demo.
- Đã xác minh có tích hợp Groq AI ở cả luồng sinh viên và giảng viên.
- Đã xác minh có xuất PDF ở màn hình báo cáo của Admin và danh sách sinh viên của Giảng viên.
- Không thấy `src/test`, nên không thể khẳng định project đã có kiểm thử tự động.
- Không thấy dữ liệu seed hoặc luồng nghiệp vụ trực tiếp cho `lecturer_subjects`; nhận định đây là phần nền tảng mở rộng là suy ra từ hiện trạng source và SQL.
