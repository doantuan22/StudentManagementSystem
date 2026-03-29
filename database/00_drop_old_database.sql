-- ============================================================
-- FILE: 00_drop_old_database.sql
-- MUC DICH: Xoa database cu de tao lai tu dau (BUOC 1 - CHAY TRUOC)
-- CANH BAO: Lenh nay se XOA TOAN BO du lieu hien tai!
--           Hay dam bao da backup truoc khi chay.
-- ============================================================

-- Xóa schema cũ của hệ thống quản lý sinh viên để chuẩn bị tạo lại.
DROP DATABASE IF EXISTS student_management;
