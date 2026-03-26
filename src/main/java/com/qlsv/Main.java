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
            // Kiểm tra DB lúc khởi động để hiển thị thông báo rõ ràng,
            // nhưng KHÔNG chặn luồng app (vẫn mở màn hình đăng nhập).
            boolean canConnect = DBConnection.canConnect();
            if (!canConnect) {
                DialogUtil.showError(null, "Không kết nối được cơ sở dữ liệu. Hãy kiểm tra MySQL và file application.properties.");
            } else if (!DBConnection.hasRequiredTables()) {
                DialogUtil.showError(
                        null,
                        "Cơ sở dữ liệu hiện tại chưa có đầy đủ schema mới");
            }
            new LoginFrame().setVisible(true);
        });
    }
}
