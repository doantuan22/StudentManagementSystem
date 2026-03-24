package com.qlsv;

import com.qlsv.config.DBConnection;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.auth.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        // Khoi dong chuong trinh tai mot diem duy nhat, sau do mo man hinh dang nhap.
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            // Kiem tra ket noi DB ngay luc khoi dong de tranh vao app roi moi biet loi cau hinh.
            if (!DBConnection.canConnect()) {
                DialogUtil.showError(null, "Khong ket noi duoc database. Hay kiem tra MySQL va application.properties.");
                return;
            }
            if (!DBConnection.hasRequiredTables()) {
                DialogUtil.showError(null, "Database dang dung chua co day du schema moi. Hay chay lai 02_create_tables.sql va 03_insert_sample_data.sql.");
                return;
            }
            new LoginFrame().setVisible(true);
        });
    }
}
