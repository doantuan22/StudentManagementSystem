package com.qlsv.view.auth;

import com.qlsv.controller.LoginController;
import com.qlsv.model.User;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BaseFrame;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class LoginFrame extends BaseFrame {

    private final LoginController loginController = new LoginController();

    public LoginFrame() {
        super("Đăng nhập");
        initComponents();
    }

    private void initComponents() {
        JPanel rootPanel = new JPanel(new GridBagLayout());
        rootPanel.setBackground(AppColors.CONTENT_BACKGROUND);
        rootPanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel eyebrowLabel = new JLabel("Hệ thống quản lý sinh viên");
        eyebrowLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        eyebrowLabel.setForeground(AppColors.BUTTON_PRIMARY);
        eyebrowLabel.setFont(eyebrowLabel.getFont().deriveFont(Font.BOLD, 13f));

        JLabel titleLabel = new JLabel("Đăng nhập hệ thống");
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 26f));

        JLabel descriptionLabel = new JLabel(
                "<html><div style='width:340px;'>Đăng nhập để truy cập các chức năng quản lý học tập, hồ sơ và báo cáo trong hệ thống.</div></html>"
        );
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descriptionLabel.setForeground(AppColors.CARD_MUTED_TEXT);
        descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(Font.PLAIN, 13.5f));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        usernameField.setToolTipText("Nhập tên đăng nhập của bạn.");
        passwordField.setToolTipText("Nhập mật khẩu để tiếp tục.");
        styleInputField(usernameField);
        styleInputField(passwordField);

        JButton loginButton = new JButton("Đăng nhập");
        stylePrimaryButton(loginButton);

        JPanel formCard = new JPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setOpaque(true);
        formCard.setBackground(AppColors.CARD_BACKGROUND);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(30, 32, 30, 32)
        ));

        formCard.add(eyebrowLabel);
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(titleLabel);
        formCard.add(Box.createVerticalStrut(8));
        formCard.add(descriptionLabel);
        formCard.add(Box.createVerticalStrut(24));
        formCard.add(createFieldLabel("Tên đăng nhập"));
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(usernameField);
        formCard.add(Box.createVerticalStrut(16));
        formCard.add(createFieldLabel("Mật khẩu"));
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(passwordField);
        formCard.add(Box.createVerticalStrut(22));
        formCard.add(loginButton);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.insets = new Insets(12, 12, 12, 12);
        constraints.anchor = GridBagConstraints.CENTER;
        rootPanel.add(formCard, constraints);

        JScrollPane scrollPane = new JScrollPane(rootPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(AppColors.CONTENT_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        setContentPane(scrollPane);
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

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setForeground(AppColors.CARD_TITLE_TEXT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        return label;
    }

    private void styleInputField(JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        component.setPreferredSize(new Dimension(356, 42));
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.INPUT_BORDER),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)
        ));
        component.setFont(component.getFont().deriveFont(Font.PLAIN, 14f));
    }

    private void stylePrimaryButton(JButton button) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setPreferredSize(new Dimension(356, 44));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(AppColors.BUTTON_PRIMARY);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 14f));
        button.setBorder(BorderFactory.createEmptyBorder(11, 16, 11, 16));
    }
}
