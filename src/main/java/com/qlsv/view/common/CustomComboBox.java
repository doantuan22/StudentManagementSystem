package com.qlsv.view.common;

import javax.swing.JComboBox;

public class CustomComboBox extends JComboBox<Object> {

    public CustomComboBox() {
        super();
        AppTheme.applyTree(this);
    }

    public CustomComboBox(Object[] items) {
        super(items);
        AppTheme.applyTree(this);
    }
}
