# Student Management System

Project Java Swing + Hibernate/JPA + JPQL + MySQL 8 cho hệ thống quản lý sinh viên theo 3 vai trò: `ADMIN`, `LECTURER`, `STUDENT`.

Trạng thái hiện tại:

- Đã hoàn thiện phần lớn luồng chính để test hệ thống.
- Đã bổ sung schema database, dữ liệu mẫu, phân quyền, CRUD, đăng ký học phần, điểm, lịch học và báo cáo cơ bản.
- UI Swing đủ dùng để test chức năng, không tập trung vào giao diện đẹp.

## Công nghệ sử dụng

- Java 17
- Maven
- Java Swing
- Hibernate / JPA
- JPQL
- MySQL 8
- iText PDF

## Cấu trúc project

- `database/`: script tạo database, dữ liệu mẫu, views, indexes và truy vấn test
- `src/main/java/com/qlsv/config`: đọc config, kết nối DB, session
- `src/main/java/com/qlsv/model`: model dữ liệu và DTO thống kê
- `src/main/java/com/qlsv/dao`: JPA/JPQL cho các module nghiệp vụ chính, DAO JDBC chỉ còn lại ở phần báo cáo/hạ tầng ngoài scope
- `src/main/java/com/qlsv/service`: xử lý nghiệp vụ, validate, phân quyền, tính điểm, chặn trùng lịch
- `src/main/java/com/qlsv/controller`: lớp nối giữa UI và service
- `src/main/java/com/qlsv/view`: Swing frame/panel/dialog
- `src/main/java/com/qlsv/security`: hash mật khẩu và mapping quyền
- `src/main/java/com/qlsv/utils`: dialog, validate, PDF export
- `src/main/resources`: `application.properties`

## Các chức năng đã hoàn thành

### 1. Khởi động, config và kết nối DB

- Đọc `db.url`, `db.username`, `db.password`, `app.name` từ `application.properties`
- Kiểm tra kết nối MySQL ngay lúc khởi động trong `Main`
- Quản lý session đăng nhập bằng `SessionManager`

### 2. Đăng nhập và phân quyền

- Đăng nhập bằng `username/password`
- Mật khẩu lưu dạng SHA-256
- Phân quyền theo enum `Role { ADMIN, LECTURER, STUDENT }`
- Điều hướng đúng dashboard theo role
- Đăng xuất và xóa session
- Kiểm tra quyền tập trung tại `AuthManager` + `RolePermission` + `PermissionService`

### 3. CRUD admin

- Quản lý sinh viên
- Quản lý giảng viên
- Quản lý khoa
- Quản lý lớp
- Quản lý môn học
- Quản lý học phần
- Quản lý enrollment
- Quản lý điểm
- Quản lý lịch học

Tất cả các panel CRUD đã có:

- JTable
- Thêm / sửa / xóa
- Tải lại dữ liệu
- Tìm kiếm client-side trên dữ liệu đã nạp

### 4. Module sinh viên

- Xem hồ sơ cá nhân
- Đăng ký học phần
- Hủy đăng ký học phần của chính mình
- Xem học phần đã đăng ký
- Xem bảng điểm cá nhân
- Xem lịch học

### 5. Module giảng viên

- Xem hồ sơ cá nhân
- Xem học phần phụ trách
- Xem danh sách sinh viên thuộc học phần mình dạy
- Nhập / cập nhật điểm cho học phần được phân công
- Xem lịch dạy

### 6. Đăng ký học phần

- Tách rõ `Subject` / `CourseSection` / `Enrollment`
- Chặn đăng ký trùng học phần
- Chặn vượt sĩ số tối đa
- Chặn trùng lịch dựa trên bảng `schedules`
- Cho phép admin tạo enrollment thủ công
- Cho phép student đăng ký và hủy học phần của chính mình

### 7. Quản lý điểm

- Admin xem và quản lý toàn bộ điểm
- Giảng viên chỉ nhập / sửa điểm cho học phần mình phụ trách
- Tự động tính:

```text
totalScore = processScore * 0.3 + midtermScore * 0.2 + finalScore * 0.5
```

- Tự động xác định:
  - `PASS` nếu `totalScore >= 5.0`
  - `FAIL` nếu `< 5.0`

### 8. Lịch học / lịch dạy

- Thêm bảng `schedules`
- Admin quản lý lịch học theo học phần
- Student xem lịch học của mình
- Lecturer xem lịch dạy của mình
- Chặn trùng lịch theo giảng viên, phòng học và sinh viên

### 9. Báo cáo cơ bản

- Danh sách sinh viên theo lớp
- Danh sách giảng viên theo khoa
- Danh sách sinh viên trong học phần
- Bảng điểm theo học phần
- Thống kê nhanh tổng sinh viên / giảng viên / môn học / học phần / enrollment
- Xuất PDF từ màn hình báo cáo

## Những thay đổi mới đã thực hiện

### Đã sửa / hoàn thiện các file chính

- `src/main/java/com/qlsv/Main.java`
- `src/main/java/com/qlsv/config/DBConnection.java`
- `src/main/java/com/qlsv/security/RolePermission.java`
- `src/main/java/com/qlsv/utils/ValidationUtil.java`
- `src/main/java/com/qlsv/utils/PDFExportUtil.java`
- `src/main/java/com/qlsv/view/common/AbstractCrudPanel.java`
- `src/main/java/com/qlsv/view/common/BaseFrame.java`
- `src/main/java/com/qlsv/service/StudentService.java`
- `src/main/java/com/qlsv/service/LecturerService.java`
- `src/main/java/com/qlsv/service/ClassRoomService.java`
- `src/main/java/com/qlsv/service/FacultyService.java`
- `src/main/java/com/qlsv/service/SubjectService.java`
- `src/main/java/com/qlsv/service/CourseSectionService.java`
- `src/main/java/com/qlsv/service/EnrollmentService.java`
- `src/main/java/com/qlsv/service/ScoreService.java`
- `src/main/java/com/qlsv/service/ScheduleService.java`
- `src/main/java/com/qlsv/service/ReportService.java`
- `src/main/java/com/qlsv/controller/EnrollmentController.java`
- `src/main/java/com/qlsv/controller/ScheduleController.java`
- `src/main/java/com/qlsv/controller/ReportController.java`
- `src/main/java/com/qlsv/dao/StudentDAO.java`
- `src/main/java/com/qlsv/dao/LecturerDAO.java`
- `src/main/java/com/qlsv/dao/FacultyDAO.java`
- `src/main/java/com/qlsv/dao/ClassRoomDAO.java`
- `src/main/java/com/qlsv/dao/SubjectDAO.java`
- `src/main/java/com/qlsv/dao/CourseSectionDAO.java`
- `src/main/java/com/qlsv/dao/EnrollmentDAO.java`
- `src/main/java/com/qlsv/dao/ScoreDAO.java`
- `src/main/java/com/qlsv/model/User.java`
- `src/main/java/com/qlsv/model/Faculty.java`
- `src/main/java/com/qlsv/model/ClassRoom.java`
- `src/main/java/com/qlsv/model/Student.java`
- `src/main/java/com/qlsv/model/Lecturer.java`
- `src/main/java/com/qlsv/model/Subject.java`
- `src/main/java/com/qlsv/model/CourseSection.java`
- `src/main/java/com/qlsv/model/Enrollment.java`
- `src/main/java/com/qlsv/model/Schedule.java`
- `src/main/java/com/qlsv/view/admin/AdminDashboardFrame.java`
- `src/main/java/com/qlsv/view/admin/AdminHomePanel.java`
- `src/main/java/com/qlsv/view/admin/ScheduleManagementPanel.java`
- `src/main/java/com/qlsv/view/admin/ReportManagementPanel.java`
- `src/main/java/com/qlsv/view/admin/SystemStatisticsPanel.java`
- `src/main/java/com/qlsv/view/lecturer/LecturerDashboardFrame.java`
- `src/main/java/com/qlsv/view/lecturer/LecturerCourseSectionPanel.java`
- `src/main/java/com/qlsv/view/lecturer/LecturerStudentListPanel.java`
- `src/main/java/com/qlsv/view/lecturer/LecturerSchedulePanel.java`
- `src/main/java/com/qlsv/view/student/StudentDashboardFrame.java`
- `src/main/java/com/qlsv/view/student/StudentEnrollmentPanel.java`
- `src/main/java/com/qlsv/view/student/StudentRegisteredSubjectsPanel.java`
- `src/main/java/com/qlsv/view/student/StudentResultPanel.java`
- `src/main/java/com/qlsv/view/student/StudentSchedulePanel.java`
- `database/00_drop_old_database.sql`
- `database/01_create_schema.sql`
- `database/02_seed_full_data.sql`
- `database/03_verify_data.sql`
- `database/README.md`

### Đã thêm file / module mới

- `src/main/java/com/qlsv/dao/ReportDAO.java`
- `src/main/java/com/qlsv/model/SystemStatistics.java`
- `src/main/java/com/qlsv/controller/RoomController.java`
- `src/main/java/com/qlsv/dao/RoomDAO.java`
- `src/main/java/com/qlsv/model/Room.java`
- `src/main/java/com/qlsv/service/RoomService.java`
- `src/main/java/com/qlsv/view/admin/RoomManagementPanel.java`

### Đã bổ sung module

- Module `schedule` đầy đủ ở mức dùng để test
- Module `report` và `system statistics`
- Luồng xuất PDF cho báo cáo
- Validate email / phone / score
- Chặn trùng lịch cho đăng ký học phần
- Footer / dashboard role-based đầy đủ hơn

## Hướng dẫn cấu hình `application.properties`

Mở file:

- `src/main/resources/application.properties`

Nội dung mặc định:

```properties
db.url=jdbc:mysql://localhost:3306/student_management
db.username=root
db.password=123456
app.name=Student Management System
```

Cần sửa lại `db.username` / `db.password` nếu MySQL của bạn dùng tài khoản khác.

## Hướng dẫn tạo database

### Thứ tự chạy SQL khuyến nghị

Các script tạo DB hiện đang được gộp theo luồng sau (chuẩn bị MySQL trước):

1. (Tùy chọn) Nếu muốn reset dữ liệu cũ: `database/00_drop_old_database.sql`
2. Tạo schema (tạo bảng + view + index): `database/01_create_schema.sql`
3. Nạp dữ liệu mẫu: `database/02_seed_full_data.sql`
4. Kiểm tra nhanh sau khi nạp: `database/03_verify_data.sql`

Lưu ý: `02_seed_full_data.sql` là script `INSERT INTO` (không dùng `ON DUPLICATE KEY`), nên nếu DB đã có dữ liệu bạn nên reset bằng `00_drop_old_database.sql` trước khi chạy lại để tránh trùng khóa.

### Các bảng chính

- `roles`
- `users`
- `faculties`
- `class_rooms`
- `rooms`
- `students`
- `lecturers`
- `subjects`
- `lecturer_subjects`
- `course_sections`
- `schedules`
- `enrollments`
- `scores`

## Tài khoản mẫu để test

Tất cả tài khoản đều dùng mật khẩu:

- Password: `123456`

Danh sách:

- `admin`
- `gv001`
- `gv002`
- `gv003`
- `sv2200001`
- `sv2200002`
- `sv2200003`

## Cách chạy project

### Cách 1: Chạy bằng IDE

1. Clone repo
2. Import project Maven vào IntelliJ IDEA / Eclipse / NetBeans
3. Đảm bảo MySQL đang chạy và database đã import xong (chạy các script trong `database/`)
4. Cập nhật `src/main/resources/application.properties` (đặc biệt `db.url`, `db.username`, `db.password`)
5. Mở file `src/main/java/com/qlsv/Main.java` và Run `main`

### Cách 2: Compile / run thủ công trên Windows PowerShell

```powershell
mvn clean compile
```

Sau khi compile xong, bạn có thể Run `com.qlsv.Main` từ IDE (hoặc tự thiết lập classpath theo Maven).

Nếu máy đã cài Maven và muốn chỉ compile nhanh (không bao gồm run):

```powershell
mvn clean compile
```

## Cấu hình để chạy ổn định trên nhiều IDE

- Java: yêu cầu JDK `17` trở lên (project dùng các tính năng Java mới như text block/pattern matching/stream `toList()`).
- Maven build: `pom.xml` đã được cấu hình để `maven-compiler-plugin` build theo `release 17` nhằm tránh tình trạng IDE dùng JDK thấp hơn làm fail compile.
- Config `application.properties`:
  - Ứng dụng tải cấu hình bắt buộc từ `classpath` (nằm trong `src/main/resources` khi build bằng Maven).
  - Đã loại bỏ fallback đọc file theo đường dẫn filesystem (`src/main/resources/...`) để giảm phụ thuộc vào `working directory` của IDE.
- Khởi động ứng dụng & DB:
  - `Main` luôn mở `LoginFrame` ngay cả khi DB không kết nối được/thiếu schema, thay vì “silent fail” hoặc dừng luồng app.
  - `DBConnection` bổ sung log chi tiết khi không connect được MySQL hoặc thiếu table/cột schema, để dễ debug từ IDE khác.

## Ghi chú kỹ thuật quan trọng

- Mật khẩu đăng nhập được hash bằng SHA-256
- Kiểm tra phân quyền tập trung tại `RolePermission` + `PermissionService`
- `ScoreService` tự động tính điểm tổng kết và `PASS/FAIL`
- `ScheduleService` + `ScheduleDAO` xử lý kiểm tra trùng lịch
- `AbstractCrudPanel` là lớp chung cho nhiều màn hình CRUD
- `ReportDAO` tổng hợp dữ liệu báo cáo để UI không truy vấn trực tiếp
- `DBConnection` kiểm tra kết nối MySQL ngay khi khởi động

## Trạng thái Hibernate/JPA

- Đã migrate sang JPA/JPQL: `Faculty`, `Room`, `ClassRoom`, `Subject`, `User/Auth`, `Student`, `CourseSection`, `Schedule`, `Enrollment`, `Score`
- Core flow đã chạy theo hướng `JPA-first`; luồng tạo/cập nhật `Student` và `Lecturer` đồng bộ `User` bằng transaction JPA, không còn rely vào trigger trong code
- Transaction strategy:
  - `@Transactional` được đánh dấu tại service layer cho các use-case ghi
  - các flow ghi nhiều bảng như `Student -> User` và `Lecturer -> User` được gom về 1 transaction JPA rõ ràng ở service
- Compatibility tạm thời:
  - `CourseSection.room`
  - `CourseSection.scheduleText`
  - placeholder `Score` cho enrollment chưa có bản ghi điểm
- Native query còn lại:
  - Không còn native query trong service/DAO nghiệp vụ đã migrate
  - JDBC chỉ còn ở `ReportDAO` và `DBConnection` cho phần báo cáo/kiểm tra kết nối ngoài scope cleanup hiện tại

## Giới hạn hiện tại / phần chưa tối ưu

- Chưa có luồng tạo tài khoản login mới đầy đủ trên UI; khi thêm sinh viên / giảng viên mới, trường `user_id` vẫn là liên kết tới `users` có sẵn
- Tìm kiếm trong các màn hình CRUD hiện đang là lọc trên tập dữ liệu đã nạp, chưa đẩy hết qua truy vấn DB riêng cho mỗi panel
- UI Swing giữ mức đơn giản để test, chưa tối ưu bố cục hay trải nghiệm người dùng
- Chưa có bộ unit test / integration test tự động
- `CourseSection` và `Schedule` đang tách thành 2 màn hình riêng: tạo học phần trước, sau đó gán lịch chi tiết trong `Quản lý lịch học`

## Trạng thái hiện tại của project

Project đã ở mức:

- Build được
- Có dữ liệu mẫu để test
- Đăng nhập / phân quyền được
- CRUD chính đã có
- Đăng ký học phần có chặn trùng và chặn vượt sĩ số
- Nhập điểm / tính điểm / PASS-FAIL đã chạy theo công thức yêu cầu
- Xem lịch học / lịch dạy cơ bản đã hoàn thành
- Báo cáo cơ bản và xuất PDF đã có

Đây là bản phát triển đủ để test hệ thống end-to-end trên cấu trúc project hiện có mà không phá vỡ kiến trúc ban đầu.

## Cập nhật UI admin và niên khóa sinh viên

- Đã nâng cấp các màn hình ADMIN theo bố cục `bộ lọc -> bảng danh sách -> chi tiết`, trong đó các màn hình sinh viên, giảng viên, môn học và các màn hình CRUD bảng dữ liệu khác đều chỉ hiện dữ liệu sau khi chọn điều kiện lọc phù hợp.
- Đã Việt hóa giao diện theo tiếng Việt có dấu, bao gồm tiêu đề frame, nút bấm, cột bảng, thông báo và các panel thông tin chi tiết.
- Đã sửa đồng bộ họ tên (full_name) giữa bảng users và students/lecturers khi admin cập nhật thông tin.
