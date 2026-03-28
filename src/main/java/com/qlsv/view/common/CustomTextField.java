package com.qlsv.view.common;

import javax.swing.JTextField;

public class CustomTextField extends JTextField {

    public CustomTextField() {
        super();
        AppTheme.applyTree(this);
    }

    public CustomTextField(int columns) {
        super(columns);
        AppTheme.applyTree(this);
    }
}
