-- ============================================================
-- FILE: 04_add_student_address.sql
-- MUC DICH: Bo sung truong dia chi cho bang students
-- ============================================================

USE student_management;

-- Kiem tra xem cot da ton tai chua truoc khi add
SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = 'student_management'
      AND table_name = 'students'
      AND column_name = 'address'
);

SET @sql_stmt = IF(@column_exists = 0,
    'ALTER TABLE students ADD COLUMN address VARCHAR(255) AFTER phone',
    'SELECT "Column address already exists in students table" AS info'
);

PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
