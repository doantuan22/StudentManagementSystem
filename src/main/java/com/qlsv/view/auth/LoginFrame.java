/**
 * Khung giao diện xác thực cho đăng nhập.
 */
package com.qlsv.view.auth;

import com.qlsv.controller.LoginController;
import com.qlsv.model.User;
import com.qlsv.navigation.AppNavigator;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.BaseFrame;
import com.qlsv.view.common.RoundedButton;
import com.qlsv.view.common.RoundedPasswordField;
import com.qlsv.view.common.RoundedTextField;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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

    private static final int WINDOW_WIDTH = 1180;
    private static final int WINDOW_HEIGHT = 700;
    private static final int LEFT_PANEL_WIDTH = 420;
    private static final int RIGHT_PANEL_WIDTH = 580;
    private static final int FORM_WIDTH = 380;
    private static final int INPUT_HEIGHT = 46;
    private static final int BUTTON_HEIGHT = 48;
    private static final int FIELD_GAP = 18;
    private static final int LABEL_GAP = 8;
    private static final int SECTION_GAP = 28;
    private static final Color GRADIENT_START = new Color(25, 55, 135);
    private static final Color GRADIENT_END = new Color(80, 180, 240);

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
        setMinimumSize(new Dimension(1070, 700));
        setLocationRelativeTo(null);
        initUi();
    }

    /**
     * Khởi tạo giao diện tổng thể.
     */
    private void initUi() {
        JPanel outerPanel = createOuterPanel();
        JPanel mainContentPanel = createMainContentPanel();
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        outerPanel.add(mainContentPanel, gbc);
        
        setContentPane(outerPanel);
    }

    /**
     * Tạo panel nền xanh gradient ngoài cùng.
     */
    private JPanel createOuterPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, 
                                    java.awt.RenderingHints.VALUE_RENDER_QUALITY);
                
                int w = getWidth();
                int h = getHeight();
                
                // Gradient mượt hơn từ xanh dương đậm sang cyan sáng
                java.awt.geom.Point2D start = new java.awt.geom.Point2D.Float(0, 0);
                java.awt.geom.Point2D end = new java.awt.geom.Point2D.Float(w * 0.8f, h);
                
                float[] dist = {0.0f, 0.5f, 1.0f};
                Color[] colors = {
                    GRADIENT_START,
                    new Color(50, 110, 180),
                    GRADIENT_END
                };
                
                java.awt.LinearGradientPaint gp = new java.awt.LinearGradientPaint(
                    start, end, dist, colors
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        panel.setOpaque(true);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        return panel;
    }

    /**
     * Tạo panel chính chứa 2 vùng trái và phải.
     */
    private JPanel createMainContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setPreferredSize(new Dimension(LEFT_PANEL_WIDTH + RIGHT_PANEL_WIDTH, 550));
        panel.setOpaque(false);
        
        JPanel leftPanel = createLeftBrandPanel();
        JPanel rightPanel = createRightLoginPanel();
        
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Tạo panel bên trái với ảnh trong khung tròn.
     */
    private JPanel createLeftBrandPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(LEFT_PANEL_WIDTH, 550));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Panel chứa ảnh tròn - giảm kích thước 15%
        JPanel imagePanel = new JPanel() {
            private java.awt.Image backgroundImage;
            
            {
                try {
                    java.io.InputStream is = getClass().getResourceAsStream("/image/logo.jpg");
                    if (is != null) {
                        backgroundImage = javax.imageio.ImageIO.read(is);
                    }
                } catch (Exception e) {
                    System.err.println("Không thể load ảnh: " + e.getMessage());
                }
            }
            
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
                    g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, 
                                        java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    int panelWidth = getWidth();
                    int panelHeight = getHeight();
                    int diameter = Math.min(panelWidth, panelHeight);
                    int x = (panelWidth - diameter) / 2;
                    int y = (panelHeight - diameter) / 2;
                    
                    // Tạo clip hình tròn
                    java.awt.Shape circle = new java.awt.geom.Ellipse2D.Float(x, y, diameter, diameter);
                    g2d.setClip(circle);
                    
                    // Vẽ ảnh trong clip tròn
                    int imgWidth = backgroundImage.getWidth(this);
                    int imgHeight = backgroundImage.getHeight(this);
                    
                    // Scale ảnh để fill đầy hình tròn
                    double scale = Math.max((double) diameter / imgWidth, (double) diameter / imgHeight);
                    int scaledWidth = (int) (imgWidth * scale);
                    int scaledHeight = (int) (imgHeight * scale);
                    
                    // Căn giữa ảnh trong hình tròn
                    int imgX = x + (diameter - scaledWidth) / 2;
                    int imgY = y + (diameter - scaledHeight) / 2;
                    
                    g2d.drawImage(backgroundImage, imgX, imgY, scaledWidth, scaledHeight, this);
                    
                    // Reset clip
                    g2d.setClip(null);
                    
                    // Vẽ viền tròn mảnh và tinh tế hơn
                    g2d.setColor(new Color(255, 255, 255, 180));
                    g2d.setStroke(new java.awt.BasicStroke(3f));
                    g2d.drawOval(x, y, diameter, diameter);
                }
            }
        };
        imagePanel.setOpaque(false);
        imagePanel.setPreferredSize(new Dimension(300, 300));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(imagePanel, gbc);
        
        return panel;
    }

    /**
     * Tạo panel bên phải - box đăng nhập riêng biệt với bo tròn.
     */
    private JPanel createRightLoginPanel() {
        JPanel outerBox = new JPanel(new GridBagLayout());
        outerBox.setPreferredSize(new Dimension(RIGHT_PANEL_WIDTH, 550));
        outerBox.setOpaque(false);
        
        // Box đăng nhập với bo tròn - giảm chiều cao, tăng padding
        JPanel loginBox = new JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow mềm và tinh tế hơn
                g2d.setColor(new Color(0, 0, 0, 15));
                g2d.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 28, 28);
                
                g2d.setColor(new Color(0, 0, 0, 10));
                g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 28, 28);
                
                // Vẽ nền trắng bo tròn
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, 28, 28);
                
                // Vẽ viền nhẹ và mảnh hơn
                g2d.setColor(new Color(230, 230, 230, 80));
                g2d.setStroke(new java.awt.BasicStroke(1.0f));
                g2d.drawRoundRect(0, 0, getWidth() - 7, getHeight() - 7, 28, 28);
            }
        };
        
        loginBox.setLayout(new BoxLayout(loginBox, BoxLayout.Y_AXIS));
        loginBox.setOpaque(false);
        loginBox.setPreferredSize(new Dimension(FORM_WIDTH + 80, 440));
        loginBox.setBorder(BorderFactory.createEmptyBorder(45, 40, 45, 40));
        
        // Tiêu đề - mềm hơn, không viết hoa toàn bộ
        JLabel titleLabel = new JLabel("Đăng nhập");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(new Color(25, 25, 25));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel subtitleLabel = new JLabel("Hệ thống quản lý sinh viên");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(130, 130, 130));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        loginBox.add(titleLabel);
        loginBox.add(Box.createVerticalStrut(6));
        loginBox.add(subtitleLabel);
        loginBox.add(Box.createVerticalStrut(SECTION_GAP + 8));
        
        // Form
        JPanel formPanel = createFormPanel();
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBox.add(formPanel);
        
        loginBox.add(Box.createVerticalGlue());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        outerBox.add(loginBox, gbc);
        
        return outerBox;
    }

    /**
     * Tạo form nhập liệu đăng nhập.
     */
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setMaximumSize(new Dimension(FORM_WIDTH, 400));
        
        RoundedTextField usernameField = createInputField();
        RoundedPasswordField passwordField = createPasswordField();
        RoundedButton loginButton = createLoginButton();
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Username label
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, LABEL_GAP, 0);
        JLabel usernameLabel = new JLabel("Tên đăng nhập");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        usernameLabel.setForeground(new Color(50, 50, 50));
        formPanel.add(usernameLabel, gbc);
        
        // Username field
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, FIELD_GAP, 0);
        formPanel.add(usernameField, gbc);
        
        // Password label
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, LABEL_GAP, 0);
        JLabel passwordLabel = new JLabel("Mật khẩu");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passwordLabel.setForeground(new Color(50, 50, 50));
        formPanel.add(passwordLabel, gbc);
        
        // Password field
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, SECTION_GAP, 0);
        formPanel.add(passwordField, gbc);
        
        // Login button
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 0, 0);
        formPanel.add(loginButton, gbc);
        
        // Set default button
        getRootPane().setDefaultButton(loginButton);
        
        // Login action
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
        
        return formPanel;
    }

    /**
     * Tạo trường input.
     */
    private RoundedTextField createInputField() {
        RoundedTextField textField = new RoundedTextField(18);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setPreferredSize(new Dimension(FORM_WIDTH, INPUT_HEIGHT));
        textField.setMinimumSize(new Dimension(FORM_WIDTH, INPUT_HEIGHT));
        textField.setMaximumSize(new Dimension(FORM_WIDTH, INPUT_HEIGHT));
        return textField;
    }

    /**
     * Tạo trường mật khẩu.
     */
    private RoundedPasswordField createPasswordField() {
        RoundedPasswordField passwordField = new RoundedPasswordField(18);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(FORM_WIDTH, INPUT_HEIGHT));
        passwordField.setMinimumSize(new Dimension(FORM_WIDTH, INPUT_HEIGHT));
        passwordField.setMaximumSize(new Dimension(FORM_WIDTH, INPUT_HEIGHT));
        return passwordField;
    }

    /**
     * Tạo nút đăng nhập.
     */
    private RoundedButton createLoginButton() {
        RoundedButton loginButton = new RoundedButton("Đăng nhập", 18);
        loginButton.setBackground(new Color(40, 120, 220));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setPreferredSize(new Dimension(FORM_WIDTH, BUTTON_HEIGHT));
        loginButton.setMinimumSize(new Dimension(FORM_WIDTH, BUTTON_HEIGHT));
        loginButton.setMaximumSize(new Dimension(FORM_WIDTH, BUTTON_HEIGHT));
        return loginButton;
    }
}
