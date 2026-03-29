/**
 * Nút Swing tùy biến theo theme ứng dụng.
 */
package com.qlsv.view.common;

import javax.swing.JButton;

public class CustomButton extends JButton {

    /**
     * Khởi tạo nút tùy biến.
     */
    public CustomButton() {
        super();
        AppTheme.applyTree(this);
    }

    /**
     * Khởi tạo nút tùy biến.
     */
    public CustomButton(String text) {
        super(text);
        AppTheme.applyTree(this);
    }
}
