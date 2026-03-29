/**
 * Mô tả thành phần authorization exception của ứng dụng.
 */
package com.qlsv.exception;

public class AuthorizationException extends AppException {

    /**
     * Khởi tạo authorization exception.
     */
    public AuthorizationException() {
    }

    /**
     * Khởi tạo authorization exception.
     */
    public AuthorizationException(String message) {
        super(message);
    }

    /**
     * Khởi tạo authorization exception.
     */
    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
