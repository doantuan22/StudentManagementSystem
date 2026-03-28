package com.qlsv.view.common;

import javax.swing.JPasswordField;

public class CustomPasswordField extends JPasswordField {

    public CustomPasswordField() {
        super();
        AppTheme.applyTree(this);
    }

    public CustomPasswordField(int columns) {
        super(columns);
        AppTheme.applyTree(this);
    }
}
