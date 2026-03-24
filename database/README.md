# Database Scripts

- `00_reset_database.sql`: xoa schema cu neu can reset lai tu dau.
- `01_create_database.sql`: khoi tao database `student_management`.
- `02_create_tables.sql`: tao day du bang, khoa ngoai, unique key va bang `schedules`.
- `03_insert_sample_data.sql`: nap role, user, khoa, lop, giang vien, sinh vien, mon hoc, hoc phan, lich hoc, enrollment va score.
- `04_insert_roles.sql`: script tuong thich neu muon chen role rieng.
- `05_seed_sample_data.sql`: script tuong thich de nap du lieu mau theo cach cu.
- `06_create_views.sql`: tao view ho tro lich hoc va bang diem theo hoc phan.
- `07_create_indexes.sql`: tao index phuc vu tim kiem va bao cao.
- `08_create_triggers.sql`: de trong de bo sung sau.
- `09_create_procedures.sql`: de trong de bo sung sau.
- `10_test_queries.sql`: truy van mau de kiem tra nhanh du lieu.

## Thu tu khuyen nghi

Neu ban da tung tao `student_management` tu schema cu, hay reset truoc:

0. Chay `00_reset_database.sql`
1. Chay `01_create_database.sql`
2. Chay `02_create_tables.sql`
3. Chay `03_insert_sample_data.sql`
4. Co the chay them `06_create_views.sql`, `07_create_indexes.sql`, `10_test_queries.sql`

## Du lieu mau da co san

- 3 role: `ADMIN`, `LECTURER`, `STUDENT`
- 6 user mau: `admin`, `lecturer01`, `lecturer02`, `student01`, `student02`, `student03`
- 2 khoa, 3 lop, 2 giang vien, 3 sinh vien
- 3 mon hoc, 4 hoc phan, 4 lich hoc
- enrollment va score mau de test luong dang ky, nhap diem va bao cao

## Tai khoan mau

- `admin` / `123456`
- `lecturer01` / `123456`
- `lecturer02` / `123456`
- `student01` / `123456`
- `student02` / `123456`
- `student03` / `123456`
