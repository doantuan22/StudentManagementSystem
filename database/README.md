# Database Scripts

Thu muc `database/` chua bo file SQL chinh thuc de tao va nap du lieu cho project (MySQL 8).

## Thu tu chay khuyen nghi

1. `00_drop_old_database.sql`
   Dung khi muon reset sach database `student_management`.
2. `01_create_schema.sql`
   Tao database, bang, khoa ngoai, index, view va trigger can thiet.
3. `02_seed_full_data.sql`
   Nap du lieu mau day du cho 3 vai tro.
4. `03_verify_data.sql`
   Kiem tra nhanh schema va du lieu sau khi nap.

## Tai khoan demo

- Admin: `admin` / `123456`
- Giang vien: `gv001`, `gv002`, `gv003` / `123456`
- Sinh vien: `sv2200001`, `sv2200002`, `sv2200003`, ... / `123456`

## Cau hinh mac dinh

- URL: `jdbc:mysql://localhost:3306/student_management`
- User: `root`
- Pass: `123456`

Su dung bo file `00` -> `03` la du de tao schema, nap du lieu mau va kiem tra nhanh database cho project hien tai.
