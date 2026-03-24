# Student Management System

Project Java Swing + JDBC + MySQL cho bai toan quan ly sinh vien.

## Cau truc chinh

- `database/`: script tao database va du lieu mau
- `src/main/java/com/qlsv`: source code Java theo cac package `config`, `model`, `dao`, `service`, `controller`, `view`, `utils`, `security`, `exception`
- `src/main/resources`: file cau hinh ung dung
- `docs/`: tai lieu phan tich va thiet ke
- `lib/`: jar dung cho classpath local khi can

## Cong nghe su dung

- Java 17
- Maven
- Java Swing
- JDBC
- MySQL 8

## Quy trinh test lan dau

Phan nay dung cho thanh vien moi trong team khi clone project va can chay nhanh he thong lan dau.

### 1. Chuan bi moi truong

Can cai san:

- JDK 17
- MySQL 8
- IDE Java bat ky: IntelliJ IDEA, Eclipse, NetBeans
- Maven neu muon build bang terminal

### 2. Kiem tra cau hinh database

Mo file [application.properties](/d:/StudentManagementSystem/src/main/resources/application.properties) va kiem tra:

```properties
db.url=jdbc:mysql://localhost:3306/student_management
db.username=root
db.password=123456
app.name=Student Management System
```

Neu MySQL tren may ban dung username/password khac, hay sua file nay truoc khi chay app.

### 3. Tao database trong MySQL

Mo MySQL Workbench hoac cong cu SQL bat ky, sau do chay cac file theo dung thu tu sau.

Neu may da tung tao database `student_management` theo schema cu, hay reset truoc:

1. [00_reset_database.sql](/d:/StudentManagementSystem/database/00_reset_database.sql)

Sau do chay tiep:

1. [01_create_database.sql](/d:/StudentManagementSystem/database/01_create_database.sql)
2. [02_create_tables.sql](/d:/StudentManagementSystem/database/02_create_tables.sql)
3. [03_insert_sample_data.sql](/d:/StudentManagementSystem/database/03_insert_sample_data.sql)

Luu y:

- Khong can chay them [04_insert_roles.sql](/d:/StudentManagementSystem/database/04_insert_roles.sql) va [05_seed_sample_data.sql](/d:/StudentManagementSystem/database/05_seed_sample_data.sql) neu da chay `03_insert_sample_data.sql`
- `03_insert_sample_data.sql` da tao san role, user, khoa, lop, giang vien, sinh vien, mon hoc, hoc phan, enrollment va score mau

### 4. Kiem tra du lieu mau trong MySQL

Sau khi chay SQL xong, ban co the kiem tra nhanh:

```sql
USE student_management;
SELECT * FROM roles;
SELECT * FROM users;
```

Mong doi:

- Bang `roles` co 3 dong: `ADMIN`, `LECTURER`, `STUDENT`
- Bang `users` co it nhat 3 tai khoan: `admin`, `lecturer01`, `student01`

### 5. Mo project va build

Mo project trong IDE.

Neu dung Maven trong terminal:

```bash
mvn clean compile
```

Neu may chua co Maven trong PATH, chi can de IDE load dependency va build project binh thuong.

### 6. Chay man hinh dang nhap

File khoi dong chuong trinh la:

- [Main.java](/d:/StudentManagementSystem/src/main/java/com/qlsv/Main.java)

Trong IDE:

1. Mo file `src/main/java/com/qlsv/Main.java`
2. Chon ham `main`
3. Bam `Run`

Khi chay thanh cong, man hinh dang nhap se mo len.

### 7. Tai khoan dang nhap mau

Tat ca tai khoan mau deu dung chung mat khau:

- Password: `123456`

Danh sach tai khoan:

- `admin`
- `lecturer01`
- `student01`

### 8. Ket qua mong doi sau khi dang nhap

Neu dang nhap dung:

- `admin` -> mo `AdminDashboardFrame`
- `lecturer01` -> mo `LecturerDashboardFrame`
- `student01` -> mo `StudentDashboardFrame`

Co the dung de test nhanh:

- Admin: quan ly sinh vien, giang vien, khoa, lop, mon hoc, hoc phan, enrollment, diem
- Lecturer: xem thong tin ca nhan, lop duoc phan cong, nhap/xem diem
- Student: xem thong tin ca nhan, dang ky hoc phan, xem diem

## Cac loi thuong gap

### Loi khong tim thay user theo username

Nguyen nhan thuong gap:

- Database dang la schema cu
- Chua chay `03_insert_sample_data.sql`

Cach xu ly:

1. Chay lai [00_reset_database.sql](/d:/StudentManagementSystem/database/00_reset_database.sql)
2. Chay lai [01_create_database.sql](/d:/StudentManagementSystem/database/01_create_database.sql)
3. Chay lai [02_create_tables.sql](/d:/StudentManagementSystem/database/02_create_tables.sql)
4. Chay lai [03_insert_sample_data.sql](/d:/StudentManagementSystem/database/03_insert_sample_data.sql)

### Loi khong ket noi duoc MySQL

Kiem tra:

- MySQL da duoc start chua
- `db.url`, `db.username`, `db.password` trong [application.properties](/d:/StudentManagementSystem/src/main/resources/application.properties) da dung chua
- Tai khoan MySQL co quyen tao database va bang hay khong

### Dang nhap dung mat khau nhung van loi

Kiem tra:

- Ban co dang dang nhap bang 1 trong 3 user mau hay khong
- Bang `users` trong MySQL co du lieu chua
- Database dang dung co phai la `student_management` hay khong

## Ghi chu cho team

- Day la skeleton de test luong chinh, khong phai ban hoan thien cuoi cung
- Khi thay doi schema database, can cap nhat dong bo SQL + model + DAO + service
- Khong xoa cac file skeleton khac neu chua dung toi
