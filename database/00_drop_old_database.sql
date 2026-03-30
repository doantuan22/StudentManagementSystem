-- ============================================================
-- FILE: 00_drop_old_database.sql
-- MỤC ĐÍCH: Xóa database cũ để tạo lại từ đầu (BƯỚC 1 - CHẠY TRƯỚC)
-- CẢNH BÁO: Lệnh này sẽ XÓA TOÀN BỘ dữ liệu hiện tại!
--           Hãy đảm bảo đã backup trước khi chạy.
-- ============================================================

-- Xóa schema cũ của hệ thống quản lý sinh viên để chuẩn bị tạo lại.
DROP DATABASE IF EXISTS student_management;
