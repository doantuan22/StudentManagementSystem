package com.qlsv.view.common;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class SidebarMenu extends JPanel {

    private final JPanel menuItemsPanel = new JPanel();
    private final Map<String, JButton> menuButtons = new LinkedHashMap<>();

    private String activeKey;

    public SidebarMenu(String title, String description) {
        setOpaque(false);
        setLayout(new BorderLayout(0, 18));
        setPreferredSize(new Dimension(280, 0));
        setMinimumSize(new Dimension(240, 0));
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppColors.SIDEBAR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(22, 18, 22, 18)
        ));

        JPanel headerPanel = new JPanel(new BorderLayout(0, 6));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));
        titleLabel.setForeground(AppColors.SIDEBAR_TEXT);

        JLabel descriptionLabel = new JLabel(toHtml(description));
        descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(Font.PLAIN, 12.5f));
        descriptionLabel.setForeground(AppColors.SIDEBAR_MUTED_TEXT);
        descriptionLabel.setVerticalAlignment(SwingConstants.TOP);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(descriptionLabel, BorderLayout.CENTER);

        menuItemsPanel.setOpaque(false);
        menuItemsPanel.setLayout(new BoxLayout(menuItemsPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(menuItemsPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public JButton addMenuItem(String key, String text, Runnable action) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        button.setMinimumSize(new Dimension(220, 46));
        button.setPreferredSize(new Dimension(220, 46));
        button.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setForeground(AppColors.SIDEBAR_TEXT);

        // Sidebar gom toan bo nut chuc nang vao mot khoi rieng, co hover va active
        // nhung van giu nguyen action dieu huong cu cua tung man hinh.
        button.addActionListener(event -> {
            setActiveItem(key);
            action.run();
        });
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                updateButtonState(key, button, true);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                updateButtonState(key, button, false);
            }
        });

        updateButtonState(key, button, false);

        menuButtons.put(key, button);
        if (menuItemsPanel.getComponentCount() > 0) {
            menuItemsPanel.add(Box.createVerticalStrut(10));
        }
        menuItemsPanel.add(button);
        return button;
    }

    public void setActiveItem(String key) {
        activeKey = key;
        for (Map.Entry<String, JButton> entry : menuButtons.entrySet()) {
            updateButtonState(entry.getKey(), entry.getValue(), false);
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(AppColors.SIDEBAR_BACKGROUND);
        graphics2D.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
        graphics2D.dispose();
        super.paintComponent(graphics);
    }

    private void updateButtonState(String key, JButton button, boolean hovered) {
        boolean isActive = key != null && key.equals(activeKey);
        if (isActive) {
            button.setBackground(AppColors.SIDEBAR_BUTTON_ACTIVE);
        } else if (hovered) {
            button.setBackground(AppColors.SIDEBAR_BUTTON_HOVER);
        } else {
            button.setBackground(AppColors.SIDEBAR_BUTTON);
        }
        button.setOpaque(true);
        button.repaint();
    }

    private String toHtml(String text) {
        return "<html><div style='width:220px;'>" + text + "</div></html>";
    }
}
