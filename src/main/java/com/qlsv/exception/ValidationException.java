/**
 * Mô tả thành phần kiểm tra exception của ứng dụng.
 */
package com.qlsv.exception;

public class ValidationException extends AppException {

    /**
     * Khởi tạo kiểm tra exception.
     */
    public ValidationException() {
    }

    /**
     * Khởi tạo kiểm tra exception.
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Khởi tạo kiểm tra exception.
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
