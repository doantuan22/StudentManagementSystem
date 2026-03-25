-- ============================================================
-- FILE: 01_create_schema.sql
-- MUC DICH: Tao toan bo schema cua He thong Quan ly Sinh vien
-- BAO GOM:  Bang rooms, course_sections.room_id, schedules.room_id,
--           students.academic_year va tat ca rang buoc hien tai.
-- CHAY SAU: 00_drop_old_database.sql
-- ============================================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS student_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE student_management;

-- ------------------------------------------------------------
-- BANG CO SO: Khong phu thuoc bang nao khac
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS roles (
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(50)  NOT NULL UNIQUE,
    role_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS faculties (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    faculty_code VARCHAR(50)  NOT NULL UNIQUE,
    faculty_name VARCHAR(150) NOT NULL,
    description  VARCHAR(255),
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rooms (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_code  VARCHAR(50)  NOT NULL UNIQUE,
    room_name  VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- BANG PHU THUOC roles va faculties
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS users (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(150) NOT NULL,
    email         VARCHAR(150),
    role_id       BIGINT NOT NULL,
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE IF NOT EXISTS class_rooms (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    class_code    VARCHAR(50)  NOT NULL UNIQUE,
    class_name    VARCHAR(150) NOT NULL,
    academic_year VARCHAR(50)  NOT NULL,
    faculty_id    BIGINT NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_class_rooms_faculty FOREIGN KEY (faculty_id) REFERENCES faculties (id)
);

CREATE TABLE IF NOT EXISTS subjects (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    subject_code VARCHAR(50)  NOT NULL UNIQUE,
    subject_name VARCHAR(150) NOT NULL,
    credits      INT NOT NULL,
    faculty_id   BIGINT NOT NULL,
    description  VARCHAR(255),
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subjects_faculty FOREIGN KEY (faculty_id) REFERENCES faculties (id),
    CONSTRAINT chk_subjects_credits CHECK (credits > 0)
);

-- ------------------------------------------------------------
-- BANG GIANG VIEN va SINH VIEN
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS lecturers (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id       BIGINT UNIQUE,
    lecturer_code VARCHAR(50)  NOT NULL UNIQUE,
    full_name     VARCHAR(150) NOT NULL,
    email         VARCHAR(150),
    phone         VARCHAR(30),
    faculty_id    BIGINT NOT NULL,
    status        VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lecturers_user    FOREIGN KEY (user_id)    REFERENCES users (id),
    CONSTRAINT fk_lecturers_faculty FOREIGN KEY (faculty_id) REFERENCES faculties (id)
);

CREATE TABLE IF NOT EXISTS students (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id       BIGINT UNIQUE,
    student_code  VARCHAR(50)  NOT NULL UNIQUE,
    full_name     VARCHAR(150) NOT NULL,
    gender        VARCHAR(20),
    date_of_birth DATE,
    email         VARCHAR(150),
    phone         VARCHAR(30),
    faculty_id    BIGINT NOT NULL,
    class_room_id BIGINT NOT NULL,
    academic_year VARCHAR(50) NOT NULL,
    status        VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_students_user       FOREIGN KEY (user_id)       REFERENCES users (id),
    CONSTRAINT fk_students_faculty    FOREIGN KEY (faculty_id)    REFERENCES faculties (id),
    CONSTRAINT fk_students_class_room FOREIGN KEY (class_room_id) REFERENCES class_rooms (id)
);

-- ------------------------------------------------------------
-- BANG PHAN CONG GIANG VIEN - MON HOC
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS lecturer_subjects (
    lecturer_id BIGINT NOT NULL,
    subject_id  BIGINT NOT NULL,
    PRIMARY KEY (lecturer_id, subject_id),
    CONSTRAINT fk_lecturer_subjects_lecturer FOREIGN KEY (lecturer_id) REFERENCES lecturers (id),
    CONSTRAINT fk_lecturer_subjects_subject  FOREIGN KEY (subject_id)  REFERENCES subjects (id)
);

-- ------------------------------------------------------------
-- BANG HOC PHAN MO - tham chieu rooms (room_id)
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS course_sections (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    section_code  VARCHAR(50)  NOT NULL UNIQUE,
    subject_id    BIGINT NOT NULL,
    lecturer_id   BIGINT NOT NULL,
    room_id       BIGINT NOT NULL,
    semester      VARCHAR(30)  NOT NULL,
    school_year   VARCHAR(30)  NOT NULL,
    schedule_text VARCHAR(255),
    max_students  INT NOT NULL DEFAULT 50,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course_sections_subject   FOREIGN KEY (subject_id)  REFERENCES subjects (id),
    CONSTRAINT fk_course_sections_lecturer  FOREIGN KEY (lecturer_id) REFERENCES lecturers (id),
    CONSTRAINT fk_course_sections_room      FOREIGN KEY (room_id)     REFERENCES rooms (id),
    CONSTRAINT chk_course_sections_max_students CHECK (max_students > 0)
);

-- ------------------------------------------------------------
-- BANG LICH HOC - tham chieu rooms (room_id)
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS schedules (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_section_id BIGINT NOT NULL,
    day_of_week       VARCHAR(20) NOT NULL,
    start_period      INT NOT NULL,
    end_period        INT NOT NULL,
    room_id           BIGINT NOT NULL,
    note              VARCHAR(255),
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_schedules_section_slot UNIQUE (course_section_id, day_of_week, start_period, room_id),
    CONSTRAINT fk_schedules_course_section FOREIGN KEY (course_section_id) REFERENCES course_sections (id),
    CONSTRAINT fk_schedules_room           FOREIGN KEY (room_id)            REFERENCES rooms (id),
    CONSTRAINT chk_schedules_period CHECK (start_period > 0 AND end_period > 0 AND start_period <= end_period)
);

-- ------------------------------------------------------------
-- BANG DANG KY HOC PHAN va DIEM
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS enrollments (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id        BIGINT NOT NULL,
    course_section_id BIGINT NOT NULL,
    status            VARCHAR(30) NOT NULL DEFAULT 'REGISTERED',
    enrolled_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_enrollment_student_section UNIQUE (student_id, course_section_id),
    CONSTRAINT fk_enrollments_student       FOREIGN KEY (student_id)        REFERENCES students (id),
    CONSTRAINT fk_enrollments_course_section FOREIGN KEY (course_section_id) REFERENCES course_sections (id)
);

CREATE TABLE IF NOT EXISTS scores (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    enrollment_id  BIGINT NOT NULL UNIQUE,
    process_score  DECIMAL(4, 2) NOT NULL DEFAULT 0,
    midterm_score  DECIMAL(4, 2) NOT NULL DEFAULT 0,
    final_score    DECIMAL(4, 2) NOT NULL DEFAULT 0,
    total_score    DECIMAL(4, 2) NOT NULL DEFAULT 0,
    result         VARCHAR(20) NOT NULL DEFAULT 'FAIL',
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_scores_enrollment FOREIGN KEY (enrollment_id) REFERENCES enrollments (id),
    CONSTRAINT chk_scores_process  CHECK (process_score BETWEEN 0 AND 10),
    CONSTRAINT chk_scores_midterm  CHECK (midterm_score BETWEEN 0 AND 10),
    CONSTRAINT chk_scores_final    CHECK (final_score   BETWEEN 0 AND 10),
    CONSTRAINT chk_scores_total    CHECK (total_score   BETWEEN 0 AND 10)
);

-- ------------------------------------------------------------
-- VIEW
-- ------------------------------------------------------------

CREATE OR REPLACE VIEW vw_student_schedules AS
SELECT st.student_code,
       st.full_name    AS student_name,
       cs.section_code,
       sb.subject_code,
       sb.subject_name,
       sc.day_of_week,
       sc.start_period,
       sc.end_period,
       r.room_code,
       r.room_name
FROM enrollments e
         JOIN students st        ON st.id = e.student_id
         JOIN course_sections cs ON cs.id = e.course_section_id
         JOIN subjects sb        ON sb.id = cs.subject_id
         JOIN schedules sc       ON sc.course_section_id = cs.id
         JOIN rooms r            ON r.id = sc.room_id;

CREATE OR REPLACE VIEW vw_section_scores AS
SELECT cs.section_code,
       st.student_code,
       st.full_name                       AS student_name,
       COALESCE(s.process_score, 0)       AS process_score,
       COALESCE(s.midterm_score, 0)       AS midterm_score,
       COALESCE(s.final_score,   0)       AS final_score,
       COALESCE(s.total_score,   0)       AS total_score,
       COALESCE(s.result,   'FAIL')       AS result
FROM enrollments e
         JOIN students st        ON st.id = e.student_id
         JOIN course_sections cs ON cs.id = e.course_section_id
         LEFT JOIN scores s      ON s.enrollment_id = e.id;

-- ------------------------------------------------------------
-- INDEX
-- ------------------------------------------------------------

CREATE INDEX idx_students_class_room       ON students (class_room_id);
CREATE INDEX idx_students_faculty          ON students (faculty_id);
CREATE INDEX idx_lecturers_faculty         ON lecturers (faculty_id);
CREATE INDEX idx_subjects_faculty          ON subjects (faculty_id);
CREATE INDEX idx_course_sections_lecturer  ON course_sections (lecturer_id);
CREATE INDEX idx_course_sections_room      ON course_sections (room_id);
CREATE INDEX idx_enrollments_section       ON enrollments (course_section_id);
CREATE INDEX idx_enrollments_student       ON enrollments (student_id);
CREATE INDEX idx_scores_enrollment         ON scores (enrollment_id);
CREATE INDEX idx_schedules_section_day     ON schedules (course_section_id, day_of_week, start_period, end_period);
CREATE INDEX idx_schedules_room            ON schedules (room_id);
