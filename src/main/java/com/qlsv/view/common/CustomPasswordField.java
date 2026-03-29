/**
 * Ô mật khẩu tùy biến theo theme ứng dụng.
 */
package com.qlsv.view.common;

import javax.swing.JPasswordField;

public class CustomPasswordField extends JPasswordField {

    /**
     * Khởi tạo trường tùy biến mật khẩu.
     */
    public CustomPasswordField() {
        super();
        AppTheme.applyTree(this);
    }

    /**
     * Khởi tạo trường tùy biến mật khẩu.
     */
    public CustomPasswordField(int columns) {
        super(columns);
        AppTheme.applyTree(this);
    }
}
