-- 11_add_student_academic_year.sql
-- Bo sung cot academic_year cho bang students ma khong lam mat du lieu cu.

USE student_management;

-- Tuong thich voi ca MySQL khong ho tro "ADD COLUMN IF NOT EXISTS".
SET @academic_year_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'students'
      AND COLUMN_NAME = 'academic_year'
);

SET @alter_sql := IF(
    @academic_year_column_exists = 0,
    'ALTER TABLE students ADD COLUMN academic_year VARCHAR(50) NULL AFTER class_room_id',
    'SELECT ''Cột academic_year đã tồn tại trên bảng students.'' AS message'
);

PREPARE stmt FROM @alter_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE students s
JOIN class_rooms c ON c.id = s.class_room_id
SET s.academic_year = c.academic_year
WHERE s.academic_year IS NULL OR TRIM(s.academic_year) = '';

ALTER TABLE students
    MODIFY COLUMN academic_year VARCHAR(50) NOT NULL;
