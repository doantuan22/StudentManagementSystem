/**
 * ComboBox tùy biến theo theme ứng dụng.
 */
package com.qlsv.view.common;

import javax.swing.JComboBox;

public class CustomComboBox extends JComboBox<Object> {

    /**
     * Khởi tạo tùy biến chọn box.
     */
    public CustomComboBox() {
        super();
        AppTheme.applyTree(this);
    }

    /**
     * Khởi tạo tùy biến chọn box.
     */
    public CustomComboBox(Object[] items) {
        super(items);
        AppTheme.applyTree(this);
    }
}
