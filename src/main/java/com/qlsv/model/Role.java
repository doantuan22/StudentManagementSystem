/**
 * Mô tả thực thể vai trò của hệ thống.
 */
package com.qlsv.model;

public enum Role {
    ADMIN("ADMIN", "Quản trị viên"),
    LECTURER("LECTURER", "Giảng viên"),
    STUDENT("STUDENT", "Sinh viên");

    private final String code;
    private final String displayName;

    /**
     * Khởi tạo vai trò.
     */
    Role(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    /**
     * Trả về mã.
     */
    public String getCode() {
        return code;
    }

    /**
     * Trả về hiển thị tên.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Xử lý from mã.
     */
    public static Role fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (Role role : values()) {
            if (role.code.equalsIgnoreCase(code) || role.name().equalsIgnoreCase(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Vai trò không hợp lệ: " + code);
    }
}
