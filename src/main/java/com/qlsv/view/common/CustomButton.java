package com.qlsv.view.common;

import javax.swing.JButton;

public class CustomButton extends JButton {

    public CustomButton() {
        super();
        AppTheme.applyTree(this);
    }

    public CustomButton(String text) {
        super(text);
        AppTheme.applyTree(this);
    }
}
