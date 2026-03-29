/**
 * Hộp thoại điểm giảng viên chi tiết dialog.
 */
package com.qlsv.view.dialog;

import javax.swing.JComponent;

public class LecturerScoreDetailDialog extends BaseDetailDialog {

    /**
     * Khởi tạo điểm giảng viên chi tiết.
     */
    public LecturerScoreDetailDialog(JComponent content) {
        super("Chi tiết nhập và sửa điểm", content, 980, 560);
    }
}
