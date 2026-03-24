package com.qlsv.view.auth;

import com.qlsv.controller.LoginController;
import com.qlsv.model.User;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.BaseFrame;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class LoginFrame extends BaseFrame {

    private final LoginController loginController = new LoginController();

    public LoginFrame() {
        super("Dang nhap");
        initComponents();
    }

    private void initComponents() {
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(80, 260, 80, 260));

        JLabel titleLabel = new JLabel("Dang nhap he thong");
        titleLabel.setFont(titleLabel.getFont().deriveFont(22f));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Dang nhap");

        JPanel formPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        formPanel.add(titleLabel);
        formPanel.add(new JLabel("Username"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password"));
        formPanel.add(passwordField);
        formPanel.add(loginButton);

        wrapperPanel.add(formPanel, BorderLayout.CENTER);
        setContentPane(wrapperPanel);
        getRootPane().setDefaultButton(loginButton);

        loginButton.addActionListener(event -> {
            try {
                User user = loginController.login(usernameField.getText(), new String(passwordField.getPassword()));
                javax.swing.JFrame dashboardFrame = loginController.openDashboard(user);
                dashboardFrame.setVisible(true);
                dispose();
            } catch (Exception exception) {
                DialogUtil.showError(this, exception.getMessage());
            }
        });
    }
}
