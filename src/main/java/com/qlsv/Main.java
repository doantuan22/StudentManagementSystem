/**
 * Khởi động ứng dụng desktop và kiểm tra trạng thái ban đầu.
 */
package com.qlsv;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.navigation.AppNavigator;
import com.qlsv.navigation.SwingAppNavigator;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.AppTheme;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class Main {

    /**
     * Điểm bắt đầu của ứng dụng (Entry point), thiết lập giao diện và khởi động luồng chính.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            AppTheme.install();

            startApplication(
                    new SwingAppNavigator(),
                    JpaBootstrap::canBootstrap,
                    JpaBootstrap::hasRequiredSchema,
                    message -> DialogUtil.showError(null, message)
            );
        });
    }

    /**
     * Khởi động logic ứng dụng: kiểm tra tính sẵn sàng của database và chuyển đến màn hình đăng nhập.
     */
    static void startApplication(AppNavigator navigator,
                                 BooleanSupplier canBootstrapCheck,
                                 BooleanSupplier schemaCheck,
                                 Consumer<String> errorHandler) {
        if (!isStartupReady(canBootstrapCheck, schemaCheck, errorHandler)) {
            return;
        }
        navigator.showLogin();
    }

    /**
     * Kiểm tra trạng thái kết nối cơ sở dữ liệu và cấu trúc bảng (schema) trước khi chạy.
     */
    static boolean isStartupReady(BooleanSupplier canBootstrapCheck,
                                  BooleanSupplier schemaCheck,
                                  Consumer<String> errorHandler) {
        if (!canBootstrapCheck.getAsBoolean()) {
            errorHandler.accept("Không kết nối được cơ sở dữ liệu. Hãy kiểm tra MySQL và file application.properties.");
            return false;
        }
        if (!schemaCheck.getAsBoolean()) {
            errorHandler.accept("Cơ sở dữ liệu hiện tại chưa có đầy đủ schema mới");
            return false;
        }
        return true;
    }
}
