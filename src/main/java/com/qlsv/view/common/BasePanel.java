/**
 * Panel cơ sở hỗ trợ reload và style dùng chung.
 */
package com.qlsv.view.common;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.BorderLayout;

public class BasePanel extends JPanel {

    /**
     * Khởi tạo cơ sở.
     */
    protected BasePanel() {
        super(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setOpaque(true);
        setBackground(AppColors.CONTENT_BACKGROUND);
    }

    /**
     * Thêm notify.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        AppTheme.applyTree(this);
    }

    /**
     * Làm mới dữ liệu đang hiển thị.
     */
    public void reloadData() {
        // Mac dinh khong lam gi, cac lop con se override neu can tai lai du lieu
    }

    /**
     * Tạo border cho input field với độ dày tốt hơn để tránh mất viền trên màn hình có scaling.
     */
    protected Border createInputBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.INPUT_BORDER, 2),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)
        );
    }

    /**
     * Tạo border cho card với độ dày tốt hơn.
     */
    protected Border createCardBorder() {
        return BorderFactory.createLineBorder(AppColors.CARD_BORDER, 2);
    }
}
