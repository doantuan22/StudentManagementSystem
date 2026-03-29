/**
 * Mô tả thành phần app exception của ứng dụng.
 */
package com.qlsv.exception;

public class AppException extends RuntimeException {

    /**
     * Khởi tạo app exception.
     */
    public AppException() {
    }

    /**
     * Khởi tạo app exception.
     */
    public AppException(String message) {
        super(message);
    }

    /**
     * Khởi tạo app exception.
     */
    public AppException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Khởi tạo app exception.
     */
    public AppException(Throwable cause) {
        super(cause);
    }
}
