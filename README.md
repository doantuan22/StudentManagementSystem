# Student Management System

Project Java Maven theo kiến trúc phân lớp cho phần mềm Quan Ly Sinh Vien.

## Cau truc chinh

- `database/`: cac script khoi tao co so du lieu
- `src/main/java/com/qlsv`: ma nguon Java theo cac package `config`, `model`, `dao`, `service`, `controller`, `view`, `utils`, `security`, `exception`
- `src/main/resources`: file cau hinh va tai nguyen giao dien
- `docs/`: tai lieu phan tich va thiet ke
- `lib/`: vi tri placeholder cho cac thu vien jar neu can quan ly thu cong

## Cong nghe du kien

- Java 17
- Maven
- Java Swing
- JDBC
- MySQL 8
- iTextPDF
- JCalendar

## Cach chay so bo

1. Cai dat JDK 17 va Maven.
2. Cap nhat cau hinh trong `src/main/resources/application.properties`.
3. Chuan bi co so du lieu MySQL va cac script trong thu muc `database/`.
4. Chay lenh Maven thong thuong nhu `mvn clean compile` hoac khoi dong tu lop `com.qlsv.Main`.

