package com.qlsv;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.navigation.AppNavigator;
import com.qlsv.navigation.SwingAppNavigator;
import com.qlsv.utils.DialogUtil;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            startApplication(
                    new SwingAppNavigator(),
                    JpaBootstrap::canBootstrap,
                    JpaBootstrap::hasRequiredSchema,
                    message -> DialogUtil.showError(null, message)
            );
        });
    }

    static void startApplication(AppNavigator navigator,
                                 BooleanSupplier canBootstrapCheck,
                                 BooleanSupplier schemaCheck,
                                 Consumer<String> errorHandler) {
        if (!isStartupReady(canBootstrapCheck, schemaCheck, errorHandler)) {
            return;
        }
        navigator.showLogin();
    }

    static boolean isStartupReady(BooleanSupplier canBootstrapCheck,
                                  BooleanSupplier schemaCheck,
                                  Consumer<String> errorHandler) {
        if (!canBootstrapCheck.getAsBoolean()) {
            errorHandler.accept("Khong ket noi duoc co so du lieu. Hay kiem tra MySQL va file application.properties.");
            return false;
        }
        if (!schemaCheck.getAsBoolean()) {
            errorHandler.accept("Co so du lieu hien tai chua co day du schema moi");
            return false;
        }
        return true;
    }
}
