/**
 * Băm và đối chiếu mật khẩu người dùng.
 */
package com.qlsv.security;

import com.qlsv.exception.AppException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordHasher {

    /**
     * Khởi tạo mật khẩu hasher.
     */
    private PasswordHasher() {
    }

    /**
     * Mã hóa mật khẩu thô sang chuỗi băm (SHA-256) phục vụ lưu trữ bảo mật.
     */
    public static String hash(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte hashByte : hashBytes) {
                builder.append(String.format("%02x", hashByte));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new AppException("Không tạo được bộ băm mật khẩu.", exception);
        }
    }

    /**
     * Kiểm tra xem mật khẩu thô người dùng nhập vào có khớp với chuỗi băm đã lưu không.
     */
    public static boolean matches(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        return hash(rawPassword).equalsIgnoreCase(storedHash);
    }
}
