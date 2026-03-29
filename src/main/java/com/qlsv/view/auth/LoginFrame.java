/**
 * Khung giao diện xác thực cho đăng nhập.
 */
package com.qlsv.view.auth;

import com.qlsv.controller.LoginController;
import com.qlsv.model.User;
import com.qlsv.navigation.AppNavigator;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BaseFrame;
import com.qlsv.view.common.RoundedButton;
import com.qlsv.view.common.RoundedPasswordField;
import com.qlsv.view.common.RoundedTextField;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class LoginFrame extends BaseFrame {

    private static final int WINDOW_WIDTH = 480;
    private static final int WINDOW_HEIGHT = 600;
    private static final int FORM_WIDTH = 344;
    private static final int INPUT_HEIGHT = 42;
    private static final int BUTTON_HEIGHT = 44;
    private static final int HEADER_HEIGHT = 140;
    private static final int HEADER_GAP = 8;
    private static final int TITLE_GAP = 6;
    private static final int SECTION_GAP = 24;
    private static final int FIELD_GAP = 16;
    private static final int LABEL_GAP = 10;
    private static final double TOP_SPACER_WEIGHT = 0.40;
    private static final double BOTTOM_SPACER_WEIGHT = 0.60;
    private static final float APP_TITLE_FONT_SIZE = 58f;

    private final LoginController loginController;
    private final AppNavigator navigator;

    /**
     * Khởi tạo đăng nhập.
     */
    public LoginFrame(AppNavigator navigator) {
        this(navigator, new LoginController());
    }

    /**
     * Khởi tạo đăng nhập.
     */
    LoginFrame(AppNavigator navigator, LoginController loginController) {
        super("Đăng nhập");
        this.navigator = navigator;
        this.loginController = loginController;
        setExtendedState(JFrame.NORMAL);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setMinimumSize(new Dimension(440, 560));
        setLocationRelativeTo(null);
        initComponents();
    }

    /**
     * Khởi tạo giao diện đăng nhập với các thành phần: tiêu đề, form nhập liệu và nút bấm.
     */
    private void initComponents() {
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setOpaque(true);
        wrapperPanel.setBackground(AppColors.CARD_BACKGROUND);
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(20, 36, 20, 36));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);

        RoundedTextField usernameField = createInputField();
        RoundedPasswordField passwordField = createPasswordField();
        RoundedButton loginButton = createLoginButton();

        GridBagConstraints contentConstraints = new GridBagConstraints();
        contentConstraints.gridx = 0;
        contentConstraints.weightx = 1;
        contentConstraints.anchor = GridBagConstraints.CENTER;
        contentConstraints.fill = GridBagConstraints.NONE;

        contentConstraints.gridy = 0;
        contentConstraints.insets = new Insets(0, 0, SECTION_GAP, 0);
        contentPanel.add(createHeaderSection(), contentConstraints);

        contentConstraints.gridy = 1;
        contentConstraints.insets = new Insets(0, 0, SECTION_GAP, 0);
        contentPanel.add(createFormSection(usernameField, passwordField), contentConstraints);

        contentConstraints.gridy = 2;
        contentConstraints.insets = new Insets(0, 0, 0, 0);
        contentPanel.add(createActionSection(loginButton), contentConstraints);

        GridBagConstraints spacerConstraints = new GridBagConstraints();
        spacerConstraints.gridx = 0;
        spacerConstraints.weightx = 1;
        spacerConstraints.fill = GridBagConstraints.BOTH;

        JPanel topSpacer = new JPanel();
        topSpacer.setOpaque(false);
        spacerConstraints.gridy = 0;
        spacerConstraints.weighty = TOP_SPACER_WEIGHT;
        wrapperPanel.add(topSpacer, spacerConstraints);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = new Insets(0, 0, 0, 0);
        wrapperPanel.add(contentPanel, constraints);

        JPanel bottomSpacer = new JPanel();
        bottomSpacer.setOpaque(false);
        spacerConstraints.gridy = 2;
        spacerConstraints.weighty = BOTTOM_SPACER_WEIGHT;
        wrapperPanel.add(bottomSpacer, spacerConstraints);

        setContentPane(wrapperPanel);
        getRootPane().setDefaultButton(loginButton);

        loginButton.addActionListener(event -> {
            try {
                User user = loginController.login(
                        usernameField.getText(),
                        new String(passwordField.getPassword())
                );
                navigator.showDashboard(user);
                dispose();
            } catch (Exception exception) {
                DialogUtil.showError(this, exception.getMessage());
            }
        });
    }

    /**
     * Tạo phần tiêu đề của trang đăng nhập (Logo và tên hệ thống).
     */
    private JPanel createHeaderSection() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.setPreferredSize(new Dimension(FORM_WIDTH, HEADER_HEIGHT));
        headerPanel.setMinimumSize(new Dimension(FORM_WIDTH, HEADER_HEIGHT));
        headerPanel.setMaximumSize(new Dimension(FORM_WIDTH, HEADER_HEIGHT));

        JLabel appLabel = new JLabel("TKL");
        appLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        appLabel.setHorizontalAlignment(SwingConstants.CENTER);
        appLabel.setForeground(AppColors.INPUT_BORDER_FOCUS);
        appLabel.setFont(new Font("Segoe UI", Font.BOLD, Math.round(APP_TITLE_FONT_SIZE)));
        appLabel.putClientProperty("appTheme.labelStyled", Boolean.TRUE);

        JLabel subTitleLabel = new JLabel("HỆ THỐNG QUẢN LÝ SINH VIÊN");
        subTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subTitleLabel.setForeground(AppColors.CARD_MUTED_TEXT);
        subTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        headerPanel.add(appLabel);
        headerPanel.add(Box.createVerticalStrut(HEADER_GAP));
        headerPanel.add(Box.createVerticalStrut(TITLE_GAP));
        headerPanel.add(subTitleLabel);
        return headerPanel;
    }

    /**
     * Tạo phần form chứa các ô nhập tên đăng nhập và mật khẩu.
     */
    private JPanel createFormSection(RoundedTextField usernameField, RoundedPasswordField passwordField) {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.setPreferredSize(new Dimension(FORM_WIDTH, 156));
        formPanel.setMinimumSize(new Dimension(FORM_WIDTH, 156));
        formPanel.setMaximumSize(new Dimension(FORM_WIDTH, 156));

        formPanel.add(createFieldGroup("Tên đăng nhập", usernameField));
        formPanel.add(Box.createVerticalStrut(FIELD_GAP));
        formPanel.add(createFieldGroup("Mật khẩu", passwordField));
        return formPanel;
    }

    /**
     * Tạo phần chứa nút bấm thực hiện hành động đăng nhập.
     */
    private JPanel createActionSection(RoundedButton loginButton) {
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setOpaque(false);
        actionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionPanel.setPreferredSize(new Dimension(FORM_WIDTH, BUTTON_HEIGHT));
        actionPanel.setMinimumSize(new Dimension(FORM_WIDTH, BUTTON_HEIGHT));
        actionPanel.setMaximumSize(new Dimension(FORM_WIDTH, BUTTON_HEIGHT));
        actionPanel.add(loginButton, BorderLayout.CENTER);
        return actionPanel;
    }

    /**
     * Tạo trường input.
     */
    private RoundedTextField createInputField() {
        RoundedTextField textField = new RoundedTextField(16);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setPreferredSize(new Dimension(FORM_WIDTH, INPUT_HEIGHT));
        textField.setMinimumSize(new Dimension(FORM_WIDTH, INPUT_HEIGHT));
        textField.setMaximumSize(new Dimension(FORM_WIDTH, INPUT_HEIGHT));
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);
        return textField;
    }

    /**
     * Tạo trường mật khẩu.
     */
    private RoundedPasswordField createPasswordField() {
        RoundedPasswordField passwordField = new RoundedPasswordField(16);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(FORM_WIDTH, INPUT_HEIGHT));
        passwordField.setMinimumSize(new Dimension(FORM_WIDTH, INPUT_HEIGHT));
        passwordField.setMaximumSize(new Dimension(FORM_WIDTH, INPUT_HEIGHT));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        return passwordField;
    }

    /**
     * Tạo nút đăng nhập.
     */
    private RoundedButton createLoginButton() {
        RoundedButton loginButton = new RoundedButton("Đăng nhập", 18);
        loginButton.setBackground(AppColors.LOGIN_PRIMARY);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setPreferredSize(new Dimension(FORM_WIDTH, BUTTON_HEIGHT));
        loginButton.setMinimumSize(new Dimension(FORM_WIDTH, BUTTON_HEIGHT));
        loginButton.setMaximumSize(new Dimension(FORM_WIDTH, BUTTON_HEIGHT));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        return loginButton;
    }

    /**
     * Tạo một nhóm thành phần bao gồm nhãn (label) và ô nhập liệu tương ứng.
     */
    private JPanel createFieldGroup(String labelText, JComponent inputComponent) {
        JPanel fieldGroup = new JPanel();
        fieldGroup.setLayout(new BoxLayout(fieldGroup, BoxLayout.Y_AXIS));
        fieldGroup.setOpaque(false);
        fieldGroup.setAlignmentX(Component.CENTER_ALIGNMENT);
        fieldGroup.setPreferredSize(new Dimension(FORM_WIDTH, 70));
        fieldGroup.setMinimumSize(new Dimension(FORM_WIDTH, 70));
        fieldGroup.setMaximumSize(new Dimension(FORM_WIDTH, 70));

        JLabel label = new JLabel(labelText);
        label.setForeground(AppColors.CARD_TITLE_TEXT);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        fieldGroup.add(label);
        fieldGroup.add(Box.createVerticalStrut(LABEL_GAP));
        fieldGroup.add(inputComponent);
        return fieldGroup;
    }
}
