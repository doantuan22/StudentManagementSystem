-- 04_insert_roles.sql
-- Script tuong thich de chen role mac dinh neu can chay rieng.

USE student_management;

INSERT INTO roles(role_code, role_name)
VALUES ('ADMIN', 'Quan tri vien'),
       ('LECTURER', 'Giang vien'),
       ('STUDENT', 'Sinh vien')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);
