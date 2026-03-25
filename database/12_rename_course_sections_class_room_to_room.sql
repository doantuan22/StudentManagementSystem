-- 12_rename_course_sections_class_room_to_room.sql
-- Nang cap bang course_sections tu cot class_room_id sai nghia sang cot room dung nghiep vu.

USE student_management;

SET @legacy_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'course_sections'
      AND COLUMN_NAME = 'class_room_id'
);

SET @room_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'course_sections'
      AND COLUMN_NAME = 'room'
);

SET @legacy_fk_exists := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'course_sections'
      AND CONSTRAINT_NAME = 'fk_course_sections_class_room'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @legacy_index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'course_sections'
      AND INDEX_NAME = 'idx_course_sections_class_room'
);

SET @room_index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'course_sections'
      AND INDEX_NAME = 'idx_course_sections_room'
);

SET @add_room_sql := IF(
    @room_column_exists = 0,
    'ALTER TABLE course_sections ADD COLUMN room VARCHAR(50) NULL AFTER lecturer_id',
    'SELECT ''Cot room da ton tai tren bang course_sections.'' AS message'
);

PREPARE stmt FROM @add_room_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE course_sections cs
LEFT JOIN (
    SELECT s.course_section_id, s.room
    FROM schedules s
    JOIN (
        SELECT course_section_id, MIN(id) AS first_schedule_id
        FROM schedules
        GROUP BY course_section_id
    ) first_schedule ON first_schedule.first_schedule_id = s.id
) schedule_room ON schedule_room.course_section_id = cs.id
SET cs.room = COALESCE(NULLIF(TRIM(schedule_room.room), ''), CONCAT('P.', LPAD(cs.id, 3, '0')))
WHERE cs.room IS NULL OR TRIM(cs.room) = '';

ALTER TABLE course_sections
    MODIFY COLUMN room VARCHAR(50) NOT NULL;

SET @drop_fk_sql := IF(
    @legacy_fk_exists = 1,
    'ALTER TABLE course_sections DROP FOREIGN KEY fk_course_sections_class_room',
    'SELECT ''Khong tim thay khoa ngoai fk_course_sections_class_room de xoa.'' AS message'
);

PREPARE stmt FROM @drop_fk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @drop_index_sql := IF(
    @legacy_index_exists = 1,
    'DROP INDEX idx_course_sections_class_room ON course_sections',
    'SELECT ''Khong tim thay index idx_course_sections_class_room de xoa.'' AS message'
);

PREPARE stmt FROM @drop_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @drop_legacy_column_sql := IF(
    @legacy_column_exists = 1,
    'ALTER TABLE course_sections DROP COLUMN class_room_id',
    'SELECT ''Cot class_room_id da duoc loai bo truoc do.'' AS message'
);

PREPARE stmt FROM @drop_legacy_column_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @create_room_index_sql := IF(
    @room_index_exists = 0,
    'CREATE INDEX idx_course_sections_room ON course_sections(room)',
    'SELECT ''Index idx_course_sections_room da ton tai.'' AS message'
);

PREPARE stmt FROM @create_room_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
