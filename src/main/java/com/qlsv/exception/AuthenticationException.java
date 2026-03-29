/**
 * Mô tả thành phần authentication exception của ứng dụng.
 */
package com.qlsv.exception;

public class AuthenticationException extends AppException {

    /**
     * Khởi tạo authentication exception.
     */
    public AuthenticationException() {
    }

    /**
     * Khởi tạo authentication exception.
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * Khởi tạo authentication exception.
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
