package com.qlsv.view.common;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class BasePanel extends JPanel {

    protected BasePanel() {
        super(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setOpaque(true);
        setBackground(AppColors.CONTENT_BACKGROUND);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        AppTheme.applyTree(this);
    }

    public void reloadData() {
        // Mac dinh khong lam gi, cac lop con se override neu can tai lai du lieu
    }
}
