package com.qlsv.utils;

import com.qlsv.security.PasswordHasher;

public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hash(String rawPassword) {
        return PasswordHasher.hash(rawPassword);
    }

    public static boolean matches(String rawPassword, String hashedPassword) {
        return PasswordHasher.matches(rawPassword, hashedPassword);
    }
}
