/**
 * Hộp thoại điểm chi tiết dialog.
 */
package com.qlsv.view.dialog;

import javax.swing.JComponent;

public class ScoreDetailDialog extends BaseDetailDialog {

    /**
     * Khởi tạo điểm chi tiết.
     */
    public ScoreDetailDialog(JComponent content) {
        super("Chi tiết thông tin điểm", content, 980, 680);
    }
}
