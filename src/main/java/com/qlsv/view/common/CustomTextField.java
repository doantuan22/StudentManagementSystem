/**
 * Ô nhập tùy biến theo theme ứng dụng.
 */
package com.qlsv.view.common;

import javax.swing.JTextField;

public class CustomTextField extends JTextField {

    /**
     * Khởi tạo trường tùy biến văn bản.
     */
    public CustomTextField() {
        super();
        AppTheme.applyTree(this);
    }

    /**
     * Khởi tạo trường tùy biến văn bản.
     */
    public CustomTextField(int columns) {
        super(columns);
        AppTheme.applyTree(this);
    }
}
