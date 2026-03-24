package com.qlsv.model;

public enum Role {
    ADMIN("ADMIN", "Quan tri vien"),
    LECTURER("LECTURER", "Giang vien"),
    STUDENT("STUDENT", "Sinh vien");

    private final String code;
    private final String displayName;

    Role(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Role fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (Role role : values()) {
            if (role.code.equalsIgnoreCase(code) || role.name().equalsIgnoreCase(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Role khong hop le: " + code);
    }
}
