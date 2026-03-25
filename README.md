# Student Management System

Project Java Swing + JDBC + MySQL 8 de test he thong quan ly sinh vien theo 3 vai tro: `ADMIN`, `LECTURER`, `STUDENT`.

Trang thai hien tai:

- Da hoan thien phan lon luong chinh de test he thong.
- Da bo sung schema database, du lieu mau, phan quyen, CRUD, dang ky hoc phan, diem, lich hoc va bao cao co ban.
- UI Swing du dung de test chuc nang, khong tap trung vao giao dien dep.

## Cong nghe su dung

- Java 17
- Maven
- Java Swing
- JDBC
- MySQL 8
- iText PDF

## Cau truc project

- `database/`: script tao database, du lieu mau, views, indexes va truy van test
- `src/main/java/com/qlsv/config`: doc config, ket noi DB, session
- `src/main/java/com/qlsv/model`: model du lieu va DTO thong ke
- `src/main/java/com/qlsv/dao`: JDBC CRUD va truy van tong hop
- `src/main/java/com/qlsv/service`: xu ly nghiep vu, validate, phan quyen, tinh diem, chan trung lich
- `src/main/java/com/qlsv/controller`: lop noi giua UI va service
- `src/main/java/com/qlsv/view`: Swing frame/panel/dialog
- `src/main/java/com/qlsv/security`: hash mat khau va mapping quyen
- `src/main/java/com/qlsv/utils`: dialog, validate, PDF export
- `src/main/resources`: `application.properties`

## Cac chuc nang da hoan thanh

### 1. Khoi dong, config va ket noi DB

- Doc `db.url`, `db.username`, `db.password`, `app.name` tu `application.properties`
- Kiem tra ket noi MySQL ngay luc khoi dong trong `Main`
- Quan ly session dang nhap bang `SessionManager`

### 2. Dang nhap va phan quyen

- Dang nhap bang `username/password`
- Mat khau luu dang SHA-256
- Phan quyen theo enum `Role { ADMIN, LECTURER, STUDENT }`
- Dieu huong dung dashboard theo role
- Dang xuat va xoa session
- Kiem tra quyen tap trung tai `AuthManager` + `RolePermission` + `PermissionService`

### 3. CRUD admin

- Quan ly sinh vien
- Quan ly giang vien
- Quan ly khoa
- Quan ly lop
- Quan ly mon hoc
- Quan ly hoc phan
- Quan ly enrollment
- Quan ly diem
- Quan ly lich hoc

Tat ca cac panel CRUD da co:

- JTable
- Them / sua / xoa
- Tai lai du lieu
- Tim kiem client-side tren du lieu da nap

### 4. Module sinh vien

- Xem ho so ca nhan
- Dang ky hoc phan
- Huy dang ky hoc phan cua chinh minh
- Xem hoc phan da dang ky
- Xem bang diem ca nhan
- Xem lich hoc

### 5. Module giang vien

- Xem ho so ca nhan
- Xem hoc phan phu trach
- Xem danh sach sinh vien thuoc hoc phan minh day
- Nhap / cap nhat diem cho hoc phan duoc phan cong
- Xem lich day

### 6. Dang ky hoc phan

- Tach ro `Subject` / `CourseSection` / `Enrollment`
- Chan dang ky trung hoc phan
- Chan vuot si so toi da
- Chan trung lich dua tren bang `schedules`
- Cho phep admin tao enrollment thu cong
- Cho phep student dang ky va huy hoc phan cua chinh minh

### 7. Quan ly diem

- Admin xem va quan ly toan bo diem
- Giang vien chi nhap / sua diem cho hoc phan minh phu trach
- Tu dong tinh:

```text
totalScore = processScore * 0.3 + midtermScore * 0.2 + finalScore * 0.5
```

- Tu dong xac dinh:
  - `PASS` neu `totalScore >= 5.0`
  - `FAIL` neu `< 5.0`

### 8. Lich hoc / lich day

- Them bang `schedules`
- Admin quan ly lich hoc theo hoc phan
- Student xem lich hoc cua minh
- Lecturer xem lich day cua minh
- Chan trung lich theo giang vien, phong hoc va sinh vien

### 9. Bao cao co ban

- Danh sach sinh vien theo lop
- Danh sach giang vien theo khoa
- Danh sach sinh vien trong hoc phan
- Bang diem theo hoc phan
- Thong ke nhanh tong sinh vien / giang vien / mon hoc / hoc phan / enrollment
- Xuat PDF tu man hinh bao cao

## Nhung thay doi moi da thuc hien

### Da sua / hoan thien cac file chinh

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
- `database/02_create_tables.sql`
- `database/03_insert_sample_data.sql`
- `database/06_create_views.sql`
- `database/07_create_indexes.sql`
- `database/10_test_queries.sql`
- `database/README.md`

### Da them file / module moi

- `src/main/java/com/qlsv/dao/ReportDAO.java`
- `src/main/java/com/qlsv/model/SystemStatistics.java`

### Da bo sung module

- Module `schedule` day du o muc dung de test
- Module `report` va `system statistics`
- Luong xuat PDF cho bao cao
- Validate email / phone / score
- Chan trung lich cho dang ky hoc phan
- Footer / dashboard role-based day du hon

## Huong dan cau hinh `application.properties`

Mo file:

- `src/main/resources/application.properties`

Noi dung mac dinh:

```properties
db.url=jdbc:mysql://localhost:3306/student_management
db.username=root
db.password=123456
app.name=Student Management System
```

Can sua lai `db.username` / `db.password` neu MySQL cua ban dung tai khoan khac.

## Huong dan tao database

### Thu tu chay SQL khuyen nghi

Neu may da tung co schema cu:

1. `database/00_reset_database.sql`

Sau do chay:

1. `database/01_create_database.sql`
2. `database/02_create_tables.sql`
3. `database/03_insert_sample_data.sql`

Neu may da co du lieu tu schema cu cua `course_sections.class_room_id`, chay them:

1. `database/11_add_student_academic_year.sql`
2. `database/12_rename_course_sections_class_room_to_room.sql`

Co the chay them:

1. `database/06_create_views.sql`
2. `database/07_create_indexes.sql`
3. `database/10_test_queries.sql`

### Cac bang chinh

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

## Tai khoan mau de test

Tat ca tai khoan deu dung mat khau:

- Password: `123456`

Danh sach:

- `admin`
- `lecturer01`
- `lecturer02`
- `student01`
- `student02`
- `student03`

## Cach chay project

### Cach 1: Chay bang IDE

1. Import project Maven vao IntelliJ IDEA / Eclipse / NetBeans
2. Dam bao MySQL dang chay va database da import xong
3. Mo file `src/main/java/com/qlsv/Main.java`
4. Run ham `main`

### Cach 2: Compile / run thu cong tren Windows PowerShell

```powershell
rg --files src/main/java -g "*.java" | Set-Content sources.txt
javac -encoding UTF-8 -cp "lib/*" -d target/classes @sources.txt
java -cp "target/classes;src/main/resources;lib/*" com.qlsv.Main
```

Neu may da cai Maven va muon chi compile nhanh:

```powershell
mvn clean compile
```

## Ghi chu ky thuat quan trong

- Mat khau dang nhap duoc hash bang SHA-256
- Kiem tra phan quyen tap trung tai `RolePermission` + `PermissionService`
- `ScoreService` tu dong tinh diem tong ket va `PASS/FAIL`
- `ScheduleService` + `ScheduleDAO` xu ly kiem tra trung lich
- `AbstractCrudPanel` la lop chung cho nhieu man hinh CRUD
- `ReportDAO` tong hop du lieu bao cao de UI khong truy van truc tiep
- `DBConnection` kiem tra ket noi MySQL ngay khi khoi dong

## Gioi han hien tai / phan chua toi uu

- Chua co luong tao tai khoan login moi day du tren UI; khi them sinh vien / giang vien moi, truong `user_id` van la lien ket toi `users` co san
- Tim kiem trong cac man hinh CRUD hien dang la loc tren tap du lieu da nap, chua day het qua truy van DB rieng cho moi panel
- UI Swing giu muc don gian de test, chua toi uu bo cuc hay trai nghiem nguoi dung
- Chua co bo unit test / integration test tu dong
- `CourseSection` va `Schedule` dang tach thanh 2 man hinh rieng: tao hoc phan truoc, sau do gan lich chi tiet trong `Quan ly lich hoc`

## Trang thai hien tai cua project

Project da o muc:

- Build duoc
- Co du lieu mau de test
- Dang nhap / phan quyen duoc
- CRUD chinh da co
- Dang ky hoc phan co chan trung va chan vuot si so
- Nhap diem / tinh diem / PASS-FAIL da chay theo cong thuc yeu cau
- Xem lich hoc / lich day co ban da hoan thanh
- Bao cao co ban va xuat PDF da co

Day la ban phat trien du de test he thong end-to-end tren cau truc project hien co ma khong pha vo kien truc ban dau.

## Cap nhat UI admin va nien khoa sinh vien

- Da bo sung cot `students.academic_year` va script nang cap `database/11_add_student_academic_year.sql` de bo sung du lieu nien khoa an toan tren database cu.
- Da doi cot sai nghia `course_sections.class_room_id` thanh `course_sections.room` va bo sung script nang cap `database/12_rename_course_sections_class_room_to_room.sql`.
- Da nang cap cac man hinh ADMIN theo bo cuc `bo loc -> bang danh sach -> chi tiet`, trong do cac man hinh sinh vien, giang vien, mon hoc va cac man hinh CRUD bang du lieu khac deu chi hien du lieu sau khi chon dieu kien loc phu hop.
- Da Viet hoa giao dien theo tieng Viet co dau, bao gom tieu de frame, nut bam, cot bang, thong bao va cac panel thong tin chi tiet.
